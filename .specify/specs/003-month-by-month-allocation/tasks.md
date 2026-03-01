# Implementation Tasks: Month-by-Month Allocation Feature

**Feature**: 003-month-by-month-allocation
**Branch**: `feature/003-month-by-month-allocation`
**Date**: 2026-02-25
**Status**: Ready for Implementation

---

## Task Breakdown

### Phase 1: Backend Core Implementation

#### Task 1.1: Update AllocationDTO ⏱️ 30 min

**File**: `backend/src/main/java/com/atlas/dto/AllocationDTO.java`

**Changes**:
- [ ] Add `List<MonthlyAllocationDTO> monthlyAllocations` field (null in single percentage mode)
- [ ] Keep existing `Integer currentMonthAllocation` field for backward compatibility
- [ ] Ensure field is marked as optional in API contract
- [ ] Add JavaDoc comments explaining the two modes

**Acceptance Criteria**:
- DTO compiles without errors
- Both fields present for backward compatibility
- monthlyAllocations is null when not in month-by-month mode
- currentMonthAllocation still works for single percentage mode

---

#### Task 1.2: Update Validation Method ⏱️ 15 min

**File**: `backend/src/main/java/com/atlas/service/AllocationService.java`

**Changes**:
- [ ] Update `validateAllocationPercentage()` method
- [ ] Change validation range from {25, 50, 75, 100} to 1-100
- [ ] Update error message: "Invalid allocation percentage. Must be between 1 and 100."
- [ ] Remove hardcoded list of allowed percentages

**Acceptance Criteria**:
- Method accepts any integer from 1 to 100
- Validation throws exception for percentages < 1 or > 100
- Error message accurately reflects new range

---

#### Task 1.3: Update AllocationService.toDTO() ⏱️ 45 min

**File**: `backend/src/main/java/com/atlas/service/AllocationService.java`

**Changes**:
- [ ] Update `toDTO(Allocation allocation)` method
- [ ] Fetch ALL monthly allocations using `allocation.getMonthlyAllocations()`
- [ ] Map MonthlyAllocation entities to MonthlyAllocationDTO list
- [ ] Populate `monthlyAllocations` field in AllocationDTO
- [ ] Keep existing `currentMonthAllocation` calculation (using projection query)

**Code Example**:
```java
List<MonthlyAllocationDTO> monthlyAllocations = allocation.getMonthlyAllocations()
    .stream()
    .map(ma -> MonthlyAllocationDTO.builder()
        .id(ma.getId())
        .allocationId(allocation.getId())
        .year(ma.getYear())
        .month(ma.getMonth())
        .percentage(ma.getPercentage())
        .build())
    .collect(Collectors.toList());
```

**Acceptance Criteria**:
- toDTO() returns full monthlyAllocations array
- Each MonthlyAllocationDTO has correct year, month, percentage
- currentMonthAllocation still works for allocations page display
- Edit modal receives complete monthlyAllocations array

---

#### Task 1.4: Update AllocationService.createAllocation() ⏱️ 1 hour

**File**: `backend/src/main/java/com/atlas/service/AllocationService.java`

**Changes**:
- [ ] Add conditional logic: check if `dto.getMonthlyAllocations() != null`
- [ ] **Month-by-Month Mode**:
  - Validate each month's percentage (1-100 range)
  - Create individual MonthlyAllocation records using `allocation.setAllocationForYearMonth()`
  - Throw exception if any percentage is < 1 or > 100
- [ ] **Single Percentage Mode** (existing):
  - Keep current logic using `currentMonthAllocation`
  - Apply uniform percentage to all months in allocation period
- [ ] Save allocation entity with monthly records

**Code Example**:
```java
if (dto.getMonthlyAllocations() != null && !dto.getMonthlyAllocations().isEmpty()) {
    // Month-by-month mode
    for (MonthlyAllocationDTO monthDto : dto.getMonthlyAllocations()) {
        if (monthDto.getPercentage() < 1 || monthDto.getPercentage() > 100) {
            throw new RuntimeException("Invalid percentage for " + monthDto.getMonth()
                + "/" + monthDto.getYear() + ". Must be between 1 and 100.");
        }
        allocation.setAllocationForYearMonth(
            monthDto.getYear(),
            monthDto.getMonth(),
            monthDto.getPercentage()
        );
    }
} else {
    // Single percentage mode (existing behavior)
    // ... keep current implementation
}
```

**Acceptance Criteria**:
- Single percentage mode still works (backward compatibility)
- Month-by-month mode creates individual monthly records
- Validation throws exception for invalid percentages
- All months in monthlyAllocations array are persisted to database

---

