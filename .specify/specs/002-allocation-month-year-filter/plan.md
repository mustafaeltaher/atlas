# Implementation Plan: Allocation Month/Year Filter

**Branch**: `002-allocation-month-year-filter` | **Date**: 2026-02-23 | **Spec**: [spec.md](../specs/002-allocation-month-year-filter.md)

## Summary

Add a month/year date picker filter to the allocations page as the second filter element (after search box). The filter defaults to the current month and integrates with the existing faceted search system. Database-level filtering applies date range logic to show only allocations active in the selected period. Special handling for BENCH employees (visible across all months) and PROJECT allocation type (indicates available/dimmed months in the picker).

## Technical Context

**Language/Version**: Java 17 (backend), TypeScript/Angular 17+ (frontend)
**Primary Dependencies**: Spring Boot 3.2.1, Hibernate 6.4.1, Angular 17+ with Signals
**Storage**: PostgreSQL 15
**Testing**: JUnit 5 + Mockito (backend), Jasmine/Karma (frontend)
**Target Platform**: Web application (backend API + Angular SPA)
**Project Type**: Web (separate backend/frontend)
**Performance Goals**: Filter change < 2 seconds, faceted dropdowns load < 1 second, supports 10,000+ allocations
**Constraints**: ABAC access control, database-first filtering, faceted search compliance, NULL-safe queries
**Scale/Scope**: Enhance existing allocations page with time-dimension filtering

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### I. ABAC-First Security ✅ PASS

**Compliance**:
- Month/year filter will work with existing `getAccessibleEmployeeIds()` pattern
- All queries filter by accessible employees first, then apply month/year filter
- No new access control mechanisms needed

**Implementation Approach**:
- Reuse `employeeService.getAccessibleEmployeeIds(currentUser)` in all endpoints
- Month/year parameters passed alongside existing filters
- Access control applied before date filtering in query predicates

### II. Database-First Performance ✅ PASS

**Compliance**:
- All date filtering occurs at database level using JPA Specifications
- Date range calculations performed in SQL: `startDate <= :lastDayOfMonth AND (endDate IS NULL OR endDate >= :firstDayOfMonth)`
- No in-memory date comparisons or filtering

**Implementation Approach**:
- Add `byMonthYear(Integer year, Integer month)` method to `AllocationSpecification`
- Calculate first/last day of month in Java, pass as parameters to query
- Use SQL date comparison predicates in Criteria API
- For faceted dropdowns: EntityManager with CriteriaQuery applies all filters including month/year

### III. Faceted Search Architecture ✅ PASS

**Compliance**:
- All filter endpoints (`/managers`, `/allocation-types`) accept `year` and `month` parameters
- Frontend passes ALL current filter values when loading each dropdown
- Database queries apply month/year filter alongside other dimensions (manager, type, search)
- Custom repository methods use JPA Specification for faceted distinct value queries

**Implementation Approach**:
- Update `AllocationSpecification.withFilters()` to accept `year` and `month` parameters
- Update `AllocationRepository.findDistinctManagers()` to accept and apply `year`/`month` filters
- Update `AllocationService` methods to thread `year`/`month` through all faceted queries
- Frontend signals: `selectedYear`, `selectedMonth` passed to all API calls

### IV. NULL-Safe Native Queries ✅ PASS

**Compliance**:
- Month/year parameters are optional (nullable)
- All native SQL queries (if any) use `(:year IS NULL OR ...)` pattern
- Default behavior when NULL: show current month (handled in service layer, not SQL)

**Implementation Approach**:
- Service layer: if `year == null`, set to `LocalDate.now().getYear()`
- Service layer: if `month == null`, set to `LocalDate.now().getMonthValue()`
- Pass non-null values to Specification (Specifications handle non-null parameters)
- For BENCH special case: separate query path that ignores month/year filter

### V. Enum Handling Standards ✅ PASS

**Compliance**:
- No new enums introduced
- Existing `AllocationType` enum already uses `@Enumerated(EnumType.STRING)` correctly
- Month/year are Integer types, not enums

### Technology Stack ✅ PASS

**Compliance**:
- Uses Java 17 (existing)
- Spring Boot 3.2.1, Hibernate 6.4.1 (existing)
- Angular 17+ with Signals (existing)
- PostgreSQL 15 (existing)

## Project Structure

### Documentation (this feature)

```text
.specify/
├── specs/
│   └── 002-allocation-month-year-filter.md    # Feature specification
└── plans/
    └── 002-allocation-month-year-filter.md    # This file
```

### Source Code (repository root)

