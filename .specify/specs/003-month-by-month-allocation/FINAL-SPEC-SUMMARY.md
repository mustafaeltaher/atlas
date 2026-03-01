# Final Specification - Month-by-Month Allocation Feature

**Date**: 2026-02-25
**Status**: ✅ **COMPLETE - Ready for Planning**
**Feature Branch**: `feature/003-month-by-month-allocation`

---

## 📋 **Complete Requirements Summary**

### **Core Feature**
Enable managers to enter allocation percentages either:
1. **Single percentage mode** (default): One percentage applies to all months
2. **Month-by-month mode**: Individual percentages for each month in the allocation period

### **Key Behaviors**

| Aspect | Behavior |
|--------|----------|
| **Input Control** | Number stepper: `<input type="number" min="1" max="100" step="1">%` |
| **Minimum Allocation** | 1% (not 0%) |
| **CREATE Mode** | Dates editable, month list grows/shrinks dynamically as dates change |
| **EDIT Mode** | Dates READ-ONLY (cannot change allocation period), only percentages editable |
| **Historical Months** | Past months DISABLED (cannot edit), greyed-out with "Past" indicator |
| **All Months Past** | Checkbox disabled entirely with tooltip "Cannot edit past allocations" |
| **Validation** | Basic frontend validation (prevent submit if invalid) + Backend validation |
| **Smart Update** | Frontend tracks changes, sends only modified months to backend (EDIT mode) |
| **State Management** | Map<"YYYY-MM", number> for efficient month percentage tracking |

---

## ✅ **All Clarifications Resolved**

### **1. Input Control**
✅ Number input stepper with min=1, max=100, step=1, "%" suffix
✅ Applies to BOTH single percentage mode AND month-by-month mode
✅ Replaces previous dropdown (25/50/75/100)

### **2. Date Range Editing**
✅ CREATE mode: Dates editable, month list updates dynamically
✅ EDIT mode: Start/End dates READ-ONLY (disabled)
✅ To change allocation period: delete and recreate allocation

### **3. Historical Months**
✅ Past months (before current month) are DISABLED in edit mode
✅ Visual indicator (greyed-out, "Past" label)
✅ If ALL months are past: checkbox disabled with tooltip

### **4. Validation**
✅ Frontend: Basic validation on submit (no empty fields, 1-100 range)
✅ Backend: Validate percentage range 1-100, relax current 25/50/75/100 restriction
✅ Error messages updated to reflect 1-100 range

### **5. Smart Update (EDIT Mode)**
✅ Frontend tracks original values loaded from database
✅ On submit, compare current vs original, send only changed months
✅ Backend updates only provided months, doesn't delete anything
✅ Example: If only February changed, send only February in request

### **6. State Management**
✅ Use Map<"YYYY-MM", number> for storing month percentages
✅ Efficient preservation during date range changes (CREATE mode)
✅ Easy comparison for smart update detection (EDIT mode)

---

## 📝 **Functional Requirements (28 Total)**

### **New FRs Added:**

- **FR-024**: Start/End dates READ-ONLY in EDIT mode
- **FR-025**: Past months DISABLED in month-by-month mode (only current/future editable)
- **FR-026**: Checkbox disabled if all months are past, with tooltip
- **FR-027**: Smart update - frontend sends only changed months in EDIT mode
- **FR-028**: Frontend uses Map<"YYYY-MM", number> for state management

### **Updated FRs:**

- **FR-003**: Single percentage dropdown → number stepper (min=1, max=100, step=1)
- **FR-005**: Month-by-month inputs → number steppers (min=1, max=100, step=1)
- **FR-014**: Validation range changed from 0-100 to **1-100**
- **FR-016**: Error messages updated to reflect 1-100 range
- **FR-020**: Backend validation range changed from 0-100 to **1-100**

---

## 🎯 **User Stories (5 Total)**

