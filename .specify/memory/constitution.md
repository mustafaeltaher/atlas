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

### Testing Standards (NON-NEGOTIABLE)

**Backend unit tests are MANDATORY for all new features:**

**Requirements**:
- Every new service class MUST have a corresponding test class (e.g., `FooService` → `FooServiceTest`)
- Minimum coverage for service methods:
  - Happy path (successful operation)
  - ABAC access denied scenario
  - Not found scenarios (employee, entity, etc.)
  - Validation failures (duplicate detection, invalid input)
  - Edge cases (null handling, empty lists)
- Use Mockito for mocking dependencies (`@Mock`, `@InjectMocks`)
- Use JUnit 5 with `@ExtendWith(MockitoExtension.class)`
- Test naming: `methodName_scenario_expectedResult` (e.g., `addSkill_duplicateSkill_throwsException`)

**What to test**:
- ✅ Service layer: Business logic, ABAC validation, error handling
- ✅ Repository custom queries: Complex JPQL/native SQL queries
- ⚠️ Controller layer: Optional (integration tests preferred)
- ❌ Frontend: Optional (not standard practice for this project)

**Rationale**: Backend services contain critical business logic (ABAC, validation, data integrity). Unit tests provide regression protection and document expected behavior.

**Test Quality Standards**:
- Tests must be deterministic (no flaky tests)
- Use `@BeforeEach` for test fixture setup
- Mock external dependencies (repositories, other services)
- Verify method calls with `verify()` when testing side effects
- Use `assertThat()` from AssertJ for readable assertions

**Example Test Structure**:
```java
@ExtendWith(MockitoExtension.class)
class FooServiceTest {
    @Mock private FooRepository fooRepository;
    @Mock private EmployeeService employeeService;
    @InjectMocks private FooService fooService;

    @BeforeEach
    void setUp() {
        // Setup test fixtures
    }

    @Test
    @DisplayName("methodName - should succeed when conditions met")
    void methodName_validInput_succeeds() {
        // Arrange: Setup mocks
        // Act: Call method
        // Assert: Verify behavior
    }
}
```

### Git Flow Standards (NON-NEGOTIABLE)

**All development MUST follow git flow branching model:**

**Branch Naming Conventions**:
- `feature/XXX-description` - New features (e.g., `feature/001-edit-employee-skills`)
- `bugfix/XXX-description` - Bug fixes (e.g., `bugfix/042-fix-allocation-filter`)
- `hotfix/XXX-description` - Critical production fixes
- `release/vX.Y.Z` - Release preparation branches

**Branching Rules (NON-NEGOTIABLE)**:
- ✅ All work must be done in feature/bugfix/hotfix branches
- ❌ **NO direct commits to `main` branch** (push will be rejected)
- ✅ Feature branches created from `main`
- ✅ Merge back to `main` via Pull Request only
- ✅ Delete feature branch after successful merge

**Pre-Merge Requirements**:
- ✅ **All tests MUST pass** (`./mvnw test` exits 0)
- ✅ Pull Request created with descriptive title and summary
- ✅ Constitution compliance verified
- ✅ No merge conflicts with `main`
- ✅ Frontend builds successfully (`npx ng build` exits 0)

**Commit Guidelines**:
- Commits should be logical, focused units of work
- Include co-authorship when assisted: `Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>`
- Commit message format is optional (may be added in future amendment)

**Workflow Example**:
```bash
# Create feature branch
git checkout -b feature/123-add-feature main

# Work and commit
git add .
git commit -m "feat: add new feature"

# Push and create PR
git push -u origin feature/123-add-feature
gh pr create --title "Add new feature" --body "Description..."

# After PR approval and tests pass: merge via GitHub UI
# GitHub will delete branch automatically if configured
```

**Rationale**: Git flow provides structured development workflow, prevents accidental main branch corruption, ensures code review, and maintains test coverage through CI/CD integration.

## Development Workflow

### Feature Development Process

1. **Specification**: Use `/speckit.specify` to create feature spec with user stories, requirements, success criteria
2. **Clarification** (optional): Use `/speckit.clarify` to resolve ambiguities
3. **Planning**: Use `/speckit.plan` to design DB-level implementation approach
4. **Analysis** (optional): Use `/speckit.analyze` to verify cross-artifact consistency
5. **Tasks**: Use `/speckit.tasks` to break down into actionable items
6. **Implementation**: Use `/speckit.implement` with adherence to constitution
7. **Backend Testing (MANDATORY)**: Write unit tests for all service classes covering happy path, ABAC, validation, and edge cases
8. **Manual Testing**: Verify against acceptance criteria and constitution compliance

### Code Review Requirements

Every PR must verify:
- **Git flow compliance**: Proper branch naming, no direct main commits (NON-NEGOTIABLE)
- **All tests pass**: Backend (`./mvnw test`) and frontend (`npx ng build`) must succeed (NON-NEGOTIABLE)
- Constitution compliance (especially ABAC, DB-first, NULL-safety)
- **Unit tests exist for all new/modified service classes** (NON-NEGOTIABLE)
- Test coverage includes: happy path, ABAC, validation failures, edge cases
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

---

## Amendment Log

### v1.2.0 (2026-02-22)
**Added**: Git Flow branching standards and pre-merge requirements

**Rationale**: Structured branching workflow prevents accidental corruption of main branch, ensures code review, maintains test coverage, and provides clear development lifecycle. All tests passing before merge prevents broken builds in main branch.

**Impact**: All development must follow feature/bugfix/hotfix branch naming. Direct commits to main are prohibited. PRs require all tests to pass before merge.

**Migration Plan**:
- Current work: Continue on existing branches (e.g., `001-edit-employee-skills`)
- New work: Use proper branch prefixes (e.g., `feature/002-description`)
- Configure GitHub branch protection rules to enforce main branch protection
- Existing branches may be merged with current naming but future branches must comply

### v1.1.0 (2026-02-20)
**Added**: Mandatory backend unit testing standards

**Rationale**: Backend services contain critical business logic (ABAC, validation, data integrity). Without mandatory tests, features could be shipped with untested code paths, leading to production bugs and security issues.

**Impact**: All new backend features must include unit tests. Existing code is grandfathered in but should be tested when modified.

**Migration Plan**:
- New features: Include unit tests in initial implementation
- Bug fixes: Add tests reproducing the bug before fixing
- Refactoring: Add tests for existing code being modified

---

**Version**: 1.2.0 | **Ratified**: 2026-02-17 | **Last Amended**: 2026-02-22
