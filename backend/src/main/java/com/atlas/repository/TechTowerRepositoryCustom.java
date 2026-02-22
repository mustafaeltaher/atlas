package com.atlas.repository;

import java.util.List;

/**
 * Custom repository interface for TechTower queries that require
 * complex filtering based on Employee specifications.
 */
public interface TechTowerRepositoryCustom {

    /**
     * Find distinct tower descriptions for employees matching the given filters.
     * Uses EmployeeSpecification to apply filters at DB level.
     *
     * @param search Employee name or email search term
     * @param tower Tower filter (should be null when getting distinct towers)
     * @param managerId Manager ID filter
     * @param status Employee status filter (ACTIVE, BENCH, PROSPECT, etc.)
     * @param accessibleIds List of accessible employee IDs for ABAC
     * @param managerName Manager name search filter
     * @return List of distinct tower descriptions, sorted alphabetically
     */
    List<String> findDistinctDescriptionsByEmployeeSpec(
            String search,
            String tower,
            Long managerId,
            String status,
            List<Long> accessibleIds,
            String managerName
    );
}
