package com.atlas.repository;

import com.atlas.entity.Allocation;
import com.atlas.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AllocationRepository extends JpaRepository<Allocation, Long>, JpaSpecificationExecutor<Allocation>, AllocationRepositoryCustom {

        @Query("SELECT a FROM Allocation a JOIN FETCH a.employee LEFT JOIN FETCH a.project WHERE a.project.id = :projectId AND a.allocationType = :allocationType")
        List<Allocation> findAllocationsByProjectIdAndType(
                        @Param("projectId") Long projectId,
                        @Param("allocationType") Allocation.AllocationType allocationType);

        default List<Allocation> findProjectAllocationsByProjectId(Long projectId) {
                return findAllocationsByProjectIdAndType(projectId, Allocation.AllocationType.PROJECT);
        }

        @Query("SELECT a FROM Allocation a JOIN FETCH a.employee LEFT JOIN FETCH a.project WHERE a.employee IN :employees")
        List<Allocation> findAllocationsByEmployees(@Param("employees") List<Employee> employees);

        @Query("SELECT a FROM Allocation a WHERE a.project.id IN :projectIds AND a.allocationType = :allocationType")
        List<Allocation> findAllocationsByProjectIdsAndType(
                        @Param("projectIds") List<Long> projectIds,
                        @Param("allocationType") Allocation.AllocationType allocationType);

        default List<Allocation> findProjectAllocationsByProjectIds(List<Long> projectIds) {
                return findAllocationsByProjectIdsAndType(projectIds, Allocation.AllocationType.PROJECT);
        }

        @Query("SELECT a FROM Allocation a JOIN FETCH a.employee LEFT JOIN FETCH a.project")
        List<Allocation> findAllWithEmployeeAndProject();

        @Query("SELECT a FROM Allocation a JOIN FETCH a.employee LEFT JOIN FETCH a.project WHERE a.employee.id = :employeeId")
        List<Allocation> findByEmployeeIdWithDetails(@Param("employeeId") Long employeeId);

        @Query("SELECT a FROM Allocation a JOIN FETCH a.employee LEFT JOIN FETCH a.project WHERE a.project.id = :projectId")
        List<Allocation> findByProjectIdWithDetails(@Param("projectId") Long projectId);

        @Query("SELECT a FROM Allocation a JOIN FETCH a.employee LEFT JOIN FETCH a.project WHERE a.id = :id")
        java.util.Optional<Allocation> findByIdWithDetails(@Param("id") Long id);

        @Query("SELECT a FROM Allocation a JOIN FETCH a.employee LEFT JOIN FETCH a.project WHERE a.employee.id IN :employeeIds")
        List<Allocation> findByEmployeeIdsWithDetails(@Param("employeeIds") List<Long> employeeIds);

        // Distinct values for facets
        @Query("SELECT DISTINCT a.allocationType FROM Allocation a JOIN a.employee e WHERE " +
                        "(:managerId IS NULL OR e.manager.id = :managerId)")
        List<Allocation.AllocationType> findDistinctAllocationTypesByManager(@Param("managerId") Long managerId);

        // Get distinct managers from allocations (for faceted search dropdown)
        // Use native SQL with JOIN to match EmployeeRepository pattern

        @Query(value = "SELECT DISTINCT m.* FROM allocations a " +
                        "JOIN employees e ON e.id = a.employee_id " +
                        "JOIN employees m ON m.id = e.manager_id " +
                        "LEFT JOIN projects p ON p.id = a.project_id " +
                        "WHERE e.manager_id IS NOT NULL " +
                        "AND e.resignation_date IS NULL " +
                        "AND m.resignation_date IS NULL " +
                        "AND (CAST(:allocationType AS text) IS NULL OR CAST(a.allocation_type AS text) = CAST(:allocationType AS text)) " +
                        "AND (CAST(:search AS text) IS NULL OR LOWER(e.name) LIKE CAST(:search AS text) " +
                        "     OR (p.id IS NOT NULL AND LOWER(p.description) LIKE CAST(:search AS text))) " +
                        "AND (CAST(:managerSearch AS text) IS NULL OR LOWER(m.name) LIKE CAST(:managerSearch AS text)) " +
                        "ORDER BY m.name", nativeQuery = true)
        List<Employee> findDistinctManagersByFiltersNoIds(
                        @Param("allocationType") String allocationType,
                        @Param("search") String search,
                        @Param("managerSearch") String managerSearch);

        @Query(value = "SELECT DISTINCT m.* FROM allocations a " +
                        "JOIN employees e ON e.id = a.employee_id " +
                        "JOIN employees m ON m.id = e.manager_id " +
                        "LEFT JOIN projects p ON p.id = a.project_id " +
                        "WHERE e.manager_id IS NOT NULL " +
                        "AND e.resignation_date IS NULL " +
                        "AND m.resignation_date IS NULL " +
                        "AND e.id IN :employeeIds " +
                        "AND (CAST(:allocationType AS text) IS NULL OR CAST(a.allocation_type AS text) = CAST(:allocationType AS text)) " +
                        "AND (CAST(:search AS text) IS NULL OR LOWER(e.name) LIKE CAST(:search AS text) " +
                        "     OR (p.id IS NOT NULL AND LOWER(p.description) LIKE CAST(:search AS text))) " +
                        "AND (CAST(:managerSearch AS text) IS NULL OR LOWER(m.name) LIKE CAST(:managerSearch AS text)) " +
                        "ORDER BY m.name", nativeQuery = true)
        List<Employee> findDistinctManagersByFiltersWithIds(
                        @Param("employeeIds") List<Long> employeeIds,
                        @Param("allocationType") String allocationType,
                        @Param("search") String search,
                        @Param("managerSearch") String managerSearch);

        // Default method to route between queries and convert enum to string
        default List<Employee> findDistinctManagersByFilters(
                        Allocation.AllocationType allocationType,
                        List<Long> employeeIds,
                        String search,
                        String managerSearch) {
                // Format search parameters to include wildcards (or NULL for IS NULL checks)
                String searchParam = (search != null && !search.trim().isEmpty())
                        ? "%" + search.trim().toLowerCase() + "%"
                        : null;
                String managerSearchParam = (managerSearch != null && !managerSearch.trim().isEmpty())
                        ? "%" + managerSearch.trim().toLowerCase() + "%"
                        : null;
                // Convert enum to String (NULL for IS NULL checks)
                String allocationTypeParam = (allocationType != null) ? allocationType.name() : null;

                // Handle empty list case - IN () clause fails in native SQL
                if (employeeIds != null && employeeIds.isEmpty()) {
                        return java.util.Collections.emptyList();
                }

                if (employeeIds == null) {
                        return findDistinctManagersByFiltersNoIds(allocationTypeParam, searchParam, managerSearchParam);
                }
                return findDistinctManagersByFiltersWithIds(employeeIds, allocationTypeParam, searchParam, managerSearchParam);
        }
}
