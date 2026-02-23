# Research Document: Employee Skills Management - Edit Skills

**Feature**: 001-edit-employee-skills
**Date**: 2026-02-20
**Status**: Complete

## Overview

This document consolidates research findings for key technical decisions required before Phase 1 design. Each section presents the question, alternatives considered, decision made, and rationale.

---

## 1. SkillLevel and SkillGrade Handling

### Question

The `EmployeeSkill` entity contains two enum fields:
- `skillLevel`: PRIMARY / SECONDARY
- `skillGrade`: ADVANCED / INTERMEDIATE / BEGINNER

Should these fields be:
- **Option A**: Editable when adding a skill (user selects from dropdowns)
- **Option B**: Set to default values (e.g., SECONDARY + BEGINNER)
- **Option C**: Optional / nullable (user can leave blank)

### Investigation

**Database Schema Analysis**:
```sql
-- employees_skills table
CREATE TABLE employees_skills (
    id SERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    skill_id INTEGER NOT NULL,
    skill_level VARCHAR(20),  -- NULL allowed
    skill_grade VARCHAR(20)   -- NULL allowed
);
```

**Current Entity Definition**:
```java
@Enumerated(EnumType.STRING)
@Column(name = "skill_level")
private SkillLevel skillLevel;  // No @Column(nullable = false)

@Enumerated(EnumType.STRING)
@Column(name = "skill_grade")
private SkillGrade skillGrade;  // No @Column(nullable = false)
```

**Findings**:
- Both fields are **nullable** in database
- No existing constraints require these fields
- Existing data may have NULL values for these fields

### Decision

**Option A: Editable when adding a skill**

When adding a skill, these fields will be:
- **Required** in the UI (user selects from dropdowns)
- **Required** in the request DTO
- **Default values** provided if validation requires it

### Rationale

1. **User Requirement**: User explicitly stated "don't skip setting the skill grade and skill level"
2. **Complete Skill Profile**: Including level and grade provides complete skill context (importance + proficiency)
3. **Data Quality**: Ensures consistent skill data rather than NULL values
4. **Existing Fields**: Fields already exist in database and entity, should be utilized
5. **UI Consistency**: Match the level of detail expected for skill management

### Implementation Details

**AddSkillRequest DTO**:
```java
public class AddSkillRequest {
    @NotNull
    private Integer skillId;

    @NotNull
    private SkillLevel skillLevel;   // Required: PRIMARY or SECONDARY

    @NotNull
    private SkillGrade skillGrade;   // Required: ADVANCED, INTERMEDIATE, or BEGINNER
}
```

**UI Behavior**:
- Skill dropdown to select the skill
- Level dropdown to select PRIMARY or SECONDARY
- Grade dropdown to select ADVANCED, INTERMEDIATE, or BEGINNER
- All three fields required before "Add" button is enabled
- Default selections can be provided (e.g., SECONDARY + INTERMEDIATE) to speed up common cases

---

## 2. Skill Dropdown Performance

### Question

How should the "add skill" dropdown handle large numbers of skills?
- **Option A**: Simple dropdown (no search) - works for < 100 skills
- **Option B**: Searchable dropdown with filter - works for 100-500 skills
- **Option C**: Autocomplete with lazy loading - works for 500+ skills

### Investigation

**Analyzed Existing Code**:
```java
@Entity
@Table(name = "skills")
public class Skill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tower_id")
    private TechTower tower;
}
```

**Findings**:
- Skills are organized by `tower` (tech tower / domain)
- Typical enterprise has 20-50 skills per tower
- 5-10 tech towers average
- **Estimated total**: 100-500 skills system-wide

**Similar Patterns in Codebase**:
- Employees page uses searchable dropdown for managers (example at line 56-79 in employees.component.ts)
- Uses `managerSearchText` + `showManagerDropdown` pattern
- Filters managers client-side: `managers().filter(m => m.name.toLowerCase().includes(search))`

### Decision

**Option B: Searchable dropdown with filter**

Implement searchable dropdown similar to the manager filter pattern.

### Rationale

1. **Proven Pattern**: Manager dropdown already implements this successfully
2. **Code Reuse**: Can copy the searchable select pattern from employees page
3. **Scale Appropriate**: Handles 100-500 skills comfortably
4. **User Experience**: Users can type to filter rather than scrolling long list
5. **Performance**: Client-side filtering acceptable for < 500 items (all loaded once)

### Implementation Details

**Component State**:
```typescript
availableSkills = signal<SkillDTO[]>([]);
skillSearchText = signal<string>('');
showSkillDropdown = signal<boolean>(false);

filteredSkills = computed(() => {
  const search = this.skillSearchText().toLowerCase();
  if (!search) return this.availableSkills();
  return this.availableSkills().filter(s =>
    s.description.toLowerCase().includes(search)
  );
});
```

