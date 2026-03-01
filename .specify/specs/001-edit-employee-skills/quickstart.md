# Quickstart Guide: Employee Skills Management - Edit Skills

**Feature**: 001-edit-employee-skills
**Date**: 2026-02-20

## Overview

This guide helps developers quickly understand and implement the employee skills editing feature. It covers setup, key concepts, implementation steps, and testing.

---

## Prerequisites

- Java 17 installed at `/Users/mustafaabdelhalim/Library/Java/JavaVirtualMachines/corretto-17.0.16/Contents/Home`
- PostgreSQL 15 running locally or via Docker
- Node.js 18+ with npm
- Existing Atlas codebase cloned
- IDE (IntelliJ IDEA, VS Code, etc.)

---

## Quick Setup

### 1. Environment Setup

```bash
# Set Java 17
export JAVA_HOME=/Users/mustafaabdelhalim/Library/Java/JavaVirtualMachines/corretto-17.0.16/Contents/Home

# Verify Java version
java -version  # Should show Java 17

# Start PostgreSQL (if using Docker)
docker compose up -d postgres

# Verify database connection
psql -h localhost -U atlas_user -d atlas_db -c "SELECT COUNT(*) FROM employees"
```

### 2. Branch Setup

```bash
# Checkout feature branch
git checkout 001-edit-employee-skills

# Pull latest changes
git pull origin 001-edit-employee-skills

# Verify you're on correct branch
git branch --show-current  # Should show: 001-edit-employee-skills
```

### 3. Build & Run

**Backend**:
```bash
cd backend

# Clean build
JAVA_HOME=/Users/mustafaabdelhalim/Library/Java/JavaVirtualMachines/corretto-17.0.16/Contents/Home ./mvnw clean install

# Run tests
JAVA_HOME=/Users/mustafaabdelhalim/Library/Java/JavaVirtualMachines/corretto-17.0.16/Contents/Home ./mvnw test

# Start Spring Boot app
JAVA_HOME=/Users/mustafaabdelhalim/Library/Java/JavaVirtualMachines/corretto-17.0.16/Contents/Home ./mvnw spring-boot:run
```

**Frontend**:
```bash
cd frontend

# Install dependencies (if needed)
npm install

# Start Angular dev server
npm start

# Or build for production
npx ng build
```

**Access Application**:
- Frontend: http://localhost:4200
- Backend API: http://localhost:8080
- API Docs: http://localhost:8080/swagger-ui.html (if Swagger configured)

---

## Key Concepts

### 1. ABAC Access Control

**What**: Attribute-Based Access Control via manager hierarchy

**How it works**:
- Employees have `manager_id` field (FK to employees.id)
- Top-level managers have `manager_id = NULL` (full access)
- Access is transitive: managers can edit skills for all subordinates
- `EmployeeService.getAccessibleEmployeeIds(User currentUser)` returns accessible employee IDs

**Implementation**:
```java
// In EmployeeSkillService
private void validateAccess(Long employeeId, User currentUser) {
    List<Long> accessibleIds = employeeService.getAccessibleEmployeeIds(currentUser);
    if (accessibleIds != null && !accessibleIds.contains(employeeId)) {
        throw new ForbiddenException("Access denied to employee " + employeeId);
    }
}
```

### 2. Skill Association Model

**Entities**:
- `Employee` (1) ←→ (N) `EmployeeSkill` ←→ (N) (1) `Skill`
- `EmployeeSkill` is the junction table with optional `skillLevel` and `skillGrade` fields

**Key Constraint**:
- `UNIQUE(employee_id, skill_id)` prevents duplicate skill assignments

**Operations**:
- **Add**: Create new `EmployeeSkill` record
- **Remove**: Delete `EmployeeSkill` record (hard delete)
- **List**: Query `EmployeeSkill` joined with `Skill`

### 3. Frontend Modal Pattern

**Pattern**: Inline modal template (matches projects page)