```text
backend/
├── src/main/java/com/atlas/
│   ├── controller/
│   │   └── AllocationController.java                # UPDATE: Add year/month params to endpoints
│   ├── dto/
│   │   └── AvailableMonthDTO.java                   # NEW (P3): For available months endpoint
│   ├── entity/
│   │   ├── Allocation.java                          # EXISTING: No changes (has startDate, endDate)
│   │   └── MonthlyAllocation.java                   # EXISTING: No changes
│   ├── repository/
│   │   ├── AllocationRepository.java                # UPDATE: Add findAvailableMonths() for P3
│   │   └── AllocationRepositoryCustom.java          # UPDATE: Add year/month to distinct queries
│   ├── specification/
│   │   └── AllocationSpecification.java             # UPDATE: Add byMonthYear() method
│   └── service/
│       └── AllocationService.java                   # UPDATE: Thread year/month through methods
└── src/test/java/com/atlas/
    ├── specification/
    │   └── AllocationSpecificationTest.java         # NEW: Test month/year filtering logic
    └── service/
        └── AllocationServiceTest.java               # UPDATE: Add tests for month/year scenarios

frontend/
├── src/app/
│   ├── pages/allocations/
│   │   ├── allocations.component.ts                 # UPDATE: Add month/year picker state
│   │   └── allocations.component.html               # UPDATE: Add date picker UI (2nd filter)
│   ├── models/
│   │   └── filters.ts                               # NEW or UPDATE: Add year/month to filter model
│   └── services/
│       └── api.service.ts                           # UPDATE: Add year/month params to API calls
└── package.json                                     # UPDATE: Add Angular Material if needed
```

**Structure Decision**: Web application structure with existing backend/frontend separation. This feature modifies:
- **Backend**: Update `AllocationSpecification` for date filtering, update controller/service to accept year/month params, update repository custom queries for faceted search
- **Frontend**: Add month/year picker component (Angular Material DatePicker in month/year mode), update state management (signals), thread year/month through all API calls
- Follows existing faceted search patterns from manager/allocation type filters

## Complexity Tracking

> **All Constitution gates passed - no violations to justify**

No complexity violations. This feature:
- Uses existing ABAC patterns (no new access control mechanisms)
- Uses existing JPA Specifications (extends `AllocationSpecification.withFilters()`)
- Uses existing faceted search architecture (threads new parameter through existing endpoints)
- Adds standard filter dimension (time-based filtering is common in allocation systems)

---

## Phase 0: Research & Design Decisions

### Research Tasks

The following decisions need to be made before implementation:

#### 1. Frontend Date Picker Component

**Question**: Which Angular component library should be used for the month/year picker?

**Options**:
- a) **Angular Material DatePicker** with `startView="multi-year"` and custom mode
  - Pros: Industry standard, well-documented, accessible, matches modern UI patterns
  - Cons: Adds dependency (~500KB), requires Material theme integration
- b) **Native HTML5 `<input type="month">`**
  - Pros: Zero dependencies, native browser support, simple implementation
  - Cons: Limited styling control, inconsistent UI across browsers, less polished UX
- c) **Custom calendar component** (build from scratch)
  - Pros: Full control, no dependencies, lightweight
  - Cons: High development effort, accessibility concerns, maintenance burden

**Recommendation**: **Option A (Angular Material)** - The project likely already uses Angular Material (verify in package.json). If not, Option B (native input) is acceptable for MVP.

**Research Action**: Check `frontend/package.json` for existing `@angular/material` dependency.

---

#### 2. BENCH Employees Filtering Logic

**Question**: How should BENCH employees be included when a month/year filter is active?

**Technical Detail**: BENCH means "employee with NO allocation records." When filtering by month X, should we:
- a) Query employees with allocations in month X **UNION** employees with zero allocations (two queries)
- b) Use `LEFT JOIN allocations` with date filter, include employees where join is NULL (single query)
- c) Always query all BENCH employees separately, then merge with month-filtered results in service layer

**Recommendation**: **Option B (LEFT JOIN)** - Most efficient, single query, handles faceted search cleanly.

**SQL Pattern**:
```sql
SELECT DISTINCT e.*
FROM employees e
LEFT JOIN allocations a ON e.id = a.employee_id
  AND a.start_date <= :lastDayOfMonth
  AND (a.end_date IS NULL OR a.end_date >= :firstDayOfMonth)
WHERE e.id IN (:accessibleIds)
  AND (a.id IS NOT NULL OR NOT EXISTS (SELECT 1 FROM allocations WHERE employee_id = e.id))
```

**Implementation**: Use JPA Criteria API with LEFT JOIN and conditional predicates.

---

#### 3. Default Month Behavior

**Question**: Should the default month be:
- a) Current month (always "February 2026" when opened in Feb 2026)
- b) Last accessed month (persist in localStorage)
- c) No default (empty/all months shown until user selects)

