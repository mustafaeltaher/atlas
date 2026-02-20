# Tasks: Employee Skills Management - Edit Skills

**Input**: Design documents from `/specs/001-edit-employee-skills/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/api-contracts.yaml

**Tests**: Backend tests are included. Frontend tests are omitted as they are not standard practice for this project based on the quickstart.md guidance.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Backend**: `backend/src/main/java/com/atlas/`
- **Frontend**: `frontend/src/app/`
- **Tests**: `backend/src/test/java/com/atlas/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Verify project environment and branch setup

- [ ] T001 Verify branch `001-edit-employee-skills` is checked out
- [ ] T002 Verify Java 17 environment at `/Users/mustafaabdelhalim/Library/Java/JavaVirtualMachines/corretto-17.0.16/Contents/Home`
- [ ] T003 [P] Verify backend builds successfully with `JAVA_HOME=... ./mvnw clean compile`
- [ ] T004 [P] Verify frontend builds successfully with `npx ng build`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Create DTOs and repository methods that ALL user stories depend on

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

### Backend DTOs

- [ ] T005 [P] Create `SkillDTO.java` in `backend/src/main/java/com/atlas/dto/` with fields: id, description, towerDescription
- [ ] T006 [P] Create `EmployeeSkillDTO.java` in `backend/src/main/java/com/atlas/dto/` with fields: id, skillId, skillDescription, skillLevel, skillGrade
- [ ] T007 [P] Create `AddSkillRequest.java` in `backend/src/main/java/com/atlas/dto/` with @NotNull validations for skillId, skillLevel, skillGrade

### Backend Repositories

- [ ] T008 [P] Add `findByEmployeeId(Long employeeId)` method to `EmployeeSkillRepository` in `backend/src/main/java/com/atlas/repository/EmployeeSkillRepository.java`
- [ ] T009 [P] Add `findByEmployeeIdAndSkillId(Long employeeId, Integer skillId)` method to `EmployeeSkillRepository` in `backend/src/main/java/com/atlas/repository/EmployeeSkillRepository.java`
- [ ] T010 [P] Add `existsByEmployeeIdAndSkillId(Long employeeId, Integer skillId)` method to `EmployeeSkillRepository` in `backend/src/main/java/com/atlas/repository/EmployeeSkillRepository.java`
- [ ] T011 [P] Add `findAvailableSkillsForEmployee(Long employeeId)` query method to `SkillRepository` in `backend/src/main/java/com/atlas/repository/SkillRepository.java` using @Query with NOT IN subquery

### Backend Service

- [ ] T012 Create `EmployeeSkillService.java` in `backend/src/main/java/com/atlas/service/` with constructor dependencies: EmployeeRepository, SkillRepository, EmployeeSkillRepository, EmployeeService
- [ ] T013 [P] Implement `validateAccess(Long employeeId, User currentUser)` private method in `EmployeeSkillService` using `EmployeeService.getAccessibleEmployeeIds()`
- [ ] T014 [P] Implement `toDTO(EmployeeSkill employeeSkill)` and `toSkillDTO(Skill skill)` private helper methods in `EmployeeSkillService`

### Backend Controller

- [ ] T015 Create `EmployeeSkillController.java` in `backend/src/main/java/com/atlas/controller/` with @RestController, @RequestMapping("/api/employees/{employeeId}/skills"), and @RequiredArgsConstructor annotations

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - View Employee Skills Edit Option (Priority: P1) 🎯 MVP

**Goal**: Add pencil icon to employees page that opens an edit modal when clicked

**Independent Test**: View employees page, verify pencil icon appears next to each employee, click it to open edit modal with employee name

### Backend for User Story 1

