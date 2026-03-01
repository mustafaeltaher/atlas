# Feature Specification: Month-by-Month Allocation Percentage Entry

**Feature Branch**: `003-month-by-month-allocation`
**Created**: 2026-02-25
**Status**: Draft
**Input**: User description: "Introduce a new feature where managers should have an option to either enter the percentage of allocation once (during creation of an allocation entry, or editing an existing allocation) and this is what's happening now already and is translated to several monthly allocation entries depending on how long the allocation period is, or enter the allocation percentage individually (this is the new part) which should present to the user all the months of the allocation period to enter the desired allocation percentage for each of them, this should be achieved through a checkbox called 'Month by month allocation', note that entering the allocation entries should have no effect on the backend until the user submits the allocation"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Single Percentage Allocation (Existing Behavior) (Priority: P1)

As a manager, I want to create/edit an allocation with a single percentage that applies to all months in the allocation period, so that I can quickly set up uniform allocations without entering each month individually.

**Why this priority**: This is the existing functionality that must continue to work. It's the default behavior and covers the majority of use cases where allocations remain constant throughout the period. This is the foundation upon which the new feature will be built.

**Independent Test**: Can be fully tested by creating an allocation with start date Jan 1, end date Mar 31, selecting 100% allocation, and verifying that all three months (Jan, Feb, Mar) receive 100% allocation entries in the database.

**Acceptance Scenarios**:

1. **Given** I am creating a new allocation, **When** the modal opens, **Then** I see the "Month by month allocation" checkbox unchecked by default and a single allocation percentage dropdown
2. **Given** I have entered employee, project, start date (2026-03-01), end date (2026-05-31), and selected 75% allocation, **When** I submit the form, **Then** the system creates monthly allocation entries for March, April, and May 2026, each with 75%
3. **Given** I am editing an existing allocation, **When** the edit modal opens, **Then** I see the current allocation percentage displayed in the single dropdown and the checkbox remains unchecked
4. **Given** I modify the allocation percentage from 50% to 100% in edit mode, **When** I submit, **Then** all monthly allocation entries for the allocation period are updated to 100%

---

### User Story 2 - Month-by-Month Allocation Entry (Priority: P1)

As a manager, I want to enable "Month by month allocation" mode to enter different allocation percentages for each month in the allocation period, so that I can handle varying workload patterns (e.g., 100% in January, 50% in February, 75% in March).

**Why this priority**: This is the core new functionality requested. It provides flexibility for managers to model realistic allocation patterns where employee availability varies month-to-month. Essential for accurate resource planning.

**Independent Test**: Can be fully tested by checking the "Month by month allocation" checkbox, entering different percentages for each displayed month (e.g., Jan: 100%, Feb: 50%, Mar: 75%), submitting the form, and verifying each monthly allocation entry in the database matches the entered values.

**Acceptance Scenarios**:

1. **Given** I am creating a new allocation with start date (2026-06-01) and end date (2026-08-31), **When** I check the "Month by month allocation" checkbox, **Then** the single percentage dropdown is hidden and I see three month entries (June 2026, July 2026, August 2026) each with an individual percentage input
2. **Given** I have enabled month-by-month mode and see the month list, **When** I enter different percentages (June: 100%, July: 50%, August: 75%), **Then** the form holds these values client-side without making any backend calls
3. **Given** I have entered month-by-month percentages (June: 100%, July: 50%, August: 75%), **When** I submit the form, **Then** the system creates three monthly allocation entries with the exact percentages I specified for each month
4. **Given** I enable month-by-month mode, **When** I change the start or end date, **Then** the month list dynamically updates to show only the months within the new date range, preserving previously entered percentages where months overlap
5. **Given** I have entered month-by-month percentages, **When** I uncheck the "Month by month allocation" checkbox, **Then** the system shows a confirmation dialog warning that individual month data will be replaced by a single percentage, and upon confirmation, switches back to single percentage mode

---

### User Story 3 - Edit Existing Allocation with Month-by-Month Mode (Priority: P2)

As a manager, I want to edit an existing allocation and switch it to month-by-month mode to adjust individual month percentages retroactively, so that I can correct historical allocation data or adjust future months individually.

**Why this priority**: This extends the month-by-month feature to editing scenarios. While important for data correction and flexibility, users can achieve similar outcomes by deleting and recreating allocations. This is a convenience feature that improves the editing experience.

