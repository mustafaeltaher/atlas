# Implementation Plan: Employee Skills Management - Edit Skills

**Branch**: `001-edit-employee-skills` | **Date**: 2026-02-20 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-edit-employee-skills/spec.md`

## Summary

Add the ability to edit employee skills through a modal dialog in the employees page. Users can add existing skills from a dropdown and remove currently assigned skills. Only skills are editable; other employee fields remain read-only. The implementation follows existing UI patterns from the projects page (pencil icon + edit modal) and backend ABAC patterns (manager hierarchy access control).

## Technical Context

**Language/Version**: Java 17 (backend), TypeScript/Angular 17+ (frontend)
**Primary Dependencies**: Spring Boot 3.2.1, Hibernate 6.4.1, Angular 17+ with Signals
**Storage**: PostgreSQL 15
**Testing**: JUnit 5 (backend), Jasmine/Karma (frontend assumed)
**Target Platform**: Web application (backend API + Angular SPA)
**Project Type**: Web (separate backend/frontend)
**Performance Goals**: Dialog opens < 2 seconds, skill operations complete < 30 seconds, 95% success rate
**Constraints**: ABAC access control, skills-only editing, no new skill creation, follow existing UI patterns
**Scale/Scope**: Existing employee management system, add edit capability to skills association

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### I. ABAC-First Security ✅ PASS

**Compliance**:
- Edit skills endpoint will check user access via existing `getAccessibleEmployeeIds()` pattern
- Only employees within user's manager hierarchy can have skills edited
- Pencil icon displayed only for accessible employees (frontend filtering)
- Backend validates access on every save operation

**Implementation Approach**:
- Reuse `EmployeeService.getAccessibleEmployees()` method for access checks
- Filter editable employees in frontend before showing pencil icon
- Backend endpoint validates `employeeId` against accessible IDs

### II. Database-First Performance ✅ PASS

**Compliance**:
- All skill queries use JPA repositories (no in-memory filtering)
- Fetch existing employee skills via `EmployeeSkillRepository.findByEmployeeId()`
- Fetch available skills via `SkillRepository.findAll()` with exclusion logic in query
- Save/delete operations use repository methods

**Implementation Approach**:
- Use existing `EmployeeSkillRepository` for CRUD operations
- Query for skills with `SkillRepository` (existing)
- Filter out assigned skills at query level: `NOT IN (SELECT skill_id FROM employees_skills WHERE employee_id = ?)`

### III. Faceted Search Architecture ✅ N/A

**Compliance**: Not applicable - this feature doesn't add filter dimensions to faceted search

### IV. NULL-Safe Native Queries ✅ N/A

**Compliance**: Not applicable - this feature uses JPA repository methods, not native SQL queries

### V. Enum Handling Standards ✅ PASS

**Compliance**:
- Existing `EmployeeSkill` entity already uses `@Enumerated(EnumType.STRING)` for `SkillLevel` and `SkillGrade`
- No new enums introduced
- Follows existing VARCHAR pattern

**Note**: The feature spec mentions skills only (add/remove). The existing `EmployeeSkill` entity has `skillLevel` and `skillGrade` fields. **Research needed**: Should these be editable when adding skills, or default to null/preset values?

### Technology Stack ✅ PASS

**Compliance**:
- Uses Java 17 (existing)
- Spring Boot 3.2.1, Hibernate 6.4.1 (existing)
- Angular 17+ with Signals (existing)
- PostgreSQL 15 (existing)

## Project Structure

### Documentation (this feature)

```text
specs/001-edit-employee-skills/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
│   └── api-contracts.yaml
├── checklists/
│   └── requirements.md  # Already created
└── spec.md              # Already created
```

### Source Code (repository root)

```text
backend/
├── src/main/java/com/atlas/
│   ├── controller/
│   │   └── EmployeeSkillController.java        # NEW: REST endpoints for skills editing
│   ├── dto/
│   │   ├── EmployeeSkillDTO.java               # NEW: Skill add/remove request/response
│   │   └── SkillDTO.java                       # NEW or EXISTING: Skill info for dropdown
│   ├── entity/
│   │   ├── Employee.java                       # EXISTING: no changes
│   │   ├── EmployeeSkill.java                  # EXISTING: no changes
│   │   └── Skill.java                          # EXISTING: no changes
│   ├── repository/
│   │   ├── EmployeeSkillRepository.java        # EXISTING: may add custom queries
│   │   └── SkillRepository.java                # EXISTING: may add findAvailableForEmployee()
│   └── service/
│       └── EmployeeSkillService.java           # NEW: business logic for add/remove skills
└── src/test/java/com/atlas/
    ├── controller/
    │   └── EmployeeSkillControllerTest.java    # NEW: API tests
    ├── service/
    │   └── EmployeeSkillServiceTest.java       # NEW: Service tests
    └── repository/
        └── EmployeeSkillRepositoryTest.java    # NEW or UPDATE: Repository tests