**State Management** (Angular 17+ Signals):
```typescript
showEditSkillsModal = signal<boolean>(false);
editingEmployee = signal<Employee | null>(null);
assignedSkills = signal<EmployeeSkillDTO[]>([]);
availableSkills = signal<SkillDTO[]>([]);
```

**Modal Lifecycle**:
1. User clicks pencil icon → `openEditSkillsModal(employee)` called
2. Load current skills + available skills (2 API calls)
3. User adds/removes skills (optimistic UI updates)
4. User clicks Save → batch save changes, close modal, refresh list

---

## Implementation Checklist

### Phase 1: Backend API ✅

- [ ] **DTOs** (`backend/src/main/java/com/atlas/dto/`)
  - [ ] Create `SkillDTO.java`
  - [ ] Create `EmployeeSkillDTO.java`
  - [ ] Create `AddSkillRequest.java`

- [ ] **Repository** (`backend/src/main/java/com/atlas/repository/`)
  - [ ] Add `findByEmployeeId()` to `EmployeeSkillRepository`
  - [ ] Add `findAvailableSkillsForEmployee()` to `SkillRepository`
  - [ ] Add `existsByEmployeeIdAndSkillId()` to `EmployeeSkillRepository`

- [ ] **Service** (`backend/src/main/java/com/atlas/service/`)
  - [ ] Create `EmployeeSkillService.java`
  - [ ] Implement `getEmployeeSkills(Long employeeId, User currentUser)`
  - [ ] Implement `getAvailableSkills(Long employeeId, User currentUser)`
  - [ ] Implement `addSkillToEmployee(Long employeeId, AddSkillRequest request, User currentUser)`
  - [ ] Implement `removeSkillFromEmployee(Long employeeId, Integer skillId, User currentUser)`
  - [ ] Implement `validateAccess(Long employeeId, User currentUser)`

- [ ] **Controller** (`backend/src/main/java/com/atlas/controller/`)
  - [ ] Create `EmployeeSkillController.java`
  - [ ] Add `GET /api/employees/{id}/skills`
  - [ ] Add `GET /api/employees/{id}/skills/available`
  - [ ] Add `POST /api/employees/{id}/skills`
  - [ ] Add `DELETE /api/employees/{id}/skills/{skillId}`

- [ ] **Tests** (`backend/src/test/java/com/atlas/`)
  - [ ] Create `EmployeeSkillServiceTest.java` (unit tests)
  - [ ] Create `EmployeeSkillControllerTest.java` (integration tests)
  - [ ] Update `EmployeeSkillRepositoryTest.java` (if exists)

### Phase 2: Frontend UI ✅

- [ ] **Component** (`frontend/src/app/pages/employees/`)
  - [ ] Add pencil icon to `employees.component.html` (table actions column)
  - [ ] Add edit modal template to `employees.component.html`
  - [ ] Add modal state signals to `employees.component.ts`
  - [ ] Implement `openEditSkillsModal(employee: Employee)`
  - [ ] Implement `closeEditSkillsModal()`
  - [ ] Implement `addSkillToEmployee(skillId: number)`
  - [ ] Implement `removeSkillFromEmployee(skillId: number)`
  - [ ] Implement `saveSkillChanges()`

- [ ] **API Service** (`frontend/src/app/services/`)
  - [ ] Add `getEmployeeSkills(employeeId: number)` to `api.service.ts`
  - [ ] Add `getAvailableSkills(employeeId: number)` to `api.service.ts`
  - [ ] Add `addSkillToEmployee(employeeId: number, request: AddSkillRequest)` to `api.service.ts`
  - [ ] Add `removeSkillFromEmployee(employeeId: number, skillId: number)` to `api.service.ts`

- [ ] **Models** (`frontend/src/app/models/`)
  - [ ] Create or update `skill.ts` interface
  - [ ] Create or update `employee-skill.ts` interface