#### Task 1.5: Update AllocationService.updateAllocation() ⏱️ 1 hour 15 min

**File**: `backend/src/main/java/com/atlas/service/AllocationService.java`

**Changes**:
- [ ] Add smart update logic for month-by-month mode
- [ ] Only update MonthlyAllocation records provided in DTO (don't delete anything)
- [ ] Use `allocation.setAllocationForYearMonth()` to update existing or create new
- [ ] Validate each month's percentage (1-100 range)
- [ ] Handle switch from single percentage to month-by-month mode
- [ ] Handle switch from month-by-month to single percentage mode

**Code Example**:
```java
if (dto.getMonthlyAllocations() != null && !dto.getMonthlyAllocations().isEmpty()) {
    // Month-by-month mode - smart update
    for (MonthlyAllocationDTO monthDto : dto.getMonthlyAllocations()) {
        if (monthDto.getPercentage() < 1 || monthDto.getPercentage() > 100) {
            throw new RuntimeException("Invalid percentage. Must be between 1 and 100.");
        }
        allocation.setAllocationForYearMonth(
            monthDto.getYear(),
            monthDto.getMonth(),
            monthDto.getPercentage()
        );
    }
} else if (dto.getCurrentMonthAllocation() != null) {
    // Switched to single percentage mode - update all months uniformly
    // ... existing implementation
}
```

**Acceptance Criteria**:
- Smart update: Only provided months are updated in database
- Frontend sends only changed months, backend updates only those
- Mode switching works correctly (single ↔ month-by-month)
- Validation throws exception for invalid percentages

---

### Phase 2: Backend Testing

#### Task 2.1: Create AllocationService Unit Tests ⏱️ 3 hours

**File**: `backend/src/test/java/com/atlas/service/AllocationServiceTest.java`

**Tests to Create** (8 Mandatory Tests):

**CREATE Mode Tests**:

- [ ] `createAllocation_monthByMonthMode_createsIndividualMonthlyAllocations()`
  - Given: AllocationDTO with monthlyAllocations array (3 months with different %)
  - When: createAllocation() is called
  - Then: 3 MonthlyAllocation records created with correct year/month/percentage

- [ ] `createAllocation_singlePercentageMode_appliesUniformPercentage()`
  - Given: AllocationDTO with currentMonthAllocation = 75%
  - When: createAllocation() is called
  - Then: All months in allocation period have 75% (existing behavior)

**UPDATE Mode Tests**:

- [ ] `updateAllocation_switchToMonthByMonth_updatesMonthlyAllocations()`
  - Given: Existing allocation in single percentage mode
  - When: updateAllocation() called with monthlyAllocations array
  - Then: Individual monthly percentages applied, overriding uniform percentage

- [ ] `updateAllocation_switchToSinglePercentage_unifiesMonthlyAllocations()`
  - Given: Existing allocation in month-by-month mode
  - When: updateAllocation() called with currentMonthAllocation (no monthlyAllocations)
  - Then: All months updated to uniform percentage

- [ ] `updateAllocation_smartUpdate_updatesOnlyChangedMonths()`
  - Given: Existing allocation with 3 months (25%, 50%, 75%)
  - When: updateAllocation() called with only month 2 changed to 60%
  - Then: Month 1 = 25%, Month 2 = 60%, Month 3 = 75%

**VALIDATION Tests**:

- [ ] `createAllocation_monthByMonthWithInvalidPercentage_throwsValidationException()`
  - Given: AllocationDTO with monthlyAllocations containing percentage = 101
  - When: createAllocation() is called
  - Then: RuntimeException thrown with message "Must be between 1 and 100"

- [ ] `createAllocation_monthByMonthWithZeroPercent_throwsValidationException()`
  - Given: AllocationDTO with monthlyAllocations containing percentage = 0
  - When: createAllocation() is called
  - Then: RuntimeException thrown (minimum is 1%)

- [ ] `updateAllocation_smartUpdate_handlesPartialMonthUpdates()`
  - Given: Allocation with 6 months, frontend sends only 2 changed months
  - When: updateAllocation() is called
  - Then: Only 2 months updated, other 4 remain unchanged

**Acceptance Criteria**:
- All 8 tests pass (`./mvnw test`)
- Tests use Mockito for repository mocking
- Tests verify both entity state and saved data
- Edge cases covered (0%, >100%, mode switching)

---

#### Task 2.2: Run Full Backend Test Suite ⏱️ 30 min

**Commands**:
```bash
cd backend
JAVA_HOME=/Users/mustafaabdelhalim/Library/Java/JavaVirtualMachines/corretto-17.0.16/Contents/Home ./mvnw clean test
```

**Checklist**:
- [ ] All 8 new tests pass
- [ ] All existing tests still pass (no regressions)
- [ ] Test coverage > 80% for modified code
- [ ] No console errors or warnings

**Acceptance Criteria**:
- Build succeeds with `BUILD SUCCESS`
- Total test count increases by 8
- No test failures or errors

---

### Phase 3: Frontend Foundation

#### Task 3.1: Add Component State Management ⏱️ 1 hour

**File**: `frontend/src/app/pages/allocations/allocations.component.ts`

**Changes**:

**Add Signals**:
- [ ] `monthByMonthMode = signal<boolean>(false)` - Checkbox state
- [ ] `monthlyPercentages = signal<Map<string, number>>(new Map())` - Month percentages
- [ ] `isEditMode = signal<boolean>(false)` - CREATE vs EDIT mode
- [ ] `monthList = signal<Array<{year: number, month: number, label: string, key: string}>>([])`

**Add Properties**:
- [ ] `originalMonthlyPercentages = new Map<string, number>()` - For smart update detection

**Add Methods** (signatures only, implementation in Task 3.3):
- [ ] `generateMonthList(): void` - Calculate months from date range
- [ ] `onMonthByMonthToggle(event: Event): void` - Handle checkbox change
- [ ] `isPastMonth(year: number, month: number): boolean` - Check if month is historical
- [ ] `getChangedMonths(): MonthlyAllocationDTO[]` - Smart update helper
- [ ] `validateMonthByMonth(): boolean` - Check all percentages 1-100

**Acceptance Criteria**:
- Component compiles without TypeScript errors
- Signals declared with correct types
- Methods defined (stubbed if not implemented yet)
- No breaking changes to existing functionality

---

#### Task 3.2: Update Component Template ⏱️ 2 hours

**File**: `frontend/src/app/pages/allocations/allocations.component.ts` (template section)

**Changes**:

**1. Update Single Percentage Input**:
- [ ] Replace dropdown with number stepper:
  ```html
  <input type="number"
         min="1"
         max="100"
         step="1"
         [(ngModel)]="percentage"
         class="form-control" />
  <span class="percentage-symbol">%</span>
  ```
- [ ] Remove old dropdown `<select>` element

**2. Add Month-by-Month Checkbox** (after allocation type):
- [ ] Add checkbox with label:
  ```html
  <div class="form-group">
    <label>
      <input type="checkbox"
             [(ngModel)]="monthByMonthMode"
             (change)="onMonthByMonthToggle($event)"
             [disabled]="isEditMode() && allMonthsArePast()" />
      Month by month allocation
    </label>
    @if (isEditMode() && allMonthsArePast()) {
      <span class="tooltip-text">Cannot edit past allocations</span>
    }
  </div>
  ```

**3. Add Month-by-Month Inputs Section**:
- [ ] Add conditional rendering using `@if (monthByMonthMode())`
- [ ] Generate input for each month in `monthList()`
- [ ] Use number stepper for each month:
  ```html
  @if (monthByMonthMode()) {
    <div class="month-by-month-section">
      @for (month of monthList(); track month.key) {
        <div class="month-row">
          <label>{{ month.label }}</label>
          <input type="number"
                 min="1"
                 max="100"
                 step="1"
                 [(ngModel)]="monthlyPercentages().get(month.key)"
                 [disabled]="isPastMonth(month.year, month.month)"
                 class="form-control" />
          <span class="percentage-symbol">%</span>
          @if (isPastMonth(month.year, month.month)) {
            <span class="past-indicator">Past</span>
          }
        </div>
      }
    </div>
  }
  ```

**4. Update Start/End Date Inputs**:
- [ ] Add `[disabled]="isEditMode()"` to both date inputs
- [ ] Add CSS class for disabled styling

**Acceptance Criteria**:
- Single percentage uses number stepper (not dropdown)
- Checkbox renders correctly with label
- Month list displays when checkbox checked
- Each month has number stepper with % symbol
- Past months show greyed-out with "Past" label
- Date inputs disabled in EDIT mode
- UI matches existing modal styling

---

### Phase 4: Frontend Logic Implementation

#### Task 4.1: Implement Month List Generation ⏱️ 45 min

**File**: `frontend/src/app/pages/allocations/allocations.component.ts`

**Method**: `generateMonthList()`

**Logic**:
- [ ] Parse `startDate` and `endDate` signals
- [ ] Normalize to first day of each month
- [ ] Generate array of all months in range
- [ ] Create objects with `{ year, month, label, key }` format
- [ ] In EDIT mode: filter out past months (months before current month)
- [ ] In CREATE mode: include all months in range
- [ ] Update `monthList` signal

**Code Example**:
```typescript
generateMonthList(): void {
  const start = new Date(this.startDate());
  const end = new Date(this.endDate());
  const current = new Date();
  const months: Array<{year: number, month: number, label: string, key: string}> = [];

  let yearMonth = new Date(start.getFullYear(), start.getMonth(), 1);
  const endYearMonth = new Date(end.getFullYear(), end.getMonth(), 1);

  while (yearMonth <= endYearMonth) {
    const year = yearMonth.getFullYear();
    const month = yearMonth.getMonth() + 1;

    // In EDIT mode, skip past months
    if (this.isEditMode()) {
      const currentYearMonth = new Date(current.getFullYear(), current.getMonth(), 1);
      if (yearMonth < currentYearMonth) {
        yearMonth.setMonth(yearMonth.getMonth() + 1);
        continue;
      }
    }

    months.push({
      year,
      month,
      label: `${yearMonth.toLocaleString('default', { month: 'long' })} ${year}`,
      key: `${year}-${String(month).padStart(2, '0')}`
    });

    yearMonth.setMonth(yearMonth.getMonth() + 1);
  }

  this.monthList.set(months);
}
```

**Acceptance Criteria**:
- Method generates correct months from start to end date
- CREATE mode: All months in range included
- EDIT mode: Past months excluded from list
- Keys in "YYYY-MM" format for Map lookup
- Labels human-readable (e.g., "February 2026")

---

#### Task 4.2: Implement Checkbox Toggle Logic ⏱️ 30 min

**File**: `frontend/src/app/pages/allocations/allocations.component.ts`

**Method**: `onMonthByMonthToggle(event: Event)`

**Logic**:
- [ ] If toggling ON (unchecked → checked):
  - Call `generateMonthList()`
  - Pre-populate monthlyPercentages Map with current single percentage if available
- [ ] If toggling OFF (checked → unchecked):
  - Show confirmation dialog if user has entered custom percentages
  - If confirmed: clear `monthlyPercentages` Map, set `monthByMonthMode = false`
  - If cancelled: revert checkbox state

**Code Example**:
```typescript
onMonthByMonthToggle(event: Event): void {
  const checked = (event.target as HTMLInputElement).checked;

  if (checked) {
    // Switching to month-by-month mode
    this.generateMonthList();

    // Pre-populate with current single percentage if available
    if (this.percentage()) {
      this.monthList().forEach(month => {
        this.monthlyPercentages().set(month.key, this.percentage()!);
      });
    }
    this.monthByMonthMode.set(true);
  } else {
    // Switching to single percentage mode
    if (this.monthlyPercentages().size > 0) {
      const confirmed = confirm('Switching to single percentage mode will discard individual month values. Continue?');
      if (confirmed) {
        this.monthlyPercentages().clear();
        this.monthByMonthMode.set(false);
      } else {
        // Revert checkbox
        (event.target as HTMLInputElement).checked = true;
      }
    } else {
      this.monthByMonthMode.set(false);
    }
  }
}
```

**Acceptance Criteria**:
- Toggling ON generates month list and pre-populates percentages
- Toggling OFF shows confirmation if data exists
- Cancel confirmation reverts checkbox state
- Confirm confirmation clears monthlyPercentages Map

---

#### Task 4.3: Implement Date Change Handler (CREATE Mode) ⏱️ 20 min

**File**: `frontend/src/app/pages/allocations/allocations.component.ts`

**Method**: `onDateChange()`

**Logic**:
- [ ] Only execute if `monthByMonthMode()` is true AND `!isEditMode()`
- [ ] Preserve existing monthlyPercentages for overlapping months
- [ ] Regenerate monthList based on new date range
- [ ] Show warning if date range shrinks and removes months with entered data

**Code Example**:
```typescript
onDateChange(): void {
  if (!this.monthByMonthMode() || this.isEditMode()) {
    return; // Dates can't change in EDIT mode
  }

  // Regenerate month list with new date range
  const oldKeys = new Set(this.monthlyPercentages().keys());
  this.generateMonthList();
  const newKeys = new Set(this.monthList().map(m => m.key));

  // Check if any months with data are being removed
  const removedKeys = [...oldKeys].filter(key => !newKeys.has(key));
  const hasDataLoss = removedKeys.some(key => this.monthlyPercentages().get(key) !== undefined);

  if (hasDataLoss) {
    const confirmed = confirm('Changing the date range will remove some months with entered percentages. Continue?');
    if (!confirmed) {
      // Revert dates (implementation depends on how dates are stored)
      return;
    }
  }

  // Clean up removed months from Map
  removedKeys.forEach(key => this.monthlyPercentages().delete(key));
}
```

**Acceptance Criteria**:
- Only works in CREATE mode (dates disabled in EDIT)
- Preserves percentages for overlapping months
- Shows confirmation if data loss occurs
- Cancelling confirmation reverts date change

---

#### Task 4.4: Implement Helper Methods ⏱️ 30 min

**File**: `frontend/src/app/pages/allocations/allocations.component.ts`

**Methods**:

**1. isPastMonth()**:
```typescript
isPastMonth(year: number, month: number): boolean {
  if (!this.isEditMode()) return false;

  const current = new Date();
  const currentYearMonth = new Date(current.getFullYear(), current.getMonth(), 1);
  const checkYearMonth = new Date(year, month - 1, 1);

  return checkYearMonth < currentYearMonth;
}
```

**2. allMonthsArePast()**:
```typescript
allMonthsArePast(): boolean {
  if (!this.isEditMode()) return false;
  return this.monthList().every(month => this.isPastMonth(month.year, month.month));
}
```

**3. validateMonthByMonth()**:
```typescript
validateMonthByMonth(): boolean {
  if (!this.monthByMonthMode()) return true;

  for (const month of this.monthList()) {
    const percentage = this.monthlyPercentages().get(month.key);
    if (!percentage || percentage < 1 || percentage > 100) {
      alert(`Invalid percentage for ${month.label}. Must be between 1 and 100.`);
      return false;
    }
  }

  return true;
}
```

**4. getChangedMonths() (for EDIT mode smart update)**:
```typescript
getChangedMonths(): MonthlyAllocationDTO[] {
  if (!this.isEditMode()) {
    // CREATE mode: return all months
    return this.monthList().map(month => ({
      year: month.year,
      month: month.month,
      percentage: this.monthlyPercentages().get(month.key)!
    }));
  }

  // EDIT mode: return only changed months
  const changed: MonthlyAllocationDTO[] = [];
  this.monthList().forEach(month => {
    const currentValue = this.monthlyPercentages().get(month.key);
    const originalValue = this.originalMonthlyPercentages.get(month.key);

    if (currentValue !== originalValue) {
      changed.push({
        year: month.year,
        month: month.month,
        percentage: currentValue!
      });
    }
  });

  return changed;
}
```

**Acceptance Criteria**:
- `isPastMonth()` correctly identifies months before current month
- `allMonthsArePast()` returns true only when all months are historical
- `validateMonthByMonth()` checks 1-100 range for all months
- `getChangedMonths()` returns all months in CREATE, only changed in EDIT

---

#### Task 4.5: Update Create/Edit Submission Logic ⏱️ 1 hour 30 min

**File**: `frontend/src/app/pages/allocations/allocations.component.ts`

**Changes**:

**1. Update createAllocation() method**:
- [ ] Add validation call: `if (!this.validateMonthByMonth()) return;`
- [ ] Build AllocationDTO with conditional logic:
  ```typescript
  const dto: AllocationDTO = {
    employeeId: this.selectedEmployee()?.id,
    projectId: this.selectedProject()?.id,
    startDate: this.startDate(),
    endDate: this.endDate(),
    allocationType: this.allocationType(),
    // Single percentage mode
    currentMonthAllocation: this.monthByMonthMode() ? null : this.percentage(),
    // Month-by-month mode
    monthlyAllocations: this.monthByMonthMode() ? this.monthList().map(month => ({
      year: month.year,
      month: month.month,
      percentage: this.monthlyPercentages().get(month.key)!
    })) : null
  };
  ```
- [ ] Send to backend via API service
- [ ] Handle success/error responses

**2. Update editAllocation() method**:
- [ ] Add validation call
- [ ] Build DTO with smart update:
  ```typescript
  const dto: AllocationDTO = {
    id: this.currentAllocationId(),
    // ... other fields ...
    currentMonthAllocation: this.monthByMonthMode() ? null : this.percentage(),
    monthlyAllocations: this.monthByMonthMode() ? this.getChangedMonths() : null
  };
  ```
- [ ] Send to backend (only changed months sent in EDIT mode)

**3. Update modal open logic for EDIT mode**:
- [ ] Set `isEditMode.set(true)`
- [ ] Load monthlyAllocations from API response
- [ ] Populate `monthlyPercentages` Map
- [ ] Store original values in `originalMonthlyPercentages`
- [ ] If monthlyAllocations exists: set `monthByMonthMode.set(true)`, call `generateMonthList()`
- [ ] Disable start/end date inputs

**Acceptance Criteria**:
- CREATE mode sends all months or single percentage
- EDIT mode sends only changed months
- Validation prevents submission if any percentage invalid
- Modal correctly populates for edit (loads monthlyAllocations from backend)
- Original values stored for change detection

---

### Phase 5: Integration & Testing

#### Task 5.1: Backend Integration Testing ⏱️ 1 hour

**Environment Setup**:
```bash
cd backend
JAVA_HOME=/Users/mustafaabdelhalim/Library/Java/JavaVirtualMachines/corretto-17.0.16/Contents/Home ./mvnw spring-boot:run
```

**Test Scenarios** (using Postman or curl):

**Scenario 1: Create Allocation (Single Percentage)**:
- [ ] POST `/api/allocations`
- [ ] Body: `{ "currentMonthAllocation": 50, "monthlyAllocations": null, ... }`
- [ ] Verify: All months in allocation period have 50%

**Scenario 2: Create Allocation (Month-by-Month)**:
- [ ] POST `/api/allocations`
- [ ] Body: `{ "currentMonthAllocation": null, "monthlyAllocations": [...], ... }`
- [ ] Verify: Individual monthly percentages saved correctly

**Scenario 3: Update Allocation (Smart Update)**:
- [ ] PUT `/api/allocations/{id}`
- [ ] Body: Only 2 months in monthlyAllocations array
- [ ] Verify: Only those 2 months updated, others unchanged

**Scenario 4: Validation Error**:
- [ ] POST `/api/allocations` with percentage = 0
- [ ] Verify: 400 error with message "Must be between 1 and 100"

**Acceptance Criteria**:
- All 4 scenarios pass without server errors
- Database records match expected values
- Error responses include clear messages

---

#### Task 5.2: Frontend Integration Testing ⏱️ 1 hour

**Environment Setup**:
```bash
cd frontend
npm start
```

**Test Scenarios**:

**Scenario 1: Single Percentage Mode (CREATE)**:
- [ ] Open allocation modal
- [ ] Enter employee, project, dates, allocation type
- [ ] Enter percentage using number stepper (e.g., 75%)
- [ ] Submit
- [ ] Verify: Allocation created with uniform 75% for all months

**Scenario 2: Month-by-Month Mode (CREATE)**:
- [ ] Open allocation modal
- [ ] Check "Month by month allocation" checkbox
- [ ] Verify: Month list appears
- [ ] Enter different percentage for each month (e.g., 25%, 50%, 75%)
- [ ] Submit
- [ ] Verify: Allocation created with individual monthly percentages

**Scenario 3: Date Change in Month-by-Month (CREATE)**:
- [ ] Enable month-by-month mode
- [ ] Enter percentages for 3 months (Jan, Feb, Mar)
- [ ] Change end date to remove March
- [ ] Verify: Confirmation dialog appears
- [ ] Confirm
- [ ] Verify: Only Jan and Feb remain

**Scenario 4: Edit Allocation (Month-by-Month)**:
- [ ] Click edit icon on existing allocation
- [ ] Verify: Dates are disabled (READ-ONLY)
- [ ] Check month-by-month checkbox
- [ ] Change February's percentage from 50% to 60%
- [ ] Submit
- [ ] Verify: Only February updated in database (smart update)

**Scenario 5: Past Months Disabled (EDIT)**:
- [ ] Edit an allocation with past months (e.g., January in February)
- [ ] Enable month-by-month mode
- [ ] Verify: January is greyed-out with "Past" label
- [ ] Verify: Cannot edit January's percentage (input disabled)

**Scenario 6: All Months Past (EDIT)**:
- [ ] Edit an allocation entirely in the past
- [ ] Verify: Checkbox disabled
- [ ] Hover over checkbox
- [ ] Verify: Tooltip shows "Cannot edit past allocations"

**Acceptance Criteria**:
- All 6 scenarios work as expected
- No console errors
- UI responds correctly to user actions
- Data persists correctly to backend

---

#### Task 5.3: Manual Acceptance Testing ⏱️ 2 hours

**Test Against User Stories**:

**User Story 1: Single Percentage Allocation (P1)**:
- [ ] Scenario 1: Create allocation with 50% (existing behavior)
- [ ] Scenario 2: Verify all months have 50%
- [ ] **Acceptance**: Single percentage mode works unchanged

**User Story 2: Month-by-Month Allocation Entry (P1)**:
- [ ] Scenario 1: Enable checkbox, see month list
- [ ] Scenario 2: Enter 25%, 50%, 75% for 3 consecutive months
- [ ] Scenario 3: Submit and verify individual percentages saved
- [ ] **Acceptance**: Can enter different percentages for each month

**User Story 3: Edit Existing Allocation (P2)**:
- [ ] Scenario 1: Edit single percentage allocation, switch to month-by-month
- [ ] Scenario 2: Edit month-by-month allocation, change only February
- [ ] Scenario 3: Verify only changed month updated (smart update)
- [ ] Scenario 4: Try to change dates in EDIT mode - verify disabled
- [ ] **Acceptance**: Can edit allocations, dates READ-ONLY, smart update works

**User Story 4: Validation and Feedback (P2)**:
- [ ] Scenario 1: Enter percentage = 0, verify error message
- [ ] Scenario 2: Enter percentage = 101, verify error message
- [ ] Scenario 3: Leave month field empty, verify error on submit
- [ ] **Acceptance**: Validation prevents invalid data, clear error messages

**Edge Cases Testing**:
- [ ] Single month allocation (start = end date)
- [ ] 12+ month allocation (long period)
- [ ] Allocation spanning year boundary (Dec 2025 → Jan 2026)
- [ ] Switch modes with confirmation dialog (Cancel vs Confirm)
- [ ] Percentage input using keyboard (type vs stepper buttons)

**Acceptance Criteria**:
- All user stories pass acceptance scenarios
- All edge cases handled gracefully
- No data loss or corruption
- UI behaves predictably

---

#### Task 5.4: Bug Fixes & Polish ⏱️ 2 hours

**Potential Issues to Address**:
- [ ] Fix any TypeScript compilation warnings
- [ ] Fix any console errors in browser
- [ ] Improve error messages for clarity
- [ ] Add loading spinners during API calls
- [ ] Improve accessibility (ARIA labels, keyboard navigation)
- [ ] Test on different screen sizes (responsive design)
- [ ] Fix any CSS alignment issues
- [ ] Ensure consistent styling with existing modals

**Acceptance Criteria**:
- No TypeScript errors or warnings
- No browser console errors
- UI polished and professional
- Accessibility standards met
- Works on mobile/tablet viewports

---

### Phase 6: Documentation & Cleanup

#### Task 6.1: Code Documentation ⏱️ 30 min

**Backend**:
- [ ] Add JavaDoc comments to modified methods in AllocationService
- [ ] Document validation rules (1-100 range)
- [ ] Add comments explaining smart update logic

**Frontend**:
- [ ] Add JSDoc comments to new methods (generateMonthList, etc.)
- [ ] Document Map<string, number> structure ("YYYY-MM" key format)
- [ ] Add comments explaining edit mode vs create mode behavior

**Acceptance Criteria**:
- All public methods have documentation comments
- Complex logic is explained with inline comments
- Future developers can understand code without asking

---

#### Task 6.2: Constitution Compliance Verification ⏱️ 20 min

**Checklist**:
- [ ] **ABAC-First Security**: Allocation creation/editing enforces manager access (existing mechanism)
- [ ] **Database-First Performance**: MonthlyAllocation updates at DB level (no in-memory filtering)
- [ ] **NULL-Safe Queries**: No complex native queries added
- [ ] **Enum Handling**: No new enums introduced
- [ ] **Backend Unit Tests**: 8 mandatory tests written and passing
- [ ] **Backward Compatibility**: Single percentage mode preserved, optional monthlyAllocations field

**Acceptance Criteria**:
- All constitution principles verified
- No violations introduced
- Backward compatibility maintained

---

#### Task 6.3: Create Pull Request ⏱️ 15 min

**PR Checklist**:
- [ ] All tasks completed and tested
- [ ] All tests passing (backend + frontend)
- [ ] No merge conflicts with main branch
- [ ] Commit message follows convention
- [ ] PR description includes:
  - Feature summary
  - Link to specification
  - Testing evidence
  - Screenshots of UI

**Acceptance Criteria**:
- PR created successfully
- All CI checks pass
- Ready for code review

---

## Summary

### Total Estimated Effort

| Phase | Tasks | Estimated Time |
|-------|-------|----------------|
| Backend Core | 5 tasks | 3h 45min |
| Backend Testing | 2 tasks | 3h 30min |
| Frontend Foundation | 2 tasks | 3h |
| Frontend Logic | 5 tasks | 4h 15min |
| Integration & Testing | 4 tasks | 6h |
| Documentation & Cleanup | 3 tasks | 1h 5min |
| **Total** | **21 tasks** | **~21.75 hours (~3 days)** |

**Note**: Estimates are for focused development time. Include buffer for context switching, debugging, and code review iterations.

---

## Task Dependencies

```
Task 1.1 (DTO)          → Task 1.2 (Validation) → Task 1.3 (toDTO)
                         ↓
Task 1.4 (Create)       → Task 1.5 (Update)
                         ↓
Task 2.1 (Tests)        → Task 2.2 (Run Tests)
                         ↓
Task 3.1 (State)        → Task 3.2 (Template)
                         ↓
Task 4.1 (Month List)   → Task 4.2 (Checkbox)   → Task 4.3 (Date Change)
                         ↓
Task 4.4 (Helpers)      → Task 4.5 (Submit Logic)
                         ↓
Task 5.1 (Backend Test) → Task 5.2 (Frontend Test) → Task 5.3 (Acceptance)
                         ↓
Task 5.4 (Bug Fixes)    → Task 6.1 (Docs)       → Task 6.2 (Compliance) → Task 6.3 (PR)
```

**Parallelization Opportunity**:
- Backend tasks (1.1-2.2) and Frontend tasks (3.1-4.5) can be worked on simultaneously
- Integration testing (5.1-5.3) must wait for both backend and frontend completion

---

## Progress Tracking

**Status Legend**:
- ⬜ Not Started
- 🟦 In Progress
- ✅ Completed
- ❌ Blocked

| Task ID | Description | Status | Assignee | Notes |
|---------|-------------|--------|----------|-------|
| 1.1 | Update AllocationDTO | ⬜ | - | - |
| 1.2 | Update Validation | ⬜ | - | - |
| 1.3 | Update toDTO() | ⬜ | - | - |
| 1.4 | Update createAllocation() | ⬜ | - | - |
| 1.5 | Update updateAllocation() | ⬜ | - | - |
| 2.1 | Create Unit Tests | ⬜ | - | 8 mandatory tests |
| 2.2 | Run Test Suite | ⬜ | - | - |
| 3.1 | Add State Management | ⬜ | - | - |
| 3.2 | Update Template | ⬜ | - | - |
| 4.1 | Implement generateMonthList() | ⬜ | - | - |
| 4.2 | Implement Checkbox Toggle | ⬜ | - | - |
| 4.3 | Implement Date Change | ⬜ | - | CREATE mode only |
| 4.4 | Implement Helpers | ⬜ | - | 4 methods |
| 4.5 | Update Submit Logic | ⬜ | - | - |
| 5.1 | Backend Integration Test | ⬜ | - | - |
| 5.2 | Frontend Integration Test | ⬜ | - | - |
| 5.3 | Manual Acceptance Test | ⬜ | - | All user stories |
| 5.4 | Bug Fixes & Polish | ⬜ | - | - |
| 6.1 | Code Documentation | ⬜ | - | - |
| 6.2 | Constitution Compliance | ⬜ | - | - |
| 6.3 | Create Pull Request | ⬜ | - | - |

---

## Ready to Start?

1. **Create feature branch**: Already created `feature/003-month-by-month-allocation`
2. **Start with backend**: Begin with Task 1.1 (Update AllocationDTO)
3. **Test incrementally**: Run tests after completing Phase 2
4. **Frontend development**: Begin Phase 3 after backend tests pass
5. **Integration testing**: Phase 5 after both backend and frontend complete
6. **Refer to specifications**:
   - [spec.md](./spec.md) for complete requirements (28 FRs)
   - [FINAL-SPEC-SUMMARY.md](./FINAL-SPEC-SUMMARY.md) for quick reference
   - [plan.md](./plan.md) for implementation strategy
   - [constitution.md](../../memory/constitution.md) for compliance principles

**Questions?** All clarifications resolved. Ready for implementation. 🚀

---

## Key Implementation Notes

### Critical Requirements:
1. **Minimum Percentage**: 1% (not 0%, not 25%)
2. **Input Control**: Number stepper `<input type="number" min="1" max="100" step="1">`
3. **EDIT Mode Restrictions**:
   - Start/End dates READ-ONLY (disabled)
   - Past months DISABLED (greyed-out with "Past" indicator)
   - Checkbox disabled if all months are past
4. **Smart Update**: Frontend sends only changed months, backend updates only those
5. **State Management**: Use `Map<"YYYY-MM", number>` for efficient month tracking
6. **Validation**: Frontend (pre-submit) + Backend (1-100 range)
7. **Backward Compatibility**: Single percentage mode must continue working

### Test Coverage:
- 8 mandatory backend unit tests (AllocationServiceTest)
- Manual frontend testing (6 scenarios)
- Acceptance testing (5 user stories)
- Edge case coverage (15 documented edge cases)

### Performance Considerations:
- No in-memory filtering of large datasets
- Database-level updates only (smart update pattern)
- Lazy loading avoided where possible
