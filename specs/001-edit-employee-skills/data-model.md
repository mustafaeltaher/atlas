# Data Model: Employee Skills Management - Edit Skills

**Feature**: 001-edit-employee-skills
**Date**: 2026-02-20

## Overview

This feature uses existing database schema with no migrations required. All entities, relationships, and constraints already exist in the system.

---

## Entity Diagram

```
┌─────────────────┐         ┌──────────────────┐         ┌─────────────────┐
│    Employee     │         │  EmployeeSkill   │         │     Skill       │
├─────────────────┤         ├──────────────────┤         ├─────────────────┤
│ id (PK)         │◄───────┤ employee_id (FK) │         │ id (PK)         │
│ oracle_id       │         │ skill_id (FK)────┼────────►│ description     │
│ name            │         │ skill_level      │         │ tower_id (FK)   │
│ email           │         │ skill_grade      │         └─────────────────┘
│ ...             │         │ id (PK)          │
└─────────────────┘         └──────────────────┘
```

---

## Entities

### Employee (Existing)

**Table**: `employees`

**Purpose**: Represents a person in the organization.

**Key Attributes**:
- `id` (BIGINT, PK): Unique identifier
- `oracle_id` (INTEGER, UNIQUE): External system ID
- `name` (VARCHAR, NOT NULL): Full name
- `email` (VARCHAR): Email address
- `manager_id` (BIGINT, FK → employees.id): Manager for ABAC hierarchy
- `resignation_date` (DATE): If set, employee is RESIGNED status

**Relationships**:
- 1:N to EmployeeSkill (one employee has many skills)
- Self-referential N:1 to Employee (manager hierarchy)

**Constraints**:
- `oracle_id` must be unique
- `name` cannot be NULL

**Notes**:
- No changes to this entity for this feature
- ABAC access controlled via `manager_id` hierarchy

---

### Skill (Existing)

**Table**: `skills`

**Purpose**: Represents a defined skill/competency in the system.

**Key Attributes**:
- `id` (INTEGER, PK): Unique identifier
- `description` (VARCHAR, NOT NULL): Skill name (e.g., "Java", "Python", "AWS")
- `tower_id` (INTEGER, FK → tech_towers.id): Associated tech tower/domain

**Relationships**:
- 1:N to EmployeeSkill (one skill can be assigned to many employees)
- N:1 to TechTower (skill belongs to a tech domain)

**Constraints**:
- `description` should be unique (assumed, verify in implementation)
- `tower_id` references `tech_towers.id`

**Notes**:
- No changes to this entity for this feature
- Skills are **NOT created** through this feature (per spec constraints)
- Existing skills are fetched and displayed in dropdown

---

### EmployeeSkill (Existing - Junction Table)

**Table**: `employees_skills`

**Purpose**: Many-to-many relationship between Employee and Skill with additional attributes.

**Key Attributes**:
- `id` (INTEGER, PK): Unique identifier for the association
- `employee_id` (BIGINT, FK → employees.id, NOT NULL): Employee reference
- `skill_id` (INTEGER, FK → skills.id, NOT NULL): Skill reference
- `skill_level` (VARCHAR, ENUM, NULLABLE): PRIMARY | SECONDARY
- `skill_grade` (VARCHAR, ENUM, NULLABLE): ADVANCED | INTERMEDIATE | BEGINNER

**Relationships**:
- N:1 to Employee (many skill associations per employee)
- N:1 to Skill (many employee associations per skill)

**Constraints**:
- `UNIQUE(employee_id, skill_id)`: An employee cannot have the same skill assigned twice
- `employee_id` NOT NULL
- `skill_id` NOT NULL
- `skill_level` can be NULL in database (for backwards compatibility) but REQUIRED when adding via UI
- `skill_grade` can be NULL in database (for backwards compatibility) but REQUIRED when adding via UI