- [ ] T016 [US1] Implement `getEmployeeSkills(Long employeeId, User currentUser)` method in `EmployeeSkillService` in `backend/src/main/java/com/atlas/service/EmployeeSkillService.java`
- [ ] T017 [US1] Add GET endpoint `/api/employees/{employeeId}/skills` in `EmployeeSkillController` in `backend/src/main/java/com/atlas/controller/EmployeeSkillController.java`
- [ ] T018 [P] [US1] Write service test for `getEmployeeSkills()` in `backend/src/test/java/com/atlas/service/EmployeeSkillServiceTest.java` covering ABAC access checks and skill retrieval
- [ ] T019 [P] [US1] Write controller test for GET endpoint in `backend/src/test/java/com/atlas/controller/EmployeeSkillControllerTest.java` covering 200, 403, 404 responses

### Frontend for User Story 1

- [ ] T020 [US1] Add `showEditSkillsModal` signal in `employees.component.ts` in `frontend/src/app/pages/employees/employees.component.ts`
- [ ] T021 [US1] Add `editingEmployee` signal in `employees.component.ts` in `frontend/src/app/pages/employees/employees.component.ts`
- [ ] T022 [US1] Add `assignedSkills` signal in `employees.component.ts` in `frontend/src/app/pages/employees/employees.component.ts`
- [ ] T023 [US1] Implement `openEditSkillsModal(employee: Employee)` method in `employees.component.ts` in `frontend/src/app/pages/employees/employees.component.ts`
- [ ] T024 [US1] Implement `closeEditSkillsModal()` method with unsaved changes warning in `employees.component.ts` in `frontend/src/app/pages/employees/employees.component.ts`
- [ ] T025 [P] [US1] Add `getEmployeeSkills(employeeId: number)` method to `api.service.ts` in `frontend/src/app/services/api.service.ts`
- [ ] T026 [US1] Add pencil icon button to actions column in employees table template in `frontend/src/app/pages/employees/employees.component.html` (after eye icon)
- [ ] T027 [US1] Add edit modal structure with header, close button, and employee name display in `frontend/src/app/pages/employees/employees.component.html`
- [ ] T028 [P] [US1] Add modal overlay and content styles (reuse from projects page pattern) in component styles or `frontend/src/styles/components.css`

**Checkpoint**: At this point, User Story 1 should be fully functional - pencil icon appears, modal opens with employee name and loads current skills

---

## Phase 4: User Story 2 - View and Remove Employee Skills (Priority: P1)

**Goal**: Display current skills in edit modal with remove buttons, persist deletions to backend

**Independent Test**: Open edit dialog for employee with skills, verify all skills display with remove buttons, click remove on a skill, save, confirm skill is deleted from employee profile

### Backend for User Story 2

- [ ] T029 [US2] Implement `removeSkillFromEmployee(Long employeeId, Integer skillId, User currentUser)` method in `EmployeeSkillService` in `backend/src/main/java/com/atlas/service/EmployeeSkillService.java`
- [ ] T030 [US2] Add DELETE endpoint `/api/employees/{employeeId}/skills/{skillId}` in `EmployeeSkillController` in `backend/src/main/java/com/atlas/controller/EmployeeSkillController.java`
- [ ] T031 [P] [US2] Write service test for `removeSkillFromEmployee()` in `backend/src/test/java/com/atlas/service/EmployeeSkillServiceTest.java` covering ABAC, not found scenarios
- [ ] T032 [P] [US2] Write controller test for DELETE endpoint in `backend/src/test/java/com/atlas/controller/EmployeeSkillControllerTest.java` covering 204, 403, 404 responses

### Frontend for User Story 2

