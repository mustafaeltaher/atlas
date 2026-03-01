package com.atlas.repository;

import com.atlas.entity.Employee;

import java.util.List;

/**
 * Custom repository interface for Employee-specific queries that require
 * EntityManager and CriteriaQuery for DB-level projections and distinct operations.
 */
public interface EmployeeRepositoryCustom {

    /**
     * Finds distinct managers (Employee entities) whose subordinates match the given filters.
     * Uses EmployeeSpecification to filter subordinate employees at DB level,
     * then selects their distinct managers.
     *
     * @param search Search term for employee name/email
     * @param tower Tower filter
     * @param managerId Manager ID filter (applied to subordinates)
     * @param status Status filter (employee or allocation status)
     * @param accessibleIds List of accessible employee IDs for ABAC
     * @param managerName Manager name search filter
     * @param year Year for status checks (e.g., PROSPECT, ACTIVE, BENCH in specific month)
     * @param month Month for status checks (e.g., PROSPECT, ACTIVE, BENCH in specific month)
     * @return List of distinct manager Employee entities, ordered by name
     */
    List<Employee> findDistinctManagersByEmployeeSpec(
            String search,
            String tower,
            Long managerId,
            String status,
            List<Long> accessibleIds,
            String managerName,
            Integer year,
            Integer month);
}
