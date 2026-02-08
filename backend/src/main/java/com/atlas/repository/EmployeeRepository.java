package com.atlas.repository;

import com.atlas.entity.Allocation;
import com.atlas.entity.Employee;
import com.atlas.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

        Optional<Employee> findByOracleId(String oracleId);

        Optional<Employee> findByEmail(String email);

        List<Employee> findByIsActiveTrue();

        List<Employee> findByParentTower(String parentTower);

        List<Employee> findByTower(String tower);

        List<Employee> findByManager(Employee manager);

        @Query("SELECT e FROM Employee e WHERE e.parentTower = :parentTower AND e.isActive = true")
        List<Employee> findActiveByParentTower(@Param("parentTower") String parentTower);

        @Query("SELECT e FROM Employee e WHERE e.tower = :tower AND e.isActive = true")
        List<Employee> findActiveByTower(@Param("tower") String tower);

        @Query("SELECT DISTINCT e.parentTower FROM Employee e WHERE e.parentTower IS NOT NULL")
        List<String> findDistinctParentTowers();

        @Query("SELECT DISTINCT e.tower FROM Employee e WHERE e.tower IS NOT NULL")
        List<String> findDistinctTowers();

        @Query("SELECT COUNT(e) FROM Employee e WHERE e.isActive = true")
        long countActiveEmployees();

        boolean existsByOracleId(String oracleId);

        boolean existsByEmail(String email);

        @Query("SELECT e FROM Employee e WHERE e.manager.id = :managerId AND e.isActive = true")
        List<Employee> findActiveByManagerId(@Param("managerId") Long managerId);

        // Pagination methods with manager eager-loading
        @Query(value = "SELECT e FROM Employee e LEFT JOIN FETCH e.manager WHERE e.isActive = true", countQuery = "SELECT COUNT(e) FROM Employee e WHERE e.isActive = true")
        org.springframework.data.domain.Page<Employee> findByIsActiveTrue(
                        org.springframework.data.domain.Pageable pageable);

        // Search with pagination
        @Query(value = "SELECT e FROM Employee e LEFT JOIN FETCH e.manager WHERE e.isActive = true AND " +
                        "(LOWER(e.name) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) OR " +
                        "LOWER(e.primarySkill) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) OR " +
                        "LOWER(e.tower) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) OR " +
                        "LOWER(e.email) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')))", countQuery = "SELECT COUNT(e) FROM Employee e WHERE e.isActive = true AND "
                                        +
                                        "(LOWER(e.name) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) OR " +
                                        "LOWER(e.primarySkill) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) OR "
                                        +
                                        "LOWER(e.tower) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) OR " +
                                        "LOWER(e.email) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')))")
        org.springframework.data.domain.Page<Employee> searchActiveEmployees(
                        @Param("search") String search,
                        org.springframework.data.domain.Pageable pageable);

        // Comprehensive paginated query with all filters (tower, managerId, search) at
        // database level
        @Query(value = "SELECT e FROM Employee e LEFT JOIN FETCH e.manager WHERE e.isActive = true " +
                        "AND (:tower IS NULL OR e.tower = :tower) " +
                        "AND (:managerId IS NULL OR e.manager.id = :managerId) " +
                        "AND (:search IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) " +
                        "OR LOWER(e.primarySkill) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) " +
                        "OR LOWER(e.tower) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) " +
                        "OR LOWER(e.email) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')))", countQuery = "SELECT COUNT(e) FROM Employee e WHERE e.isActive = true "
                                        +
                                        "AND (:tower IS NULL OR e.tower = :tower) " +
                                        "AND (:managerId IS NULL OR e.manager.id = :managerId) " +
                                        "AND (:search IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) "
                                        +
                                        "OR LOWER(e.primarySkill) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) "
                                        +
                                        "OR LOWER(e.tower) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) " +
                                        "OR LOWER(e.email) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')))")
        Page<Employee> findActiveWithFilters(
                        @Param("tower") String tower,
                        @Param("managerId") Long managerId,
                        @Param("search") String search,
                        Pageable pageable);

        // Paginated query for employees by ID list (for non-admin hierarchy-based
        // access)
        @Query(value = "SELECT e FROM Employee e LEFT JOIN FETCH e.manager WHERE e.isActive = true " +
                        "AND e.id IN :employeeIds " +
                        "AND (:tower IS NULL OR e.tower = :tower) " +
                        "AND (:managerId IS NULL OR e.manager.id = :managerId) " +
                        "AND (:search IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) " +
                        "OR LOWER(e.primarySkill) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) " +
                        "OR LOWER(e.tower) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) " +
                        "OR LOWER(e.email) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')))", countQuery = "SELECT COUNT(e) FROM Employee e WHERE e.isActive = true "
                                        +
                                        "AND e.id IN :employeeIds " +
                                        "AND (:tower IS NULL OR e.tower = :tower) " +
                                        "AND (:managerId IS NULL OR e.manager.id = :managerId) " +
                                        "AND (:search IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) "
                                        +
                                        "OR LOWER(e.primarySkill) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) "
                                        +
                                        "OR LOWER(e.tower) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) " +
                                        "OR LOWER(e.email) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')))")
        Page<Employee> findByIdsWithFilters(
                        @Param("employeeIds") List<Long> employeeIds,
                        @Param("tower") String tower,
                        @Param("managerId") Long managerId,
                        @Param("search") String search,
                        Pageable pageable);

        @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.manager WHERE e.isActive = true")
        List<Employee> findByIsActiveTrueWithManager();

        @Query("SELECT DISTINCT m FROM Employee m WHERE m.isActive = true AND EXISTS (SELECT e FROM Employee e WHERE e.manager = m AND e.isActive = true)")
        List<Employee> findActiveManagers();

        @Query("SELECT DISTINCT m FROM Employee m WHERE m.isActive = true AND m.parentTower = :parentTower AND EXISTS (SELECT e FROM Employee e WHERE e.manager = m AND e.isActive = true)")
        List<Employee> findActiveManagersByParentTower(@Param("parentTower") String parentTower);

        @Query("SELECT DISTINCT m FROM Employee m WHERE m.isActive = true AND m.tower = :tower AND EXISTS (SELECT e FROM Employee e WHERE e.manager = m AND e.isActive = true)")
        List<Employee> findActiveManagersByTower(@Param("tower") String tower);

        @Query("SELECT DISTINCT e FROM Employee e JOIN User u ON u.employee = e " +
                        "WHERE e.isActive = true AND e.parentTower = :parentTower " +
                        "AND u.managerLevel > :minLevel " +
                        "AND EXISTS (SELECT emp FROM Employee emp WHERE emp.manager = e AND emp.isActive = true)")
        List<Employee> findActiveManagersByParentTowerHierarchical(
                        @Param("parentTower") String parentTower, @Param("minLevel") int minLevel);

        @Query("SELECT DISTINCT e FROM Employee e JOIN User u ON u.employee = e " +
                        "WHERE e.isActive = true AND e.tower = :tower " +
                        "AND u.managerLevel > :minLevel " +
                        "AND EXISTS (SELECT emp FROM Employee emp WHERE emp.manager = e AND emp.isActive = true)")
        List<Employee> findActiveManagersByTowerHierarchical(
                        @Param("tower") String tower, @Param("minLevel") int minLevel);

        // Paginated query for allocation view by employee IDs (reporting-chain based)
        @Query(value = "SELECT DISTINCT e FROM Employee e " +
                        "LEFT JOIN Allocation a ON a.employee = e " +
                        "LEFT JOIN a.project p " +
                        "WHERE e.isActive = true AND e.id IN :employeeIds " +
                        "AND (:search IS NULL OR LOWER(CAST(e.name AS string)) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) "
                        +
                        "OR (p IS NOT NULL AND LOWER(CAST(p.name AS string)) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))) "
                        +
                        "AND (:managerId IS NULL OR e.manager.id = :managerId) " +
                        "AND (:status IS NULL " +
                        "OR EXISTS (SELECT a2 FROM Allocation a2 WHERE a2.employee = e AND a2.status = :status) " +
                        "OR NOT EXISTS (SELECT a3 FROM Allocation a3 WHERE a3.employee = e)) " +
                        "ORDER BY e.name", countQuery = "SELECT COUNT(DISTINCT e.id) FROM Employee e " +
                                        "LEFT JOIN Allocation a ON a.employee = e " +
                                        "LEFT JOIN a.project p " +
                                        "WHERE e.isActive = true AND e.id IN :employeeIds " +
                                        "AND (:search IS NULL OR LOWER(CAST(e.name AS string)) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) "
                                        +
                                        "OR (p IS NOT NULL AND LOWER(CAST(p.name AS string)) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))) "
                                        +
                                        "AND (:managerId IS NULL OR e.manager.id = :managerId) " +
                                        "AND (:status IS NULL " +
                                        "OR EXISTS (SELECT a2 FROM Allocation a2 WHERE a2.employee = e AND a2.status = :status) "
                                        +
                                        "OR NOT EXISTS (SELECT a3 FROM Allocation a3 WHERE a3.employee = e))")
        Page<Employee> findEmployeesForAllocationViewByIds(
                        @Param("employeeIds") List<Long> employeeIds,
                        @Param("search") String search,
                        @Param("managerId") Long managerId,
                        @Param("status") Allocation.AllocationStatus status,
                        Pageable pageable);

        // Paginated query for allocation view: LEFT JOINs with allocations to include
        // employees with zero allocations
        @Query(value = "SELECT DISTINCT e FROM Employee e " +
                        "LEFT JOIN Allocation a ON a.employee = e " +
                        "LEFT JOIN a.project p " +
                        "WHERE e.isActive = true " +
                        "AND (:search IS NULL OR LOWER(CAST(e.name AS string)) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) "
                        +
                        "OR (p IS NOT NULL AND LOWER(CAST(p.name AS string)) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))) "
                        +
                        "AND (:managerId IS NULL OR e.manager.id = :managerId) " +
                        "AND (:status IS NULL " +
                        "OR EXISTS (SELECT a2 FROM Allocation a2 WHERE a2.employee = e AND a2.status = :status) " +
                        "OR NOT EXISTS (SELECT a3 FROM Allocation a3 WHERE a3.employee = e)) " +
                        "AND (:parentTower IS NULL OR e.parentTower = :parentTower) " +
                        "AND (:tower IS NULL OR e.tower = :tower) " +
                        "ORDER BY e.name", countQuery = "SELECT COUNT(DISTINCT e.id) FROM Employee e " +
                                        "LEFT JOIN Allocation a ON a.employee = e " +
                                        "LEFT JOIN a.project p " +
                                        "WHERE e.isActive = true " +
                                        "AND (:search IS NULL OR LOWER(CAST(e.name AS string)) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) "
                                        +
                                        "OR (p IS NOT NULL AND LOWER(CAST(p.name AS string)) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))) "
                                        +
                                        "AND (:managerId IS NULL OR e.manager.id = :managerId) " +
                                        "AND (:status IS NULL " +
                                        "OR EXISTS (SELECT a2 FROM Allocation a2 WHERE a2.employee = e AND a2.status = :status) "
                                        +
                                        "OR NOT EXISTS (SELECT a3 FROM Allocation a3 WHERE a3.employee = e)) " +
                                        "AND (:parentTower IS NULL OR e.parentTower = :parentTower) " +
                                        "AND (:tower IS NULL OR e.tower = :tower)")
        Page<Employee> findEmployeesForAllocationView(
                        @Param("search") String search,
                        @Param("managerId") Long managerId,
                        @Param("status") Allocation.AllocationStatus status,
                        @Param("parentTower") String parentTower,
                        @Param("tower") String tower,
                        Pageable pageable);

        // Find BENCH employees: those with no active numeric allocation in current
        // year/month
        // Uses monthly_allocations table for normalized data model
        @Query(value = "SELECT DISTINCT e.* FROM employees e " +
                        "WHERE e.is_active = true " +
                        "AND e.status = 'ACTIVE' " +
                        "AND (:search IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                        "AND (:managerId IS NULL OR e.manager_id = :managerId) " +
                        "AND NOT EXISTS (" +
                        "  SELECT 1 FROM allocations a " +
                        "  JOIN monthly_allocations ma ON ma.allocation_id = a.id " +
                        "  WHERE a.employee_id = e.id " +
                        "  AND a.status = 'ACTIVE' " +
                        "  AND ma.year = :currentYear " +
                        "  AND ma.month = :currentMonth " +
                        "  AND ma.percentage > 0" +
                        ") " +
                        "ORDER BY e.name", countQuery = "SELECT COUNT(DISTINCT e.id) FROM employees e " +
                                        "WHERE e.is_active = true " +
                                        "AND e.status = 'ACTIVE' " +
                                        "AND (:search IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%'))) "
                                        +
                                        "AND (:managerId IS NULL OR e.manager_id = :managerId) " +
                                        "AND NOT EXISTS (" +
                                        "  SELECT 1 FROM allocations a " +
                                        "  JOIN monthly_allocations ma ON ma.allocation_id = a.id " +
                                        "  WHERE a.employee_id = e.id " +
                                        "  AND a.status = 'ACTIVE' " +
                                        "  AND ma.year = :currentYear " +
                                        "  AND ma.month = :currentMonth " +
                                        "  AND ma.percentage > 0" +
                                        ")", nativeQuery = true)
        Page<Employee> findBenchEmployees(
                        @Param("search") String search,
                        @Param("managerId") Long managerId,
                        @Param("currentYear") int currentYear,
                        @Param("currentMonth") int currentMonth,
                        Pageable pageable);

        // Find BENCH employees by IDs (for non-admin access)
        @Query(value = "SELECT DISTINCT e.* FROM employees e " +
                        "WHERE e.is_active = true " +
                        "AND e.status = 'ACTIVE' " +
                        "AND e.id IN :employeeIds " +
                        "AND (:search IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                        "AND (:managerId IS NULL OR e.manager_id = :managerId) " +
                        "AND NOT EXISTS (" +
                        "  SELECT 1 FROM allocations a " +
                        "  JOIN monthly_allocations ma ON ma.allocation_id = a.id " +
                        "  WHERE a.employee_id = e.id " +
                        "  AND a.status = 'ACTIVE' " +
                        "  AND ma.year = :currentYear " +
                        "  AND ma.month = :currentMonth " +
                        "  AND ma.percentage > 0" +
                        ") " +
                        "ORDER BY e.name", countQuery = "SELECT COUNT(DISTINCT e.id) FROM employees e " +
                                        "WHERE e.is_active = true " +
                                        "AND e.status = 'ACTIVE' " +
                                        "AND e.id IN :employeeIds " +
                                        "AND (:search IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%'))) "
                                        +
                                        "AND (:managerId IS NULL OR e.manager_id = :managerId) " +
                                        "AND NOT EXISTS (" +
                                        "  SELECT 1 FROM allocations a " +
                                        "  JOIN monthly_allocations ma ON ma.allocation_id = a.id " +
                                        "  WHERE a.employee_id = e.id " +
                                        "  AND a.status = 'ACTIVE' " +
                                        "  AND ma.year = :currentYear " +
                                        "  AND ma.month = :currentMonth " +
                                        "  AND ma.percentage > 0" +
                                        ")", nativeQuery = true)
        Page<Employee> findBenchEmployeesByIds(
                        @Param("employeeIds") List<Long> employeeIds,
                        @Param("search") String search,
                        @Param("managerId") Long managerId,
                        @Param("currentYear") int currentYear,
                        @Param("currentMonth") int currentMonth,
                        Pageable pageable);

        // Find PROSPECT employees: those with PROSPECT allocation status
        @Query(value = "SELECT DISTINCT e.* FROM employees e " +
                        "WHERE e.is_active = true " +
                        "AND e.status = 'ACTIVE' " +
                        "AND (:search IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                        "AND (:managerId IS NULL OR e.manager_id = :managerId) " +
                        "AND EXISTS (" +
                        "  SELECT 1 FROM allocations a " +
                        "  WHERE a.employee_id = e.id " +
                        "  AND a.status = 'PROSPECT'" +
                        ") " +
                        "AND NOT EXISTS (" +
                        "  SELECT 1 FROM allocations a " +
                        "  JOIN monthly_allocations ma ON ma.allocation_id = a.id " +
                        "  WHERE a.employee_id = e.id " +
                        "  AND a.status = 'ACTIVE' " +
                        "  AND ma.year = :currentYear " +
                        "  AND ma.month = :currentMonth " +
                        "  AND ma.percentage > 0" +
                        ") " +
                        "ORDER BY e.name", countQuery = "SELECT COUNT(DISTINCT e.id) FROM employees e " +
                                        "WHERE e.is_active = true " +
                                        "AND e.status = 'ACTIVE' " +
                                        "AND (:search IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%'))) "
                                        +
                                        "AND (:managerId IS NULL OR e.manager_id = :managerId) " +
                                        "AND EXISTS (" +
                                        "  SELECT 1 FROM allocations a " +
                                        "  WHERE a.employee_id = e.id " +
                                        "  AND a.status = 'PROSPECT'" +
                                        ") " +
                                        "AND NOT EXISTS (" +
                                        "  SELECT 1 FROM allocations a " +
                                        "  JOIN monthly_allocations ma ON ma.allocation_id = a.id " +
                                        "  WHERE a.employee_id = e.id " +
                                        "  AND a.status = 'ACTIVE' " +
                                        "  AND ma.year = :currentYear " +
                                        "  AND ma.month = :currentMonth " +
                                        "  AND ma.percentage > 0" +
                                        ")", nativeQuery = true)
        Page<Employee> findProspectEmployees(
                        @Param("search") String search,
                        @Param("managerId") Long managerId,
                        @Param("currentYear") int currentYear,
                        @Param("currentMonth") int currentMonth,
                        Pageable pageable);

        // Find PROSPECT employees by IDs (for non-admin access)
        @Query(value = "SELECT DISTINCT e.* FROM employees e " +
                        "WHERE e.is_active = true " +
                        "AND e.status = 'ACTIVE' " +
                        "AND e.id IN :employeeIds " +
                        "AND (:search IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                        "AND (:managerId IS NULL OR e.manager_id = :managerId) " +
                        "AND EXISTS (" +
                        "  SELECT 1 FROM allocations a " +
                        "  WHERE a.employee_id = e.id " +
                        "  AND a.status = 'PROSPECT'" +
                        ") " +
                        "AND NOT EXISTS (" +
                        "  SELECT 1 FROM allocations a " +
                        "  JOIN monthly_allocations ma ON ma.allocation_id = a.id " +
                        "  WHERE a.employee_id = e.id " +
                        "  AND a.status = 'ACTIVE' " +
                        "  AND ma.year = :currentYear " +
                        "  AND ma.month = :currentMonth " +
                        "  AND ma.percentage > 0" +
                        ") " +
                        "ORDER BY e.name", countQuery = "SELECT COUNT(DISTINCT e.id) FROM employees e " +
                                        "WHERE e.is_active = true " +
                                        "AND e.status = 'ACTIVE' " +
                                        "AND e.id IN :employeeIds " +
                                        "AND (:search IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%'))) "
                                        +
                                        "AND (:managerId IS NULL OR e.manager_id = :managerId) " +
                                        "AND EXISTS (" +
                                        "  SELECT 1 FROM allocations a " +
                                        "  WHERE a.employee_id = e.id " +
                                        "  AND a.status = 'PROSPECT'" +
                                        ") " +
                                        "AND NOT EXISTS (" +
                                        "  SELECT 1 FROM allocations a " +
                                        "  JOIN monthly_allocations ma ON ma.allocation_id = a.id " +
                                        "  WHERE a.employee_id = e.id " +
                                        "  AND a.status = 'ACTIVE' " +
                                        "  AND ma.year = :currentYear " +
                                        "  AND ma.month = :currentMonth " +
                                        "  AND ma.percentage > 0" +
                                        ")", nativeQuery = true)
        Page<Employee> findProspectEmployeesByIds(
                        @Param("employeeIds") List<Long> employeeIds,
                        @Param("search") String search,
                        @Param("managerId") Long managerId,
                        @Param("currentYear") int currentYear,
                        @Param("currentMonth") int currentMonth,
                        Pageable pageable);

        // Find ACTIVE employees: those with active numeric allocation in current
        // year/month
        @Query(value = "SELECT DISTINCT e.* FROM employees e " +
                        "WHERE e.is_active = true " +
                        "AND e.status = 'ACTIVE' " +
                        "AND (:search IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                        "AND (:managerId IS NULL OR e.manager_id = :managerId) " +
                        "AND EXISTS (" +
                        "  SELECT 1 FROM allocations a " +
                        "  JOIN monthly_allocations ma ON ma.allocation_id = a.id " +
                        "  WHERE a.employee_id = e.id " +
                        "  AND a.status = 'ACTIVE' " +
                        "  AND ma.year = :currentYear " +
                        "  AND ma.month = :currentMonth " +
                        "  AND ma.percentage > 0" +
                        ") " +
                        "ORDER BY e.name", countQuery = "SELECT COUNT(DISTINCT e.id) FROM employees e " +
                                        "WHERE e.is_active = true " +
                                        "AND e.status = 'ACTIVE' " +
                                        "AND (:search IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%'))) "
                                        +
                                        "AND (:managerId IS NULL OR e.manager_id = :managerId) " +
                                        "AND EXISTS (" +
                                        "  SELECT 1 FROM allocations a " +
                                        "  JOIN monthly_allocations ma ON ma.allocation_id = a.id " +
                                        "  WHERE a.employee_id = e.id " +
                                        "  AND a.status = 'ACTIVE' " +
                                        "  AND ma.year = :currentYear " +
                                        "  AND ma.month = :currentMonth " +
                                        "  AND ma.percentage > 0" +
                                        ")", nativeQuery = true)
        Page<Employee> findActiveAllocatedEmployees(
                        @Param("search") String search,
                        @Param("managerId") Long managerId,
                        @Param("currentYear") int currentYear,
                        @Param("currentMonth") int currentMonth,
                        Pageable pageable);

        // Find ACTIVE employees by IDs (for non-admin access)
        @Query(value = "SELECT DISTINCT e.* FROM employees e " +
                        "WHERE e.is_active = true " +
                        "AND e.status = 'ACTIVE' " +
                        "AND e.id IN :employeeIds " +
                        "AND (:search IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                        "AND (:managerId IS NULL OR e.manager_id = :managerId) " +
                        "AND EXISTS (" +
                        "  SELECT 1 FROM allocations a " +
                        "  JOIN monthly_allocations ma ON ma.allocation_id = a.id " +
                        "  WHERE a.employee_id = e.id " +
                        "  AND a.status = 'ACTIVE' " +
                        "  AND ma.year = :currentYear " +
                        "  AND ma.month = :currentMonth " +
                        "  AND ma.percentage > 0" +
                        ") " +
                        "ORDER BY e.name", countQuery = "SELECT COUNT(DISTINCT e.id) FROM employees e " +
                                        "WHERE e.is_active = true " +
                                        "AND e.status = 'ACTIVE' " +
                                        "AND e.id IN :employeeIds " +
                                        "AND (:search IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%'))) "
                                        +
                                        "AND (:managerId IS NULL OR e.manager_id = :managerId) " +
                                        "AND EXISTS (" +
                                        "  SELECT 1 FROM allocations a " +
                                        "  JOIN monthly_allocations ma ON ma.allocation_id = a.id " +
                                        "  WHERE a.employee_id = e.id " +
                                        "  AND a.status = 'ACTIVE' " +
                                        "  AND ma.year = :currentYear " +
                                        "  AND ma.month = :currentMonth " +
                                        "  AND ma.percentage > 0" +
                                        ")", nativeQuery = true)
        Page<Employee> findActiveAllocatedEmployeesByIds(
                        @Param("employeeIds") List<Long> employeeIds,
                        @Param("search") String search,
                        @Param("managerId") Long managerId,
                        @Param("currentYear") int currentYear,
                        @Param("currentMonth") int currentMonth,
                        Pageable pageable);

        // Faceted Search: Get Towers based on filters
        @Query("SELECT DISTINCT e.tower FROM Employee e WHERE e.tower IS NOT NULL " +
                        "AND (:managerId IS NULL OR e.manager.id = :managerId) " +
                        "AND (:status IS NULL " +
                        "  OR EXISTS (SELECT a FROM Allocation a WHERE a.employee = e AND a.status = :status) " +
                        "  OR (:status = 'BENCH' AND NOT EXISTS (SELECT a FROM Allocation a WHERE a.employee = e))) " +
                        "AND e.isActive = true")
        List<String> findDistinctTowersByFilters(
                        @Param("managerId") Long managerId,
                        @Param("status") String status);

        // Faceted Search: Get Managers based on filters
        @Query("SELECT DISTINCT m FROM Employee m WHERE m.isActive = true " +
                        "AND EXISTS (SELECT e FROM Employee e WHERE e.manager = m AND e.isActive = true " +
                        "  AND (:tower IS NULL OR e.tower = :tower) " +
                        "  AND (:status IS NULL " +
                        "    OR EXISTS (SELECT a FROM Allocation a WHERE a.employee = e AND a.status = :status) " +
                        "    OR (:status = 'BENCH' AND NOT EXISTS (SELECT a FROM Allocation a WHERE a.employee = e))) "
                        +
                        ")")
        List<Employee> findManagersByFilters(
                        @Param("tower") String tower,
                        @Param("status") String status);
}
