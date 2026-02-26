# Specification Review Summary - Month-by-Month Allocation

**Date**: 2026-02-25
**Status**: Updated with user clarifications

---

## ✅ **Clarifications Received & Applied**

### 1. Edit Modal Access Pattern ✓
**Clarification**: Allocations can be edited from the main allocations page via a pencil button that opens a dialog allowing managers to create new allocations or edit existing ones for that employee.

**Applied Changes**:
- Added edge case documenting the pencil button and edit flow
- User Story 3 already covers edit scenarios
- No changes needed to acceptance scenarios

---

### 2. Mandatory Percentage Entry ✓
**Clarification**: All month percentages must be entered (no empty values allowed).

**Applied Changes**:
- Updated User Story 3, Scenario 4: New months display with empty fields that MUST be filled
- Updated FR-021: Explicitly states no default values, empty fields required
- Removed ambiguity about "0% or prompting for input"

---

### 3. Mid-Month Allocation Logic ✓
**Clarification**: Regardless of day, allocations that touch a month count as FULL month allocation. Utilization calculation (pro-rating) deferred to future phase.

**Applied Changes**:
- Added edge case explaining mid-month logic with examples
- Added FR-018: Codifies month normalization matching existing backend logic
- References existing code (AllocationService.java lines 293-294)

---

### 4. Cross-Allocation Validation (Out of Scope) ✓
**Clarification**: Do NOT implement warnings when employee total allocation exceeds 100% across multiple allocations.

**Applied Changes**:
- Removed User Story 4, Scenario 4 (cross-allocation validation)
- Added FR-023: Explicitly marks cross-allocation validation as out of scope
- Simplified validation requirements to focus on single allocation integrity

---

## 📋 **Additional Improvements Made**

### New Functional Requirements Added:
- **FR-018**: Month calculation logic (normalize to first day of month)
- **FR-019**: GET /api/allocations/{id} must return monthlyAllocations array
- **FR-020**: Backend must accept 0-100% range (relax current 25/50/75/100 restriction)
- **FR-021**: New months in date range must have empty fields (no defaults)
- **FR-022**: Confirmation dialog behavior specified (Cancel default, backdrop closes)
- **FR-023**: Cross-allocation validation explicitly out of scope

### API Contract Expanded:
- Added GET /api/allocations/{id} endpoint with full response structure
- Added PUT /api/allocations/{id} behavior specification
- Clarified backend logic for update (delete out-of-range, update existing, create new)

### Edge Cases Added:
- Edit modal access pattern (pencil button)
- Mid-month allocation logic with examples
- Date range shrinking (data loss warning)
- 0% percentage validity

### Technical Considerations Updated:
- Added toDTO() modification requirement (include monthlyAllocations)
- Added validateAllocationPercentage() update (0-100 range)
- Specified update logic for MonthlyAllocation records

---

## ⚠️ **Remaining Items Needing Clarification**

### 1. **Percentage Input Control (P3 Story)** - LOW PRIORITY
**Question**: For MVP, should month-by-month percentage inputs be:
- **Option A**: Simple text inputs (0-100) - Simplest, fastest to implement
- **Option B**: Dropdown with common values (0, 25, 50, 75, 100) - Matches current single percentage UI
- **Option C**: Dropdown with "Other" option for custom values - Balance of convenience and flexibility

**Recommendation**: Choose **Option A** for MVP (simple text inputs), defer dropdowns/sliders to P3 UX enhancements phase.

**Impact**: Low - doesn't affect core functionality, only convenience. Can be changed post-MVP.

---

### 2. **Historical Month Indicator** - LOW PRIORITY
**Question**: Should past months in the month-by-month list have visual indicators (greyed out, labeled "Past")?

**Current Spec**: Edge case mentions "may display visual indicator" but doesn't mandate it.

**Recommendation**: Defer to post-MVP. Allow full editing of all months without visual distinction. Add as UX enhancement later if user feedback requests it.

**Impact**: Low - cosmetic feature, doesn't affect functionality.

---