- [ ] **Styles** (`frontend/src/styles/` or component styles)
  - [ ] Reuse existing modal styles from projects page
  - [ ] Add skill chip styles (for current skills display)
  - [ ] Add searchable dropdown styles (for add skill)

### Phase 3: Testing & Polish ✅

- [ ] **Manual Testing**
  - [ ] Test pencil icon appears only for accessible employees
  - [ ] Test modal opens with correct employee name
  - [ ] Test current skills load correctly
  - [ ] Test available skills excludes assigned skills
  - [ ] Test add skill (optimistic update + API save)
  - [ ] Test remove skill (optimistic update + API save)
  - [ ] Test save button disabled when no changes
  - [ ] Test unsaved changes warning on close
  - [ ] Test ABAC access control (403 errors)
  - [ ] Test error handling (network failures, duplicates, etc.)

- [ ] **Automated Tests**
  - [ ] Backend unit tests passing (EmployeeSkillServiceTest)
  - [ ] Backend integration tests passing (EmployeeSkillControllerTest)
  - [ ] Frontend unit tests (if applicable)

- [ ] **Code Review**
  - [ ] Constitution compliance check
  - [ ] No in-memory filtering violations
  - [ ] ABAC checks present
  - [ ] Error handling comprehensive
  - [ ] Code follows existing patterns

---

## Code Examples

### Backend: EmployeeSkillService

```java
@Service
@RequiredArgsConstructor
public class EmployeeSkillService {
    private final EmployeeRepository employeeRepository;
    private final SkillRepository skillRepository;
    private final EmployeeSkillRepository employeeSkillRepository;
    private final EmployeeService employeeService;

    @Transactional(readOnly = true)
    public List<EmployeeSkillDTO> getEmployeeSkills(Long employeeId, User currentUser) {
        validateAccess(employeeId, currentUser);

        List<EmployeeSkill> skills = employeeSkillRepository.findByEmployeeId(employeeId);
        return skills.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SkillDTO> getAvailableSkills(Long employeeId, User currentUser) {
        validateAccess(employeeId, currentUser);

        List<Skill> availableSkills = skillRepository.findAvailableSkillsForEmployee(employeeId);
        return availableSkills.stream()
                .map(this::toSkillDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public EmployeeSkillDTO addSkillToEmployee(Long employeeId, AddSkillRequest request, User currentUser) {
        validateAccess(employeeId, currentUser);

        // Validate employee exists
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("Employee not found: " + employeeId));

        // Validate skill exists
        Skill skill = skillRepository.findById(request.getSkillId())
                .orElseThrow(() -> new NotFoundException("Skill not found: " + request.getSkillId()));

        // Check for duplicate
        if (employeeSkillRepository.existsByEmployeeIdAndSkillId(employeeId, request.getSkillId())) {
            throw new BadRequestException("Skill " + request.getSkillId() + " is already assigned to employee " + employeeId);
        }

        // Create association
        EmployeeSkill employeeSkill = EmployeeSkill.builder()
                .employee(employee)
                .skill(skill)
                .skillLevel(request.getSkillLevel())
                .skillGrade(request.getSkillGrade())
                .build();

        employeeSkill = employeeSkillRepository.save(employeeSkill);
        return toDTO(employeeSkill);
    }

    @Transactional
    public void removeSkillFromEmployee(Long employeeId, Integer skillId, User currentUser) {
        validateAccess(employeeId, currentUser);

        EmployeeSkill employeeSkill = employeeSkillRepository
                .findByEmployeeIdAndSkillId(employeeId, skillId)
                .orElseThrow(() -> new NotFoundException("Skill " + skillId + " is not assigned to employee " + employeeId));

        employeeSkillRepository.delete(employeeSkill);
    }

    private void validateAccess(Long employeeId, User currentUser) {
        List<Long> accessibleIds = employeeService.getAccessibleEmployeeIds(currentUser);
        if (accessibleIds != null && !accessibleIds.contains(employeeId)) {
            throw new ForbiddenException("Access denied to employee " + employeeId);
        }
    }

    private EmployeeSkillDTO toDTO(EmployeeSkill employeeSkill) {
        return EmployeeSkillDTO.builder()
                .id(employeeSkill.getId())
                .skillId(employeeSkill.getSkill().getId())
                .skillDescription(employeeSkill.getSkill().getDescription())
                .skillLevel(employeeSkill.getSkillLevel())
                .skillGrade(employeeSkill.getSkillGrade())
                .build();
    }

    private SkillDTO toSkillDTO(Skill skill) {
        return SkillDTO.builder()
                .id(skill.getId())
                .description(skill.getDescription())
                .towerDescription(skill.getTower() != null ? skill.getTower().getDescription() : null)
                .build();
    }
}

// AddSkillRequest DTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddSkillRequest {
    @NotNull(message = "Skill ID is required")
    private Integer skillId;

    @NotNull(message = "Skill level is required")
    private EmployeeSkill.SkillLevel skillLevel;

    @NotNull(message = "Skill grade is required")
    private EmployeeSkill.SkillGrade skillGrade;
}
```