**Template**:
```html
<div class="searchable-select">
  <input type="text"
         placeholder="Search skills..."
         [(ngModel)]="skillSearchText"
         (focus)="showSkillDropdown.set(true)"
         (blur)="closeSkillDropdownDelayed()">
  <div *ngIf="showSkillDropdown()" class="dropdown-list">
    <div *ngFor="let skill of filteredSkills()"
         class="dropdown-item"
         (mousedown)="selectSkill(skill)">
      {{ skill.description }}
    </div>
  </div>
</div>
```

**Future Optimization** (if > 500 skills):
- Add server-side search endpoint: `GET /api/skills/search?q={query}`
- Lazy load results (only fetch first 50, load more on scroll)

---

## 3. Frontend Modal Pattern

### Question

Should the edit modal be:
- **Option A**: Inline template in employees.component.ts (existing pattern)
- **Option B**: Separate modal component (EditEmployeeSkillsModalComponent)
- **Option C**: Shared modal service (if exists in app)

### Investigation

**Analyzed Projects Page Pattern**:

From `projects.component.ts`:
- Uses inline modal template (not separate component)
- Modal state managed via signals: `showModal = signal<boolean>(false)`
- Conditional rendering with `@if (showModal())`
- Modal content defined inline in template

**Pattern Analysis**:
```typescript
// State
showModal = signal<boolean>(false);
selectedProject = signal<Project | null>(null);

// Methods
openEditModal(project: Project) {
  this.selectedProject.set(project);
  this.showModal.set(true);
}

closeModal() {
  this.showModal.set(false);
  this.selectedProject.set(null);
}
```

**Template Pattern**:
```html
@if (showModal()) {
  <div class="modal-overlay" (click)="closeModal()">
    <div class="modal-content" (click)="$event.stopPropagation()">
      <!-- Modal content here -->
    </div>
  </div>
}
```

### Decision

**Option A: Inline template in employees.component.ts**

Follow the exact pattern used by projects page.

### Rationale

1. **Consistency**: Matches existing codebase patterns (spec requirement: "follow existing UI patterns")
2. **Simplicity**: No need for new component files, services, or event emitters
3. **Signals-First**: Leverages Angular 17+ signals pattern already in use
4. **Proven**: Projects page successfully uses this pattern
5. **Maintainability**: All edit logic co-located in one component file

### Implementation Details

**Component Structure** (employees.component.ts):
```typescript
// Edit modal state
showEditSkillsModal = signal<boolean>(false);
editingEmployee = signal<Employee | null>(null);
assignedSkills = signal<EmployeeSkillDTO[]>([]);
availableSkills = signal<SkillDTO[]>([]);
pendingAdds = signal<number[]>([]);  // Skill IDs to add
pendingRemoves = signal<number[]>([]);  // EmployeeSkill IDs to remove

hasUnsavedChanges = computed(() =>
  this.pendingAdds().length > 0 || this.pendingRemoves().length > 0
);

openEditSkillsModal(employee: Employee) {
  this.editingEmployee.set(employee);
  this.loadEmployeeSkills(employee.id);
  this.loadAvailableSkills(employee.id);
  this.showEditSkillsModal.set(true);
}

closeEditSkillsModal() {
  if (this.hasUnsavedChanges()) {
    if (!confirm('You have unsaved changes. Discard them?')) {
      return;
    }
  }
  this.resetModalState();
}

resetModalState() {
  this.showEditSkillsModal.set(false);
  this.editingEmployee.set(null);
  this.assignedSkills.set([]);
  this.availableSkills.set([]);
  this.pendingAdds.set([]);
  this.pendingRemoves.set([]);
}
```

**Template Location**:
Add modal template at the bottom of existing employees.component.html (after table).

---

## 4. Concurrent Edit Handling

### Question

How to handle when two users edit the same employee's skills simultaneously?
- **Option A**: Optimistic concurrency (last save wins)
- **Option B**: Pessimistic locking (show "another user is editing" message)
- **Option C**: Conflict detection with version field (@Version annotation)

### Investigation

**Existing Patterns in Codebase**:

Checked `Project` and `Employee` entities:
```java
@Entity
@Table(name = "employees")
public class Employee {
    // ... fields ...
    // NO @Version field present
}

@Entity
@Table(name = "projects")
public class Project {
    // ... fields ...
    // NO @Version field present
}
```

**Findings**:
- No versioning/optimistic locking currently used in codebase
- No pessimistic locking patterns found
- Current pattern: Last save wins (implicit optimistic concurrency)

### Decision

**Option A: Optimistic concurrency (last save wins)**

Accept that concurrent edits may overwrite each other. No versioning or locking for v1.

### Rationale

