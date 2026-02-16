package com.atlas.repository;

import java.util.List;

/**
 * Custom repository interface for Project-specific queries that require
 * EntityManager and CriteriaQuery for DB-level projections and distinct operations.
 */
public interface ProjectRepositoryCustom {

    /**
     * Finds distinct regions from projects matching the given filters.
     * Uses ProjectSpecification to filter projects at DB level,
     * then selects their distinct regions.
     *
     * @param status Project status filter
     * @param search Search term for project description or ID
     * @param projectIds List of accessible project IDs for access control
     * @return List of distinct region strings, ordered alphabetically
     */
    List<String> findDistinctRegionsByProjectSpec(
            com.atlas.entity.Project.ProjectStatus status,
            String search,
            List<Long> projectIds);
}
