package com.atlas.repository;

import com.atlas.entity.Allocation;
import com.atlas.specification.AllocationSpecification;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
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
            List<Long> accessibleEmployeeIds) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Allocation.AllocationType> query = cb.createQuery(Allocation.AllocationType.class);
        Root<Allocation> root = query.from(Allocation.class);

        // Build specification with all allocation filters
        Specification<Allocation> spec = AllocationSpecification.withFilters(
                null, managerId, search, accessibleEmployeeIds);

        // Apply specification predicates
        Predicate predicate = spec.toPredicate(root, query, cb);

        // Select distinct allocation types, apply filters, and sort alphabetically
        query.select(root.get("allocationType"))
             .distinct(true)
             .where(predicate)
             .orderBy(cb.asc(root.get("allocationType")));

        return entityManager.createQuery(query).getResultList();
    }
}