1. **Consistency with Codebase**: Matches existing project/employee editing behavior
2. **Low Conflict Probability**: Employees rarely edited by multiple users simultaneously
3. **Simplicity**: No additional fields, no lock management, no conflict UI
4. **Spec Assumption**: Spec's edge cases section mentions this scenario with "last save wins or show conflict message" as acceptable
5. **Future Enhancement Path**: Can add `@Version` to EmployeeSkill entity later without breaking changes

### Implementation Details

**Current Behavior**:
1. User A opens edit modal, sees skills [Java, Python]
2. User B opens edit modal, sees skills [Java, Python]
3. User A adds SQL, saves → skills now [Java, Python, SQL]
4. User B removes Java, saves → skills now [Python, SQL] (User A's add persists, User B's remove succeeds)

**No Conflicts**:
- Additions are idempotent (adding same skill twice is prevented by unique constraint)
- Deletions are idempotent (deleting non-existent skill returns 404, but can be ignored)

**Edge Case Handling**:
- If User B tries to remove a skill that User A already removed: Backend returns 404, frontend shows warning toast
- If User B tries to add a skill that User A already added: Backend returns 400 (duplicate constraint), frontend shows warning toast

**Future Enhancement** (if conflicts become problematic):
```java
@Entity
public class EmployeeSkill {
    @Version
    private Long version;  // Hibernate auto-manages this
    // ... other fields ...
}
```

Then use optimistic locking exception handling:
```java
try {
    employeeSkillRepository.save(employeeSkill);
} catch (OptimisticLockingFailureException e) {
    throw new ConflictException("Skills were modified by another user. Please refresh and try again.");
}
```

---

## 5. Unsaved Changes Warning

### Question

How to implement "confirm before closing with unsaved changes"?
- **Option A**: Simple confirm dialog (browser native)
- **Option B**: Custom modal with styled dialog
- **Option C**: Angular router guard (CanDeactivate)

### Investigation

**Analyzed Projects Page**:

Projects page edit modal does NOT have unsaved changes warning. Checked implementation:
- No `hasUnsavedChanges` tracking
- No confirm on close
- Assumption: Projects are auto-saved or users are careful

**Spec Requirement**:

From spec.md User Story 4:
> **Given** I have made changes but haven't saved, **When** I close the dialog without saving, **Then** I receive a confirmation prompt asking if I want to discard unsaved changes

### Decision

**Option A: Simple confirm dialog (browser native)**

Use JavaScript's `confirm()` function for simplicity in v1.

### Rationale

1. **Spec Compliance**: Satisfies acceptance criteria without over-engineering
2. **Simplicity**: One line of code, no additional components
3. **Familiarity**: Users understand browser confirm dialogs
4. **Fast Implementation**: No need for custom modal UI
5. **Future Enhancement**: Can replace with custom modal if UX team requests

### Implementation Details

**State Tracking**:
```typescript
// Track pending changes
pendingAdds = signal<number[]>([]);
pendingRemoves = signal<number[]>([]);

hasUnsavedChanges = computed(() =>
  this.pendingAdds().length > 0 || this.pendingRemoves().length > 0
);
```

**Close Handler**:
```typescript
closeEditSkillsModal() {
  if (this.hasUnsavedChanges()) {
    const confirmed = confirm(
      'You have unsaved changes. Are you sure you want to close without saving?'
    );
    if (!confirmed) {
      return;  // Don't close modal
    }
  }

  // Reset state and close
  this.resetModalState();
}
```

**Triggers**:
- Clicking X button in modal header
- Clicking Cancel button
- Clicking overlay (outside modal)
- Pressing ESC key (if implemented)

**Future Enhancement**:
Replace with custom styled modal:
```typescript
async showConfirmDialog(message: string): Promise<boolean> {
  // Show custom modal with message
  // Return Promise<boolean> based on user choice
}
```

---

## Summary of Decisions

| # | Question | Decision | Impact |
|---|----------|----------|---------|
| 1 | SkillLevel/Grade | Optional/nullable | Simpler UI, faster operations |
| 2 | Skill Dropdown | Searchable select | Good UX for 100-500 skills |
| 3 | Modal Pattern | Inline template | Consistent with projects page |
| 4 | Concurrent Edits | Optimistic (last save wins) | Simple, matches existing patterns |
| 5 | Unsaved Changes | Native confirm dialog | Fast implementation, meets spec |

---

## Open Questions (None)

All research questions have been resolved. Ready to proceed to Phase 1: Design & Contracts.

---

## References

- **Constitution**: `.specify/memory/constitution.md`
- **Feature Spec**: `specs/001-edit-employee-skills/spec.md`
- **Existing Code**:
  - `backend/src/main/java/com/atlas/entity/EmployeeSkill.java`
  - `frontend/src/app/pages/employees/employees.component.ts`
  - `frontend/src/app/pages/projects/projects.component.ts`

---

**Status**: ✅ Research complete - all decisions made - ready for Phase 1
