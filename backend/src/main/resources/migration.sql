-- ============================================================================
-- Atlas RBAC → ABAC Migration Script
-- Migrates from role-based access to hierarchy-based (manager chain) access
-- Also restructures entities: tech_towers, skills, allocations, projects
-- ============================================================================

BEGIN;

-- ============================================================================
-- PHASE 0: Create PostgreSQL enum types
-- ============================================================================

DO $$ BEGIN CREATE TYPE gender_type AS ENUM ('MALE', 'FEMALE'); EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN CREATE TYPE hiring_type AS ENUM ('FULL_TIME', 'PART_TIME'); EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN CREATE TYPE job_level_type AS ENUM ('ENTRY_LEVEL', 'MID_LEVEL', 'ADVANCED_MANAGER_LEVEL', 'EXECUTIVE_LEVEL'); EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN CREATE TYPE allocation_type AS ENUM ('PROJECT', 'PROSPECT', 'VACATION', 'MATERNITY'); EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN CREATE TYPE project_type AS ENUM ('PROJECT', 'OPPORTUNITY'); EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN CREATE TYPE project_status_type AS ENUM ('ACTIVE', 'COMPLETED', 'ON_HOLD'); EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN CREATE TYPE skill_level_type AS ENUM ('PRIMARY', 'SECONDARY'); EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN CREATE TYPE skill_grade_type AS ENUM ('ADVANCED', 'INTERMEDIATE', 'BEGINNER'); EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- ============================================================================
-- PHASE 1: Create new tables
-- ============================================================================

-- 1.1 tech_towers (replaces string-based parent_tower/tower)
CREATE TABLE IF NOT EXISTS tech_towers (
    id SERIAL PRIMARY KEY,
    description VARCHAR(255),
    parent_tower_id INTEGER REFERENCES tech_towers(id)
);

-- 1.2 skills (linked to tech_towers)
CREATE TABLE IF NOT EXISTS skills (
    id SERIAL PRIMARY KEY,
    description VARCHAR(255),
    tower_id INTEGER REFERENCES tech_towers(id)
);

-- 1.3 employees_skills (junction table with native enum types)
CREATE TABLE IF NOT EXISTS employees_skills (
    id SERIAL PRIMARY KEY,
    employee_id BIGINT REFERENCES employees(id),
    skill_id INTEGER REFERENCES skills(id),
    skill_level skill_level_type,
    skill_grade skill_grade_type
);

-- ============================================================================
-- PHASE 2: Populate tech_towers from existing string-based tower data
-- ============================================================================

-- Insert distinct parent towers as root towers (parent_tower_id = NULL)
INSERT INTO tech_towers (description)
SELECT DISTINCT parent_tower
FROM employees
WHERE parent_tower IS NOT NULL AND parent_tower != ''
ON CONFLICT DO NOTHING;

-- Insert distinct child towers, linking to their parent
INSERT INTO tech_towers (description, parent_tower_id)
SELECT DISTINCT e.tower, pt.id
FROM employees e
JOIN tech_towers pt ON pt.description = e.parent_tower AND pt.parent_tower_id IS NULL
WHERE e.tower IS NOT NULL AND e.tower != ''
AND NOT EXISTS (
    SELECT 1 FROM tech_towers t2
    WHERE t2.description = e.tower AND t2.parent_tower_id = pt.id
);

-- ============================================================================
-- PHASE 3: Populate skills from existing primary_skill/secondary_skill data
-- ============================================================================

-- Create skills from distinct skill names (no tower assignment for migrated data)
INSERT INTO skills (description)
SELECT DISTINCT skill_name FROM (
    SELECT DISTINCT primary_skill AS skill_name FROM employees WHERE primary_skill IS NOT NULL AND primary_skill != ''
    UNION
    SELECT DISTINCT secondary_skill AS skill_name FROM employees WHERE secondary_skill IS NOT NULL AND secondary_skill != ''
) AS all_skills
ON CONFLICT DO NOTHING;

-- Migrate primary skills to employees_skills
INSERT INTO employees_skills (employee_id, skill_id, skill_level, skill_grade)
SELECT e.id, s.id, 'PRIMARY'::skill_level_type, 'INTERMEDIATE'::skill_grade_type
FROM employees e
JOIN skills s ON s.description = e.primary_skill
WHERE e.primary_skill IS NOT NULL AND e.primary_skill != '';

-- Migrate secondary skills to employees_skills
INSERT INTO employees_skills (employee_id, skill_id, skill_level, skill_grade)
SELECT e.id, s.id, 'SECONDARY'::skill_level_type, 'INTERMEDIATE'::skill_grade_type
FROM employees e
JOIN skills s ON s.description = e.secondary_skill
WHERE e.secondary_skill IS NOT NULL AND e.secondary_skill != ''
AND NOT EXISTS (
    SELECT 1 FROM employees_skills es
    WHERE es.employee_id = e.id AND es.skill_id = s.id
);

-- ============================================================================
-- PHASE 4: Alter employees table
-- ============================================================================

-- Add temporary column for new tower FK
ALTER TABLE employees ADD COLUMN IF NOT EXISTS tower_id INTEGER;

-- Populate tower_id from existing tower string by matching against tech_towers
UPDATE employees e
SET tower_id = tt.id
FROM tech_towers tt
WHERE tt.description = e.tower
AND tt.parent_tower_id IS NOT NULL;

-- For employees whose tower matched a parent tower (no child), use that
UPDATE employees e
SET tower_id = tt.id
FROM tech_towers tt
WHERE tt.description = e.tower
AND tt.parent_tower_id IS NULL
AND e.tower_id IS NULL;