**Spec Requirement**: "Default value should be pointing to the current month"

**Decision**: **Option A (Current month)** - Per spec requirement.

**Implementation**:
- Frontend: Initialize `selectedYear = signal(new Date().getFullYear())`
- Frontend: Initialize `selectedMonth = signal(new Date().getMonth() + 1)` (1-indexed for backend)
- Backend: If `year` or `month` params are NULL, service layer defaults to current date

---

#### 4. P3: Available Months Indicator - Implementation Strategy

**Question**: For P3 (showing which months have PROJECT allocations), should this be:
- a) **Client-side logic**: Fetch all distinct months with allocations, dim others in picker UI
- b) **Server-side endpoint**: `GET /api/allocations/available-months?type=PROJECT&managerId=X&search=Y` returns list of months
- c) **Hybrid**: Server returns bitmask or date range, client renders UI
- d) **Skip for MVP**: Implement in later iteration

**Recommendation**: **Option B (Server-side endpoint)** for P3 implementation, **Option D (Skip)** for MVP (P1/P2 only).

**P3 Endpoint Design**:
```
GET /api/allocations/available-months
Query Params: allocationType, managerId, search
Response: [
  { "year": 2026, "month": 1, "count": 15 },
  { "year": 2026, "month": 2, "count": 23 },
  ...
]
```

**Frontend Logic**: Iterate through calendar months, check if `{year, month}` exists in response, apply `dimmed` CSS class if not.

---

#### 5. Date Range Calculation Logic

**Question**: How to determine if an allocation is "active" in month X?

**Business Rule**: An allocation with `startDate` and `endDate` is active in month/year if:
```
startDate <= lastDayOfMonth(year, month)
AND
(endDate IS NULL OR endDate >= firstDayOfMonth(year, month))
```

**Edge Cases**:
- Allocation starts mid-month (e.g., Jan 15): Should appear in January filter ✅
- Allocation ends mid-month (e.g., ends Jan 20): Should appear in January filter ✅
- Allocation spans multiple months (e.g., Jan 1 - June 30): Should appear in Jan, Feb, Mar, Apr, May, Jun ✅
- Allocation has NULL endDate (ongoing): Should appear in all months >= startDate ✅

**Implementation**:
```java
LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
LocalDate lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth());

// JPA Criteria API predicate:
Predicate startDateCheck = cb.lessThanOrEqualTo(root.get("startDate"), lastDayOfMonth);
Predicate endDateNull = cb.isNull(root.get("endDate"));
Predicate endDateCheck = cb.greaterThanOrEqualTo(root.get("endDate"), firstDayOfMonth);
predicates.add(cb.and(startDateCheck, cb.or(endDateNull, endDateCheck)));
```

---

#### 6. Pagination Interaction

**Question**: When user changes month/year filter, should pagination:
- a) Reset to page 0 (like existing filters)
- b) Maintain current page if results still exist
- c) Smart jump to nearest page with results

**Recommendation**: **Option A (Reset to page 0)** - Consistent with existing filter behavior (see `onFilter()` in allocations.component.ts line 1309).

**Implementation**: Frontend: `selectedMonth.set(newMonth); currentPage.set(0); loadAllocations();`

---

### Research Deliverables

Before proceeding to Phase 1, confirm:
1. ✅ Angular Material availability (check package.json)
2. ✅ Database performance with LEFT JOIN + date range (test on staging with 10k+ allocations)
3. ✅ Date range calculation logic validated with edge cases
4. ✅ P3 scope decision: include in MVP or defer?

---

## Phase 1: Database Schema & Query Design

*Prerequisites: Research decisions finalized*

### 1.1 Database Schema Changes

**No schema changes required.** This feature uses existing columns:
- `allocations.start_date` (DATE, nullable)
- `allocations.end_date` (DATE, nullable)
- `allocations.allocation_type` (VARCHAR, not nullable)

**Verification**: Confirm indexes exist for performance:
```sql
-- Check if indexes exist on date columns
SELECT indexname, indexdef
FROM pg_indexes
WHERE tablename = 'allocations'
  AND (indexdef LIKE '%start_date%' OR indexdef LIKE '%end_date%');
```

**Recommendation**: If no indexes exist, add composite index for common query pattern:
```sql
CREATE INDEX idx_allocations_employee_dates
ON allocations(employee_id, start_date, end_date);
```

---

### 1.2 JPA Specification Design

**File**: `backend/src/main/java/com/atlas/specification/AllocationSpecification.java`

**Changes**:

```java
// UPDATE: Add year/month parameters to withFilters()
public static Specification<Allocation> withFilters(
        Allocation.AllocationType allocationType,
        Long managerId,
        String search,
        List<Long> accessibleEmployeeIds,
        Integer year,      // NEW
        Integer month) {   // NEW

    return (root, query, cb) -> {
        List<Predicate> predicates = new ArrayList<>();

        Join<Allocation, Employee> employeeJoin = root.join("employee", JoinType.INNER);
        Join<Allocation, Project> projectJoin = root.join("project", JoinType.LEFT);

        // ABAC
        if (accessibleEmployeeIds != null) {
            if (accessibleEmployeeIds.isEmpty()) {
                predicates.add(cb.disjunction()); // 1=0
            } else {
                predicates.add(employeeJoin.get("id").in(accessibleEmployeeIds));
            }
        }

        // Allocation Type filter
        if (allocationType != null) {
            predicates.add(cb.equal(root.get("allocationType"), allocationType));
        }

        // Manager Filter
        if (managerId != null) {
            predicates.add(cb.equal(employeeJoin.get("manager").get("id"), managerId));
        }

        // Search Filter
        if (search != null && !search.trim().isEmpty()) {
            String searchLike = "%" + search.trim().toLowerCase() + "%";
            Predicate employeeNameLike = cb.like(cb.lower(employeeJoin.get("name")), searchLike);
            Predicate employeeEmailLike = cb.like(cb.lower(employeeJoin.get("email")), searchLike);
            predicates.add(cb.or(employeeNameLike, employeeEmailLike));
        }

        // NEW: Month/Year Filter
        if (year != null && month != null) {
            LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
            LocalDate lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth());

            // startDate <= lastDayOfMonth
            Predicate startDateCheck = cb.lessThanOrEqualTo(root.get("startDate"), lastDayOfMonth);

            // endDate IS NULL OR endDate >= firstDayOfMonth
            Predicate endDateNull = cb.isNull(root.get("endDate"));
            Predicate endDateCheck = cb.greaterThanOrEqualTo(root.get("endDate"), firstDayOfMonth);

            predicates.add(cb.and(startDateCheck, cb.or(endDateNull, endDateCheck)));
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    };
}
```

---

### 1.3 Repository Custom Queries (Faceted Search)

**File**: `backend/src/main/java/com/atlas/repository/AllocationRepositoryCustom.java`

**Changes**:

```java
public interface AllocationRepositoryCustom {
    // UPDATE: Add year/month params to existing methods
    List<Map<String, Object>> findDistinctManagers(
        String allocationType,
        String search,
        String managerSearch,
        List<Long> accessibleIds,
        Integer year,    // NEW
        Integer month);  // NEW

    // P3 ONLY: New method for available months
    List<AvailableMonthDTO> findAvailableMonths(
        String allocationType,
        Long managerId,
        String search,
        List<Long> accessibleIds,
        Integer startYear,  // e.g., current year - 1
        Integer endYear);   // e.g., current year + 2
}
```

**Implementation**: `AllocationRepositoryCustomImpl.java`

```java
@Override
public List<Map<String, Object>> findDistinctManagers(
        String allocationType, String search, String managerSearch,
        List<Long> accessibleIds, Integer year, Integer month) {

    // Early return for empty accessible IDs (native SQL IN clause requirement)
    if (accessibleIds != null && accessibleIds.isEmpty()) {
        return Collections.emptyList();
    }

    // Calculate date range if year/month provided
    LocalDate firstDay = null;
    LocalDate lastDay = null;
    if (year != null && month != null) {
        firstDay = LocalDate.of(year, month, 1);
        lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());
    }

    // Build native query with month/year filter
    String sql = """
        SELECT DISTINCT m.id, m.name
        FROM employees m
        INNER JOIN employees e ON e.manager_id = m.id
        LEFT JOIN allocations a ON a.employee_id = e.id
            AND (:firstDay IS NULL OR a.start_date <= :lastDay)
            AND (:firstDay IS NULL OR a.end_date IS NULL OR a.end_date >= :firstDay)
        WHERE (:accessibleIds IS NULL OR e.id IN (:accessibleIds))
          AND (:allocationType IS NULL OR LOWER(a.allocation_type) = LOWER(:allocationType))
          AND (:search IS NULL OR LOWER(e.name) LIKE :search OR LOWER(e.email) LIKE :search)
          AND (:managerSearch IS NULL OR LOWER(m.name) LIKE :managerSearch)
          AND (a.id IS NOT NULL OR :allocationType IS NULL)
        ORDER BY m.name
        """;

    Query query = entityManager.createNativeQuery(sql);
    query.setParameter("accessibleIds", accessibleIds);
    query.setParameter("allocationType", allocationType);
    query.setParameter("search", search);
    query.setParameter("managerSearch", managerSearch);
    query.setParameter("firstDay", firstDay);
    query.setParameter("lastDay", lastDay);

    // ... result mapping logic
}
```

