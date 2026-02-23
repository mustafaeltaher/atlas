# Implementation Tasks: Allocation Month/Year Filter

**Feature**: 002-allocation-month-year-filter
**Branch**: `002-allocation-month-year-filter`
**Date**: 2026-02-23
**Status**: Ready for Implementation

---

## Task Breakdown

### Phase 1: Backend Core Implementation

#### Task 1.1: Update AllocationSpecification ⏱️ 30 min

**File**: `backend/src/main/java/com/atlas/specification/AllocationSpecification.java`

**Changes**:
- [ ] Add `Integer year` parameter to `withFilters()` method signature
- [ ] Add `Integer month` parameter to `withFilters()` method signature
- [ ] Add date range calculation logic:
  ```java
  LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
  LocalDate lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth());
  ```
- [ ] Add date range predicate to filter allocations active in selected month
- [ ] Handle NULL endDate case (ongoing allocations)

**Acceptance Criteria**:
- Specification compiles without errors
- Date range predicate correctly filters allocations overlapping with selected month
- NULL endDate allocations are included if they started before/during selected month

---

#### Task 1.2: Update AllocationService ⏱️ 1 hour

**File**: `backend/src/main/java/com/atlas/service/AllocationService.java`

**Changes**:
- [ ] Update `getGroupedAllocations()`: Add `Integer year, Integer month` parameters
- [ ] Update `getGroupedAllocations()`: Default NULL year/month to current date
- [ ] Update `getGroupedAllocations()`: Pass year/month to AllocationSpecification
- [ ] Update `getManagersForAllocations()`: Add `Integer year, Integer month` parameters
- [ ] Update `getManagersForAllocations()`: Default NULL year/month to current date
- [ ] Update `getManagersForAllocations()`: Pass year/month to repository method
- [ ] Update `getDistinctAllocationTypes()`: Add `Integer year, Integer month` parameters
- [ ] Update `getDistinctAllocationTypes()`: Default NULL year/month to current date

**Acceptance Criteria**:
- All service methods compile and accept year/month parameters
- NULL parameters default to current year/month using `LocalDate.now()`
- Year/month correctly threaded through to Specification and Repository

---

#### Task 1.3: Update AllocationController ⏱️ 20 min

**File**: `backend/src/main/java/com/atlas/controller/AllocationController.java`

**Changes**:
- [ ] Add `@RequestParam(required = false) Integer year` to `/grouped` endpoint
- [ ] Add `@RequestParam(required = false) Integer month` to `/grouped` endpoint
- [ ] Add `@RequestParam(required = false) Integer year` to `/managers` endpoint
- [ ] Add `@RequestParam(required = false) Integer month` to `/managers` endpoint
- [ ] Add `@RequestParam(required = false) Integer year` to `/allocation-types` endpoint
- [ ] Add `@RequestParam(required = false) Integer month` to `/allocation-types` endpoint
- [ ] Pass year/month parameters to corresponding service methods

**Acceptance Criteria**:
- All endpoints compile with new parameters
- Swagger/OpenAPI documentation generates correctly
- Parameters are optional (endpoints work with and without them)

---

#### Task 1.4: Update Repository Custom Queries ⏱️ 45 min

**Files**:
- `backend/src/main/java/com/atlas/repository/AllocationRepositoryCustom.java`
- `backend/src/main/java/com/atlas/repository/AllocationRepositoryCustomImpl.java`

**Changes**:
- [ ] Update `AllocationRepositoryCustom` interface: Add year/month params to `findDistinctManagers()`
- [ ] Update `AllocationRepositoryCustomImpl`: Add year/month params to `findDistinctManagers()` method
- [ ] Update native SQL query to include date range filter:
  ```sql
  LEFT JOIN allocations a ON e.id = a.employee_id
    AND (:firstDay IS NULL OR a.start_date <= :lastDay)
    AND (:firstDay IS NULL OR a.end_date IS NULL OR a.end_date >= :firstDay)
  ```
- [ ] Calculate `firstDay` and `lastDay` from year/month parameters
- [ ] Add early return for empty `accessibleIds` list (native SQL IN clause requirement)
- [ ] Update similar patterns for other custom repository methods if needed

**Acceptance Criteria**:
- Native SQL queries execute without errors
- Date range filter correctly applied in JOIN condition (not WHERE clause)
- BENCH employees (no allocations) included regardless of month filter
- Empty accessibleIds handled correctly (returns empty list, not SQL error)

---

### Phase 2: Frontend Implementation

#### Task 2.1: Update Component State ⏱️ 20 min

**File**: `frontend/src/app/pages/allocations/allocations.component.ts`

**Changes**:
- [ ] Add signal: `selectedYear = signal<number>(new Date().getFullYear())`
- [ ] Add signal: `selectedMonth = signal<number>(new Date().getMonth() + 1)`
- [ ] Add method: `onMonthInputChange(event: Event)` to parse month input value
- [ ] Add method: `onMonthYearChange()` to reset pagination and reload data
- [ ] Update `loadAllocations()`: Pass `year` and `month` to API call
- [ ] Update `loadManagers()`: Pass `year` and `month` to API call
- [ ] Update `loadAllocationTypes()`: Pass `year` and `month` to API call