**Independent Test**: Can be tested by opening an existing allocation (e.g., Jan-Mar 2026, all at 100%), checking the month-by-month checkbox, modifying individual months (Jan: 100%, Feb: 75%, Mar: 50%), submitting, and verifying the monthly allocation percentages are updated in the database.

**Acceptance Scenarios**:

1. **Given** I open the edit modal for an existing allocation (Jan-Mar 2026, all at 100%), **When** the modal opens, **Then** I see the "Month by month allocation" checkbox unchecked and the single percentage dropdown showing 100%
2. **Given** I am editing an existing allocation, **When** I check the "Month by month allocation" checkbox, **Then** the system displays all months in the allocation period pre-populated with their current percentages (if uniform, all show the same value; if varied, each shows its specific value)
3. **Given** I have enabled month-by-month mode in edit mode and modified percentages (Jan: 100%, Feb: 75%, Mar: 50%), **When** I submit, **Then** the system updates the monthly allocation entries to match the new percentages
4. **Given** I am editing an existing allocation, **When** I view the Start Date and End Date fields, **Then** both fields are READ-ONLY (disabled) and cannot be modified - the allocation period is immutable in edit mode

---

### User Story 4 - Validation and User Feedback (Priority: P2)

As a manager, I want the system to validate my month-by-month entries and provide clear feedback on errors (e.g., missing percentages, invalid values), so that I don't accidentally submit incomplete or incorrect allocation data.

**Why this priority**: Data integrity is important, but basic validation (e.g., required fields, date ranges) already exists. This story adds specific validations for the month-by-month feature. It's a quality-of-life improvement but not blocking for core functionality—users can still manually check their entries.

**Independent Test**: Can be tested by attempting to submit a month-by-month allocation with missing percentage values, invalid percentages (e.g., 150%), or non-numeric input, and verifying the form shows clear error messages and prevents submission.

**Acceptance Scenarios**:

1. **Given** I have enabled month-by-month mode and left some month percentages empty, **When** I attempt to submit, **Then** the system highlights the empty fields with an error message "Percentage is required for all months" and prevents submission
2. **Given** I have entered an invalid percentage value (e.g., 0%, 150%, or -10%), **When** I attempt to submit, **Then** the system shows an error message "Percentage must be between 1 and 100" and highlights the invalid field
3. **Given** I have entered non-numeric characters in a percentage field, **When** I move focus or attempt to submit, **Then** the system shows an error message "Percentage must be a valid number" and clears the invalid input

---

### User Story 5 - UI/UX Enhancements for Month Display (Priority: P3)

As a manager, I want the month-by-month entry interface to be user-friendly with clear month labels, percentage controls (dropdowns or sliders), and visual indicators, so that I can quickly and accurately enter allocation data.

**Why this priority**: This is a UX polish story that improves the user experience but doesn't affect core functionality. The feature works with basic text inputs; this story adds conveniences like dropdowns, sliders, or date formatting that make the feature more pleasant to use but aren't critical for MVP.

**Independent Test**: Can be tested by reviewing the UI for month-by-month entry mode, ensuring month names are formatted clearly (e.g., "January 2026"), percentage inputs have appropriate controls (dropdowns with common values like 0%, 25%, 50%, 75%, 100% or sliders), and the interface is responsive and intuitive.

**Acceptance Scenarios**:

1. **Given** I have enabled month-by-month mode, **When** I view the month list, **Then** each month is displayed with a clear label format (e.g., "January 2026", "February 2026") and an intuitive percentage input control
2. **Given** I am entering percentages for multiple months, **When** I use the percentage input, **Then** I have access to quick-select options (e.g., dropdown with 0%, 25%, 50%, 75%, 100%) OR a slider with percentage markers
3. **Given** I have entered percentages for some months, **When** I review the form, **Then** I see visual feedback (e.g., filled vs. empty, progress bars, or color coding) indicating which months have been populated
4. **Given** The allocation period spans many months (e.g., 12+ months), **When** I view the month-by-month list, **Then** the interface provides scrolling or pagination to handle long lists without overwhelming the UI

---

### Edge Cases

- **How do managers access the edit allocation modal?**
  - From the main allocations page, each employee row has a pencil (edit) button. Clicking it opens a dialog that allows managers to either create a new allocation for that employee OR edit individual allocations the employee already has. The month-by-month checkbox appears in both create and edit modes for PROJECT allocations.

- **What happens when the start date is after the end date?**
  - System should validate and show an error message "Start date must be before end date" and prevent submission (existing validation should cover this).

- **How does the system handle allocations that span a single month?**
  - If start date and end date are in the same month (e.g., Jan 15 - Jan 25), the month-by-month mode should display only one month entry (January 2026) for percentage input.