### Frontend: Employees Component (Edit Skills Modal)

```typescript
// Add to employees.component.ts

// Modal state
showEditSkillsModal = signal<boolean>(false);
editingEmployee = signal<Employee | null>(null);
assignedSkills = signal<EmployeeSkillDTO[]>([]);
availableSkills = signal<SkillDTO[]>([]);
skillSearchText = signal<string>('');
selectedSkillId = signal<number | null>(null);
selectedSkillLevel = signal<string | null>(null);
selectedSkillGrade = signal<string | null>(null);

// Computed
filteredSkills = computed(() => {
  const search = this.skillSearchText().toLowerCase();
  if (!search) return this.availableSkills();
  return this.availableSkills().filter(s =>
    s.description.toLowerCase().includes(search)
  );
});

hasUnsavedChanges = signal<boolean>(false);

// Methods
openEditSkillsModal(employee: Employee) {
  this.editingEmployee.set(employee);
  this.loadEmployeeSkills(employee.id);
  this.loadAvailableSkills(employee.id);
  this.showEditSkillsModal.set(true);
}

loadEmployeeSkills(employeeId: number) {
  this.api.getEmployeeSkills(employeeId).subscribe({
    next: (skills) => this.assignedSkills.set(skills),
    error: (err) => console.error('Failed to load skills:', err)
  });
}

loadAvailableSkills(employeeId: number) {
  this.api.getAvailableSkills(employeeId).subscribe({
    next: (skills) => this.availableSkills.set(skills),
    error: (err) => console.error('Failed to load available skills:', err)
  });
}

addSkillToEmployee() {
  const skillId = this.selectedSkillId();
  const skillLevel = this.selectedSkillLevel();
  const skillGrade = this.selectedSkillGrade();
  const employeeId = this.editingEmployee()?.id;

  if (!skillId || !skillLevel || !skillGrade || !employeeId) return;

  const request = { skillId, skillLevel, skillGrade };

  this.api.addSkillToEmployee(employeeId, request).subscribe({
    next: (newSkill) => {
      // Optimistic update
      this.assignedSkills.update(skills => [...skills, newSkill]);
      // Remove from available
      this.availableSkills.update(skills =>
        skills.filter(s => s.id !== skillId)
      );
      this.selectedSkillId.set(null);
      this.skillSearchText.set('');
      this.hasUnsavedChanges.set(true);
    },
    error: (err) => alert('Failed to add skill: ' + err.message)
  });
}

removeSkillFromEmployee(skillId: number) {
  const employeeId = this.editingEmployee()?.id;
  if (!employeeId) return;

  this.api.removeSkillFromEmployee(employeeId, skillId).subscribe({
    next: () => {
      // Optimistic update
      const removedSkill = this.assignedSkills().find(s => s.skillId === skillId);
      this.assignedSkills.update(skills =>
        skills.filter(s => s.skillId !== skillId)
      );
      // Add back to available
      if (removedSkill) {
        this.availableSkills.update(skills => [
          ...skills,
          { id: removedSkill.skillId, description: removedSkill.skillDescription, towerDescription: null }
        ]);
      }
      this.hasUnsavedChanges.set(true);
    },
    error: (err) => alert('Failed to remove skill: ' + err.message)
  });
}

closeEditSkillsModal() {
  if (this.hasUnsavedChanges()) {
    if (!confirm('You have unsaved changes. Close anyway?')) {
      return;
    }
  }

  this.showEditSkillsModal.set(false);
  this.editingEmployee.set(null);
  this.assignedSkills.set([]);
  this.availableSkills.set([]);
  this.skillSearchText.set('');
  this.selectedSkillId.set(null);
  this.selectedSkillLevel.set(null);
  this.selectedSkillGrade.set(null);
  this.hasUnsavedChanges.set(false);

  // Refresh employee list to show updated skills
  this.filterEmployees();
}
```