**Notes**:
- This is the **primary entity modified** by this feature
- Records are **created** when adding a skill to an employee
- Records are **deleted** when removing a skill from an employee
- `skill_level` and `skill_grade` are **user-selected** when adding a skill (per research.md decision #1)
- Existing NULL values in database are preserved for backwards compatibility

---

## Validation Rules

### Add Skill Operation

**Pre-conditions**:
- Employee with `employee_id` must exist
- Skill with `skill_id` must exist
- Current user must have access to `employee_id` (ABAC check via manager hierarchy)
- No existing EmployeeSkill record with same `(employee_id, skill_id)` pair

**Validation Checks**:
1. **Employee Exists**: `SELECT id FROM employees WHERE id = :employeeId` → must return 1 row
2. **Skill Exists**: `SELECT id FROM skills WHERE id = :skillId` → must return 1 row
3. **ABAC Access**: `employeeId IN getAccessibleEmployeeIds(currentUser)` → must be true
4. **No Duplicate**: `SELECT COUNT(*) FROM employees_skills WHERE employee_id = :employeeId AND skill_id = :skillId` → must be 0

**Post-conditions**:
- New EmployeeSkill record created with:
  - `employee_id` = provided employeeId
  - `skill_id` = provided skillId
  - `skill_level` = NULL (or provided value if future enhancement)
  - `skill_grade` = NULL (or provided value if future enhancement)

**Error Cases**:
- `404 Not Found`: Employee or Skill doesn't exist
- `403 Forbidden`: User lacks access to employee (ABAC violation)
- `400 Bad Request`: Duplicate skill already assigned
- `500 Internal Server Error`: Database constraint violation

---

### Remove Skill Operation

**Pre-conditions**:
- EmployeeSkill record with `(employee_id, skill_id)` must exist
- Current user must have access to `employee_id` (ABAC check)

**Validation Checks**:
1. **EmployeeSkill Exists**: `SELECT id FROM employees_skills WHERE employee_id = :employeeId AND skill_id = :skillId` → must return 1 row
2. **ABAC Access**: `employeeId IN getAccessibleEmployeeIds(currentUser)` → must be true

**Post-conditions**:
- EmployeeSkill record deleted (hard delete, not soft delete)
- No orphaned records (cascade handled by ORM)

**Error Cases**:
- `404 Not Found`: EmployeeSkill association doesn't exist
- `403 Forbidden`: User lacks access to employee (ABAC violation)
- `500 Internal Server Error`: Database error

---

### Get Employee Skills Operation

**Pre-conditions**:
- Employee with `employee_id` must exist
- Current user must have access to `employee_id` (ABAC check)

**Query**:
```sql
SELECT es.id, es.skill_id, s.description, es.skill_level, es.skill_grade
FROM employees_skills es
JOIN skills s ON es.skill_id = s.id
WHERE es.employee_id = :employeeId
ORDER BY s.description ASC
```

**Post-conditions**:
- Returns list of EmployeeSkillDTO objects
- Empty list if employee has no skills

**Error Cases**:
- `404 Not Found`: Employee doesn't exist
- `403 Forbidden`: User lacks access to employee

---

### Get Available Skills Operation

**Pre-conditions**:
- Employee with `employee_id` must exist
- Current user must have access to `employee_id` (ABAC check)

**Query**:
```sql
SELECT s.id, s.description, t.description AS tower_description
FROM skills s
LEFT JOIN tech_towers t ON s.tower_id = t.id
WHERE s.id NOT IN (
    SELECT skill_id
    FROM employees_skills
    WHERE employee_id = :employeeId
)
ORDER BY s.description ASC
```

**Post-conditions**:
- Returns list of SkillDTO objects (skills NOT currently assigned)
- Empty list if all skills are already assigned

**Error Cases**:
- `404 Not Found`: Employee doesn't exist
- `403 Forbidden`: User lacks access to employee

---

## State Transitions

### Skill Association Lifecycle

```
┌─────────────┐
│   UNASSIGNED│  ← Initial state (skill exists, not assigned to employee)
└─────┬───────┘
      │
      │ ADD operation (POST /api/employees/{id}/skills)
      ▼
┌─────────────┐
│   ASSIGNED  │  ← EmployeeSkill record exists
└─────┬───────┘
      │
      │ REMOVE operation (DELETE /api/employees/{id}/skills/{skillId})
      ▼
┌─────────────┐
│   UNASSIGNED│  ← EmployeeSkill record deleted, skill still exists in system
└─────────────┘
```

**Notes**:
- Skill entity itself is never deleted
- Only the association (EmployeeSkill) is created/deleted
- Skills can be reassigned after removal

---

## Data Transfer Objects (DTOs)

### SkillDTO (Response)

**Purpose**: Represents a skill for display in dropdown/list

**Fields**:
```java
{
  "id": Integer,              // Skill ID
  "description": String,      // Skill name (e.g., "Java Programming")
  "towerDescription": String  // Tech tower name (e.g., "Backend Development")
}
```

**Example**:
```json
{
  "id": 42,
  "description": "Java Programming",
  "towerDescription": "Backend Development"
}
```

---

### EmployeeSkillDTO (Response)

**Purpose**: Represents an assigned skill with association details

**Fields**:
```java
{
  "id": Integer,              // EmployeeSkill association ID
  "skillId": Integer,         // Skill ID
  "skillDescription": String, // Skill name for display
  "skillLevel": String,       // "PRIMARY" | "SECONDARY" | null
  "skillGrade": String        // "ADVANCED" | "INTERMEDIATE" | "BEGINNER" | null
}
```

**Example**:
```json
{
  "id": 123,
  "skillId": 42,
  "skillDescription": "Java Programming",
  "skillLevel": "PRIMARY",
  "skillGrade": "ADVANCED"
}
```

---

### AddSkillRequest (Request)

**Purpose**: Request body for adding a skill to an employee

**Fields**:
```java
{
  "skillId": Integer,         // REQUIRED: Skill to assign
  "skillLevel": String,       // REQUIRED: "PRIMARY" | "SECONDARY"
  "skillGrade": String        // REQUIRED: "ADVANCED" | "INTERMEDIATE" | "BEGINNER"
}
```

**Validation**:
- `skillId`: REQUIRED, NOT NULL, must reference existing skill
- `skillLevel`: REQUIRED, NOT NULL, must be "PRIMARY" or "SECONDARY"
- `skillGrade`: REQUIRED, NOT NULL, must be "ADVANCED", "INTERMEDIATE", or "BEGINNER"

**Example**:
```json
{
  "skillId": 42,
  "skillLevel": "SECONDARY",
  "skillGrade": "INTERMEDIATE"
}
```

---

## Database Indexes

### Existing Indexes (Assumed)

Based on typical JPA conventions:

```sql
-- Primary keys (auto-indexed)
CREATE INDEX idx_employees_pk ON employees(id);
CREATE INDEX idx_skills_pk ON skills(id);
CREATE INDEX idx_employees_skills_pk ON employees_skills(id);

-- Foreign keys (should be indexed for performance)
CREATE INDEX idx_employees_skills_employee_id ON employees_skills(employee_id);
CREATE INDEX idx_employees_skills_skill_id ON employees_skills(skill_id);
CREATE INDEX idx_skills_tower_id ON skills(tower_id);

-- Unique constraint index
CREATE UNIQUE INDEX idx_employees_skills_unique ON employees_skills(employee_id, skill_id);
```

### Recommendations

**No new indexes needed** for this feature. Existing indexes support:
- Fast lookup of skills by employee: `idx_employees_skills_employee_id`
- Fast duplicate check: `idx_employees_skills_unique`
- Fast skill filtering: `idx_employees_skills_skill_id`

---

## Data Integrity

### Constraints

1. **Foreign Key Constraints**:
   - `employees_skills.employee_id` → `employees.id` (ON DELETE CASCADE assumed)
   - `employees_skills.skill_id` → `skills.id` (ON DELETE RESTRICT assumed)

2. **Unique Constraints**:
   - `employees_skills(employee_id, skill_id)`: Prevents duplicate skill assignments

3. **Not Null Constraints**:
   - `employees_skills.employee_id`: NOT NULL
   - `employees_skills.skill_id`: NOT NULL
   - `employees_skills.skill_level`: NULLABLE
   - `employees_skills.skill_grade`: NULLABLE

### Cascade Behavior

**When Employee is deleted**:
- All associated EmployeeSkill records should be deleted (CASCADE)
- Skills themselves remain in system (not affected)

**When Skill is deleted**:
- Deletion should be RESTRICTED if any EmployeeSkill associations exist
- Admin must first remove all employee assignments before deleting skill
- (Note: Skill deletion is out of scope for this feature)

---

## Performance Considerations

### Query Performance

**Get Employee Skills** (most frequent operation):
- Uses index: `idx_employees_skills_employee_id`
- Expected rows: 0-50 per employee (typical skill count)
- Join to skills table: uses `idx_skills_pk`
- **Performance**: < 10ms for typical dataset

**Get Available Skills** (second most frequent):
- Uses `NOT IN` subquery
- Subquery uses index: `idx_employees_skills_employee_id`
- Main query scans skills table (acceptable, < 500 total skills)
- **Performance**: < 50ms for typical dataset

**Add Skill** (write operation):
- Duplicate check uses: `idx_employees_skills_unique`
- Insert uses primary key generation
- **Performance**: < 20ms

**Remove Skill** (write operation):
- Lookup uses: `idx_employees_skills_employee_id` + `idx_employees_skills_skill_id`
- Delete by primary key
- **Performance**: < 20ms

### Scalability

**Assumptions**:
- Average 10-20 skills per employee
- Maximum 50 skills per employee
- 500 total skills in system
- 10,000 employees

**Projected Data Volume**:
- EmployeeSkill records: 10,000 employees × 15 avg skills = **150,000 records**
- Skill records: **500 records**
- Query response times remain acceptable at this scale with proper indexing

---

## Migration Strategy

### Database Changes

**Required**: NONE

This feature uses 100% existing schema. No migrations needed.

### Data Changes

**Required**: NONE

No data migration, seeding, or cleanup required.

---

## Testing Data Requirements

### Test Fixtures

**Employees**:
```sql
INSERT INTO employees (id, oracle_id, name, email, manager_id) VALUES
(1, 1001, 'Alice Manager', 'alice@atlas.com', NULL),  -- Top-level manager
(2, 1002, 'Bob Employee', 'bob@atlas.com', 1),        -- Reports to Alice
(3, 1003, 'Charlie Employee', 'charlie@atlas.com', 1); -- Reports to Alice
```

**Skills**:
```sql
INSERT INTO skills (id, description, tower_id) VALUES
(1, 'Java Programming', 1),
(2, 'Python Programming', 1),
(3, 'AWS Cloud', 2),
(4, 'React.js', 3),
(5, 'Angular', 3);
```

**EmployeeSkills** (initial state):
```sql
INSERT INTO employees_skills (id, employee_id, skill_id, skill_level, skill_grade) VALUES
(1, 2, 1, NULL, NULL),  -- Bob has Java
(2, 2, 4, NULL, NULL);  -- Bob has React
-- Charlie has no skills (can test adding first skill)
```

### Test Scenarios

1. **Add First Skill**: Charlie (no existing skills) adds Python
2. **Add Additional Skill**: Bob (has Java, React) adds AWS
3. **Remove Only Skill**: Create employee with 1 skill, remove it
4. **Remove One of Many**: Bob removes React (keeps Java)
5. **Add Duplicate**: Try to add Java to Bob (should fail with 400)
6. **ABAC Violation**: User without access tries to edit skills (should fail with 403)

---

**Status**: ✅ Data model complete - no schema changes needed - ready for implementation