frontend/
├── src/app/
│   ├── pages/employees/
│   │   ├── employees.component.ts              # UPDATE: Add pencil icon, modal state
│   │   └── employees.component.html            # UPDATE: Add edit modal template
│   ├── models/
│   │   ├── employee.ts                         # EXISTING: may add skills array
│   │   └── skill.ts                            # NEW or EXISTING: Skill model
│   └── services/
│       └── api.service.ts                      # UPDATE: Add skill CRUD methods
└── src/styles/
    └── components.css                          # UPDATE: Add edit modal styles (or reuse existing)
```

**Structure Decision**: Web application structure with existing backend/frontend separation. This feature adds:
- **Backend**: New controller for skills API, new service for business logic, DTOs for requests/responses
- **Frontend**: Update to employees.component.ts/html for UI, possibly new modal component (or inline template)
- Follows existing patterns from projects page (edit modal with pencil icon)

## Complexity Tracking

> **All Constitution gates passed - no violations to justify**

No complexity violations. This feature:
- Uses existing ABAC patterns (no new access control mechanisms)
- Uses existing JPA repositories (no new persistence patterns)
- Uses existing Angular component patterns (no new frameworks)
- Adds standard CRUD endpoints (no novel architectures)

---

## Phase 0: Outline & Research

### Research Tasks

The following areas need investigation before design:

1. **SkillLevel and SkillGrade Handling**
   - **Question**: The `EmployeeSkill` entity has `skillLevel` (PRIMARY/SECONDARY) and `skillGrade` (ADVANCED/INTERMEDIATE/BEGINNER). Should these be:
     - a) Editable when adding a skill (user selects from dropdowns)
     - b) Set to default values (e.g., SECONDARY + BEGINNER)
     - c) Optional (nullable, user can skip)
   - **Impact**: Affects UI complexity (2 additional dropdowns vs simple add button)
   - **Recommendation**: Research current usage in codebase - are these fields actively used or legacy?

2. **Skill Dropdown Performance**
   - **Question**: How many skills exist in the system? Need to determine if dropdown needs search/filter or pagination.
   - **Research**: Query `SELECT COUNT(*) FROM skills` in production/staging
   - **Threshold**: If < 100 skills, simple dropdown. If 100-500, add search filter. If > 500, add pagination or autocomplete.
   - **Assumption**: Spec assumes ~200 skills max (reasonable for dropdown)

3. **Frontend Modal Pattern**
   - **Question**: Should the edit modal be:
     - a) Inline template in employees.component.ts (like current detail view)
     - b) Separate modal component (e.g., `edit-employee-skills-modal.component.ts`)
     - c) Shared modal service (if one exists)
   - **Research**: Check existing projects page implementation pattern for edit modal
   - **Recommendation**: Follow existing pattern for consistency

4. **Concurrent Edit Handling**
   - **Question**: How to handle when two users edit the same employee's skills simultaneously?
   - **Options**:
     - a) Optimistic concurrency (last save wins) - simpler
     - b) Pessimistic locking (show "another user is editing" message)
     - c) Conflict detection with version field
   - **Recommendation**: Optimistic concurrency for v1 (spec assumption), add versioning later if needed

5. **Unsaved Changes Warning**
   - **Question**: How to implement "confirm before closing with unsaved changes"?
   - **Research**: Check if Angular app has a global modal service with canDeactivate guard
   - **Implementation**: Simple boolean flag `hasUnsavedChanges` + confirm dialog on close

**Research Deliverable**: `research.md` with decisions on each question above.

---

## Phase 1: Design & Contracts

*Prerequisites: research.md complete*

### 1. Data Model (`data-model.md`)

**Entities** (all existing, no schema changes needed):

- **Employee**: `id`, `name`, `email`, `oracleId`, etc. (unchanged)
- **Skill**: `id`, `description`, `tower_id` (unchanged)
- **EmployeeSkill**: `id`, `employee_id`, `skill_id`, `skill_level`, `skill_grade` (unchanged)

**Relationships**:
- `Employee` 1:N `EmployeeSkill` (existing)
- `Skill` 1:N `EmployeeSkill` (existing)

**Validation Rules**:
- Cannot add duplicate skill to same employee
- Cannot remove skill that doesn't exist for employee
- skill_id must reference existing skill
- employee_id must be accessible to current user (ABAC check)

**State Transitions**:
- Skill added: `EmployeeSkill` record created
- Skill removed: `EmployeeSkill` record deleted
- No soft delete needed (per spec assumptions)

### 2. API Contracts (`/contracts/api-contracts.yaml`)

**Endpoints**:

```yaml
GET /api/employees/{employeeId}/skills
  Summary: Get current skills for an employee
  Parameters:
    - employeeId: path, required, Long
  Responses:
    200: Array of SkillDTO
    403: Forbidden (ABAC access denied)
    404: Employee not found

