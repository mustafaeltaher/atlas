# Quick Start: Allocation Month/Year Filter

**Feature**: 002-allocation-month-year-filter
**For**: Developers implementing this feature
**Last Updated**: 2026-02-23

## 30-Second Summary

Add a month/year picker to the allocations page (second filter, after search box). Filter allocations by time period using date range overlap logic. Default to current month. BENCH employees always visible. Integrate with faceted search (all filters affect each other).

---

## Implementation Checklist

### Backend (Java/Spring Boot)

- [ ] **Update `AllocationSpecification.java`**
  - Add `year` and `month` parameters to `withFilters()` method
  - Add date range predicate: `startDate <= lastDay AND (endDate IS NULL OR endDate >= firstDay)`
  - ~20 lines of code

- [ ] **Update `AllocationService.java`**
  - Add `year`/`month` params to service methods
  - Default NULL params to current date
  - Pass to Specification
  - ~10 lines per method (3 methods)

- [ ] **Update `AllocationController.java`**
  - Add `@RequestParam Integer year` and `@RequestParam Integer month` to endpoints:
    - `/api/allocations/grouped`
    - `/api/allocations/managers`
    - `/api/allocations/allocation-types`
  - ~2 lines per endpoint

- [ ] **Update `AllocationRepositoryCustomImpl.java`**
  - Add `year`/`month` params to custom query methods
  - Update native SQL with date range conditions
  - ~15 lines per method (2 methods)

- [ ] **Add database index** (optional but recommended)
  ```sql
  CREATE INDEX CONCURRENTLY idx_allocations_employee_dates
  ON allocations(employee_id, start_date, end_date);
  ```

- [ ] **Write unit tests**
  - `AllocationSpecificationTest`: Date range logic, NULL endDate, exclusions
  - `AllocationServiceTest`: Defaults, BENCH handling, faceted integration
  - ~8 test methods total

### Frontend (Angular/TypeScript)

- [ ] **Update `allocations.component.ts`**
  - Add signals: `selectedYear = signal<number>(new Date().getFullYear())`
  - Add signals: `selectedMonth = signal<number>(new Date().getMonth() + 1)`
  - Add method: `onMonthYearChange()` (reset pagination, reload data)
  - Update `loadAllocations()`, `loadManagers()`, `loadAllocationTypes()` to pass year/month
  - ~30 lines of code

- [ ] **Update `allocations.component.html`** (template)
  - Add month picker after search box:
    ```html
    <input type="month" class="filter-select month-picker"
           [value]="selectedYear() + '-' + selectedMonth().toString().padStart(2, '0')"
           (change)="onMonthInputChange($event)">
    ```
  - ~5 lines in template

- [ ] **Update `api.service.ts`**
  - Add `year?: number, month?: number` params to:
    - `getGroupedAllocations()`
    - `getAllocationManagers()`
    - `getAllocationTypes()`
  - Add params to HttpParams if not null
  - ~3 lines per method

- [ ] **Add CSS styling** (if needed)
  ```css
  .month-picker { min-width: 160px; cursor: pointer; }
  ```

### Testing

- [ ] **Manual testing scenarios**
  1. Load page → Verify current month selected
  2. Change month → Verify allocations update
  3. Select manager + month → Verify faceted dropdowns update
  4. Search + month → Verify results combine both filters
  5. BENCH filter + change months → BENCH employees remain visible
  6. Edge cases: Spanning allocations, mid-month dates, NULL endDate

- [ ] **Performance testing**
  - Load 10,000+ allocations → Verify < 2s response
  - Change month → Verify < 1s dropdown refresh

---

## Key Code Snippets

### Backend: Date Range Predicate (JPA Criteria)

```java
// In AllocationSpecification.withFilters()
if (year != null && month != null) {
    LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
    LocalDate lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth());

    Predicate startCheck = cb.lessThanOrEqualTo(root.get("startDate"), lastDayOfMonth);
    Predicate endNull = cb.isNull(root.get("endDate"));
    Predicate endCheck = cb.greaterThanOrEqualTo(root.get("endDate"), firstDayOfMonth);

    predicates.add(cb.and(startCheck, cb.or(endNull, endCheck)));
}
```

### Backend: Default to Current Month

```java
// In AllocationService
if (year == null) year = LocalDate.now().getYear();
if (month == null) month = LocalDate.now().getMonthValue();
```

### Frontend: Month Picker Input Handler

```typescript
onMonthInputChange(event: Event): void {
  const input = event.target as HTMLInputElement;
  const [year, month] = input.value.split('-').map(Number);
  this.selectedYear.set(year);
  this.selectedMonth.set(month);
  this.onMonthYearChange();
}

onMonthYearChange(): void {
  this.currentPage.set(0); // Reset pagination
  this.loadAllocations();
  this.loadManagers();
  this.loadAllocationTypes();
}
```

### Frontend: Pass Params to API