- **How are mid-month dates handled for month calculation?**
  - System considers any month touched by the allocation period as a FULL month allocation. If startDate is Jan 15, January counts as full month. If endDate is Mar 10, March counts as full month. Backend normalizes dates to first day of month for range calculation (existing logic in AllocationService.java lines 293-294). Utilization calculation (pro-rated for partial months) is deferred to a future phase.

- **What if a manager checks and unchecks the "Month by month allocation" checkbox multiple times before submitting?**
  - Each time the checkbox is unchecked after entering month-by-month data, a confirmation dialog should warn the user that individual month data will be lost. If confirmed, the system should clear the month-by-month data and revert to single percentage mode. If canceled, the checkbox remains checked and data is preserved.

- **Can users change the allocation period (start/end dates) in edit mode?**
  - No. In EDIT mode, the Start Date and End Date fields are READ-ONLY (disabled). Users can only modify allocation percentages, not the date range. To change the allocation period, users must delete the allocation and create a new one. This simplifies the logic and prevents complex edge cases around date range changes in edit mode.

- **What happens if user changes start/end dates in CREATE mode (shrinking the date range)?**
  - In CREATE mode only (dates are editable), if the user changes dates to REDUCE the range and remove months that have percentages entered, the system detects this and shows a confirmation dialog: "Changing the date range will remove data for [list of months]. Continue?" If confirmed, remove those months from the list. If canceled, revert the date change.

- **How does the system handle editing allocations where months have already passed (historical data)?**
  - Past months (months before the current month) are DISABLED and cannot be edited. They display with a visual indicator (greyed-out or labeled "Past") and have disabled input fields. Only current and future months can be edited. If ALL months in an allocation are in the past, the "Month by month allocation" checkbox is disabled entirely with a tooltip "Cannot edit past allocations".

- **What happens if the backend API call fails during submission?**
  - The form should display an error message (e.g., "Failed to save allocation: [error message]") and preserve the user's input (month-by-month or single percentage) so they can retry without re-entering data.

- **Can managers switch from month-by-month mode to single percentage mode after submitting?**
  - Yes. Managers can edit the allocation, uncheck "Month by month allocation", enter a single percentage, and submit. The system should update all monthly allocation entries to the new uniform percentage.

- **What if an allocation has no end date (open-ended)?**
  - If `endDate` is NULL, month-by-month mode should display months starting from `startDate` for a reasonable default period (e.g., 12 months ahead or until the end of the current year). The manager can then extend the date range by setting an explicit end date.

- **What is the minimum allocation percentage?**
  - Minimum allocation is 1%. The system does not support 0% allocations. For employees ramping down or transitioning off projects, managers should use appropriate end dates rather than 0% allocations.

- **How does month-by-month mode interact with allocation types other than PROJECT (e.g., VACATION, MATERNITY)?**
  - Currently, allocation percentage is only applicable for PROJECT type. For other types (VACATION, MATERNITY, PROSPECT), the "Month by month allocation" checkbox should be hidden or disabled, and no percentage input should be displayed.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST display a checkbox labeled "Month by month allocation" in the create and edit allocation modals, positioned below the allocation type field and visible only when allocation type is PROJECT
