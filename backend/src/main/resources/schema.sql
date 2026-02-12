-- PostgreSQL enum types for Atlas
-- Runs BEFORE Hibernate DDL so column types are available
-- continue-on-error=true handles duplicate type errors on restart

CREATE TYPE gender_type AS ENUM ('MALE', 'FEMALE');
CREATE TYPE hiring_type AS ENUM ('FULL_TIME', 'PART_TIME');
CREATE TYPE job_level_type AS ENUM ('ENTRY_LEVEL', 'MID_LEVEL', 'ADVANCED_MANAGER_LEVEL', 'EXECUTIVE_LEVEL');
CREATE TYPE allocation_type AS ENUM ('PROJECT', 'PROSPECT', 'VACATION', 'MATERNITY');
CREATE TYPE project_type AS ENUM ('PROJECT', 'OPPORTUNITY');
CREATE TYPE project_status_type AS ENUM ('ACTIVE', 'COMPLETED', 'ON_HOLD');
CREATE TYPE skill_level_type AS ENUM ('PRIMARY', 'SECONDARY');
CREATE TYPE skill_grade_type AS ENUM ('ADVANCED', 'INTERMEDIATE', 'BEGINNER');