**Acceptance Criteria**:
- Component compiles without TypeScript errors
- Signals initialize with current year/month on page load
- Changing month/year triggers data reload
- Pagination resets to page 0 when month changes

---

#### Task 2.2: Update Component Template ⏱️ 15 min

**File**: `frontend/src/app/pages/allocations/allocations.component.ts` (template section)

**Changes**:
- [ ] Add month picker `<input type="month">` after `.search-box` div
- [ ] Bind input value to `selectedYear()` and `selectedMonth()` signals
- [ ] Add `(change)` event handler calling `onMonthInputChange($event)`
- [ ] Apply `filter-select month-picker` CSS classes
- [ ] Add `title="Select Month"` for accessibility

**Acceptance Criteria**:
- Month picker renders in correct position (2nd filter, after search)
- Picker displays current month by default
- Changing picker value triggers component update
- UI matches existing filter styling

---

#### Task 2.3: Update API Service ⏱️ 15 min

**File**: `frontend/src/app/services/api.service.ts`

**Changes**:
- [ ] Update `getGroupedAllocations()`: Add `year?: number` parameter
- [ ] Update `getGroupedAllocations()`: Add `month?: number` parameter
- [ ] Update `getGroupedAllocations()`: Add year/month to HttpParams if provided
- [ ] Update `getAllocationManagers()`: Add `year?: number, month?: number` parameters
- [ ] Update `getAllocationManagers()`: Add year/month to HttpParams
- [ ] Update `getAllocationTypes()`: Add `year?: number, month?: number` parameters (if not already)
- [ ] Update `getAllocationTypes()`: Add year/month to HttpParams

**Acceptance Criteria**:
- API service methods compile with new parameters
- Parameters correctly appended to query string when provided
- Null/undefined parameters are not included in URL
- API calls succeed and return expected data

---

### Phase 3: Testing

#### Task 3.1: Backend Unit Tests ⏱️ 45 min

**Files**:
- `backend/src/test/java/com/atlas/specification/AllocationSpecificationTest.java` (NEW)
- `backend/src/test/java/com/atlas/service/AllocationServiceTest.java` (UPDATE)

**Tests to Create**:

**AllocationSpecificationTest**:
- [ ] `byMonthYear_validMonthYear_filtersCorrectly()` - Verify date range predicate
- [ ] `byMonthYear_nullEndDate_includesOngoingAllocations()` - Test NULL endDate case
- [ ] `byMonthYear_allocationOutsideRange_excludes()` - Test exclusion logic
- [ ] `byMonthYear_allocationSpansMonth_includes()` - Test mid-month overlap

**AllocationServiceTest** (update existing):
- [ ] `getGroupedAllocations_withMonthYearFilter_returnsFilteredResults()` - Integration test
- [ ] `getGroupedAllocations_nullYearMonth_defaultsToCurrentMonth()` - Test defaults
- [ ] `getGroupedAllocations_benchEmployees_visibleRegardlessOfMonth()` - BENCH special case
- [ ] `getManagersForAllocations_withMonthYear_returnsFactedResults()` - Faceted search test

**Acceptance Criteria**:
- All tests pass (`./mvnw test`)
- Test coverage > 80% for new code
- Edge cases covered (NULL endDate, date boundaries, BENCH employees)

---

#### Task 3.2: Frontend Unit Tests ⏱️ 30 min

**File**: `frontend/src/app/pages/allocations/allocations.component.spec.ts`

**Tests to Create**:
- [ ] `should initialize with current month and year`
- [ ] `should reset pagination when month changes`
- [ ] `should pass year and month to API when loading allocations`
- [ ] `should parse month input value correctly`
- [ ] `should reload all faceted filters when month changes`

**Acceptance Criteria**:
- All tests pass (`npm test`)
- Component behavior verified for month selection and data loading

---

#### Task 3.3: Manual Integration Testing ⏱️ 1 hour

**Test Scenarios**:

**Scenario 1: Default Behavior**
- [ ] Load allocations page
- [ ] Verify month picker shows current month (e.g., "2026-02")
- [ ] Verify allocations displayed are for current month

**Scenario 2: Month Change**
- [ ] Select different month (e.g., January 2026)
- [ ] Verify allocations update to show January data
- [ ] Verify pagination resets to page 1

**Scenario 3: Faceted Search Integration**
- [ ] Select month: February 2026
- [ ] Select allocation type: PROJECT
- [ ] Open manager dropdown
- [ ] Verify dropdown shows only managers with PROJECT allocations in February

**Scenario 4: BENCH Employees**
- [ ] Filter by allocation type: BENCH (or identify BENCH employee visually)
- [ ] Change months (Jan, Feb, Mar)
- [ ] Verify BENCH employees remain visible across all months

**Scenario 5: Edge Cases**
- [ ] Select future month (e.g., June 2026)
- [ ] Verify allocations scheduled for June are shown
- [ ] Select past month with no data
- [ ] Verify empty state message displayed

