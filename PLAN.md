# Implementation Plan - 8 Features

## Phase 1: Backend Changes

### Step 1: Add RBAC-filtered managers endpoint (shared by features 2 & 5)
**Files:**
- `backend/src/main/java/com/atlas/repository/EmployeeRepository.java` — Add query to find employees who are managers (have reports)
- `backend/src/main/java/com/atlas/controller/EmployeeController.java` — Add `GET /api/employees/managers` endpoint that returns RBAC-filtered manager list
- `backend/src/main/java/com/atlas/service/EmployeeService.java` — Add `getAccessibleManagers(User)` method:
  - SYSTEM_ADMIN/EXECUTIVE: all managers
  - HEAD: managers in user's parentTower
  - DEPARTMENT_MANAGER: managers in user's tower
  - TEAM_LEAD: only themselves

### Step 2: Add employeeOracleId to AllocationDTO (feature 6)
**Files:**
- `backend/src/main/java/com/atlas/dto/AllocationDTO.java` — Add `employeeOracleId` field
- `backend/src/main/java/com/atlas/service/AllocationService.java` — Populate `employeeOracleId` in `toDTO()`

### Step 3: Add manager filter to employees endpoint (feature 2)
**Files:**
- `backend/src/main/java/com/atlas/controller/EmployeeController.java` — Add `managerId` query param to `GET /api/employees`
- `backend/src/main/java/com/atlas/service/EmployeeService.java` — Filter employees by managerId when provided

### Step 4: Add manager filter to allocations endpoint (feature 5)
**Files:**
- `backend/src/main/java/com/atlas/controller/AllocationController.java` — Add `managerId` query param to `GET /api/allocations`
- `backend/src/main/java/com/atlas/service/AllocationService.java` — Filter allocations by employee's managerId when provided

### Step 5: Restrict allocation update to percentage only (feature 8)
**Files:**
- `backend/src/main/java/com/atlas/service/AllocationService.java` — In `updateAllocation()`, only update the current month's allocation value (ignore other fields from the DTO)

---

## Phase 2: Frontend Changes

### Step 6: Frontend models and API service updates
**Files:**
- `frontend/src/app/models/index.ts` — Add `employeeOracleId` to Allocation interface, add `Manager` interface (id, name)
- `frontend/src/app/services/api.service.ts` — Add:
  - `getManagers()` method → `GET /api/employees/managers`
  - Update `getEmployees()` to accept `managerId` param
  - Update `getAllocations()` to accept `managerId` param

### Step 7: Employees page (features 1, 2, 3)
**File:** `frontend/src/app/pages/employees/employees.component.ts`
- Add Oracle ID column to table
- Add Manager column to table
- Add Manager filter dropdown (populated from `getManagers()`)
- Tower filter visibility: only show when user role is EXECUTIVE (N1)
- Pass managerId to API when filter is selected

### Step 8: Projects page (feature 4)
**File:** `frontend/src/app/pages/projects/projects.component.ts`
- Add edit modal with fields: status (dropdown), start date, end date
- Grid view: clicking a card opens edit modal
- List view: add pencil icon button that opens edit modal
- On save: call `apiService.updateProject(id, data)` then reload

### Step 9: Allocations page (features 5, 6, 7, 8)
**File:** `frontend/src/app/pages/allocations/allocations.component.ts`
- Add Oracle ID column to table
- Add Manager filter dropdown (same RBAC-filtered list from `getManagers()`)
- Add "Create Allocation" button + modal with: employee dropdown, project dropdown, allocation %, start date, end date, status
- Add edit button (pencil icon) per row → opens edit modal with only allocation % editable
- Pass managerId to API when filter is selected

---

## Phase 3: Docker rebuild
### Step 10: Rebuild and test
- `docker compose down && docker compose up --build -d`
