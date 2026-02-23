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
     * @param year Year filter for allocations active in selected month
     * @param month Month filter for allocations active in selected month
     * @return List of distinct allocation types, ordered alphabetically
     */
    List<Allocation.AllocationType> findDistinctAllocationTypesBySpec(
            Long managerId,
            String search,
            List<Long> accessibleEmployeeIds,
            Integer year,
            Integer month);

    /**
     * Finds distinct year-month combinations from allocations matching the given filters.
     * Returns list of strings in "YYYY-MM" format representing months with allocations.
     *
     * @param allocationType Allocation type filter
     * @param managerId Manager ID filter
     * @param search Search term for employee name
     * @param accessibleEmployeeIds List of accessible employee IDs for ABAC
     * @return List of year-month strings in "YYYY-MM" format, sorted chronologically
     */
    List<String> findDistinctAvailableMonths(
            Allocation.AllocationType allocationType,
            Long managerId,
            String search,
            List<Long> accessibleEmployeeIds);
}