---

### 1.4 Service Layer Updates

**File**: `backend/src/main/java/com/atlas/service/AllocationService.java`

**Changes**:

```java
public Page<EmployeeAllocationSummaryDTO> getGroupedAllocations(
        User currentUser, Pageable pageable, String search,
        String allocationType, Long managerId,
        Integer year, Integer month) {  // NEW params

    // Default to current month if not provided
    if (year == null) {
        year = LocalDate.now().getYear();
    }
    if (month == null) {
        month = LocalDate.now().getMonthValue();
    }

    // ... existing logic, pass year/month to Specification
    Specification<Allocation> spec = AllocationSpecification.withFilters(
        filterTypeEnum, managerId, searchParam, accessibleIds, year, month);

    // ... rest of method
}

public List<Map<String, Object>> getManagersForAllocations(
        User currentUser, String allocationType, String search,
        String managerSearch, Integer year, Integer month) {  // NEW params

    // Default to current month
    if (year == null) year = LocalDate.now().getYear();
    if (month == null) month = LocalDate.now().getMonthValue();

    List<Long> accessibleIds = employeeService.getAccessibleEmployeeIds(currentUser);
    return allocationRepository.findDistinctManagers(
        allocationType, search, managerSearch, accessibleIds, year, month);
}

// P3 ONLY
public List<AvailableMonthDTO> getAvailableMonths(
        User currentUser, String allocationType, Long managerId, String search) {

    int currentYear = LocalDate.now().getYear();
    List<Long> accessibleIds = employeeService.getAccessibleEmployeeIds(currentUser);

    return allocationRepository.findAvailableMonths(
        allocationType, managerId, search, accessibleIds,
        currentYear - 1,  // Start: last year
        currentYear + 2); // End: 2 years ahead
}
```

---

### 1.5 Controller Updates

**File**: `backend/src/main/java/com/atlas/controller/AllocationController.java`

**Changes**:

```java
@GetMapping("/grouped")
public ResponseEntity<Page<EmployeeAllocationSummaryDTO>> getGroupedAllocations(
        Authentication authentication,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String allocationType,
        @RequestParam(required = false) Long managerId,
        @RequestParam(required = false) Integer year,    // NEW
        @RequestParam(required = false) Integer month) {  // NEW

    page = Math.max(0, page);
    size = Math.max(1, Math.min(size, MAX_PAGE_SIZE));
    User currentUser = userDetailsService.getUserByUsername(authentication.getName());

    return ResponseEntity.ok(allocationService.getGroupedAllocations(
        currentUser, PageRequest.of(page, size), search, allocationType, managerId, year, month));
}

@GetMapping("/managers")
public ResponseEntity<List<Map<String, Object>>> getAllocationManagers(
        Authentication authentication,
        @RequestParam(required = false) String allocationType,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String managerSearch,
        @RequestParam(required = false) Integer year,    // NEW
        @RequestParam(required = false) Integer month) {  // NEW

    User currentUser = userDetailsService.getUserByUsername(authentication.getName());
    return ResponseEntity.ok(allocationService.getManagersForAllocations(
        currentUser, allocationType, search, managerSearch, year, month));
}

// P3 ONLY
@GetMapping("/available-months")
public ResponseEntity<List<AvailableMonthDTO>> getAvailableMonths(
        Authentication authentication,
        @RequestParam(required = false) String allocationType,
        @RequestParam(required = false) Long managerId,
        @RequestParam(required = false) String search) {

    User currentUser = userDetailsService.getUserByUsername(authentication.getName());
    return ResponseEntity.ok(allocationService.getAvailableMonths(
        currentUser, allocationType, managerId, search));
}
```

---

## Phase 2: Frontend Implementation

### 2.1 Component State (Signals)

**File**: `frontend/src/app/pages/allocations/allocations.component.ts`

**Changes**:

