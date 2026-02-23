package com.atlas.repository;

import com.atlas.entity.Allocation;
import com.atlas.specification.AllocationSpecification;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Implementation of custom Allocation repository methods.
 * Uses JPA Criteria API to build queries that select distinct allocation types
 * while applying AllocationSpecification filters at the database level.
 */
public class AllocationRepositoryCustomImpl implements AllocationRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Allocation.AllocationType> findDistinctAllocationTypesBySpec(
            Long managerId,
            String search,
            List<Long> accessibleEmployeeIds,
            Integer year,
            Integer month) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Allocation.AllocationType> query = cb.createQuery(Allocation.AllocationType.class);
        Root<Allocation> root = query.from(Allocation.class);

        // Build specification with all allocation filters including year/month
        Specification<Allocation> spec = AllocationSpecification.withFilters(
                null, managerId, search, accessibleEmployeeIds, year, month);

        // Apply specification predicates
        Predicate predicate = spec.toPredicate(root, query, cb);

        // Select distinct allocation types, apply filters, and sort alphabetically
        query.select(root.get("allocationType"))
             .distinct(true)
             .where(predicate)
             .orderBy(cb.asc(root.get("allocationType")));

        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public List<String> findDistinctAvailableMonths(
            Allocation.AllocationType allocationType,
            Long managerId,
            String search,
            List<Long> accessibleEmployeeIds) {

        // Query monthly_allocations table for distinct year-month combinations
        // PROJECT and PROSPECT allocations have MonthlyAllocation records for each month in their date range
        // MATERNITY and VACATION allocations use only startDate/endDate (no MonthlyAllocation records)

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<com.atlas.entity.MonthlyAllocation> maRoot = query.from(com.atlas.entity.MonthlyAllocation.class);
        Join<com.atlas.entity.MonthlyAllocation, Allocation> allocJoin = maRoot.join("allocation");
        Join<Allocation, com.atlas.entity.Employee> employeeJoin = allocJoin.join("employee", jakarta.persistence.criteria.JoinType.INNER);

        List<Predicate> predicates = new java.util.ArrayList<>();

        // Access Control (ABAC)
        if (accessibleEmployeeIds != null) {
            if (accessibleEmployeeIds.isEmpty()) {
                predicates.add(cb.disjunction()); // 1=0
            } else {
                predicates.add(employeeJoin.get("id").in(accessibleEmployeeIds));
            }
        }

        // Allocation Type filter
        if (allocationType != null) {
            predicates.add(cb.equal(allocJoin.get("allocationType"), allocationType));
        }

        // Manager Filter
        if (managerId != null) {
            predicates.add(cb.equal(employeeJoin.get("manager").get("id"), managerId));
        }

        // Search Filter (Employee Name or Email)
        if (search != null && !search.trim().isEmpty()) {
            String searchLike = "%" + search.trim().toLowerCase() + "%";
            Predicate employeeNameLike = cb.like(cb.lower(employeeJoin.get("name")), searchLike);
            Predicate employeeEmailLike = cb.like(cb.lower(employeeJoin.get("email")), searchLike);
            predicates.add(cb.or(employeeNameLike, employeeEmailLike));
        }

        // Select distinct year and month from MonthlyAllocation
        query.multiselect(maRoot.get("year"), maRoot.get("month"))
             .distinct(true)
             .where(cb.and(predicates.toArray(new Predicate[0])))
             .orderBy(cb.asc(maRoot.get("year")), cb.asc(maRoot.get("month")));

        List<Object[]> results = entityManager.createQuery(query).getResultList();

        // Format results as "YYYY-MM" strings
        return results.stream()
                .map(row -> String.format("%04d-%02d", (Integer) row[0], (Integer) row[1]))
                .collect(java.util.stream.Collectors.toList());
    }
}