GET /api/employees/{employeeId}/skills/available
  Summary: Get skills NOT currently assigned to employee
  Parameters:
    - employeeId: path, required, Long
  Responses:
    200: Array of SkillDTO (excluding assigned skills)
    403: Forbidden
    404: Employee not found

POST /api/employees/{employeeId}/skills
  Summary: Add a skill to an employee
  Parameters:
    - employeeId: path, required, Long
  Request Body:
    {
      "skillId": Integer,
      "skillLevel": "PRIMARY" | "SECONDARY" | null,
      "skillGrade": "ADVANCED" | "INTERMEDIATE" | "BEGINNER" | null
    }
  Responses:
    201: SkillDTO (created skill association)
    400: Invalid request (duplicate skill, invalid skillId)
    403: Forbidden
    404: Employee or Skill not found

DELETE /api/employees/{employeeId}/skills/{skillId}
  Summary: Remove a skill from an employee
  Parameters:
    - employeeId: path, required, Long
    - skillId: path, required, Integer
  Responses:
    204: No content (success)
    403: Forbidden
    404: Employee or EmployeeSkill not found
```

**DTOs**:

```java
// SkillDTO.java
{
  "id": Integer,
  "description": String,
  "towerDescription": String  // For display in dropdown
}

// EmployeeSkillDTO.java
{
  "id": Integer,
  "skillId": Integer,
  "skillDescription": String,
  "skillLevel": "PRIMARY" | "SECONDARY" | null,
  "skillGrade": "ADVANCED" | "INTERMEDIATE" | "BEGINNER" | null
}

// AddSkillRequest.java
{
  "skillId": Integer,
  "skillLevel": "PRIMARY" | "SECONDARY" | null,
  "skillGrade": "ADVANCED" | "INTERMEDIATE" | "BEGINNER" | null
}
```

### 3. Component Design

**Frontend Changes** (employees.component.ts):

```typescript
// New signals
showEditSkillsModal = signal<boolean>(false);
editingEmployee = signal<Employee | null>(null);
assignedSkills = signal<EmployeeSkillDTO[]>([]);
availableSkills = signal<SkillDTO[]>([]);
selectedSkillToAdd = signal<number | null>(null);
selectedSkillLevel = signal<string | null>(null);  // PRIMARY or SECONDARY
selectedSkillGrade = signal<string | null>(null);  // ADVANCED, INTERMEDIATE, or BEGINNER
hasUnsavedChanges = signal<boolean>(false);

// New methods
openEditSkillsModal(employee: Employee) {
  // Load current skills
  // Load available skills
  // Show modal
}

addSkillToEmployee() {
  const skillId = this.selectedSkillToAdd();
  const level = this.selectedSkillLevel();
  const grade = this.selectedSkillGrade();

  if (!skillId || !level || !grade) return;

  const request = { skillId, skillLevel: level, skillGrade: grade };
  // API call with request
  // Optimistic update
  // Mark hasUnsavedChanges
  // Reset selections
}

removeSkillFromEmployee(skillId: number) {
  // Optimistic update
  // Mark hasUnsavedChanges
}

saveSkillChanges() {
  // Batch save all changes
  // Close modal
  // Refresh employee list
}

closeEditModal() {
  // Check hasUnsavedChanges
  // Confirm dialog if needed
  // Reset state
}
```

**Template Structure**:

```html
<!-- Add pencil icon to actions column -->
<button class="btn-icon" (click)="openEditSkillsModal(employee)" title="Edit Skills">
  <svg><!-- pencil icon SVG --></svg>
</button>

