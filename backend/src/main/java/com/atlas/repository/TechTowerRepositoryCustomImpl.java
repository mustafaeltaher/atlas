package com.atlas.repository;

import com.atlas.entity.Employee;
import com.atlas.entity.TechTower;
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

import java.util.List;

/**
 * Implementation of custom TechTower repository methods.
 * Uses JPA Criteria API to build queries that select distinct tower descriptions
 * while applying EmployeeSpecification filters at the database level.
 */
public class TechTowerRepositoryCustomImpl implements TechTowerRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<String> findDistinctDescriptionsByEmployeeSpec(
            String search,
            String tower,
            Long managerId,
            String status,
            List<Long> accessibleIds,
            String managerName) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<Employee> root = query.from(Employee.class);

        // Join with TechTower to get descriptions
        Join<Employee, TechTower> towerJoin = root.join("tower", JoinType.INNER);

        // Build specification with all employee filters
        Specification<Employee> spec = EmployeeSpecification.withFilters(
                search, tower, managerId, status, accessibleIds, managerName);

        // Apply specification predicates
        Predicate predicate = spec.toPredicate(root, query, cb);

        // Select distinct tower descriptions, apply filters, and sort
        query.select(towerJoin.get("description"))
             .distinct(true)
             .where(predicate)
             .orderBy(cb.asc(towerJoin.get("description")));

        return entityManager.createQuery(query).getResultList();
    }
}
