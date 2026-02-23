# Feature Specification: Month/Year Filter for Allocations Page

**Feature Branch**: `002-allocation-month-year-filter`
**Created**: 2026-02-23
**Status**: Completed
**Completed**: 2026-02-23
**Input**: User description: "Add a date picker filter in the allocations page that should be shown as a regular calendar component in the UI only without days, that enables the manager to select the year/month and add to the already existing faceted search a new search dimension to fetch the selected month/year allocations. The default value of the component should be pointing to the current month. Since faceted search should be supported, selecting any other filter in the page should impact the new date picker filter. The date filter should be the second filter in the page after the free search text filter. BENCH-based allocations can be seen even if no allocations exist in the selected month, while if a PROJECT-based allocation was selected then all the months that have project allocations should be shown while the other months should be dimmed."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - View Allocations for Specific Month (Priority: P1)

As a manager, I want to select a specific year/month to view all employee allocations for that time period, so that I can understand resource allocation at any point in time.

**Why this priority**: This is the core functionality - without the ability to filter by month/year, the entire feature has no value. This delivers immediate business value by allowing historical and future allocation tracking.

**Independent Test**: Can be fully tested by selecting different months in the date picker and verifying that allocation data updates to show only allocations active in the selected period. Delivers value by enabling time-based allocation analysis.

**Acceptance Scenarios**:

1. **Given** I am on the allocations page, **When** I open the month/year picker, **Then** I see a calendar UI showing only months and years (no day selection)
2. **Given** The page loads for the first time, **When** I view the month/year filter, **Then** it defaults to the current month and year (e.g., "February 2026")
3. **Given** I select "January 2026" from the month/year picker, **When** the data refreshes, **Then** I see only employees with allocations active during January 2026
4. **Given** I select a future month (e.g., "June 2026"), **When** the data loads, **Then** I see employees with allocations scheduled for that future period

---

### User Story 2 - Faceted Search Integration (Priority: P1)

As a manager, I want the month/year filter to work seamlessly with other filters (allocation type, manager, search), so that I can narrow down allocations across multiple dimensions simultaneously.

**Why this priority**: Faceted search is a core architectural principle (per constitution). Without this, the filter would break existing search patterns and provide inconsistent UX. This is essential for the feature to align with system architecture.

**Independent Test**: Can be tested by combining the month/year filter with existing filters (manager dropdown, allocation type, search text) and verifying that all filters affect each other's available options at the database level.

**Acceptance Scenarios**:

1. **Given** I have selected "January 2026" in the month/year filter and "PROJECT" in allocation type filter, **When** I open the manager dropdown, **Then** I see only managers who have employees with PROJECT allocations in January 2026
2. **Given** I have selected a specific manager and month, **When** I open the allocation type dropdown, **Then** I see only allocation types that exist for that manager's employees in the selected month
3. **Given** I have entered a search term "John", **When** I select a different month, **Then** the search results update to show only "John" entries with allocations in the newly selected month
4. **Given** I select a month with no allocations matching my current filters, **When** the page refreshes, **Then** I see an empty state message indicating no allocations found for the selected criteria

---

### User Story 3 - BENCH Status Special Handling (Priority: P2)

As a manager, I want to see BENCH employees regardless of the selected month (since BENCH means NO allocations exist), so that I can identify available resources in any time period.

**Why this priority**: BENCH is a derived status (employee with NO allocation records). This special case handling prevents BENCH employees from disappearing when filtering by month. Critical for resource planning but secondary to core filtering (P1).

**Independent Test**: Can be tested by filtering allocations by "BENCH" in allocation status and changing months - BENCH employees should remain visible across all months since they have no month-specific allocations.

**Acceptance Scenarios**:

1. **Given** An employee has NO allocation records (BENCH status), **When** I select any month in the month/year picker, **Then** that BENCH employee remains visible in the results
2. **Given** I filter by allocation type "BENCH" (if supported) or allocation status "BENCH", **When** I change the month filter, **Then** the BENCH employees list does not change based on month selection
3. **Given** I search for a BENCH employee by name, **When** I select different months, **Then** that employee appears consistently across all months
4. **Given** I combine BENCH status filter with a manager filter, **When** I change months, **Then** I see all BENCH employees under that manager regardless of selected month