-- Change oracle_id from VARCHAR to INTEGER
ALTER TABLE employees ALTER COLUMN oracle_id TYPE INTEGER USING oracle_id::INTEGER;

-- Drop old columns
ALTER TABLE employees DROP COLUMN IF EXISTS primary_skill;
ALTER TABLE employees DROP COLUMN IF EXISTS secondary_skill;
ALTER TABLE employees DROP COLUMN IF EXISTS parent_tower;
ALTER TABLE employees DROP COLUMN IF EXISTS future_manager;
ALTER TABLE employees DROP COLUMN IF EXISTS is_active;
ALTER TABLE employees DROP COLUMN IF EXISTS status;

-- Drop old tower column and rename tower_id to tower
ALTER TABLE employees DROP COLUMN IF EXISTS tower;
ALTER TABLE employees RENAME COLUMN tower_id TO tower;

-- Add FK constraint for tower
ALTER TABLE employees ADD CONSTRAINT fk_employees_tower
    FOREIGN KEY (tower) REFERENCES tech_towers(id);

-- Convert enum columns from VARCHAR to native PostgreSQL enum types
ALTER TABLE employees ALTER COLUMN gender TYPE gender_type USING gender::gender_type;
ALTER TABLE employees ALTER COLUMN hiring_type TYPE hiring_type USING hiring_type::hiring_type;
ALTER TABLE employees ALTER COLUMN job_level TYPE job_level_type USING job_level::job_level_type;

-- ============================================================================
-- PHASE 5: Alter projects table
-- ============================================================================

-- Add new columns with native enum types
ALTER TABLE projects ADD COLUMN IF NOT EXISTS project_type_new project_type;
ALTER TABLE projects ADD COLUMN IF NOT EXISTS region VARCHAR(255);
ALTER TABLE projects ADD COLUMN IF NOT EXISTS vertical VARCHAR(255);

-- Migrate: set description from name if description is empty
UPDATE projects
SET description = name
WHERE (description IS NULL OR description = '') AND name IS NOT NULL;

-- Migrate project_type if old column exists
UPDATE projects SET project_type_new = 'PROJECT'::project_type WHERE project_type_new IS NULL;

-- Drop old columns
ALTER TABLE projects DROP COLUMN IF EXISTS name;
ALTER TABLE projects DROP COLUMN IF EXISTS parent_tower;
ALTER TABLE projects DROP COLUMN IF EXISTS tower;
ALTER TABLE projects DROP COLUMN IF EXISTS project_type;
ALTER TABLE projects RENAME COLUMN project_type_new TO project_type;

-- Convert status column to native enum type
ALTER TABLE projects ALTER COLUMN status TYPE project_status_type USING status::project_status_type;

-- ============================================================================
-- PHASE 6: Alter allocations table
-- ============================================================================

-- Add new allocation_type column with native enum type
ALTER TABLE allocations ADD COLUMN IF NOT EXISTS allocation_type allocation_type;

-- Migrate status to allocation_type
UPDATE allocations SET allocation_type = 'PROJECT'::allocation_type WHERE status = 'ACTIVE';
UPDATE allocations SET allocation_type = 'PROSPECT'::allocation_type WHERE status = 'PROSPECT';
UPDATE allocations SET allocation_type = 'PROJECT'::allocation_type WHERE allocation_type IS NULL;

-- Make project_id nullable (for Vacation/Maternity allocations)
ALTER TABLE allocations ALTER COLUMN project_id DROP NOT NULL;

-- Drop old status column
ALTER TABLE allocations DROP COLUMN IF EXISTS status;

-- ============================================================================
-- PHASE 7: Alter monthly_allocations table
-- ============================================================================

-- Change percentage from DOUBLE to INTEGER
ALTER TABLE monthly_allocations ALTER COLUMN percentage TYPE INTEGER USING (percentage * 100)::INTEGER;

-- ============================================================================
-- PHASE 8: Alter users table
-- ============================================================================

-- Link existing SYSTEM_ADMIN user to N1 employee (top-level, no manager)
UPDATE users
SET employee_id = (
    SELECT id FROM employees WHERE manager_id IS NULL LIMIT 1
)
WHERE role = 'SYSTEM_ADMIN' AND employee_id IS NULL;

-- Drop role-related columns
ALTER TABLE users DROP COLUMN IF EXISTS role;
ALTER TABLE users DROP COLUMN IF EXISTS manager_level;
ALTER TABLE users DROP COLUMN IF EXISTS is_active;

-- Make employee_id NOT NULL (every user must be linked to an employee)
-- First, delete any orphaned users without employee_id
DELETE FROM users WHERE employee_id IS NULL;

ALTER TABLE users ALTER COLUMN employee_id SET NOT NULL;

-- ============================================================================
-- PHASE 9: Clean up orphaned monthly allocations
-- ============================================================================
-- Delete any monthly entries that fall outside their parent allocation's start/end dates
DELETE FROM monthly_allocations
WHERE id IN (
    SELECT ma.id
    FROM monthly_allocations ma
    JOIN allocations a ON ma.allocation_id = a.id
    WHERE 
        DATE(ma.year || '-' || LPAD(ma.month::text, 2, '0') || '-01') < DATE_TRUNC('month', a.start_date)
        OR 
        DATE(ma.year || '-' || LPAD(ma.month::text, 2, '0') || '-01') > DATE_TRUNC('month', a.end_date)
);

COMMIT;