1. **P1 - Single Percentage Allocation** (existing behavior, must work)
2. **P1 - Month-by-Month Allocation Entry** (core new feature)
3. **P2 - Edit Existing Allocation** (edit mode with month-by-month)
4. **P2 - Validation and Feedback** (data integrity checks)
5. **P3 - UI/UX Enhancements** (optional polish, can defer)

---

## 🔧 **Technical Implementation Summary**

### **Frontend (Angular)**

**Allocation Modal Component**:
```typescript
// State Management
monthByMonthMode = signal<boolean>(false);
monthlyPercentages = signal<Map<string, number>>(new Map());
isEditMode = signal<boolean>(false);
originalMonthlyPercentages = new Map<string, number>(); // for smart update

// Methods
generateMonthList(): void { /* filters out past months in EDIT mode */ }
isPastMonth(year: number, month: number): boolean { /* checks if before current month */ }
getChangedMonths(): MonthlyAllocationDTO[] { /* returns only changed months */ }
onDateChange(): void { /* CREATE mode only - regenerate month list */ }
```

**UI Template**:
```html
<!-- Checkbox (disabled if all months past) -->
<input type="checkbox" [(ngModel)]="monthByMonthMode" [disabled]="allMonthsArePast()" />

<!-- Dates (disabled in EDIT mode) -->
<input type="date" [(ngModel)]="startDate" [disabled]="isEditMode()" />
<input type="date" [(ngModel)]="endDate" [disabled]="isEditMode()" />

<!-- Single Percentage Mode -->
@if (!monthByMonthMode()) {
  <input type="number" min="1" max="100" step="1" [(ngModel)]="percentage" /> %
}

<!-- Month-by-Month Mode -->
@if (monthByMonthMode()) {
  @for (month of monthList(); track month.key) {
    <label>{{ month.label }}</label>
    <input type="number" min="1" max="100" step="1"
           [(ngModel)]="monthlyPercentages().get(month.key)"
           [disabled]="isPastMonth(month.year, month.month)" /> %
    @if (isPastMonth(month.year, month.month)) {
      <span class="past-indicator">Past</span>
    }
  }
}
```

### **Backend (Spring Boot)**

**AllocationDTO.java**:
```java
@Data
@Builder
public class AllocationDTO {
    private Long id;
    private Long employeeId;
    private Long projectId;
    private LocalDate startDate;
    private LocalDate endDate;
    private AllocationType allocationType;

    // Single percentage mode
    private Integer currentMonthAllocation;

    // Month-by-month mode
    private List<MonthlyAllocationDTO> monthlyAllocations; // null in single mode
}
```

**AllocationService.java**:
```java
@Transactional
public AllocationDTO updateAllocation(Long id, AllocationDTO dto) {
    // ...existing code...

    // Month-by-month mode
    if (dto.getMonthlyAllocations() != null) {
        for (MonthlyAllocationDTO monthDto : dto.getMonthlyAllocations()) {
            // Validate 1-100 range
            if (monthDto.getPercentage() < 1 || monthDto.getPercentage() > 100) {
                throw new ValidationException("Percentage must be between 1 and 100");
            }

            // Update existing or create new
            allocation.setAllocationForYearMonth(
                monthDto.getYear(),
                monthDto.getMonth(),
                monthDto.getPercentage()
            );
        }
    }

    return toDTO(allocation);
}

private AllocationDTO toDTO(Allocation allocation) {
    // Fetch all monthly allocations for edit mode
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

    return AllocationDTO.builder()
        // ...other fields...
        .monthlyAllocations(monthlyAllocations)
        .build();
}
```

### **API Endpoints**

**GET /api/allocations/{id}** - Returns allocation with full monthlyAllocations array
**POST /api/allocations** - Accepts currentMonthAllocation OR monthlyAllocations
**PUT /api/allocations/{id}** - Smart update: only updates provided months

---

## 🧪 **Testing Requirements**

### **Backend Unit Tests (8 Mandatory)**