---

### User Story 4 - Available Months Indicator for PROJECT Allocations (Priority: P3)

As a manager, when viewing PROJECT allocations, I want to see which months have allocation data (enabled/highlighted) and which months are empty (dimmed/disabled), so that I can quickly identify time periods with activity.

**Why this priority**: This is a UX enhancement that improves navigation efficiency but is not essential for core functionality. Users can still select any month and see results (or empty state). This provides visual guidance but doesn't block core workflows.

**Independent Test**: Can be tested by selecting the PROJECT allocation type filter, then opening the month/year picker - months with PROJECT allocations should be visually distinct (enabled) from months without (dimmed).

**Acceptance Scenarios**:

1. **Given** I have filtered by allocation type "PROJECT", **When** I open the month/year picker, **Then** months that contain PROJECT allocations are shown as enabled/highlighted, and months without PROJECT allocations are dimmed/disabled
2. **Given** I select a dimmed month (no PROJECT allocations), **When** the page loads, **Then** I see an empty state message indicating no PROJECT allocations exist for that month
3. **Given** I select an enabled month (has PROJECT allocations), **When** the page loads, **Then** I see the list of employees with PROJECT allocations in that month
4. **Given** I change other filters (manager, search), **When** I open the month/year picker again, **Then** the enabled/dimmed month indicators update to reflect the available months for the current filter combination

---

### Edge Cases

- **What happens when a user selects a month far in the future (e.g., December 2030) with no allocations?**
  - System should display an empty state message: "No allocations found for the selected criteria"

- **How does the system handle allocations that span multiple months (e.g., startDate: Jan 1, endDate: June 30)?**
  - An allocation should be considered "active" in month X if: `startDate <= lastDayOfMonth(X) AND (endDate IS NULL OR endDate >= firstDayOfMonth(X))`

- **What happens when the month/year picker is opened for the first time?**
  - For P3 (available months indicator): System should query the database for distinct months with allocations matching current filters (allocation type, manager, search), then highlight those months in the picker

- **How does pagination interact with the month filter?**
  - When month filter changes, reset to page 0 (same pattern as existing filters in the codebase)

- **What if an employee has both PROJECT and BENCH allocations (e.g., 50% allocated, 50% bench)?**
  - This is not possible per the current system design: BENCH means NO allocation records exist. If an employee has any allocation record, they are not BENCH. This edge case should not occur.

- **What date range should the month/year picker support?**
  - Backend should support querying any year/month, but the UI picker could reasonably limit to ±5 years from current date to prevent excessive scrolling

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST provide a month/year picker UI component that displays months and years without day selection
- **FR-002**: System MUST default the month/year filter to the current month and year on initial page load
- **FR-003**: System MUST position the month/year filter as the second filter element, immediately after the free-text search box
- **FR-004**: System MUST filter allocation data at the database level (NOT in-memory) to show only allocations active in the selected month
- **FR-005**: System MUST apply faceted search logic: the month/year filter must accept and apply all other filter parameters (allocation type, manager, search)
- **FR-006**: System MUST allow other filter dropdowns (manager, allocation type) to query available options considering the selected month
- **FR-007**: For BENCH status employees (no allocation records), system MUST display them regardless of selected month
- **FR-008**: System MUST calculate allocation activity for a given month using date range logic: `startDate <= lastDayOfMonth AND (endDate IS NULL OR endDate >= firstDayOfMonth)`
- **FR-009**: System MUST reset pagination to page 0 when the month/year filter value changes
- **FR-010**: (P3 only) When allocation type "PROJECT" is selected, system SHOULD indicate which months have PROJECT allocations (enabled) vs which do not (dimmed)

### Key Entities *(include if feature involves data)*