**Scenario 6: Multi-Filter Combination**
- [ ] Apply search term: "John"
- [ ] Select manager: "Alice Manager"
- [ ] Select month: February 2026
- [ ] Verify results match ALL three filters

**Acceptance Criteria**:
- All scenarios pass without errors
- UI remains responsive (< 2s for data load)
- No console errors
- Faceted search behaves correctly

---

### Phase 4: Database Optimization (Optional)

#### Task 4.1: Add Performance Index ⏱️ 15 min

**Database**: PostgreSQL

**Changes**:
- [ ] Create index on allocations table:
  ```sql
  CREATE INDEX CONCURRENTLY idx_allocations_employee_dates
  ON allocations(employee_id, start_date, end_date);
  ```
- [ ] Verify index creation (check `pg_indexes`)
- [ ] Test query performance with EXPLAIN ANALYZE

**Acceptance Criteria**:
- Index created successfully
- Query plan uses index (not sequential scan)
- Query execution time < 200ms for 10k+ allocations

---

### Phase 5: Documentation & Cleanup

#### Task 5.1: Update Documentation ⏱️ 15 min

**Changes**:
- [ ] Update API documentation (Swagger/OpenAPI) if not auto-generated
- [ ] Add JSDoc comments to new TypeScript methods
- [ ] Update README.md with new filter feature (if applicable)

**Acceptance Criteria**:
- API docs reflect new year/month parameters
- Code is well-commented

---

#### Task 5.2: Code Review Preparation ⏱️ 10 min

**Checklist**:
- [ ] All constitution compliance checks passed (see [plan.md](./plan.md))
- [ ] ABAC: All queries filter by `getAccessibleEmployeeIds()`
- [ ] Database-First: No in-memory filtering of allocations
- [ ] Faceted Search: All filter endpoints accept year/month params
- [ ] NULL-Safe: Service defaults NULL year/month to current date
- [ ] BENCH Handling: LEFT JOIN pattern correctly includes BENCH employees
- [ ] Testing: Unit tests pass, manual testing complete
- [ ] Performance: Query times < 2 seconds

**Acceptance Criteria**:
- All checklist items verified
- Code ready for PR submission

---

## Summary

### Total Estimated Effort

| Phase | Tasks | Estimated Time |
|-------|-------|----------------|
| Backend Core | 4 tasks | 2h 35min |
| Frontend | 3 tasks | 50min |
| Testing | 3 tasks | 2h 15min |
| Database | 1 task | 15min |
| Documentation | 2 tasks | 25min |
| **Total** | **13 tasks** | **~6 hours** |

**Note**: Estimates are for focused development time. Include buffer for context switching, debugging, and code review iterations.

---

## Task Dependencies

```
Task 1.1 (Specification) → Task 1.2 (Service) → Task 1.3 (Controller)
                         ↓
Task 1.4 (Repository)   → Task 3.1 (Backend Tests)
                         ↓
Task 2.1 (Component)    → Task 2.2 (Template) → Task 2.3 (API Service)
                         ↓
Task 3.2 (Frontend Tests)
                         ↓
Task 3.3 (Integration Testing)
                         ↓
Task 4.1 (Index)        → Task 5.1 (Docs) → Task 5.2 (Review)
```

**Parallelization Opportunity**: Tasks 1.1-1.4 (Backend) and Tasks 2.1-2.3 (Frontend) can be worked on simultaneously by different developers.

---

## Progress Tracking

**Status Legend**:
- ⬜ Not Started
- 🟦 In Progress
- ✅ Completed
- ❌ Blocked

| Task ID | Description | Status | Assignee | Notes |
|---------|-------------|--------|----------|-------|
| 1.1 | Update AllocationSpecification | ⬜ | - | - |
| 1.2 | Update AllocationService | ⬜ | - | - |
| 1.3 | Update AllocationController | ⬜ | - | - |
| 1.4 | Update Repository Queries | ⬜ | - | - |
| 2.1 | Update Component State | ⬜ | - | - |
| 2.2 | Update Template | ⬜ | - | - |
| 2.3 | Update API Service | ⬜ | - | - |
| 3.1 | Backend Unit Tests | ⬜ | - | - |
| 3.2 | Frontend Unit Tests | ⬜ | - | - |
| 3.3 | Manual Integration Testing | ⬜ | - | - |
| 4.1 | Add Performance Index | ⬜ | - | Optional |
| 5.1 | Update Documentation | ⬜ | - | - |
| 5.2 | Code Review Prep | ⬜ | - | - |

---

## Ready to Start?

1. **Create feature branch**: `git checkout -b 002-allocation-month-year-filter`
2. **Start with backend**: Begin with Task 1.1 (AllocationSpecification)
3. **Test incrementally**: Run tests after each task completion
4. **Refer to quickstart**: See [quickstart.md](./quickstart.md) for code snippets
5. **Check constitution**: Verify compliance at each phase

**Questions?** See [plan.md](./plan.md) or [research.md](./research.md) for detailed decisions.