```java
@ExtendWith(MockitoExtension.class)
class AllocationServiceTest {
    // CREATE tests
    createAllocation_monthByMonthMode_createsIndividualMonthlyAllocations()
    createAllocation_singlePercentageMode_appliesUniformPercentage()

    // UPDATE tests
    updateAllocation_switchToMonthByMonth_updatesMonthlyAllocations()
    updateAllocation_switchToSinglePercentage_unifiesMonthlyAllocations()
    updateAllocation_smartUpdate_updatesOnlyChangedMonths()

    // VALIDATION tests
    createAllocation_monthByMonthWithInvalidPercentage_throwsValidationException() // <1 or >100
    createAllocation_monthByMonthWithZeroPercent_throwsValidationException() // 0% not allowed
    updateAllocation_smartUpdate_handlesPartialMonthUpdates()
}
```

### **Frontend Manual Testing**

- [ ] Checkbox toggle behavior (with confirmation dialog)
- [ ] Month list generation for various date ranges
- [ ] Date changes in CREATE mode (month list updates)
- [ ] Date fields disabled in EDIT mode
- [ ] Past months disabled with visual indicator
- [ ] Checkbox disabled when all months past
- [ ] Validation for empty and invalid percentages (1-100)
- [ ] Smart update: only changed months sent to backend
- [ ] Submission for both single percentage and month-by-month modes

---

## 📊 **Constitution Compliance**

| Principle | Status | Notes |
|-----------|--------|-------|
| ABAC-First Security | ✅ Pass | Allocation creation/editing already enforces manager access |
| Database-First Performance | ✅ Pass | MonthlyAllocation updates at DB level, no in-memory filtering |
| NULL-Safe Queries | ✅ Pass | No complex native queries added |
| Enum Handling | ✅ Pass | No new enums introduced |
| Backend Unit Tests | ✅ Pass | 8 tests specified |
| Backward Compatibility | ✅ Pass | Single percentage mode preserved, optional monthlyAllocations field |

**Overall**: Fully compliant with Atlas constitution.

---

## ✅ **Readiness for Planning: 100%**

### **All Requirements Finalized**
✅ 28 functional requirements documented
✅ 5 user stories with acceptance scenarios
✅ 15 edge cases addressed
✅ API contracts specified with examples
✅ Technical approach outlined
✅ Testing strategy defined

### **All Clarifications Resolved**
✅ Input control: Number stepper, min=1, max=100, step=1
✅ Date editing: CREATE (editable), EDIT (read-only)
✅ Historical months: Disabled, cannot edit past months
✅ Validation: Frontend + Backend, 1-100 range
✅ Smart update: Send only changed months
✅ State management: Map<"YYYY-MM", number>

### **No Blocking Issues**
✅ No ambiguous requirements
✅ No conflicting behaviors
✅ No missing technical details
✅ No unclear edge cases

---

## 🎯 **Next Steps**

1. **Create Implementation Plan**
   - Identify all files to modify (frontend + backend)
   - Design DB-level logic for smart update
   - Plan integration with existing allocation modal
   - Define file-by-file implementation sequence

2. **Generate Task Breakdown**
   - Break plan into actionable tasks
   - Assign P1, P2, P3 priorities
   - Estimate complexity for each task

3. **Begin Implementation**
   - Follow constitution principles
   - Write mandatory backend unit tests (8 tests)
   - Manual testing against acceptance criteria
   - Create PR when complete

---

## 📁 **Specification Files**

- **Main Spec**: `.specify/specs/003-month-by-month-allocation/spec.md`
- **Review Summary**: `.specify/specs/003-month-by-month-allocation/REVIEW-SUMMARY.md`
- **This Document**: `.specify/specs/003-month-by-month-allocation/FINAL-SPEC-SUMMARY.md`

---

**Status**: ✅ **APPROVED - READY FOR IMPLEMENTATION PLANNING**

🚀 **Let's move to the planning phase!**