- [ ] T033 [US2] Add `hasUnsavedChanges` computed signal based on pending changes in `employees.component.ts` in `frontend/src/app/pages/employees/employees.component.ts`
- [ ] T034 [US2] Implement `removeSkillFromEmployee(skillId: number)` method with optimistic update in `employees.component.ts` in `frontend/src/app/pages/employees/employees.component.ts`
- [ ] T035 [P] [US2] Add `removeSkillFromEmployee(employeeId: number, skillId: number)` method to `api.service.ts` in `frontend/src/app/services/api.service.ts`
- [ ] T036 [US2] Add skill chips display section in edit modal template in `frontend/src/app/pages/employees/employees.component.html` with skill name, level, grade, and remove button
- [ ] T037 [P] [US2] Add skill chip styles with skill name, metadata (level/grade), and remove button in component styles or `frontend/src/styles/components.css`
- [ ] T038 [US2] Update `closeEditSkillsModal()` to check `hasUnsavedChanges()` and show confirmation dialog if true in `employees.component.ts` in `frontend/src/app/pages/employees/employees.component.ts`

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently - can view and remove skills

---

## Phase 5: User Story 3 - Add Existing Skills to Employee (Priority: P2)

**Goal**: Provide dropdown with searchable skill list, level and grade selectors, persist additions to backend

**Independent Test**: Open edit dialog, search/select skill from dropdown, select level and grade, save, verify skill appears in employee profile with correct level/grade

### Backend for User Story 3

- [ ] T039 [US3] Implement `getAvailableSkills(Long employeeId, User currentUser)` method in `EmployeeSkillService` in `backend/src/main/java/com/atlas/service/EmployeeSkillService.java`
- [ ] T040 [US3] Implement `addSkillToEmployee(Long employeeId, AddSkillRequest request, User currentUser)` method in `EmployeeSkillService` in `backend/src/main/java/com/atlas/service/EmployeeSkillService.java` with duplicate check and validation
- [ ] T041 [US3] Add GET endpoint `/api/employees/{employeeId}/skills/available` in `EmployeeSkillController` in `backend/src/main/java/com/atlas/controller/EmployeeSkillController.java`
- [ ] T042 [US3] Add POST endpoint `/api/employees/{employeeId}/skills` in `EmployeeSkillController` in `backend/src/main/java/com/atlas/controller/EmployeeSkillController.java`
- [ ] T043 [P] [US3] Write service test for `getAvailableSkills()` in `backend/src/test/java/com/atlas/service/EmployeeSkillServiceTest.java` verifying assigned skills are excluded
- [ ] T044 [P] [US3] Write service test for `addSkillToEmployee()` in `backend/src/test/java/com/atlas/service/EmployeeSkillServiceTest.java` covering ABAC, duplicate detection, validation
- [ ] T045 [P] [US3] Write controller test for GET available skills endpoint in `backend/src/test/java/com/atlas/controller/EmployeeSkillControllerTest.java`
- [ ] T046 [P] [US3] Write controller test for POST endpoint in `backend/src/test/java/com/atlas/controller/EmployeeSkillControllerTest.java` covering 201, 400, 403, 404 responses

### Frontend for User Story 3

