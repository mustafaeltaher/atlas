package com.atlas.repository;

import com.atlas.entity.Project;
import com.atlas.specification.ProjectSpecification;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Implementation of custom Project repository methods.
 * Uses JPA Criteria API to build queries that select distinct regions
 * while applying ProjectSpecification filters at the database level.
 */
public class ProjectRepositoryCustomImpl implements ProjectRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<String> findDistinctRegionsByProjectSpec(
            Project.ProjectStatus status,
            String search,
            List<Long> projectIds) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<Project> root = query.from(Project.class);

        // Build specification with all project filters
        Specification<Project> spec = ProjectSpecification.distinctRegionsFilter(
                status, search, projectIds);

        // Apply specification predicates
        Predicate predicate = spec.toPredicate(root, query, cb);

        // Select distinct regions, apply filters, and sort alphabetically
        query.select(root.get("region"))
             .distinct(true)
             .where(predicate)
             .orderBy(cb.asc(root.get("region")));

        return entityManager.createQuery(query).getResultList();
    }
}
