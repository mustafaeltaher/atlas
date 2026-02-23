package com.atlas.repository;

import com.atlas.entity.Employee;
import com.atlas.specification.EmployeeSpecification;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of custom Employee repository methods.
 * Uses JPA Criteria API to build queries that select distinct managers
 * while applying EmployeeSpecification filters to subordinate employees at the database level.
 */
public class EmployeeRepositoryCustomImpl implements EmployeeRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Employee> findDistinctManagersByEmployeeSpec(
            String search,
            String tower,
            Long managerId,
            String status,
            List<Long> accessibleIds,
            String managerName,
            Integer year,
            Integer month) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Employee> query = cb.createQuery(Employee.class);
        Root<Employee> root = query.from(Employee.class);

        // Join with manager to get manager entities
        Join<Employee, Employee> managerJoin = root.join("manager", JoinType.INNER);

        // Build specification with all employee filters (these apply to subordinates)
        // Pass year/month so status checks (PROSPECT, ACTIVE, BENCH) use the selected month
        Specification<Employee> spec = EmployeeSpecification.withFilters(
                search, tower, managerId, status, accessibleIds, managerName, year, month);

        // Apply specification predicates to subordinates
        Predicate specPredicate = spec.toPredicate(root, query, cb);

        // Additional filter: managers must not be resigned
        Predicate managerNotResigned = cb.isNull(managerJoin.get("resignationDate"));

        // Combine predicates
        List<Predicate> predicates = new ArrayList<>();
        if (specPredicate != null) {
            predicates.add(specPredicate);
        }
        predicates.add(managerNotResigned);

        // Select distinct managers, apply filters, and sort by manager name
        query.select(managerJoin)
             .distinct(true)
             .where(cb.and(predicates.toArray(new Predicate[0])))
             .orderBy(cb.asc(managerJoin.get("name")));

        return entityManager.createQuery(query).getResultList();
    }
}