### 3. **Date Range Change Confirmation Thresholds** - MEDIUM PRIORITY
**Question**: When user changes dates in month-by-month mode:
- Should confirmation dialog appear ONLY if months with filled percentages are removed?
- Or appear for ANY date change (even if no data loss)?

**Current Spec**: "System should detect removed months that have percentages entered" suggests only when data loss occurs.

**Recommendation**: Show confirmation ONLY when removing months with entered percentages (data loss scenario). Allow silent date extension.

**Impact**: Medium - affects user experience. Too many confirmations can be annoying.

---

### 4. **Validation Timing** - MEDIUM PRIORITY
**Question**: When should validation errors appear?
- **Option A**: Real-time (as user types) - Immediate feedback but can be annoying
- **Option B**: On blur (when leaving field) - Balanced approach
- **Option C**: On submit only - Simple but late feedback

**Current Spec**: Says "When I move to the next field or attempt to submit" suggesting on blur + on submit.

**Recommendation**: **Option B** - Validate on blur (leaving field) and on submit. Provides timely feedback without being intrusive.

**Impact**: Medium - affects UX quality and user frustration levels.

---

### 5. **Backend Transaction/Update Strategy** - TECHNICAL DECISION
**Question**: When updating allocation from single to month-by-month (or vice versa), should backend:
- **Option A**: Delete all MonthlyAllocation records, then create new ones (clean slate)
- **Option B**: Smart update (keep matching months, update changed, delete removed, create new)

**Current Spec**: PUT endpoint says "Delete existing... Update existing... Create new" suggesting Option B.

**Recommendation**: **Option A** for MVP (delete all, recreate) - Simpler logic, easier to test, less risk of stale data. Optimize to Option B if performance issues arise.

**Impact**: Low for small allocations (<24 months), but simpler implementation reduces bugs.

---

### 6. **Frontend State Management for Date Changes** - TECHNICAL DECISION
**Question**: When user changes dates and month list regenerates, how to preserve percentages for overlapping months?

**Implementation Options**:
- Store percentages in a Map keyed by "YYYY-MM" string
- Store as array and match by year+month when regenerating

**Recommendation**: Use Map<string, number> where key is "YYYY-MM" format. Makes preservation logic simpler.

**Impact**: Medium - affects code maintainability. No functional difference.

---

## 🎯 **Readiness Assessment**

### Specification Completeness: **95%**

**Ready for Planning**: ✅ **YES**

The specification is now comprehensive enough to proceed to implementation planning. The remaining items are:
- **3 Low Priority** items that can be deferred or defaulted
- **3 Medium Priority** items that have clear recommendations and can be decided during planning

### Next Steps:

1. **Approve remaining decisions** (or we can proceed with recommendations)
2. **Create implementation plan** - Design DB-level approach, identify files to modify
3. **Generate tasks** - Break down into actionable implementation tasks
4. **Implement** - Follow constitution principles and write mandatory unit tests

---

## 📊 **Constitution Compliance Check**

| Principle | Status | Notes |
|-----------|--------|-------|
| ABAC-First Security | ✅ Pass | Edit access already enforced by existing allocation ABAC |
| Database-First Performance | ✅ Pass | MonthlyAllocation updates happen at DB level |
| Faceted Search | ⚠️ N/A | Feature doesn't add new filters |
| NULL-Safe Queries | ✅ Pass | No complex native queries added |
| Enum Handling | ✅ Pass | No new enums |
| Backend Unit Tests | ✅ Pass | 8 tests specified in spec |
| Backward Compatibility | ✅ Pass | Single percentage mode preserved |

**Overall**: Fully compliant with Atlas constitution.

---

## 📝 **Recommendation**

**Proceed to Implementation Planning Phase** with the following approach:

1. Use **recommended defaults** for the 6 remaining clarification items (all low-medium priority)
2. Create detailed implementation plan following constitution principles
3. Generate task breakdown
4. Begin implementation with mandatory backend unit tests

The specification is sufficiently detailed to begin planning and implementation. Remaining items can be finalized during planning phase without blocking progress.