- **FR-002**: System MUST default the "Month by month allocation" checkbox to unchecked when the modal opens
- **FR-003**: When the checkbox is unchecked (default), system MUST display a number input stepper for single percentage entry (min=1, max=100, step=1) with "%" suffix indicator
- **FR-004**: When the checkbox is checked, system MUST hide the single percentage dropdown and display a list of all months within the allocation period (from startDate to endDate)
- **FR-005**: For each month in the month-by-month list, system MUST display the month label (e.g., "January 2026") and a number input stepper for percentage entry (min=1, max=100, step=1) with "%" suffix indicator
- **FR-006**: System MUST dynamically update the month list when the user changes the startDate or endDate while month-by-month mode is enabled
- **FR-007**: System MUST preserve previously entered month percentages when the date range changes and months overlap (e.g., if user entered Jan: 100%, Feb: 50%, then changed end date from Feb to Mar, Jan and Feb percentages should be preserved, and Mar should appear as a new entry)
- **FR-008**: When a user unchecks the "Month by month allocation" checkbox after entering month-by-month data, system MUST display a confirmation dialog warning "Switching to single percentage mode will replace all individual month allocations. Continue?" with Cancel and Confirm options
- **FR-009**: If the user confirms the switch from month-by-month to single percentage mode, system MUST clear the month-by-month data and display the single percentage dropdown (defaulting to 100% or the most common value from the month-by-month data)
- **FR-010**: System MUST hold all allocation data (single percentage or month-by-month) in client-side state and make NO backend API calls until the user clicks the Submit/Create button
- **FR-011**: When submitting in single percentage mode, system MUST send the allocation data with a single `currentMonthAllocation` value (existing behavior)
- **FR-012**: When submitting in month-by-month mode, system MUST send the allocation data with an array of monthly allocations (e.g., `monthlyAllocations: [{year: 2026, month: 1, percentage: 100}, {year: 2026, month: 2, percentage: 50}, ...]`)
- **FR-013**: System MUST validate that all month percentages are provided (no empty fields) before allowing submission in month-by-month mode
- **FR-014**: System MUST validate that all month percentages are numeric values between 1 and 100 (inclusive)
- **FR-015**: When editing an existing allocation, system MUST pre-populate the month-by-month list with current monthly allocation percentages from the database if the user checks the month-by-month checkbox
- **FR-016**: System MUST display user-friendly error messages for validation failures (e.g., "Percentage is required for all months", "Percentage must be between 1 and 100")
- **FR-017**: System MUST maintain the existing allocation creation/editing behavior for non-PROJECT allocation types (VACATION, MATERNITY, PROSPECT) without displaying percentage inputs
- **FR-018**: System MUST include a month in the month-by-month list if the allocation period touches that month (normalize startDate and endDate to first day of month for range calculation, matching existing backend logic). Mid-month allocations count as FULL month allocations (utilization pro-rating deferred to future phase)
- **FR-019**: When fetching an allocation for editing (GET /api/allocations/{id}), system MUST include all MonthlyAllocation records within the allocation's date range in the response DTO as `List<MonthlyAllocationDTO> monthlyAllocations`
- **FR-020**: Backend validation MUST accept any percentage value from 1 to 100 (inclusive), relaxing current restriction to {25, 50, 75, 100} to support month-by-month flexibility
- **FR-021**: When a user changes the date range in month-by-month mode to add new months, system MUST display those months with empty percentage fields and require user to fill them before submission (no default values)
- **FR-022**: Confirmation dialog when unchecking "Month by month allocation" checkbox MUST default to Cancel action and close on backdrop click or Escape key press
- **FR-023**: System MUST NOT implement cross-allocation validation warnings (e.g., warning when employee total allocation exceeds 100% across multiple allocations) - this is out of scope for this feature
- **FR-024**: In EDIT mode, system MUST display Start Date and End Date fields as READ-ONLY (disabled) - users cannot change the allocation period when editing, only modify percentages
- **FR-025**: In month-by-month mode, system MUST disable past months (months before current month) from being edited. Past month inputs are disabled/greyed-out with visual indicator (e.g., "Past" label). Only current and future months can be edited.
- **FR-026**: If all months in an allocation period are in the past, system MUST disable the "Month by month allocation" checkbox entirely and display tooltip "Cannot edit past allocations"
- **FR-027**: When submitting in EDIT mode with month-by-month data, frontend MUST track which months have changed percentages (compared to original values loaded from database) and send ONLY the changed months in the `monthlyAllocations` array. Backend updates only the provided months.
- **FR-028**: Frontend MUST use Map<"YYYY-MM", number> data structure to store month percentages during date range changes, enabling efficient preservation of values when months overlap during date edits (CREATE mode only)

### Key Entities *(include if feature involves data)*