### Frontend: Template (Add to employees.component.html)

```html
<!-- Add pencil icon to actions column in table -->
<td>
  <div class="action-group">
    <button class="btn-icon" (click)="viewDetails(employee)" title="View Details">
      <svg><!-- eye icon --></svg>
    </button>
    <button class="btn-icon" (click)="openEditSkillsModal(employee)" title="Edit Skills">
      <svg viewBox="0 0 24 24" width="16" height="16">
        <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
        <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
      </svg>
    </button>
  </div>
</td>

<!-- Edit Skills Modal -->
@if (showEditSkillsModal()) {
  <div class="modal-overlay" (click)="closeEditSkillsModal()">
    <div class="modal-content" (click)="$event.stopPropagation()">
      <header class="modal-header">
        <h2>Edit Skills - {{ editingEmployee()?.name }}</h2>
        <button class="close-btn" (click)="closeEditSkillsModal()">&times;</button>
      </header>

      <div class="modal-body">
        <!-- Current Skills -->
        <section class="skills-section">
          <h3>Current Skills</h3>
          <div class="skill-chips">
            @for (skill of assignedSkills(); track skill.id) {
              <div class="skill-chip">
                {{ skill.skillDescription }}
                <button class="remove-btn" (click)="removeSkillFromEmployee(skill.skillId)">&times;</button>
              </div>
            } @empty {
              <p class="text-muted">No skills assigned</p>
            }
          </div>
        </section>

        <!-- Add Skill -->
        <section class="skills-section">
          <h3>Add Skill</h3>

          <!-- Skill Selection -->
          <div class="searchable-select">
            <input type="text"
                   class="form-input"
                   placeholder="Search skills..."
                   [(ngModel)]="skillSearchText"
                   autocomplete="off">
            <div class="dropdown-list">
              @for (skill of filteredSkills(); track skill.id) {
                <div class="dropdown-item" (click)="selectedSkillId.set(skill.id)">
                  {{ skill.description }}
                </div>
              } @empty {
                <div class="dropdown-item disabled">No skills available</div>
              }
            </div>
          </div>

          <!-- Skill Level Selection -->
          <select class="form-select" [(ngModel)]="selectedSkillLevel">
            <option value="">Select level...</option>
            <option value="PRIMARY">Primary</option>
            <option value="SECONDARY">Secondary</option>
          </select>

          <!-- Skill Grade Selection -->
          <select class="form-select" [(ngModel)]="selectedSkillGrade">
            <option value="">Select grade...</option>
            <option value="ADVANCED">Advanced</option>
            <option value="INTERMEDIATE">Intermediate</option>
            <option value="BEGINNER">Beginner</option>
          </select>

          <!-- Add Button -->
          <button
            class="btn-primary"
            (click)="addSkillToEmployee()"
            [disabled]="!selectedSkillId() || !selectedSkillLevel() || !selectedSkillGrade()">
            Add Skill
          </button>
        </section>
      </div>

      <footer class="modal-footer">
        <button class="btn-secondary" (click)="closeEditSkillsModal()">Close</button>
      </footer>
    </div>
  </div>
}
```