```typescript
loadAllocations(): void {
  const year = this.selectedYear();
  const month = this.selectedMonth();

  this.apiService.getGroupedAllocations(
    this.currentPage(), this.pageSize(),
    this.searchTerm, this.allocationTypeFilter, this.managerFilter,
    year, month  // NEW
  ).subscribe(/* ... */);
}
```

---

## Common Pitfalls

### ❌ Don't: Filter in memory
```java
// BAD - loads all allocations then filters in Java
List<Allocation> all = repository.findAll();
return all.stream()
    .filter(a -> isActiveInMonth(a, year, month))
    .collect(Collectors.toList());
```

### ✅ Do: Filter at database level
```java
// GOOD - filters in SQL query
Specification<Allocation> spec = AllocationSpecification.withFilters(
    allocationType, managerId, search, accessibleIds, year, month);
return repository.findAll(spec, pageable);
```

---

### ❌ Don't: Forget BENCH employees
```java
// BAD - BENCH employees disappear when month filter applied
return repository.findAllocationsByMonth(year, month);
```

### ✅ Do: Use LEFT JOIN for BENCH
```java
// GOOD - BENCH employees (no allocations) included
SELECT e.* FROM employees e
LEFT JOIN allocations a ON e.id = a.employee_id
  AND (date range conditions)
WHERE (a.id IS NOT NULL OR NOT EXISTS (SELECT 1 FROM allocations WHERE employee_id = e.id))
```

---

### ❌ Don't: Ignore NULL endDate
```java
// BAD - excludes ongoing allocations
WHERE start_date <= :lastDay AND end_date >= :firstDay
```

### ✅ Do: Handle NULL endDate
```java
// GOOD - includes ongoing allocations (endDate IS NULL)
WHERE start_date <= :lastDay
  AND (end_date IS NULL OR end_date >= :firstDay)
```

---

### ❌ Don't: Forget to reset pagination
```typescript
// BAD - user may land on empty page
onMonthYearChange(): void {
  this.loadAllocations(); // currentPage might be 5, but new filter only has 2 pages
}
```

### ✅ Do: Reset to page 0
```typescript
// GOOD - consistent with other filters
onMonthYearChange(): void {
  this.currentPage.set(0); // Always start at first page
  this.loadAllocations();
}
```

---

## Testing Commands

### Backend

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=AllocationSpecificationTest

# Run specific test method
./mvnw test -Dtest=AllocationServiceTest#getGroupedAllocations_withMonthYearFilter_returnsFilteredResults
```

### Frontend

```bash
# Run all tests
npm test

# Run specific test file
npm test -- allocations.component.spec.ts

# Run in watch mode
npm test -- --watch
```

### Database

```sql
-- Verify index exists
SELECT indexname, indexdef
FROM pg_indexes
WHERE tablename = 'allocations' AND indexname = 'idx_allocations_employee_dates';

-- Test query performance
EXPLAIN ANALYZE
SELECT * FROM allocations
WHERE employee_id = 123
  AND start_date <= '2026-02-28'
  AND (end_date IS NULL OR end_date >= '2026-02-01');
-- Should show "Index Scan using idx_allocations_employee_dates"
```

---

## API Examples

### Get allocations for February 2026

```http
GET /api/allocations/grouped?page=0&size=10&year=2026&month=2
Authorization: Bearer <token>
```

### Get allocations for current month (defaults)

```http
GET /api/allocations/grouped?page=0&size=10
Authorization: Bearer <token>
```

### Get allocations with faceted search

```http
GET /api/allocations/grouped?page=0&size=10&year=2026&month=2&allocationType=PROJECT&managerId=42&search=John
Authorization: Bearer <token>
```

### Get managers for February 2026 PROJECT allocations

```http
GET /api/allocations/managers?year=2026&month=2&allocationType=PROJECT
Authorization: Bearer <token>
```

---

## Estimated Effort

- **Backend**: 2-3 hours
  - Specification update: 30 min
  - Service/Controller updates: 1 hour
  - Repository custom queries: 45 min
  - Unit tests: 45 min

- **Frontend**: 1-2 hours
  - Component state/methods: 30 min
  - Template UI: 15 min
  - API service updates: 15 min
  - Testing: 30 min

- **Total**: 3-5 hours for P1/P2 (MVP without P3 "available months")

---

## Resources

- **Spec**: [spec.md](./spec.md)
- **Plan**: [plan.md](./plan.md)
- **Research**: [research.md](./research.md)
- **API Contracts**: [contracts/api-contracts.yaml](./contracts/api-contracts.yaml)
- **Data Model**: [data-model.md](./data-model.md)
- **Constitution**: [../../memory/constitution.md](../../memory/constitution.md)

---

## Need Help?

1. **Date range logic unclear?** → See [data-model.md](./data-model.md) examples
2. **BENCH handling confusing?** → See [research.md](./research.md) Decision #2
3. **API contracts?** → See [contracts/api-contracts.yaml](./contracts/api-contracts.yaml)
4. **Constitution compliance?** → See [plan.md](./plan.md) Phase 4 checklist