- [ ] T047 [US3] Add `availableSkills` signal in `employees.component.ts` in `frontend/src/app/pages/employees/employees.component.ts`
- [ ] T048 [US3] Add `skillSearchText` signal in `employees.component.ts` in `frontend/src/app/pages/employees/employees.component.ts`
- [ ] T049 [US3] Add `selectedSkillId` signal in `employees.component.ts` in `frontend/src/app/pages/employees/employees.component.ts`
- [ ] T050 [US3] Add `selectedSkillLevel` signal in `employees.component.ts` in `frontend/src/app/pages/employees/employees.component.ts`
- [ ] T051 [US3] Add `selectedSkillGrade` signal in `employees.component.ts` in `frontend/src/app/pages/employees/employees.component.ts`
- [ ] T052 [US3] Add `filteredSkills` computed signal that filters `availableSkills` by `skillSearchText` in `employees.component.ts` in `frontend/src/app/pages/employees/employees.component.ts`
- [ ] T053 [US3] Implement `loadAvailableSkills(employeeId: number)` method in `employees.component.ts` in `frontend/src/app/pages/employees/employees.component.ts`
- [ ] T054 [US3] Implement `addSkillToEmployee()` method with validation and optimistic update in `employees.component.ts` in `frontend/src/app/pages/employees/employees.component.ts`
- [ ] T055 [P] [US3] Add `getAvailableSkills(employeeId: number)` method to `api.service.ts` in `frontend/src/app/services/api.service.ts`
- [ ] T056 [P] [US3] Add `addSkillToEmployee(employeeId: number, request: AddSkillRequest)` method to `api.service.ts` in `frontend/src/app/services/api.service.ts`
- [ ] T057 [US3] Add "Add Skill" section in edit modal template with searchable skill input in `frontend/src/app/pages/employees/employees.component.html`
- [ ] T058 [US3] Add skill level dropdown (PRIMARY/SECONDARY) in edit modal template in `frontend/src/app/pages/employees/employees.component.html`
- [ ] T059 [US3] Add skill grade dropdown (ADVANCED/INTERMEDIATE/BEGINNER) in edit modal template in `frontend/src/app/pages/employees/employees.component.html`
- [ ] T060 [US3] Add "Add Skill" button with disabled state when skill/level/grade not all selected in edit modal template in `frontend/src/app/pages/employees/employees.component.html`
- [ ] T061 [P] [US3] Add searchable dropdown styles (dropdown-list, dropdown-item) matching manager filter pattern in component styles or `frontend/src/styles/components.css`

**Checkpoint**: At this point, User Stories 1, 2, AND 3 should all work independently - can view, add, and remove skills

---

## Phase 6: User Story 4 - Combined Add and Remove Operations (Priority: P2)

**Goal**: Enable multiple add/remove operations in single session before saving

**Independent Test**: Open edit dialog, remove 2 skills and add 3 skills, save once, verify all 5 changes applied correctly

### Implementation for User Story 4

- [ ] T062 [US4] Update `addSkillToEmployee()` to immediately call API instead of batching in `employees.component.ts` in `frontend/src/app/pages/employees/employees.component.ts` (already implemented in US3, verify behavior)
- [ ] T063 [US4] Update `removeSkillFromEmployee()` to immediately call API instead of batching in `employees.component.ts` in `frontend/src/app/pages/employees/employees.component.ts` (already implemented in US2, verify behavior)
- [ ] T064 [US4] Update `closeEditSkillsModal()` to reset all state signals including skill selections in `employees.component.ts` in `frontend/src/app/pages/employees/employees.component.ts`
- [ ] T065 [US4] Verify unsaved changes warning triggers after any add or remove operation in `employees.component.ts` in `frontend/src/app/pages/employees/employees.component.ts`

**Note**: User Story 4 is largely achieved by the implementations in US2 and US3. This phase validates the combined behavior.

**Checkpoint**: All user stories should now work together - multiple operations in single session

---

## Phase 7: User Story 5 - Error Handling and Validation (Priority: P3)

**Goal**: Display clear error messages for network failures, validation errors, and permission issues

**Independent Test**: Simulate network offline, try to save skills, verify error message displays; try to edit employee without permission, verify pencil icon not shown

### Implementation for User Story 5

- [ ] T066 [P] [US5] Add error toast/notification component or service to `frontend/src/app/` (if not already exists)
- [ ] T067 [US5] Add error handling in `addSkillToEmployee()` API call with user-friendly error message display in `employees.component.ts` in `frontend/src/app/pages/employees/employees.component.ts`
- [ ] T068 [US5] Add error handling in `removeSkillFromEmployee()` API call with user-friendly error message display in `employees.component.ts` in `frontend/src/app/pages/employees/employees.component.ts`
- [ ] T069 [US5] Add error handling in `getEmployeeSkills()` API call with user-friendly error message display in `employees.component.ts` in `frontend/src/app/pages/employees/employees.component.ts`
- [ ] T070 [US5] Add error handling in `getAvailableSkills()` API call with user-friendly error message display in `employees.component.ts` in `frontend/src/app/pages/employees/employees.component.ts`
- [ ] T071 [US5] Add loading state signal `loadingSkills` and disable modal interactions while loading in `employees.component.ts` in `frontend/src/app/pages/employees/employees.component.ts`
- [ ] T072 [US5] Add ABAC filtering to hide pencil icon for employees outside user's access in employees table template in `frontend/src/app/pages/employees/employees.component.html`
- [ ] T073 [P] [US5] Add loading spinner styles for modal in component styles or `frontend/src/styles/components.css`

