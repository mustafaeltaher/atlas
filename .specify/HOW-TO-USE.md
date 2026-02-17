# How to Use Spec Kit with Atlas

## Quick Start

### 1. Constitution (✅ DONE)
Your project constitution is at `.specify/memory/constitution.md`

### 2. Create a Feature Specification

Run this command in Claude Code:
```
/speckit.specify [feature description]
```

Example:
```
/speckit.specify Add employee search by skills on the allocations page
```

This will create a specification file with:
- User stories with priorities (P1, P2, P3)
- Acceptance scenarios (Given/When/Then)
- Functional requirements (FR-001, FR-002, etc.)
- Success criteria (measurable outcomes)

### 3. Clarify Ambiguities (Optional)

If you need to de-risk unclear areas:
```
/speckit.clarify
```

### 4. Create Implementation Plan

```
/speckit.plan
```

This generates a detailed plan that:
- Follows constitution principles (ABAC, DB-first, NULL-safe)
- Identifies files to modify
- Outlines DB schema changes
- Plans faceted search integration

### 5. Generate Tasks

```
/speckit.tasks
```

Breaks the plan into actionable tasks.

### 6. Implement

```
/speckit.implement
```

Claude Code will implement following the constitution and plan.

## Constitution Compliance

Before any PR, verify the **Query Safety Checklist** from constitution:

- [ ] Uses JPA Specification or EntityManager (no in-memory filtering)
- [ ] Applies ABAC via `accessibleEmployeeIds`
- [ ] Handles empty `accessibleEmployeeIds` correctly
- [ ] All optional parameters use `(:param IS NULL OR ...)` pattern
- [ ] No PostgreSQL `::type` casts (use `CAST(col AS type)`)
- [ ] Enum fields use `@Enumerated(EnumType.STRING)` without `columnDefinition`
- [ ] LOWER() for case-insensitive string comparisons
- [ ] BENCH/ACTIVE filters use direct Employee table queries

## Example: Adding a New Filter

Let's say you want to add a "Department" filter to the employee page:

1. **Specify**: `/speckit.specify Add department filter to employee page with faceted search`
2. **Plan**: `/speckit.plan` - Will outline:
   - Department entity/field on Employee
   - `EmployeeSpecification.byDepartment()` method
   - Custom repository method `findDistinctDepartments(Specification<Employee> spec)`
   - Frontend dropdown component
3. **Implement**: `/speckit.implement` - Follows constitution:
   - JPA Specification approach
   - EntityManager for distinct departments
   - NULL-safe parameters
   - Applies other filters when loading department dropdown

## Constitution Updates

When you discover a new pattern or gotcha:

1. Update `~/.claude/projects/.../memory/MEMORY.md` with the lesson
2. If it's a core principle, propose a constitution amendment
3. Update constitution with rationale and version bump

## File Locations

- Constitution: `.specify/memory/constitution.md`
- Specs: `.specify/specs/` (created as needed)
- Plans: `.specify/plans/` (created as needed)
- Tasks: `.specify/tasks/` (created as needed)
- Templates: `.specify/templates/`
- Scripts: `.specify/scripts/bash/`