- **Allocation**: Existing entity with `startDate`, `endDate`, `allocationType` - the month-by-month feature builds on this
- **MonthlyAllocation**: Existing entity tracking allocation percentages by month (`year`, `month`, `percentage`) - this is where individual month percentages are stored
- **AllocationDTO**: Existing DTO with `currentMonthAllocation` - may need extension to support an array of monthly allocations (e.g., `List<MonthlyAllocationDTO> monthlyAllocations`)
- **Month-by-Month UI State**: Frontend state tracking whether month-by-month mode is enabled and storing the array of month-percentage pairs before submission

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Managers can create a new allocation with a single percentage (existing behavior) and verify all monthly allocation entries in the database have the same percentage value within 2 seconds of submission
- **SC-002**: Managers can enable month-by-month mode, enter different percentages for each month, submit the allocation, and verify the database contains exactly the percentages entered for each month (100% accuracy)
- **SC-003**: Managers can edit an existing allocation, switch to month-by-month mode, modify individual month percentages, submit, and verify the updated percentages are persisted in the database
- **SC-004**: The UI dynamically updates the month list within 100ms when start or end dates change while month-by-month mode is enabled, preserving previously entered percentages for overlapping months
- **SC-005**: Form validation prevents submission of month-by-month allocations with missing or invalid percentage values, displaying clear error messages within 200ms of submission attempt
- **SC-006**: All allocation data entry (single or month-by-month) is held client-side with no backend API calls made until the user clicks Submit, verified by network traffic monitoring
- **SC-007**: The feature supports allocation periods ranging from 1 month to 24+ months without UI performance degradation (tested with 24-month allocations, form remains responsive)
- **SC-008**: Managers can switch between single percentage mode and month-by-month mode at least 3 times before submission without data loss (after confirmation dialogs), ensuring flexibility in data entry approach
- **SC-009**: The "Month by month allocation" checkbox is only visible for PROJECT allocation type and hidden for VACATION, MATERNITY, and PROSPECT types, ensuring UI consistency with business rules

---

## Technical Considerations (for planning phase)

### Frontend Changes (Angular)

**Allocation Modal Component** (`allocations.component.ts`):
- Add boolean signal: `monthByMonthMode = signal<boolean>(false)`
- Add Map signal: `monthlyPercentages = signal<Map<string, number>>(new Map())` (key format: "YYYY-MM")
- Add boolean signal: `isEditMode = signal<boolean>(false)` (tracks create vs edit mode)
- Add Map for tracking original values: `originalMonthlyPercentages = new Map<string, number>()` (for smart update detection)
- Implement `onMonthByMonthToggle()` method to handle checkbox changes with confirmation dialog
- Implement `generateMonthList()` method to calculate months from startDate to endDate, filtering out past months in EDIT mode
- Implement `onDateChange()` method to regenerate month list when dates change (CREATE mode only - dates are read-only in EDIT)
- Implement `isPastMonth(year: number, month: number): boolean` to determine if a month is before current month
- Implement `getChangedMonths()` method to compare current values with original and return only changed months (for smart update)
- Update `createAllocation()` to send all months in month-by-month mode
- Update edit submission to send only changed months (smart update)
- Add validation logic for month-by-month entries (1-100 range, no empty fields)
- Disable "Month by month allocation" checkbox if all months are in the past

**UI Template**:
- Add checkbox control in the modal form (after allocation type dropdown), disabled if all months are past
- Start Date and End Date inputs: DISABLED (read-only) in EDIT mode, ENABLED in CREATE mode
- Conditional rendering: show single percentage number stepper OR month-by-month list based on checkbox state
- Single percentage: `<input type="number" min="1" max="100" step="1">` with "%" suffix
- Month-by-month list: For each month, display:
  - Month label (e.g., "January 2026")
  - Number stepper: `<input type="number" min="1" max="100" step="1">` with "%" suffix
  - If month is past: input is DISABLED with greyed-out styling and "Past" indicator
- Add validation error display for month-by-month fields (empty, out of range 1-100)

### Backend Changes (Spring Boot)

**AllocationDTO** (`AllocationDTO.java`):
- Add optional field: `List<MonthlyAllocationDTO> monthlyAllocations` (null when single percentage mode)
- Keep existing `currentMonthAllocation` field for backward compatibility

**AllocationService** (`AllocationService.java`):
- Update `createAllocation()` method to check for `monthlyAllocations` in DTO
- If `monthlyAllocations` is present, iterate and create `MonthlyAllocation` entities for each month
- If `monthlyAllocations` is null, use existing logic (apply `currentMonthAllocation` to all months)
- Update `updateAllocation()` method to handle month-by-month updates (smart update)
  - Since dates are read-only in edit mode, date range doesn't change
  - For months provided in `monthlyAllocations` array: update existing records or create if missing
  - Do NOT delete records - only update what's provided (frontend sends only changed months)
- Add validation: ensure all provided percentages are between 1 and 100
- Optionally validate that all months in allocation period are covered (can be lenient since frontend ensures this)
- Update `toDTO()` method to include `List<MonthlyAllocationDTO> monthlyAllocations` by fetching all monthly allocation records for the allocation (needed for edit mode pre-population)
- Update `validateAllocationPercentage()` to accept 1-100 range (currently restricted to {25, 50, 75, 100})

**MonthlyAllocation** entity:
- No changes needed (already supports individual month percentages)

### API Contract