**Checkpoint**: All error scenarios handled gracefully with clear user feedback

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [ ] T074 [P] Run all backend tests with `JAVA_HOME=... ./mvnw test` and ensure 100% pass rate
- [ ] T075 [P] Test manual scenarios from `specs/001-edit-employee-skills/quickstart.md` checklist
- [ ] T076 [P] Verify ABAC access control works correctly: login as different users, verify pencil icon visibility
- [ ] T077 [P] Verify performance: modal opens < 2 seconds, operations complete < 30 seconds
- [ ] T078 [P] Add JSDoc comments to new frontend methods in `employees.component.ts`
- [ ] T079 [P] Add Javadoc comments to new backend service methods in `EmployeeSkillService.java`
- [ ] T080 Code review: verify constitution compliance (ABAC, DB-first, no in-memory filtering)
- [ ] T081 [P] Update `specs/001-edit-employee-skills/README.md` if needed with implementation notes
- [ ] T082 Test accessibility: keyboard navigation (Tab, Enter, ESC), screen reader support
- [ ] T083 [P] Verify edge cases: empty skills, no available skills, hundreds of skills (search filter)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3-7)**: All depend on Foundational phase completion
  - User Story 1 (Phase 3): Can start after Foundational - No dependencies on other stories
  - User Story 2 (Phase 4): Can start after Foundational - Depends on US1 for modal structure, but testable independently
  - User Story 3 (Phase 5): Can start after Foundational - Depends on US1 for modal structure, but testable independently
  - User Story 4 (Phase 6): Depends on US2 and US3 being complete
  - User Story 5 (Phase 7): Can enhance any completed story
- **Polish (Phase 8)**: Depends on all user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Entry point - pencil icon and modal shell ✅ Can start after Foundational
- **User Story 2 (P1)**: Remove skills - shares modal from US1 ✅ Can start after Foundational
- **User Story 3 (P2)**: Add skills - shares modal from US1 ✅ Can start after Foundational
- **User Story 4 (P2)**: Combined operations - validates US2 + US3 work together ⚠️ Requires US2 and US3
- **User Story 5 (P3)**: Error handling - can enhance any story ✅ Can enhance stories incrementally

### Within Each User Story

- Backend tests before backend implementation (TDD approach)
- DTOs and repositories (Foundational phase) before services
- Services before controllers
- Controllers before frontend components
- API service methods before component methods
- Component logic before template changes
- Template structure before styles

### Parallel Opportunities

**Setup Phase (Phase 1)**:
- T003 and T004 can run in parallel (backend vs frontend builds)

**Foundational Phase (Phase 2)**:
- T005, T006, T007 can run in parallel (different DTO files)
- T008, T009, T010, T011 can run in parallel (different repository methods)
- T013, T014 can run in parallel (different service methods)

**User Story 1 (Phase 3)**:
- T018 and T019 can run in parallel (different test files)
- T025 and T028 can run in parallel (API service vs styles)

**User Story 2 (Phase 4)**:
- T031 and T032 can run in parallel (different test files)
- T035 and T037 can run in parallel (API service vs styles)

**User Story 3 (Phase 5)**:
- T043, T044, T045, T046 can run in parallel (different test files)
- T055, T056, T061 can run in parallel (API service and styles)

**User Story 5 (Phase 7)**:
- T066 and T073 can run in parallel (error component vs styles)
- T067, T068, T069, T070 can run in parallel (different error handlers)

