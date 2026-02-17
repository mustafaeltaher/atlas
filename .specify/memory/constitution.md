# Atlas Project Constitution

Enterprise employee management system with ABAC (Attribute-Based Access Control), faceted search, and allocation tracking.

## Core Principles

### I. ABAC-First Security (NON-NEGOTIABLE)

**No role-based access control.** All access is managed through manager hierarchy:
- `employee.manager_id = NULL` → Top-level manager (full access to all subordinates)
- Access is **transitive**: managers can access all employees in their reporting chain
- Every query MUST filter by `getAccessibleEmployeeIds()` unless explicitly exempt
- **Early return exception**: Native SQL queries with `IN :ids` clauses MUST check for empty lists and return early to prevent `IN ()` syntax errors

**Rationale**: ABAC provides fine-grained, hierarchical access control that mirrors organizational structure without rigid role assignments.

### II. Database-First Performance (NON-NEGOTIABLE)

**Never filter in memory.** All filtering, searching, and pagination MUST occur at the database level:
- Use JPA Specifications (`JpaSpecificationExecutor`) for complex queries
- For projections (distinct values), use `@PersistenceContext EntityManager` with `CriteriaQuery`
- **Pattern**: Create custom repository interfaces (e.g., `TechTowerRepositoryCustom`) with `EntityManager` implementation
- Loading large result sets into memory for distinct operations is **prohibited**

**Rationale**: In-memory filtering doesn't scale. Database engines are optimized for these operations.

### III. Faceted Search Architecture

**All filter endpoints accept all other filter parameters.** Faceted search means:
- When loading a dropdown (e.g., managers), apply all OTHER current filters (project, status, etc.)
- Frontend MUST pass all current filter values when loading each dropdown
- Use JPA Specification with joins to apply filters at DB level before selecting distinct values
- **Example**: `findDistinctDescriptionsByEmployeeSpec()` joins Employee→TechTower, applies EmployeeSpecification, selects distinct at DB level

**Rationale**: True faceted search where dropdowns affect each other requires all filters to be considered for each dimension.

### IV. NULL-Safe Native Queries (NON-NEGOTIABLE)

**All optional parameters in native SQL MUST use the NULL pattern:**
```sql
(:param IS NULL OR LOWER(col) LIKE :param)
```

**Rules**:
- Pass actual `NULL` from Java for optional params (not `"%"` strings)
- For string params: `searchTerm != null ? "%" + searchTerm.toLowerCase() + "%" : null`
- For enum params: `allocationType != null ? allocationType.name() : null`
- For numeric params: Use `CAST(:param AS bigint)` to hint types: `(CAST(:param AS bigint) IS NULL OR col = :param)`
- **Never** use PostgreSQL `::type` syntax in native queries (conflicts with Hibernate `:param` binding)

**Rationale**: Prevents "could not determine data type" and "function lower(bytea) does not exist" errors.

### V. Enum Handling Standards

**Use VARCHAR columns for all Java enums, never PostgreSQL native enum types:**
- Use `@Enumerated(EnumType.STRING)` **WITHOUT** `columnDefinition`
- Let Hibernate create VARCHAR columns automatically
- Do NOT create enum types in `schema.sql`
- **Never** use `@JdbcTypeCode(SqlTypes.NAMED_ENUM)` (creates duplicate enum types)

**Rationale**: PostgreSQL enum types cause type mismatch errors with Hibernate's VARCHAR binding.

## Technical Standards

### Technology Stack (LOCKED)

- **Backend**: Spring Boot 3.2.1, Hibernate 6.4.1, PostgreSQL 15, **Java 17** (NON-NEGOTIABLE)
- **Frontend**: Angular 17+ with Signals, TypeScript
- **Local Java Path**: `/Users/mustafaabdelhalim/Library/Java/JavaVirtualMachines/corretto-17.0.16/Contents/Home`
- **Build Commands**:
  - Backend: `JAVA_HOME=<above> ./mvnw clean compile`
  - Frontend: `npx ng build`
  - Docker: `docker compose build --no-cache && docker compose up -d`

**Lombok Constraint**: Project uses Lombok, which is incompatible with Java 24. Must remain on Java 17.

### Status Derivation Logic