**GET /api/allocations/{id} - Fetch Allocation for Editing**:
```json
Response:
{
  "id": 123,
  "employeeId": 456,
  "employeeName": "John Doe",
  "employeeOracleId": "EMP001",
  "projectId": 789,
  "projectName": "Atlas Project",
  "allocationType": "PROJECT",
  "startDate": "2026-03-01",
  "endDate": "2026-05-31",
  "currentMonthAllocation": 75,
  "allocationPercentage": 75.0,
  "monthlyAllocations": [
    {"id": 1, "allocationId": 123, "year": 2026, "month": 3, "percentage": 100},
    {"id": 2, "allocationId": 123, "year": 2026, "month": 4, "percentage": 50},
    {"id": 3, "allocationId": 123, "year": 2026, "month": 5, "percentage": 75}
  ]
}
```

**POST /api/allocations - Create Allocation**

**Request Payload (Single Percentage - Existing)**:
```json
{
  "employeeId": 123,
  "projectId": 456,
  "allocationType": "PROJECT",
  "startDate": "2026-03-01",
  "endDate": "2026-05-31",
  "currentMonthAllocation": 75
}
```

**Request Payload (Month-by-Month - New)**:
```json
{
  "employeeId": 123,
  "projectId": 456,
  "allocationType": "PROJECT",
  "startDate": "2026-03-01",
  "endDate": "2026-05-31",
  "monthlyAllocations": [
    {"year": 2026, "month": 3, "percentage": 100},
    {"year": 2026, "month": 4, "percentage": 50},
    {"year": 2026, "month": 5, "percentage": 75}
  ]
}
```

**PUT /api/allocations/{id} - Update Allocation**

Same request payload structure as POST (both single percentage and month-by-month modes supported).

**Smart Update Behavior**:
- Allocation start/end dates are immutable in edit mode (frontend disables date fields)
- In month-by-month mode, frontend sends ONLY the months that have changed percentages
- Backend updates existing MonthlyAllocation records for provided months or creates them if missing
- Backend does NOT delete any MonthlyAllocation records (no date range changes in edit mode)

Example: If allocation has Jan-Mar 2026, and user only changes February from 50% to 75%, frontend sends:
```json
{
  "id": 123,
  "monthlyAllocations": [
    {"year": 2026, "month": 2, "percentage": 75}
  ]
}
```
Backend updates only February's record, leaves January and March unchanged.

### Constitution Compliance Checklist

- [x] Feature requires database-level validation (no in-memory filtering for month-by-month validation)
- [x] No ABAC considerations (allocation creation/editing already enforces manager access)
- [x] Client-side state management (all data held until submission, no premature backend calls)
- [x] Backward compatibility maintained (existing single percentage mode continues to work)
- [x] Clear separation of concerns (frontend handles UI state, backend handles persistence)
- [x] Validation at both frontend (immediate feedback) and backend (data integrity)

### Testing Requirements (NON-NEGOTIABLE per Constitution)

**Backend Unit Tests** (Mandatory):
- `AllocationServiceTest.createAllocation_monthByMonthMode_createsIndividualMonthlyAllocations()`
- `AllocationServiceTest.createAllocation_singlePercentageMode_appliesUniformPercentage()`
- `AllocationServiceTest.updateAllocation_switchToMonthByMonth_updatesMonthlyAllocations()`
- `AllocationServiceTest.updateAllocation_switchToSinglePercentage_unifiesMonthlyAllocations()`
- `AllocationServiceTest.createAllocation_monthByMonthWithMissingMonths_throwsValidationException()`
- `AllocationServiceTest.createAllocation_monthByMonthWithInvalidPercentage_throwsValidationException()`
- `AllocationServiceTest.updateAllocation_extendDateRange_addsNewMonthlyAllocations()`
- `AllocationServiceTest.updateAllocation_reduceDateRange_removesOutOfRangeMonthlyAllocations()`

**Frontend Testing** (Manual):
- Test checkbox toggle behavior
- Test month list generation for various date ranges (1 month, 6 months, 12+ months)
- Test date change with month list updates and preservation of existing data
- Test confirmation dialog when switching from month-by-month to single percentage
- Test form validation for empty and invalid percentages
- Test submission for both single percentage and month-by-month modes
- Test edit mode with pre-populated month-by-month data

### Migration Considerations

- No database migration needed (MonthlyAllocation table already exists)
- Existing allocations with uniform percentages will continue to work
- Managers can retroactively edit allocations to use month-by-month mode if needed
- No breaking changes to existing API endpoints (new field is optional)
