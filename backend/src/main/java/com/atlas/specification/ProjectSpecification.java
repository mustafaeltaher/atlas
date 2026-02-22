package com.atlas.specification;

import com.atlas.entity.Project;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ProjectSpecification {

    public static Specification<Project> withFilters(
            Project.ProjectStatus status,
            String region,
            String search,
            List<Long> projectIds) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Status filter
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            // Region filter
            if (region != null && !region.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("region"), region.trim()));
            }

            // Search filter
            if (search != null && !search.trim().isEmpty()) {
                String searchLike = "%" + search.trim().toLowerCase() + "%";
                Predicate descLike = cb.like(cb.lower(root.get("description")), searchLike);
                Predicate idLike = cb.like(cb.lower(root.get("projectId")), searchLike);
                predicates.add(cb.or(descLike, idLike));
            }

            // Project IDs filter (Access Control)
            if (projectIds != null) {
                if (projectIds.isEmpty()) {
                    // accessible but list is empty -> return no results
                    // explicitly adding false predicate
                    predicates.add(cb.disjunction());
                } else {
                    predicates.add(root.get("id").in(projectIds));
                }
            }

            // For fetching distinct regions, we want non-null regions
            // But this specification might be reused for general searches?
            // "getDistinctRegions" logic specifically needs region IS NOT NULL
            // We can add it here if this Spec is specific to that view, or add it
            // explicitly in the service via another spec.
            // For now, let's keep it general and add specific predicates if needed,
            // OR simply filter null regions in the stream if the result set is small (it
            // is).
            // BETTER: Add "region IS NOT NULL" to the query if we are fetching regions?
            // Actually, let's make a specific method for "withRegionPresent" if we want to
            // enforce it.

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Project> distinctRegionsFilter(
            Project.ProjectStatus status,
            String search,
            List<Long> projectIds) {
        return (root, query, cb) -> {
            // Re-use logic or compose?
            Specification<Project> base = withFilters(status, null, search, projectIds);
            Predicate basePredicate = base.toPredicate(root, query, cb);

            Predicate regionNotNull = cb.isNotNull(root.get("region"));

            // Ensure distinct results for the query if possible, but distinct is usually on
            // selection
            query.distinct(true);

            return cb.and(basePredicate, regionNotNull);
        };
    }
}