<!-- Edit modal (show when showEditSkillsModal() === true) -->
<div class="modal-overlay" *ngIf="showEditSkillsModal()">
  <div class="modal-content">
    <header>
      <h2>Edit Skills - {{ editingEmployee()?.name }}</h2>
      <button (click)="closeEditModal()">×</button>
    </header>

    <section class="current-skills">
      <h3>Current Skills</h3>
      <div class="skill-chips">
        <div *ngFor="let skill of assignedSkills()" class="skill-chip">
          <span class="skill-name">{{ skill.skillDescription }}</span>
          <span class="skill-meta">{{ skill.skillLevel }} • {{ skill.skillGrade }}</span>
          <button (click)="removeSkillFromEmployee(skill.skillId)">×</button>
        </div>
      </div>
    </section>

    <section class="add-skills">
      <h3>Add Skill</h3>

      <!-- Skill Selection -->
      <select [(ngModel)]="selectedSkillToAdd" class="form-select">
        <option value="">Select a skill...</option>
        <option *ngFor="let skill of availableSkills()" [value]="skill.id">
          {{ skill.description }}
        </option>
      </select>

      <!-- Skill Level Selection -->
      <select [(ngModel)]="selectedSkillLevel" class="form-select">
        <option value="">Select level...</option>
        <option value="PRIMARY">Primary</option>
        <option value="SECONDARY">Secondary</option>
      </select>

      <!-- Skill Grade Selection -->
      <select [(ngModel)]="selectedSkillGrade" class="form-select">
        <option value="">Select grade...</option>
        <option value="ADVANCED">Advanced</option>
        <option value="INTERMEDIATE">Intermediate</option>
        <option value="BEGINNER">Beginner</option>
      </select>

      <button
        (click)="addSkillToEmployee()"
        [disabled]="!selectedSkillToAdd() || !selectedSkillLevel() || !selectedSkillGrade()"
        class="btn-primary">
        Add Skill
      </button>
    </section>

    <footer>
      <button class="btn-secondary" (click)="closeEditModal()">Cancel</button>
      <button class="btn-primary" (click)="saveSkillChanges()" [disabled]="!hasUnsavedChanges()">
        Save Changes
      </button>
    </footer>
  </div>
</div>
```

### 4. Service Layer Design

**EmployeeSkillService.java**:

```java
@Service
@RequiredArgsConstructor
public class EmployeeSkillService {
    private final EmployeeRepository employeeRepository;
    private final SkillRepository skillRepository;
    private final EmployeeSkillRepository employeeSkillRepository;
    private final EmployeeService employeeService;  // For ABAC checks

    // Get current skills for employee (with ABAC check)
    public List<EmployeeSkillDTO> getEmployeeSkills(Long employeeId, User currentUser);

    // Get available skills (excluding assigned ones)
    public List<SkillDTO> getAvailableSkills(Long employeeId, User currentUser);

    // Add skill to employee (with ABAC + validation)
    public EmployeeSkillDTO addSkillToEmployee(Long employeeId, AddSkillRequest request, User currentUser);

    // Remove skill from employee (with ABAC check)
    public void removeSkillFromEmployee(Long employeeId, Integer skillId, User currentUser);

    // Helper: Check if user has access to edit employee
    private void validateAccess(Long employeeId, User currentUser);
}
```

**Repository Queries** (add to EmployeeSkillRepository):

```java
public interface EmployeeSkillRepository extends JpaRepository<EmployeeSkill, Integer> {
    List<EmployeeSkill> findByEmployeeId(Long employeeId);

    Optional<EmployeeSkill> findByEmployeeIdAndSkillId(Long employeeId, Integer skillId);

