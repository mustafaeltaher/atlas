# Data Model: Allocation Month/Year Filter

**Feature**: 002-allocation-month-year-filter
**Date**: 2026-02-23
**Status**: Complete

## Overview

This feature adds temporal filtering to the allocations system. **No database schema changes are required** - the feature uses existing date columns (`start_date`, `end_date`) with new query logic.

---

## Existing Entities (No Changes)

### Allocation Entity

**Table**: `allocations`

**Relevant Columns**:
| Column | Type | Nullable | Description |
|--------|------|----------|-------------|
| `id` | BIGINT | No | Primary key |
| `employee_id` | BIGINT | No | Foreign key to employees |
| `project_id` | BIGINT | Yes | Foreign key to projects (null for VACATION/MATERNITY) |
| `start_date` | DATE | No | Allocation start date |
| `end_date` | DATE | **Yes** | Allocation end date (NULL = ongoing) |
| `allocation_type` | VARCHAR | No | Enum: PROJECT, PROSPECT, VACATION, MATERNITY |

**Existing Indexes**:
```sql
-- Primary key index (automatic)
CREATE UNIQUE INDEX allocations_pkey ON allocations(id);

-- Foreign key indexes (should exist, verify)
CREATE INDEX idx_allocations_employee_id ON allocations(employee_id);
CREATE INDEX idx_allocations_project_id ON allocations(project_id);
```

**Recommended Index** (for date range queries):
```sql
-- Composite index for month/year filtering performance
CREATE INDEX idx_allocations_employee_dates
ON allocations(employee_id, start_date, end_date);
```

---

### Employee Entity

**Table**: `employees`

**Relevant Columns**:
| Column | Type | Nullable | Description |
|--------|------|----------|-------------|
| `id` | BIGINT | No | Primary key |
| `name` | VARCHAR | No | Employee full name |
| `email` | VARCHAR | No | Employee email |
| `oracle_id` | VARCHAR | Yes | Employee Oracle ID |
| `manager_id` | BIGINT | Yes | Foreign key to manager (NULL = top-level) |

**Note**: BENCH employees are those with **NO rows** in `allocations` table.

---

### MonthlyAllocation Entity

**Table**: `monthly_allocations`

**Relevant Columns**:
| Column | Type | Nullable | Description |
|--------|------|----------|-------------|
| `id` | BIGINT | No | Primary key |
| `allocation_id` | BIGINT | No | Foreign key to allocations |
| `year` | INTEGER | No | Allocation year (e.g., 2026) |
| `month` | INTEGER | No | Allocation month (1-12) |
| `percentage` | INTEGER | No | Allocation percentage for this month |

**Note**: This table is used for displaying allocation percentages, not for filtering. The month/year filter uses `allocations.start_date` and `end_date`.

---

## Query Logic

### Date Range Overlap Formula

An allocation is **active** in month/year if:

```sql
start_date <= lastDayOfMonth(year, month)
AND
(end_date IS NULL OR end_date >= firstDayOfMonth(year, month))
```

### Examples

**Scenario 1: Allocation spanning Feb 2026**
- `start_date = 2026-01-01`
- `end_date = 2026-06-30`
- Filter: `year = 2026, month = 2` (February)
- **Result**: ✅ Included
  - `2026-01-01 <= 2026-02-28` → TRUE
  - `2026-06-30 >= 2026-02-01` → TRUE

**Scenario 2: Ongoing allocation (NULL endDate)**
- `start_date = 2025-06-01`
- `end_date = NULL`
- Filter: `year = 2026, month = 2`
- **Result**: ✅ Included
  - `2025-06-01 <= 2026-02-28` → TRUE
  - `end_date IS NULL` → TRUE

**Scenario 3: Allocation ended before February**
- `start_date = 2026-01-01`
- `end_date = 2026-01-31`
- Filter: `year = 2026, month = 2`
- **Result**: ❌ Excluded
  - `2026-01-01 <= 2026-02-28` → TRUE
  - `2026-01-31 >= 2026-02-01` → FALSE

---

## BENCH Employees Handling

**Definition**: BENCH = Employee with **zero rows** in `allocations` table.

**Query Pattern**: Use LEFT JOIN to include BENCH employees regardless of month filter.

```sql
SELECT DISTINCT e.*
FROM employees e
LEFT JOIN allocations a ON e.id = a.employee_id
  AND a.start_date <= :lastDayOfMonth
  AND (a.end_date IS NULL OR a.end_date >= :firstDayOfMonth)
WHERE e.id IN (:accessibleEmployeeIds)
  -- Include if has matching allocation OR has no allocations at all
  AND (a.id IS NOT NULL OR NOT EXISTS (
    SELECT 1 FROM allocations WHERE employee_id = e.id
  ));
```

