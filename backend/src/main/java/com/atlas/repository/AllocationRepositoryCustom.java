package com.atlas.repository;

import com.atlas.entity.Allocation;

import java.util.List;

/**
 * Custom repository interface for Allocation-specific queries that require
 * EntityManager and CriteriaQuery for DB-level projections and distinct operations.
 */
public interface AllocationRepositoryCustom {

    /**
     * Finds distinct allocation types from allocations matching the given filters.
     * Uses AllocationSpecification to filter allocations at DB level,
     * then selects their distinct types.
     *
     * @param managerId Manager ID filter
     * @param search Search term for employee name
     * @param accessibleEmployeeIds List of accessible employee IDs for ABAC
     * @return List of distinct allocation types, ordered alphabetically
     */
    List<Allocation.AllocationType> findDistinctAllocationTypesBySpec(
            Long managerId,
            String search,
            List<Long> accessibleEmployeeIds);
}