    boolean existsByEmployeeIdAndSkillId(Long employeeId, Integer skillId);
}
```

**Repository Queries** (add to SkillRepository):

```java
public interface SkillRepository extends JpaRepository<Skill, Integer> {
    @Query("SELECT s FROM Skill s WHERE s.id NOT IN " +
           "(SELECT es.skill.id FROM EmployeeSkill es WHERE es.employee.id = :employeeId)")
    List<Skill> findAvailableSkillsForEmployee(@Param("employeeId") Long employeeId);
}
```

### 5. Agent Context Update

Run `.specify/scripts/bash/update-agent-context.sh claude` to update context with:
- New EmployeeSkillController endpoints
- New EmployeeSkillService methods
- New DTOs (SkillDTO, EmployeeSkillDTO, AddSkillRequest)
- Frontend modal state management pattern

---

## Testing Strategy

### Backend Tests

**EmployeeSkillServiceTest.java**:
- Test ABAC enforcement (access denied for non-accessible employees)
- Test add skill success case
- Test add duplicate skill (should fail)
- Test remove skill success case
- Test remove non-existent skill (should fail)
- Test get available skills (should exclude assigned)

**EmployeeSkillControllerTest.java**:
- Test GET /api/employees/{id}/skills returns current skills
- Test GET /api/employees/{id}/skills/available excludes assigned
- Test POST /api/employees/{id}/skills adds skill
- Test DELETE /api/employees/{id}/skills/{skillId} removes skill
- Test 403 Forbidden for non-accessible employees

**EmployeeSkillRepositoryTest.java**:
- Test findByEmployeeId returns all skills
- Test findAvailableSkillsForEmployee excludes assigned
- Test existsByEmployeeIdAndSkillId detects duplicates

### Frontend Tests (if applicable)

- Test pencil icon appears for accessible employees
- Test modal opens with current skills loaded
- Test add skill updates UI optimistically
- Test remove skill updates UI optimistically
- Test save button disabled when no changes
- Test unsaved changes warning on close

### Manual Testing Scenarios

1. **Happy Path**: Open edit modal, add 2 skills, remove 1 skill, save → verify skills updated
2. **Access Control**: Try to edit employee outside hierarchy → pencil icon should not appear
3. **Concurrent Edit**: Two users edit same employee → last save wins (no errors)
4. **Empty Skills**: Edit employee with no skills → can add skills successfully
5. **Full Skills**: Add all available skills → dropdown should be empty
6. **Unsaved Changes**: Make changes, try to close → should prompt for confirmation
7. **Network Error**: Simulate API failure → should show error message, preserve changes

---

## Implementation Phases

### Phase 2.1: Backend API (Priority: P1)

**Tasks**:
1. Create `EmployeeSkillDTO`, `SkillDTO`, `AddSkillRequest` DTOs
2. Create `EmployeeSkillService` with ABAC checks and business logic
3. Add repository queries (`findAvailableSkillsForEmployee`, etc.)
4. Create `EmployeeSkillController` with 4 endpoints
5. Write unit tests for service and controller
6. Test manually with Postman/curl

**Success Criteria**:
- All tests passing (service + controller)
- ABAC checks enforced
- Duplicate skill prevention works
- API returns correct data

### Phase 2.2: Frontend UI (Priority: P1)

**Tasks**:
1. Add pencil icon to employees table (next to eye icon)
2. Add edit modal template (inline or component)
3. Implement modal state management (signals)
4. Wire up add/remove skill actions
5. Implement unsaved changes warning
6. Add API service methods for skill CRUD
7. Style modal (reuse existing project edit modal styles)
8. Test manually in browser

**Success Criteria**:
- Pencil icon visible and clickable
- Modal opens with current skills
- Add/remove works with optimistic updates
- Save persists changes
- UI matches projects page pattern

### Phase 2.3: Polish & Error Handling (Priority: P3)

**Tasks**:
1. Add loading states (spinner while fetching skills)
2. Add error toasts/messages for API failures
3. Add success confirmation after save
4. Add keyboard shortcuts (ESC to close, Enter to save)
5. Add accessibility attributes (aria-labels, focus management)
6. Test edge cases (network errors, concurrent edits)

**Success Criteria**:
- Clear error messages displayed
- Loading states prevent double-clicks
- Keyboard navigation works
- Accessibility audit passes

---

## Success Metrics

- **Dialog Performance**: Opens in < 2 seconds ✅
- **Operation Speed**: Add/remove/save completes in < 30 seconds ✅
- **Success Rate**: 95% of operations succeed without errors ✅
- **UI Responsiveness**: Changes visible within 2 seconds after save ✅
- **Error Clarity**: 90% of users can resolve errors without help ✅
- **Icon Discoverability**: Pencil icon visually distinct from eye icon ✅

---

## Risks & Mitigation

| Risk | Impact | Mitigation |
|------|--------|------------|
| SkillLevel/SkillGrade complexity | High UI complexity | Research usage, make optional if unused |
| Large skill count (> 500) | Dropdown performance | Add search filter or autocomplete |
| Concurrent edits | Data loss | Accept optimistic concurrency for v1, add versioning later |
| ABAC violation | Security issue | Thorough testing of access checks, code review |
| Modal UX inconsistency | Poor UX | Follow existing projects page patterns exactly |

---

## Dependencies

**Backend**:
- Existing Employee, Skill, EmployeeSkill entities
- Existing EmployeeRepository, SkillRepository, EmployeeSkillRepository
- Existing EmployeeService (for ABAC checks)
- Spring Boot 3.2.1, Hibernate 6.4.1

**Frontend**:
- Existing employees.component.ts
- Existing ApiService
- Angular 17+ with Signals
- Existing modal/dialog styles (from projects page)

**External**:
- None (purely internal feature)

---

## Notes

- **No database migrations needed**: All required tables and relationships exist
- **Reuses existing patterns**: ABAC from EmployeeService, UI from projects page
- **Incremental delivery**: Backend API can be deployed first, then frontend
- **Future enhancements** (out of scope for v1):
  - Skill proficiency ratings
  - Skill recommendations based on role
  - Skill expiration/recertification
  - Audit log of skill changes