- **MonthlyAllocation**: Existing entity tracking allocation percentages by month (year, month, percentage fields)
- **Allocation**: Existing entity with startDate, endDate, allocationType - the month filter will query against date ranges
- **EmployeeAllocationSummary**: Existing DTO grouping allocations by employee - will be filtered by selected month
- **Month/Year Filter State**: Frontend state (selectedYear: number, selectedMonth: number) passed to backend as query parameters

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Manager can view allocations for any historical or future month by selecting the month/year picker, with results returned in under 2 seconds (database-level filtering)
- **SC-002**: When month/year filter is combined with other filters (manager, allocation type, search), the system returns accurate faceted results with all filters applied at the database level (verified via SQL query logs)
- **SC-003**: BENCH employees (those with no allocation records) remain visible across all selected months, ensuring managers can always see available resources
- **SC-004**: (P3) When PROJECT allocation type is selected, the month/year picker visually distinguishes months with data (enabled) from months without (dimmed), reducing user trial-and-error by at least 50%
- **SC-005**: All filter interactions (changing month, typing search, selecting manager) maintain consistent faceted search behavior as verified by testing all filter combinations
- **SC-006**: The month/year picker UI component renders correctly on all supported browsers (Chrome, Firefox, Safari, Edge) and does not display day selection options
- **SC-007**: Page load performance remains under 2 seconds even when filtering across large datasets (tested with 1000+ employees and 10,000+ allocations)

---

## Technical Considerations (for planning phase)

### Database Query Pattern (IMPLEMENTED)
- ✅ JPA Specification extended in `AllocationSpecification.withFilters()` with year/month date range predicates
- ✅ Date range overlap logic: `startDate <= lastDayOfMonth AND (endDate IS NULL OR endDate >= firstDayOfMonth)`
- ✅ Custom repository methods (`AllocationRepositoryCustom` + `AllocationRepositoryCustomImpl`) using EntityManager + CriteriaQuery for DB-level distinct operations
- ✅ BENCH handling: Programmatically generate 180 months (10 years back + 5 forward) instead of DB query, since BENCH employees have NO allocation records
- ✅ PostgreSQL TO_CHAR function for extracting "YYYY-MM" from startDate

### Frontend Component (IMPLEMENTED)
- **Custom month picker component** (MonthPickerComponent) - Angular Material was rejected due to UX issues (hanging, showed days instead of months)
- Dropdown-style UI with compact 4x3 month grid (Jan-Dec)
- Year navigation controls (◄ 2026 ►)
- Visual availability indicators: enabled months clickable, unavailable months grayed out
- Read-only selection (no manual typing, selection only via picker)
- Position in `.filters-bar` as second element after `.search-box`
- Signal-based state management: `selectedYear = signal<number>(new Date().getFullYear())`, `selectedMonth = signal<number>(new Date().getMonth() + 1)`, `availableMonths = signal<string[]>([])`

### API Endpoints Modified (IMPLEMENTED)
- ✅ `GET /api/allocations/grouped` - added `year` and `month` query params (defaults to current month if NULL)
- ✅ `GET /api/allocations/managers` - added `year` and `month` query params for faceted search
- ✅ `GET /api/allocations/allocation-types` - added `year` and `month` query params for faceted search
- ✅ `GET /api/allocations/available-months` - **NEW endpoint** returning distinct "YYYY-MM" strings for months with allocations (special handling for BENCH: returns 180 months programmatically)

### Constitution Compliance Checklist
- [x] Uses JPA Specification for all filtering (no in-memory filtering)
- [x] Applies ABAC via `getAccessibleEmployeeIds()`
- [x] All optional parameters use `(:param IS NULL OR ...)` pattern in native queries
- [x] NULL-safe query parameters for year/month (handle case where user clears the filter - defaults to current month)
- [x] Faceted search: all filter endpoints accept all other filter parameters
- [x] BENCH status handled separately (direct Employee table query, not Allocation table)

### Implementation Summary
See [implementation-notes.md](./implementation-notes.md) for detailed documentation of:
- Custom month picker component (replaced Angular Material)
- BENCH unlimited months handling (10 years back, 5 years forward)
- Database-level faceted search with CriteriaQuery
- Comprehensive test coverage (144 tests passing)