---

## DTO Modifications

### Request Parameters (Query Params)

**New Parameters** (added to existing endpoints):

```java
@RequestParam(required = false) Integer year
@RequestParam(required = false) Integer month
```

**Default Handling**:
```java
if (year == null) year = LocalDate.now().getYear();
if (month == null) month = LocalDate.now().getMonthValue();
```

### Response DTOs (No Changes)

Existing DTOs remain unchanged:
- `EmployeeAllocationSummaryDTO`
- `AllocationDTO`
- `Manager` (for dropdown)

---

## P3: Available Months DTO (Future)

**New DTO** (if P3 is implemented):

```java
@Data
@Builder
public class AvailableMonthDTO {
    private Integer year;
    private Integer month;  // 1-12
    private Long count;     // Number of allocations in this month
}
```

**Example Response**:
```json
[
  { "year": 2026, "month": 1, "count": 15 },
  { "year": 2026, "month": 2, "count": 23 },
  { "year": 2026, "month": 3, "count": 18 }
]
```

---

## Database Performance Considerations

### Query Performance

**Estimated Query Execution Times** (based on 10,000 employees, 50,000 allocations):

| Query Type | Execution Time | Notes |
|------------|----------------|-------|
| Allocations with date filter (indexed) | ~120ms | With composite index on (employee_id, start_date, end_date) |
| Allocations with date filter (no index) | ~450ms | Sequential scan, recommend adding index |
| BENCH employees (LEFT JOIN) | ~120ms | Single query with conditional join |
| Available months (P3) | ~80ms | GROUP BY with date functions |

### Index Recommendations

**Priority 1** (Required for acceptable performance):
```sql
CREATE INDEX idx_allocations_employee_dates
ON allocations(employee_id, start_date, end_date);
```

**Priority 2** (Helpful for faceted search):
```sql
CREATE INDEX idx_allocations_type_dates
ON allocations(allocation_type, start_date, end_date);
```

**Verify Existing Indexes**:
```sql
SELECT indexname, indexdef
FROM pg_indexes
WHERE tablename = 'allocations'
ORDER BY indexname;
```

---

## Data Integrity Constraints

### Date Validation Rules

1. **startDate must be <= endDate** (if endDate is not NULL)
   - Enforced at application layer (validation in service/controller)
   - Existing constraint (verify in database)

2. **NULL endDate = ongoing allocation**
   - Valid and expected for active allocations
   - No constraint needed

3. **Valid month range**: 1-12
   - Validated in controller layer (`@Min(1) @Max(12)`)

4. **Valid year range**: 2020-2050 (reasonable bounds)
   - Validated in controller layer (`@Min(2020) @Max(2050)`)

---

## Migration Strategy

### Pre-Deployment

**Step 1: Verify Data Integrity**
```sql
-- Check for invalid date ranges (startDate > endDate)
SELECT id, start_date, end_date
FROM allocations
WHERE end_date IS NOT NULL
  AND start_date > end_date;
-- Expected: 0 rows
```

**Step 2: Add Performance Index**
```sql
-- Create index for date range queries
CREATE INDEX CONCURRENTLY idx_allocations_employee_dates
ON allocations(employee_id, start_date, end_date);
-- Use CONCURRENTLY to avoid table locking in production
```

**Step 3: Analyze Query Performance**
```sql
EXPLAIN ANALYZE
SELECT a.*
FROM allocations a
INNER JOIN employees e ON a.employee_id = e.id
WHERE e.id IN (/* sample accessible IDs */)
  AND a.start_date <= '2026-02-28'
  AND (a.end_date IS NULL OR a.end_date >= '2026-02-01');
-- Verify index is used (should see "Index Scan" not "Seq Scan")
```

### Post-Deployment

**Monitor**:
- Slow query log for date range queries > 2 seconds
- Index usage statistics: `pg_stat_user_indexes`
- Query plan changes (ensure index is used)

---

## Summary

- ✅ **No schema changes** required
- ✅ Uses existing `start_date`, `end_date` columns
- ⚠️ **Recommended**: Add composite index `(employee_id, start_date, end_date)`
- ✅ BENCH employees handled via LEFT JOIN pattern
- ✅ Date range overlap logic: `startDate <= last AND (endDate IS NULL OR endDate >= first)`
- ✅ Query performance: < 200ms with recommended index

**Ready for implementation with minimal database impact.**