**Employee Status** (derived, not stored):
- Has `MATERNITY` allocation → `MATERNITY`
- Has `VACATION` allocation → `VACATION`
- Has `resignationDate` set → `RESIGNED`
- Otherwise → `ACTIVE`

**Allocation Status** (derived, not stored):
- Has `PROJECT` allocation with % → `ACTIVE`
- Has `PROSPECT` allocation → `PROSPECT`
- No allocation records exist → `BENCH`

**Critical**: `BENCH` and `ACTIVE` require separate query paths from standard allocation types because they query the Employee table, not the Allocation table.

### Search Scope Standards

**Keep search focused on primary entities:**
- Employee page search: search by employee name/email only
- Allocation page search: search by employee name only (NOT project description)
- Project filters: remain project-focused

**Rationale**: Mixing search dimensions (e.g., employee OR project) confuses faceted search and users.

## Data Access Patterns

### Repository Pattern Requirements

**JPA Specifications for complex queries:**
1. Create `EmployeeSpecification`, `AllocationSpecification`, etc. with static builder methods
2. Extend repositories with `JpaSpecificationExecutor<Entity>`
3. For custom projections:
   - Create `EntityRepositoryCustom` interface
   - Implement in `EntityRepositoryCustomImpl` with `@PersistenceContext EntityManager`
   - Use `CriteriaQuery` for type-safe queries
   - Main repository extends custom interface

**Hybrid Status Handling**:
- Standard types (`PROJECT`, `PROSPECT`, `MATERNITY`, `VACATION`) → query Allocation table
- `ACTIVE` status → subquery for employees WITH PROJECT allocations
- `BENCH` status → subquery for employees WITHOUT any allocations
- Manager dropdowns must handle all three categories explicitly

### Query Safety Checklist

Before merging any query code, verify:
- [ ] Uses JPA Specification or EntityManager (no in-memory filtering)
- [ ] Applies ABAC via `accessibleEmployeeIds` (if applicable)
- [ ] Handles empty `accessibleEmployeeIds` correctly (early return for native SQL `IN` clauses)
- [ ] All optional parameters use `(:param IS NULL OR ...)` pattern
- [ ] No PostgreSQL `::type` casts (use `CAST(col AS type)`)
- [ ] Enum fields use `@Enumerated(EnumType.STRING)` without `columnDefinition`
- [ ] LOWER() used for case-insensitive string comparisons
- [ ] For BENCH/ACTIVE filters, uses direct Employee table queries

## Development Workflow

### Feature Development Process

1. **Specification**: Use `/speckit.specify` to create feature spec with user stories, requirements, success criteria
2. **Clarification** (optional): Use `/speckit.clarify` to resolve ambiguities
3. **Planning**: Use `/speckit.plan` to design DB-level implementation approach
4. **Analysis** (optional): Use `/speckit.analyze` to verify cross-artifact consistency
5. **Tasks**: Use `/speckit.tasks` to break down into actionable items
6. **Implementation**: Use `/speckit.implement` with adherence to constitution
7. **Testing**: Verify against acceptance criteria and constitution compliance

### Code Review Requirements

Every PR must verify:
- Constitution compliance (especially ABAC, DB-first, NULL-safety)
- No in-memory filtering on large datasets
- Proper faceted search if adding filters
- Query safety checklist passed
- Edge cases handled (empty lists, NULL params, BENCH status)

### Memory Management

**Project memory** at `.claude/projects/.../memory/MEMORY.md`:
- Record new lessons learned (anti-patterns, gotchas, solutions)
- Update when discovering new edge cases or best practices
- Link to detailed topic files for complex subjects
- Keep MEMORY.md under 200 lines (truncation limit)

## Governance

**This constitution supersedes all other practices.**

- All new features MUST comply with these principles
- Deviations require explicit justification and approval
- When in doubt, consult MEMORY.md for lessons learned
- Constitution amendments require:
  1. Documentation of rationale
  2. Review of impact on existing code
  3. Migration plan if breaking changes
  4. Update of this document

**Complexity Budget**: Start simple. Additional complexity requires clear justification demonstrating value beyond YAGNI principles.

**Version**: 1.0.0 | **Ratified**: 2026-02-17 | **Last Amended**: 2026-02-17