```typescript
export class AllocationsComponent implements OnInit {
  // ... existing signals

  // NEW: Month/Year filter state
  selectedYear = signal<number>(new Date().getFullYear());
  selectedMonth = signal<number>(new Date().getMonth() + 1); // 1-indexed
  availableMonths = signal<AvailableMonth[]>([]); // P3 only

  // ... existing code

  ngOnInit(): void {
    this.loadManagers();
    this.loadAllocationTypes();
    this.loadAvailableMonths(); // P3 only
    this.loadAllocations();
  }

  onMonthYearChange(): void {
    this.currentPage.set(0); // Reset pagination
    this.loadAllocations();
    this.loadManagers();
    this.loadAllocationTypes();
    // P3: No need to reload available months on month change
  }

  loadAllocations(scrollToBottom: boolean = false): void {
    this.loading.set(true);
    const search = this.searchTerm || undefined;
    const allocationType = this.allocationTypeFilter || undefined;
    const managerId = this.managerFilter ? Number(this.managerFilter) : undefined;
    const year = this.selectedYear(); // NEW
    const month = this.selectedMonth(); // NEW

    this.apiService.getGroupedAllocations(
      this.currentPage(), this.pageSize(), search, allocationType, managerId, year, month)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({ /* ... */ });
  }

  loadManagers(managerNameSearch?: string): void {
    const allocationType = this.allocationTypeFilter || undefined;
    const globalSearch = this.searchTerm || undefined;
    const managerSearch = managerNameSearch || undefined;
    const year = this.selectedYear(); // NEW
    const month = this.selectedMonth(); // NEW

    this.apiService.getAllocationManagers(
      allocationType, globalSearch, managerSearch, year, month)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({ /* ... */ });
  }

  // P3 ONLY
  loadAvailableMonths(): void {
    const allocationType = this.allocationTypeFilter || undefined;
    const managerId = this.managerFilter ? Number(this.managerFilter) : undefined;
    const search = this.searchTerm || undefined;

    this.apiService.getAvailableMonths(allocationType, managerId, search)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (months) => this.availableMonths.set(months),
        error: () => {}
      });
  }

  // P3: Check if month is available
  isMonthAvailable(year: number, month: number): boolean {
    return this.availableMonths().some(m => m.year === year && m.month === month);
  }
}
```

---

### 2.2 Template UI (Month/Year Picker)

**File**: `frontend/src/app/pages/allocations/allocations.component.ts` (template section)

**Position**: After `.search-box`, before allocation type dropdown

**Option A: Native HTML5 (Simple, no dependencies)**

```html
<!-- Search and Filters -->
<div class="filters-bar">
  <!-- Existing search box -->
  <div class="search-box">
    <svg>...</svg>
    <input type="text" placeholder="Search by name or email..."
           [(ngModel)]="searchTerm" (input)="onSearch()">
  </div>

  <!-- NEW: Month/Year Picker (native) -->
  <input type="month"
         class="filter-select month-picker"
         [value]="selectedYear() + '-' + selectedMonth().toString().padStart(2, '0')"
         (change)="onMonthInputChange($event)"
         title="Select Month">

  <!-- Existing filters -->
  <select class="filter-select" [(ngModel)]="allocationTypeFilter" (change)="onFilter()">
    ...
  </select>
  ...
</div>
```

```typescript
// Component method
onMonthInputChange(event: Event): void {
  const input = event.target as HTMLInputElement;
  const [year, month] = input.value.split('-').map(Number);
  this.selectedYear.set(year);
  this.selectedMonth.set(month);
  this.onMonthYearChange();
}
```

**Option B: Angular Material (Polished UI, requires dependency)**

```html
<!-- NEW: Month/Year Picker (Material) -->
<mat-form-field class="filter-select month-picker" appearance="outline">
  <mat-label>Month</mat-label>
  <input matInput
         [matDatepicker]="monthPicker"
         [value]="getMonthYearDate()"
         (dateChange)="onMonthYearChange($event)"
         readonly>
  <mat-datepicker-toggle matSuffix [for]="monthPicker"></mat-datepicker-toggle>
  <mat-datepicker #monthPicker
                  startView="year"
                  (monthSelected)="setMonthAndYear($event, monthPicker)">
  </mat-datepicker>
</mat-form-field>
```

```typescript
// Component methods
import { DateAdapter } from '@angular/material/core';

getMonthYearDate(): Date {
  return new Date(this.selectedYear(), this.selectedMonth() - 1, 1);
}

setMonthAndYear(date: Date, datepicker: MatDatepicker<Date>): void {
  this.selectedYear.set(date.getFullYear());
  this.selectedMonth.set(date.getMonth() + 1);
  datepicker.close();
  this.onMonthYearChange();
}
```

**Recommendation**: Start with **Option A (native)** for MVP, upgrade to Option B if UX feedback demands.

---

### 2.3 API Service Updates

**File**: `frontend/src/app/services/api.service.ts`

**Changes**:

```typescript
getGroupedAllocations(
  page: number,
  size: number,
  search?: string,
  allocationType?: string,
  managerId?: number,
  year?: number,    // NEW
  month?: number    // NEW
): Observable<Page<EmployeeAllocationSummary>> {
  let params = new HttpParams()
    .set('page', page.toString())
    .set('size', size.toString());

  if (search) params = params.set('search', search);
  if (allocationType) params = params.set('allocationType', allocationType);
  if (managerId) params = params.set('managerId', managerId.toString());
  if (year) params = params.set('year', year.toString());      // NEW
  if (month) params = params.set('month', month.toString());   // NEW

  return this.http.get<Page<EmployeeAllocationSummary>>(
    `${this.apiUrl}/allocations/grouped`, { params });
}

getAllocationManagers(
  allocationType?: string,
  search?: string,
  managerSearch?: string,
  year?: number,    // NEW
  month?: number    // NEW
): Observable<Manager[]> {
  let params = new HttpParams();
  if (allocationType) params = params.set('allocationType', allocationType);
  if (search) params = params.set('search', search);
  if (managerSearch) params = params.set('managerSearch', managerSearch);
  if (year) params = params.set('year', year.toString());
  if (month) params = params.set('month', month.toString());

  return this.http.get<Manager[]>(`${this.apiUrl}/allocations/managers`, { params });
}

// P3 ONLY
getAvailableMonths(
  allocationType?: string,
  managerId?: number,
  search?: string
): Observable<AvailableMonth[]> {
  let params = new HttpParams();
  if (allocationType) params = params.set('allocationType', allocationType);
  if (managerId) params = params.set('managerId', managerId.toString());
  if (search) params = params.set('search', search);

  return this.http.get<AvailableMonth[]>(
    `${this.apiUrl}/allocations/available-months`, { params });
}
```

---

## Phase 3: Testing Strategy

### 3.1 Backend Unit Tests

**File**: `backend/src/test/java/com/atlas/specification/AllocationSpecificationTest.java`

**New Test Class**:

```java
@ExtendWith(MockitoExtension.class)
class AllocationSpecificationTest {

    @Mock private CriteriaBuilder cb;
    @Mock private CriteriaQuery<?> query;
    @Mock private Root<Allocation> root;

    @Test
    @DisplayName("byMonthYear - should filter allocations active in specified month")
    void byMonthYear_validMonthYear_filtersCorrectly() {
        // Arrange: Mock joins and predicates
        // Act: Build specification with year=2026, month=2
        // Assert: Verify date range predicates created correctly
    }

    @Test
    @DisplayName("byMonthYear - should handle NULL endDate (ongoing allocations)")
    void byMonthYear_nullEndDate_includesAllocation() {
        // Arrange: Allocation with startDate=2025-01-01, endDate=NULL
        // Act: Filter by year=2026, month=6
        // Assert: Allocation included (ongoing)
    }

    @Test
    @DisplayName("byMonthYear - should exclude allocations outside date range")
    void byMonthYear_allocationOutsideRange_excludes() {
        // Arrange: Allocation startDate=2025-01-01, endDate=2025-12-31
        // Act: Filter by year=2026, month=6
        // Assert: Allocation excluded
    }
}
```

---

**File**: `backend/src/test/java/com/atlas/service/AllocationServiceTest.java`

**New Tests**:

```java
@Test
@DisplayName("getGroupedAllocations - with year/month filter - returns allocations in range")
void getGroupedAllocations_withMonthYearFilter_returnsFilteredResults() {
    // Arrange: Create test allocations spanning multiple months
    // Mock repository to return only allocations in Feb 2026
    // Act: Call service with year=2026, month=2
    // Assert: Only Feb 2026 allocations returned
}

@Test
@DisplayName("getGroupedAllocations - with NULL year/month - defaults to current month")
void getGroupedAllocations_nullYearMonth_defaultsToCurrentMonth() {
    // Arrange: Mock current date to be Feb 2026
    // Act: Call service with year=null, month=null
    // Assert: Service defaults to Feb 2026 and queries correctly
}

@Test
@DisplayName("getGroupedAllocations - BENCH employees - visible regardless of month")
void getGroupedAllocations_benchEmployees_alwaysVisible() {
    // Arrange: Create employee with NO allocations (BENCH)
    // Act: Call service with year=2030, month=12 (far future)
    // Assert: BENCH employee still appears in results
}
```

---

### 3.2 Frontend Unit Tests

**File**: `frontend/src/app/pages/allocations/allocations.component.spec.ts`

**New Tests**:

```typescript
describe('AllocationsComponent - Month/Year Filter', () => {

  it('should initialize with current month and year', () => {
    const currentDate = new Date();
    expect(component.selectedYear()).toBe(currentDate.getFullYear());
    expect(component.selectedMonth()).toBe(currentDate.getMonth() + 1);
  });

  it('should reset pagination when month/year changes', () => {
    component.currentPage.set(5);
    component.selectedMonth.set(6);
    component.onMonthYearChange();
    expect(component.currentPage()).toBe(0);
  });

  it('should pass year/month to API when loading allocations', () => {
    spyOn(apiService, 'getGroupedAllocations').and.returnValue(of(mockPage));
    component.selectedYear.set(2025);
    component.selectedMonth.set(3);
    component.loadAllocations();

    expect(apiService.getGroupedAllocations).toHaveBeenCalledWith(
      jasmine.any(Number), jasmine.any(Number),
      jasmine.any(String), jasmine.any(String), jasmine.any(Number),
      2025, 3  // year, month
    );
  });

  // P3 test
  it('should identify available vs dimmed months', () => {
    component.availableMonths.set([
      { year: 2026, month: 1, count: 10 },
      { year: 2026, month: 3, count: 5 }
    ]);

    expect(component.isMonthAvailable(2026, 1)).toBeTrue();
    expect(component.isMonthAvailable(2026, 2)).toBeFalse(); // Dimmed
    expect(component.isMonthAvailable(2026, 3)).toBeTrue();
  });
});
```

---

### 3.3 Integration Testing Plan

**Manual Test Scenarios**:

1. **Default behavior**: Load allocations page → Verify current month is pre-selected
2. **Month change**: Select different month → Verify allocations update, pagination resets
3. **Faceted search**: Select manager + month → Verify allocation type dropdown shows only types for that manager in that month
4. **BENCH handling**: Filter by BENCH → Change months → Verify BENCH employees remain visible
5. **Date range edge cases**:
   - Allocation spanning months (Jan 1 - Mar 31) → Visible in Jan, Feb, Mar
   - Allocation ending mid-month → Visible in that month
   - Ongoing allocation (NULL endDate) → Visible in all future months
6. **Performance**: Load page with 10,000+ allocations, change month → Verify < 2s response

**SQL Query Validation**:
- Inspect generated SQL (enable Hibernate SQL logging)
- Verify no N+1 queries
- Confirm date range predicates in WHERE clause
- Check query plan for index usage

---

## Phase 4: Deployment & Rollout

### 4.1 Feature Flag (Optional)

**Recommendation**: No feature flag needed. Month/year filter is additive (doesn't break existing functionality).

**Alternative**: If cautious, add backend toggle:
```properties
# application.properties
features.allocation.month-filter.enabled=true
```

---

### 4.2 Database Migration

**No migrations required** - uses existing columns.

**Pre-deployment check**:
```sql
-- Verify date columns exist and are nullable
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'allocations'
  AND column_name IN ('start_date', 'end_date');
```

---

### 4.3 Performance Baseline

**Before deployment**, establish baseline metrics:
- Average response time for `/api/allocations/grouped` (current)
- 95th percentile response time
- Database query execution time

**After deployment**, monitor:
- New endpoint response times with year/month params
- Database slow query log for date range queries
- Frontend render time for month/year picker

**Alerts**: Set up monitoring for:
- Response time > 3 seconds (above 2s SLA)
- Error rate > 1% on allocation endpoints

---

## Constitution Compliance Checklist

Before merging to `main`, verify:

- [ ] **ABAC**: All queries filter by `getAccessibleEmployeeIds()`
- [ ] **Database-First**: No in-memory filtering of allocations by date
- [ ] **Faceted Search**: Manager/type dropdowns accept and apply year/month filters
- [ ] **NULL-Safe**: Service layer defaults NULL year/month to current date
- [ ] **Query Performance**: Date range queries use indexes, < 2s response time
- [ ] **BENCH Handling**: Employees with no allocations visible across all months
- [ ] **Testing**: Unit tests for date range logic, BENCH special case, faceted search integration
- [ ] **Pagination**: Reset to page 0 when month/year changes

---

## Open Questions for User Confirmation

1. **P3 Scope**: Should "available months indicator" be included in MVP, or deferred to v2?
   - **MVP (P1/P2)**: Month picker allows any month, shows empty state if no data
   - **v2 (P3)**: Month picker dims months with no PROJECT allocations

2. **Date Picker UI**: Prefer native HTML5 `<input type="month">` (simple) or Angular Material (polished)?
   - Recommendation: Native for MVP, Material if already a dependency

3. **Year Range**: Should month picker limit selectable years (e.g., ±5 years from current)?
   - Recommendation: No hard limit in backend, optional UI limit to prevent excessive scrolling

4. **BENCH Behavior Confirmation**: Verify BENCH employees should appear in ALL months (spec assumption)?
   - Rationale: BENCH = no allocation records, so month filter doesn't apply

---

**Next Step**: Confirm research decisions (Section: Phase 0), then proceed to implementation.