**Polish Phase (Phase 8)**:
- T074, T075, T076, T077, T078, T079, T081, T083 can all run in parallel (different validation activities)

---

## Parallel Example: User Story 3 (Add Skills)

```bash
# Launch all tests for User Story 3 together:
Task: "Write service test for getAvailableSkills() in EmployeeSkillServiceTest.java"
Task: "Write service test for addSkillToEmployee() in EmployeeSkillServiceTest.java"
Task: "Write controller test for GET available skills in EmployeeSkillControllerTest.java"
Task: "Write controller test for POST add skill in EmployeeSkillControllerTest.java"

# Launch frontend API and styles together:
Task: "Add getAvailableSkills() to api.service.ts"
Task: "Add addSkillToEmployee() to api.service.ts"
Task: "Add searchable dropdown styles in components.css"
```

---

## Implementation Strategy

### MVP First (User Stories 1 + 2 Only)

1. Complete Phase 1: Setup → ✅ Environment ready
2. Complete Phase 2: Foundational → ✅ DTOs, repositories, service shell ready
3. Complete Phase 3: User Story 1 → ✅ Pencil icon + modal opens
4. Complete Phase 4: User Story 2 → ✅ View and remove skills works
5. **STOP and VALIDATE**: Test US1+US2 independently (pencil icon → modal → remove skill → save)
6. Deploy/demo if ready (minimal viable feature)

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Deploy/Demo (can see edit modal)
3. Add User Story 2 → Test independently → Deploy/Demo (MVP! can remove skills)
4. Add User Story 3 → Test independently → Deploy/Demo (full feature! can add skills)
5. Add User Story 4 → Test combined operations → Deploy/Demo (efficiency improvement)
6. Add User Story 5 → Test error scenarios → Deploy/Demo (polish)
7. Complete Polish phase → Final validation → Production release

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together (critical path)
2. Once Foundational is done:
   - Developer A: User Story 1 (pencil icon + modal) + User Story 2 (remove skills)
   - Developer B: User Story 3 (add skills) + prepare backend tests
   - Developer C: User Story 5 (error handling) across all stories
3. Developer A completes US1+US2 → MVP demo ready
4. Developer B adds US3 → Full feature demo ready
5. All integrate for US4 validation
6. Team completes Polish phase together

---

## Notes

- [P] tasks = different files, no dependencies, can run in parallel
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Backend tests use JUnit 5, follow TDD approach (write test, fail, implement, pass)
- Frontend tests omitted per project conventions (manual testing via quickstart.md)
- Commit after each task or logical group (e.g., all DTOs together)
- Stop at any checkpoint to validate story independently
- Constitution compliance: ABAC checks in every endpoint, JPA queries (no in-memory filtering), @Enumerated(EnumType.STRING) for enums
- Avoid: vague tasks, same file conflicts, cross-story dependencies that break independence

---

## Task Summary

**Total Tasks**: 83
- Setup (Phase 1): 4 tasks
- Foundational (Phase 2): 11 tasks
- User Story 1 (Phase 3): 13 tasks (4 backend, 9 frontend)
- User Story 2 (Phase 4): 10 tasks (4 backend, 6 frontend)
- User Story 3 (Phase 5): 23 tasks (8 backend, 15 frontend)
- User Story 4 (Phase 6): 4 tasks (validation)
- User Story 5 (Phase 7): 8 tasks (error handling)
- Polish (Phase 8): 10 tasks (validation & documentation)

**Parallel Opportunities**: 35 tasks marked [P] can run in parallel within their phases

**MVP Scope** (Recommended): Phases 1-4 (US1 + US2) = 38 tasks → Can view and remove skills
**Full Feature** (Recommended): Phases 1-5 (US1 + US2 + US3) = 61 tasks → Can add and remove skills with level/grade
**Production Ready**: All phases = 83 tasks → Full feature with error handling and polish