---

## Testing Guide

### 1. Unit Tests (Backend)

```bash
# Run all tests
JAVA_HOME=/Users/mustafaabdelhalim/Library/Java/JavaVirtualMachines/corretto-17.0.16/Contents/Home ./mvnw test

# Run specific test class
JAVA_HOME=/Users/mustafaabdelhalim/Library/Java/JavaVirtualMachines/corretto-17.0.16/Contents/Home ./mvnw test -Dtest=EmployeeSkillServiceTest

# Run with coverage
JAVA_HOME=/Users/mustafaabdelhalim/Library/Java/JavaVirtualMachines/corretto-17.0.16/Contents/Home ./mvnw clean test jacoco:report
```

### 2. API Testing (Postman/curl)

**Get Employee Skills**:
```bash
curl -X GET http://localhost:8080/api/employees/123/skills \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Get Available Skills**:
```bash
curl -X GET http://localhost:8080/api/employees/123/skills/available \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Add Skill**:
```bash
curl -X POST http://localhost:8080/api/employees/123/skills \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"skillId": 42, "skillLevel": "SECONDARY", "skillGrade": "INTERMEDIATE"}'
```

**Remove Skill**:
```bash
curl -X DELETE http://localhost:8080/api/employees/123/skills/42 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 3. Manual Testing Checklist

- [ ] Open employees page, verify pencil icon appears
- [ ] Click pencil icon, verify modal opens with employee name
- [ ] Verify current skills load and display correctly with level and grade
- [ ] Verify available skills dropdown excludes assigned skills
- [ ] Search for a skill in dropdown, verify filtering works
- [ ] Select a skill, level, and grade, verify Add button becomes enabled
- [ ] Add a skill, verify it appears in current skills immediately with correct level/grade
- [ ] Remove a skill, verify it disappears immediately
- [ ] Try to close modal after changes, verify unsaved changes warning
- [ ] Click Save (if implemented), verify changes persist after page reload
- [ ] Test ABAC: Login as different users, verify access control works
- [ ] Test error cases: network offline, duplicate skill, etc.

---

## Troubleshooting

### Backend Issues

**Problem**: Tests failing with NullPointerException
- **Solution**: Check `@Mock` annotations on repositories, ensure `@InjectMocks` on service

**Problem**: 403 Forbidden when accessing endpoints
- **Solution**: Verify JWT token is valid, check ABAC logic in `validateAccess()`

**Problem**: Duplicate skill error when adding
- **Solution**: Check `UNIQUE(employee_id, skill_id)` constraint exists in database

### Frontend Issues

**Problem**: Modal not opening
- **Solution**: Check `showEditSkillsModal` signal is set to `true`, verify template has `@if (showEditSkillsModal())`

**Problem**: Skills not loading
- **Solution**: Check API service methods, verify endpoints return 200 status, check browser network tab

**Problem**: Unsaved changes warning not showing
- **Solution**: Verify `hasUnsavedChanges` computed signal updates when skills added/removed

---

## Next Steps

After completing implementation:

1. **Code Review**: Submit PR for team review
2. **Constitution Check**: Verify ABAC compliance, no in-memory filtering
3. **Integration Testing**: Test with real data in staging environment
4. **Documentation**: Update API docs, add inline code comments
5. **Deployment**: Merge to main, deploy to production

---

## Resources

- **API Contracts**: `specs/001-edit-employee-skills/contracts/api-contracts.yaml`
- **Data Model**: `specs/001-edit-employee-skills/data-model.md`
- **Research Decisions**: `specs/001-edit-employee-skills/research.md`
- **Implementation Plan**: `specs/001-edit-employee-skills/plan.md`
- **Constitution**: `.specify/memory/constitution.md`

---

**Status**: ✅ Ready for implementation - all design artifacts complete
