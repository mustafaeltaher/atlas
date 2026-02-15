package com.atlas.repository;

import com.atlas.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

        List<Employee> findByManager(Employee manager);

        long countByManager(Employee manager);

        @Query("SELECT COUNT(e) FROM Employee e WHERE e.resignationDate IS NULL")
        long countActiveEmployees();

        boolean existsByOracleId(Integer oracleId);

        boolean existsByEmail(String email);

        @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.manager WHERE e.resignationDate IS NULL")
        List<Employee> findAllWithManager();

        @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.tower")
        List<Employee> findAllWithTower();

        // Recursive query to find all subordinate IDs (direct and indirect reports)
        // This replaces the in-memory recursive check and allows unified access control
        @Query(value = "WITH RECURSIVE subordinates AS (" +
                        "  SELECT id FROM employees WHERE id = :managerId " +
                        "  UNION " +
                        "  SELECT e.id FROM employees e " +
                        "  INNER JOIN subordinates s ON s.id = e.manager_id " +
                        ") SELECT id FROM subordinates", nativeQuery = true)
        List<Long> findAllSubordinateIds(@Param("managerId") Long managerId);

        // Paginated query for allocation view: LEFT JOINs with allocations
        @Query(value = "SELECT DISTINCT e FROM Employee e " +
                        "LEFT JOIN Allocation a ON a.employee = e " +
                        "LEFT JOIN a.project p " +
                        "WHERE e.resignationDate IS NULL " +
                        "AND (:search IS NULL OR LOWER(e.name) LIKE :search " +
                        "OR (p IS NOT NULL AND LOWER(p.description) LIKE :search)) " +
                        "AND (:managerId IS NULL OR e.manager.id = :managerId) " +
                        "ORDER BY e.name", countQuery = "SELECT COUNT(DISTINCT e.id) FROM Employee e " +
                                        "LEFT JOIN Allocation a ON a.employee = e " +
                                        "LEFT JOIN a.project p " +
                                        "WHERE e.resignationDate IS NULL " +
                                        "AND (:search IS NULL OR LOWER(e.name) LIKE :search " +
                                        "OR (p IS NOT NULL AND LOWER(p.description) LIKE :search)) " +
                                        "AND (:managerId IS NULL OR e.manager.id = :managerId)")
        Page<Employee> findEmployeesForAllocationView(
                        @Param("search") String search,
                        @Param("managerId") Long managerId,
                        Pageable pageable);

        // Paginated query for allocation view by employee IDs
        @Query(value = "SELECT DISTINCT e FROM Employee e " +
                        "LEFT JOIN Allocation a ON a.employee = e " +
                        "LEFT JOIN a.project p " +
                        "WHERE e.resignationDate IS NULL AND e.id IN :employeeIds " +
                        "AND (:search IS NULL OR LOWER(e.name) LIKE :search " +
                        "OR (p IS NOT NULL AND LOWER(p.description) LIKE :search)) " +
                        "AND (:managerId IS NULL OR e.manager.id = :managerId) " +
                        "ORDER BY e.name", countQuery = "SELECT COUNT(DISTINCT e.id) FROM Employee e " +
                                        "LEFT JOIN Allocation a ON a.employee = e " +
                                        "LEFT JOIN a.project p " +
                                        "WHERE e.resignationDate IS NULL AND e.id IN :employeeIds " +
                                        "AND (:search IS NULL OR LOWER(e.name) LIKE :search " +
                                        "OR (p IS NOT NULL AND LOWER(p.description) LIKE :search)) " +
                                        "AND (:managerId IS NULL OR e.manager.id = :managerId)")
        Page<Employee> findEmployeesForAllocationViewByIds(
                        @Param("employeeIds") List<Long> employeeIds,
                        @Param("search") String search,
                        @Param("managerId") Long managerId,
                        Pageable pageable);

        // Find BENCH employees: no active PROJECT, no PROSPECT, no MATERNITY, no
        // VACATION
        @Query(value = "SELECT DISTINCT e.* FROM employees e " +
                        "WHERE e.resignation_date IS NULL " +
                        "AND (:search IS NULL OR LOWER(e.name) LIKE :search) " +
                        "AND (CAST(:managerId AS bigint) IS NULL OR e.manager_id = :managerId) " +
                        "AND NOT EXISTS (" +
                        "  SELECT 1 FROM allocations a " +
                        "  JOIN monthly_allocations ma ON ma.allocation_id = a.id " +
                        "  WHERE a.employee_id = e.id " +
                        "  AND a.allocation_type = 'PROJECT' " +
                        "  AND ma.year = :currentYear " +
                        "  AND ma.month = :currentMonth " +
                        "  AND ma.percentage > 0) " +
                        "AND NOT EXISTS (SELECT 1 FROM allocations a WHERE a.employee_id = e.id AND a.allocation_type = 'PROSPECT') "
                        +
                        "AND NOT EXISTS (SELECT 1 FROM allocations a WHERE a.employee_id = e.id AND a.allocation_type = 'MATERNITY') "
                        +
                        "AND NOT EXISTS (SELECT 1 FROM allocations a WHERE a.employee_id = e.id AND a.allocation_type = 'VACATION') "
                        +
                        "ORDER BY e.name", countQuery = "SELECT COUNT(DISTINCT e.id) FROM employees e " +
                                        "WHERE e.resignation_date IS NULL " +
                                        "AND (:search IS NULL OR LOWER(e.name) LIKE :search) " +
                                        "AND (CAST(:managerId AS bigint) IS NULL OR e.manager_id = :managerId) " +
                                        "AND NOT EXISTS (" +
                                        "  SELECT 1 FROM allocations a " +
                                        "  JOIN monthly_allocations ma ON ma.allocation_id = a.id " +
                                        "  WHERE a.employee_id = e.id " +
                                        "  AND a.allocation_type = 'PROJECT' " +
                                        "  AND ma.year = :currentYear " +
                                        "  AND ma.month = :currentMonth " +
                                        "  AND ma.percentage > 0) " +
                                        "AND NOT EXISTS (SELECT 1 FROM allocations a WHERE a.employee_id = e.id AND a.allocation_type = 'PROSPECT') "
                                        +
                                        "AND NOT EXISTS (SELECT 1 FROM allocations a WHERE a.employee_id = e.id AND a.allocation_type = 'MATERNITY') "
                                        +
                                        "AND NOT EXISTS (SELECT 1 FROM allocations a WHERE a.employee_id = e.id AND a.allocation_type = 'VACATION')", nativeQuery = true)
        Page<Employee> findBenchEmployees(
                        @Param("search") String search,
                        @Param("managerId") Long managerId,
                        @Param("currentYear") int currentYear,
                        @Param("currentMonth") int currentMonth,
                        Pageable pageable);

        // Find BENCH employees by IDs
        @Query(value = "SELECT DISTINCT e.* FROM employees e " +
                        "WHERE e.resignation_date IS NULL " +
                        "AND e.id IN :employeeIds " +
                        "AND (:search IS NULL OR LOWER(e.name) LIKE :search) " +
                        "AND (CAST(:managerId AS bigint) IS NULL OR e.manager_id = :managerId) " +
                        "AND NOT EXISTS (" +
                        "  SELECT 1 FROM allocations a " +
                        "  JOIN monthly_allocations ma ON ma.allocation_id = a.id " +
                        "  WHERE a.employee_id = e.id " +
                        "  AND a.allocation_type = 'PROJECT' " +
                        "  AND ma.year = :currentYear " +
                        "  AND ma.month = :currentMonth " +
                        "  AND ma.percentage > 0) " +
                        "AND NOT EXISTS (SELECT 1 FROM allocations a WHERE a.employee_id = e.id AND a.allocation_type = 'PROSPECT') "
                        +
                        "AND NOT EXISTS (SELECT 1 FROM allocations a WHERE a.employee_id = e.id AND a.allocation_type = 'MATERNITY') "
                        +
                        "AND NOT EXISTS (SELECT 1 FROM allocations a WHERE a.employee_id = e.id AND a.allocation_type = 'VACATION') "
                        +
                        "ORDER BY e.name", countQuery = "SELECT COUNT(DISTINCT e.id) FROM employees e " +
                                        "WHERE e.resignation_date IS NULL " +
                                        "AND e.id IN :employeeIds " +
                                        "AND (:search IS NULL OR LOWER(e.name) LIKE :search) " +
                                        "AND (CAST(:managerId AS bigint) IS NULL OR e.manager_id = :managerId) " +
                                        "AND NOT EXISTS (" +
                                        "  SELECT 1 FROM allocations a " +
                                        "  JOIN monthly_allocations ma ON ma.allocation_id = a.id " +
                                        "  WHERE a.employee_id = e.id " +
                                        "  AND a.allocation_type = 'PROJECT' " +
                                        "  AND ma.year = :currentYear " +
                                        "  AND ma.month = :currentMonth " +
                                        "  AND ma.percentage > 0) " +
                                        "AND NOT EXISTS (SELECT 1 FROM allocations a WHERE a.employee_id = e.id AND a.allocation_type = 'PROSPECT') "
                                        +
                                        "AND NOT EXISTS (SELECT 1 FROM allocations a WHERE a.employee_id = e.id AND a.allocation_type = 'MATERNITY') "
                                        +
                                        "AND NOT EXISTS (SELECT 1 FROM allocations a WHERE a.employee_id = e.id AND a.allocation_type = 'VACATION')", nativeQuery = true)
        Page<Employee> findBenchEmployeesByIds(
                        @Param("employeeIds") List<Long> employeeIds,
                        @Param("search") String search,
                        @Param("managerId") Long managerId,
                        @Param("currentYear") int currentYear,
                        @Param("currentMonth") int currentMonth,
                        Pageable pageable);

        // Find PROSPECT employees: has PROSPECT allocation but no active PROJECT
        @Query(value = "SELECT DISTINCT e.* FROM employees e " +
                        "WHERE e.resignation_date IS NULL " +
                        "AND (:search IS NULL OR LOWER(e.name) LIKE :search) " +
                        "AND (CAST(:managerId AS bigint) IS NULL OR e.manager_id = :managerId) " +
                        "AND EXISTS (SELECT 1 FROM allocations a WHERE a.employee_id = e.id AND a.allocation_type = 'PROSPECT') "
                        +
                        "AND NOT EXISTS (" +
                        "  SELECT 1 FROM allocations a " +
                        "  JOIN monthly_allocations ma ON ma.allocation_id = a.id " +
                        "  WHERE a.employee_id = e.id AND a.allocation_type = 'PROJECT' " +
                        "  AND ma.year = :currentYear AND ma.month = :currentMonth AND ma.percentage > 0) " +
                        "ORDER BY e.name", countQuery = "SELECT COUNT(DISTINCT e.id) FROM employees e " +
                                        "WHERE e.resignation_date IS NULL " +
                                        "AND (:search IS NULL OR LOWER(e.name) LIKE :search) " +
                                        "AND (CAST(:managerId AS bigint) IS NULL OR e.manager_id = :managerId) " +
                                        "AND EXISTS (SELECT 1 FROM allocations a WHERE a.employee_id = e.id AND a.allocation_type = 'PROSPECT') "
                                        +
                                        "AND NOT EXISTS (" +
                                        "  SELECT 1 FROM allocations a " +
                                        "  JOIN monthly_allocations ma ON ma.allocation_id = a.id " +
                                        "  WHERE a.employee_id = e.id AND a.allocation_type = 'PROJECT' " +
                                        "  AND ma.year = :currentYear AND ma.month = :currentMonth AND ma.percentage > 0)", nativeQuery = true)
        Page<Employee> findProspectEmployees(
                        @Param("search") String search,
                        @Param("managerId") Long managerId,
                        @Param("currentYear") int currentYear,
                        @Param("currentMonth") int currentMonth,
                        Pageable pageable);

        @Query(value = "SELECT DISTINCT e.* FROM employees e " +
                        "WHERE e.resignation_date IS NULL AND e.id IN :employeeIds " +
                        "AND (:search IS NULL OR LOWER(e.name) LIKE :search) " +
                        "AND (CAST(:managerId AS bigint) IS NULL OR e.manager_id = :managerId) " +
                        "AND EXISTS (SELECT 1 FROM allocations a WHERE a.employee_id = e.id AND a.allocation_type = 'PROSPECT') "
                        +
                        "AND NOT EXISTS (" +
                        "  SELECT 1 FROM allocations a " +
                        "  JOIN monthly_allocations ma ON ma.allocation_id = a.id " +
                        "  WHERE a.employee_id = e.id AND a.allocation_type = 'PROJECT' " +
                        "  AND ma.year = :currentYear AND ma.month = :currentMonth AND ma.percentage > 0) " +
                        "ORDER BY e.name", countQuery = "SELECT COUNT(DISTINCT e.id) FROM employees e " +
                                        "WHERE e.resignation_date IS NULL AND e.id IN :employeeIds " +
                                        "AND (:search IS NULL OR LOWER(e.name) LIKE :search) " +
                                        "AND (CAST(:managerId AS bigint) IS NULL OR e.manager_id = :managerId) " +
                                        "AND EXISTS (SELECT 1 FROM allocations a WHERE a.employee_id = e.id AND a.allocation_type = 'PROSPECT') "
                                        +
                                        "AND NOT EXISTS (" +
                                        "  SELECT 1 FROM allocations a " +
                                        "  JOIN monthly_allocations ma ON ma.allocation_id = a.id " +
                                        "  WHERE a.employee_id = e.id AND a.allocation_type = 'PROJECT' " +
                                        "  AND ma.year = :currentYear AND ma.month = :currentMonth AND ma.percentage > 0)", nativeQuery = true)
        Page<Employee> findProspectEmployeesByIds(
                        @Param("employeeIds") List<Long> employeeIds,
                        @Param("search") String search,
                        @Param("managerId") Long managerId,
                        @Param("currentYear") int currentYear,
                        @Param("currentMonth") int currentMonth,
                        Pageable pageable);

        default Page<Employee> findProspectEmployeesFiltered(
                        List<Long> employeeIds, String search, Long managerId,
                        int currentYear, int currentMonth, Pageable pageable) {
                if (employeeIds == null)
                        return findProspectEmployees(search, managerId, currentYear, currentMonth, pageable);
                return findProspectEmployeesByIds(employeeIds, search, managerId, currentYear, currentMonth, pageable);
        }

        // Find ACTIVE allocated employees: those with PROJECT allocation in current
        // year/month
        @Query(value = "SELECT DISTINCT e.* FROM employees e " +
                        "WHERE e.resignation_date IS NULL " +
                        "AND (:search IS NULL OR LOWER(e.name) LIKE :search) " +
                        "AND (CAST(:managerId AS bigint) IS NULL OR e.manager_id = :managerId) " +
                        "AND EXISTS (" +
                        "  SELECT 1 FROM allocations a " +
                        "  JOIN monthly_allocations ma ON ma.allocation_id = a.id " +
                        "  WHERE a.employee_id = e.id " +
                        "  AND a.allocation_type = 'PROJECT' " +
                        "  AND ma.year = :currentYear " +
                        "  AND ma.month = :currentMonth " +
                        "  AND ma.percentage > 0" +
                        ") " +
                        "ORDER BY e.name", countQuery = "SELECT COUNT(DISTINCT e.id) FROM employees e " +
                                        "WHERE e.resignation_date IS NULL " +
                                        "AND (:search IS NULL OR LOWER(e.name) LIKE :search) " +
                                        "AND (CAST(:managerId AS bigint) IS NULL OR e.manager_id = :managerId) " +
                                        "AND EXISTS (" +
                                        "  SELECT 1 FROM allocations a " +
                                        "  JOIN monthly_allocations ma ON ma.allocation_id = a.id " +
                                        "  WHERE a.employee_id = e.id " +
                                        "  AND a.allocation_type = 'PROJECT' " +
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

        // Find ACTIVE allocated employees by IDs
        @Query(value = "SELECT DISTINCT e.* FROM employees e " +
                        "WHERE e.resignation_date IS NULL " +
                        "AND e.id IN :employeeIds " +
                        "AND (:search IS NULL OR LOWER(e.name) LIKE :search) " +
                        "AND (CAST(:managerId AS bigint) IS NULL OR e.manager_id = :managerId) " +
                        "AND EXISTS (" +
                        "  SELECT 1 FROM allocations a " +
                        "  JOIN monthly_allocations ma ON ma.allocation_id = a.id " +
                        "  WHERE a.employee_id = e.id " +
                        "  AND a.allocation_type = 'PROJECT' " +
                        "  AND ma.year = :currentYear " +
                        "  AND ma.month = :currentMonth " +
                        "  AND ma.percentage > 0" +
                        ") " +
                        "ORDER BY e.name", countQuery = "SELECT COUNT(DISTINCT e.id) FROM employees e " +
                                        "WHERE e.resignation_date IS NULL " +
                                        "AND e.id IN :employeeIds " +
                                        "AND (:search IS NULL OR LOWER(e.name) LIKE :search) " +
                                        "AND (CAST(:managerId AS bigint) IS NULL OR e.manager_id = :managerId) " +
                                        "AND EXISTS (" +
                                        "  SELECT 1 FROM allocations a " +
                                        "  JOIN monthly_allocations ma ON ma.allocation_id = a.id " +
                                        "  WHERE a.employee_id = e.id " +
                                        "  AND a.allocation_type = 'PROJECT' " +
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

        // Find employees by allocation type (PROJECT, PROSPECT, VACATION, MATERNITY)
        @Query(value = "SELECT DISTINCT e.* FROM employees e " +
                        "WHERE e.resignation_date IS NULL " +
                        "AND (:search IS NULL OR LOWER(e.name) LIKE :search) " +
                        "AND (CAST(:managerId AS bigint) IS NULL OR e.manager_id = :managerId) " +
                        "AND EXISTS (" +
                        "  SELECT 1 FROM allocations a " +
                        "  WHERE a.employee_id = e.id " +
                        "  AND CAST(a.allocation_type AS text) = :allocationType" +
                        ") " +
                        "ORDER BY e.name", countQuery = "SELECT COUNT(DISTINCT e.id) FROM employees e " +
                                        "WHERE e.resignation_date IS NULL " +
                                        "AND (:search IS NULL OR LOWER(e.name) LIKE :search) " +
                                        "AND (CAST(:managerId AS bigint) IS NULL OR e.manager_id = :managerId) " +
                                        "AND EXISTS (" +
                                        "  SELECT 1 FROM allocations a " +
                                        "  WHERE a.employee_id = e.id " +
                                        "  AND CAST(a.allocation_type AS text) = :allocationType" +
                                        ")", nativeQuery = true)
        Page<Employee> findEmployeesByAllocationType(
                        @Param("search") String search,
                        @Param("managerId") Long managerId,
                        @Param("allocationType") String allocationType,
                        Pageable pageable);

        // Find employees by allocation type, restricted to IDs
        @Query(value = "SELECT DISTINCT e.* FROM employees e " +
                        "WHERE e.resignation_date IS NULL " +
                        "AND e.id IN :employeeIds " +
                        "AND (:search IS NULL OR LOWER(e.name) LIKE :search) " +
                        "AND (CAST(:managerId AS bigint) IS NULL OR e.manager_id = :managerId) " +
                        "AND EXISTS (" +
                        "  SELECT 1 FROM allocations a " +
                        "  WHERE a.employee_id = e.id " +
                        "  AND CAST(a.allocation_type AS text) = :allocationType" +
                        ") " +
                        "ORDER BY e.name", countQuery = "SELECT COUNT(DISTINCT e.id) FROM employees e " +
                                        "WHERE e.resignation_date IS NULL " +
                                        "AND e.id IN :employeeIds " +
                                        "AND (:search IS NULL OR LOWER(e.name) LIKE :search) " +
                                        "AND (CAST(:managerId AS bigint) IS NULL OR e.manager_id = :managerId) " +
                                        "AND EXISTS (" +
                                        "  SELECT 1 FROM allocations a " +
                                        "  WHERE a.employee_id = e.id " +
                                        "  AND CAST(a.allocation_type AS text) = :allocationType" +
                                        ")", nativeQuery = true)
        Page<Employee> findEmployeesByAllocationTypeByIds(
                        @Param("employeeIds") List<Long> employeeIds,
                        @Param("search") String search,
                        @Param("managerId") Long managerId,
                        @Param("allocationType") String allocationType,
                        Pageable pageable);

        // Unified routing methods: accept nullable employeeIds (null = no filter)

        default Page<Employee> findEmployeesForAllocationViewFiltered(
                        List<Long> employeeIds, String search, Long managerId, Pageable pageable) {
                if (employeeIds == null)
                        return findEmployeesForAllocationView(search, managerId, pageable);
                return findEmployeesForAllocationViewByIds(employeeIds, search, managerId, pageable);
        }

        default Page<Employee> findBenchEmployeesFiltered(
                        List<Long> employeeIds, String search, Long managerId,
                        int currentYear, int currentMonth, Pageable pageable) {
                if (employeeIds == null)
                        return findBenchEmployees(search, managerId, currentYear, currentMonth, pageable);
                return findBenchEmployeesByIds(employeeIds, search, managerId, currentYear, currentMonth, pageable);
        }

        default Page<Employee> findActiveAllocatedEmployeesFiltered(
                        List<Long> employeeIds, String search, Long managerId,
                        int currentYear, int currentMonth, Pageable pageable) {
                if (employeeIds == null)
                        return findActiveAllocatedEmployees(search, managerId, currentYear, currentMonth, pageable);
                return findActiveAllocatedEmployeesByIds(employeeIds, search, managerId, currentYear, currentMonth,
                                pageable);
        }

        default Page<Employee> findEmployeesByAllocationTypeFiltered(
                        List<Long> employeeIds, String search, Long managerId,
                        String allocationType, Pageable pageable) {
                if (employeeIds == null)
                        return findEmployeesByAllocationType(search, managerId, allocationType, pageable);
                return findEmployeesByAllocationTypeByIds(employeeIds, search, managerId, allocationType, pageable);
        }

        // --- Manager dropdown queries ---

        // 1. No filter: all managers of active employees
        @Query("SELECT DISTINCT m FROM Employee m " +
                        "WHERE m.resignationDate IS NULL " +
                        "AND EXISTS (SELECT 1 FROM Employee e WHERE e.manager = m AND e.resignationDate IS NULL) " +
                        "ORDER BY m.name")
        List<Employee> findDistinctManagers();

        @Query("SELECT DISTINCT m FROM Employee m " +
                        "WHERE m.resignationDate IS NULL " +
                        "AND EXISTS (SELECT 1 FROM Employee e WHERE e.manager = m AND e.resignationDate IS NULL AND e.id IN :ids) "
                        +
                        "ORDER BY m.name")
        List<Employee> findDistinctManagersByIds(@Param("ids") List<Long> ids);

        default List<Employee> findDistinctManagersFiltered(List<Long> ids) {
                if (ids == null)
                        return findDistinctManagers();
                return findDistinctManagersByIds(ids);
        }

        // 2. Standard allocation type filter (e.g. PROJECT, PROSPECT)
        @Query(value = "SELECT DISTINCT m.* FROM employees m " +
                        "WHERE m.resignation_date IS NULL " +
                        "AND EXISTS (SELECT 1 FROM employees e " +
                        "  JOIN allocations a ON a.employee_id = e.id " +
                        "  WHERE e.manager_id = m.id AND e.resignation_date IS NULL " +
                        "  AND CAST(a.allocation_type AS text) = :type) " +
                        "ORDER BY m.name", nativeQuery = true)
        List<Employee> findDistinctManagersByAllocationType(@Param("type") String type);

        @Query(value = "SELECT DISTINCT m.* FROM employees m " +
                        "WHERE m.resignation_date IS NULL " +
                        "AND EXISTS (SELECT 1 FROM employees e " +
                        "  JOIN allocations a ON a.employee_id = e.id " +
                        "  WHERE e.manager_id = m.id AND e.resignation_date IS NULL AND e.id IN :ids " +
                        "  AND CAST(a.allocation_type AS text) = :type) " +
                        "ORDER BY m.name", nativeQuery = true)
        List<Employee> findDistinctManagersByAllocationTypeByIds(
                        @Param("ids") List<Long> ids, @Param("type") String type);

        default List<Employee> findDistinctManagersByAllocationTypeFiltered(List<Long> ids, String type) {
                if (ids == null)
                        return findDistinctManagersByAllocationType(type);
                return findDistinctManagersByAllocationTypeByIds(ids, type);
        }

        // 3. BENCH: managers with employees who have no active PROJECT allocation this
        // month
        @Query(value = "SELECT DISTINCT m.* FROM employees m " +
                        "WHERE m.resignation_date IS NULL AND EXISTS (" +
                        "  SELECT 1 FROM employees e WHERE e.manager_id = m.id AND e.resignation_date IS NULL " +
                        "  AND NOT EXISTS (" +
                        "    SELECT 1 FROM allocations a JOIN monthly_allocations ma ON ma.allocation_id = a.id " +
                        "    WHERE a.employee_id = e.id AND a.allocation_type = 'PROJECT' " +
                        "    AND ma.year = :year AND ma.month = :month AND ma.percentage > 0" +
                        "  )" +
                        ") ORDER BY m.name", nativeQuery = true)
        List<Employee> findDistinctManagersOfBenchEmployees(
                        @Param("year") int year, @Param("month") int month);

        @Query(value = "SELECT DISTINCT m.* FROM employees m " +
                        "WHERE m.resignation_date IS NULL AND EXISTS (" +
                        "  SELECT 1 FROM employees e WHERE e.manager_id = m.id AND e.resignation_date IS NULL " +
                        "  AND e.id IN :ids AND NOT EXISTS (" +
                        "    SELECT 1 FROM allocations a JOIN monthly_allocations ma ON ma.allocation_id = a.id " +
                        "    WHERE a.employee_id = e.id AND a.allocation_type = 'PROJECT' " +
                        "    AND ma.year = :year AND ma.month = :month AND ma.percentage > 0" +
                        "  )" +
                        ") ORDER BY m.name", nativeQuery = true)
        List<Employee> findDistinctManagersOfBenchEmployeesByIds(
                        @Param("ids") List<Long> ids,
                        @Param("year") int year, @Param("month") int month);

        default List<Employee> findDistinctManagersOfBenchFiltered(List<Long> ids, int year, int month) {
                if (ids == null)
                        return findDistinctManagersOfBenchEmployees(year, month);
                return findDistinctManagersOfBenchEmployeesByIds(ids, year, month);
        }

        // 4. ACTIVE: managers with employees who have an active PROJECT allocation this
        // month
        @Query(value = "SELECT DISTINCT m.* FROM employees m " +
                        "WHERE m.resignation_date IS NULL AND EXISTS (" +
                        "  SELECT 1 FROM employees e WHERE e.manager_id = m.id AND e.resignation_date IS NULL " +
                        "  AND EXISTS (" +
                        "    SELECT 1 FROM allocations a JOIN monthly_allocations ma ON ma.allocation_id = a.id " +
                        "    WHERE a.employee_id = e.id AND a.allocation_type = 'PROJECT' " +
                        "    AND ma.year = :year AND ma.month = :month AND ma.percentage > 0" +
                        "  )" +
                        ") ORDER BY m.name", nativeQuery = true)
        List<Employee> findDistinctManagersOfActiveEmployees(
                        @Param("year") int year, @Param("month") int month);

        @Query(value = "SELECT DISTINCT m.* FROM employees m " +
                        "WHERE m.resignation_date IS NULL AND EXISTS (" +
                        "  SELECT 1 FROM employees e WHERE e.manager_id = m.id AND e.resignation_date IS NULL " +
                        "  AND e.id IN :ids AND EXISTS (" +
                        "    SELECT 1 FROM allocations a JOIN monthly_allocations ma ON ma.allocation_id = a.id " +
                        "    WHERE a.employee_id = e.id AND a.allocation_type = 'PROJECT' " +
                        "    AND ma.year = :year AND ma.month = :month AND ma.percentage > 0" +
                        "  )" +
                        ") ORDER BY m.name", nativeQuery = true)
        List<Employee> findDistinctManagersOfActiveEmployeesByIds(
                        @Param("ids") List<Long> ids,
                        @Param("year") int year, @Param("month") int month);

        default List<Employee> findDistinctManagersOfActiveFiltered(List<Long> ids, int year, int month) {
                if (ids == null)
                        return findDistinctManagersOfActiveEmployees(year, month);
                return findDistinctManagersOfActiveEmployeesByIds(ids, year, month);
        }

        // ===== Employee page filter dropdown queries =====

        // Distinct towers (for tower dropdown)
        @Query(value = "SELECT DISTINCT t.description FROM employees e " +
                        "JOIN tech_towers t ON e.tower = t.id " +
                        "WHERE e.resignation_date IS NULL " +
                        "AND (CAST(:managerId AS bigint) IS NULL OR e.manager_id = :managerId) " +
                        "ORDER BY t.description", nativeQuery = true)
        List<String> findDistinctTowers(@Param("managerId") Long managerId);

        @Query(value = "SELECT DISTINCT t.description FROM employees e " +
                        "JOIN tech_towers t ON e.tower = t.id " +
                        "WHERE e.resignation_date IS NULL AND e.id IN :ids " +
                        "AND (CAST(:managerId AS bigint) IS NULL OR e.manager_id = :managerId) " +
                        "ORDER BY t.description", nativeQuery = true)
        List<String> findDistinctTowersByIds(@Param("ids") List<Long> ids, @Param("managerId") Long managerId);

        default List<String> findDistinctTowersFiltered(List<Long> ids, Long managerId) {
                if (ids == null)
                        return findDistinctTowers(managerId);
                return findDistinctTowersByIds(ids, managerId);
        }

        // Distinct managers for employee page (managers whose reports match
        // tower/status filters)
        @Query(value = "SELECT DISTINCT m.* FROM employees m " +
                        "WHERE m.resignation_date IS NULL " +
                        "AND EXISTS (SELECT 1 FROM employees e " +
                        "  LEFT JOIN tech_towers t ON e.tower = t.id " +
                        "  WHERE e.manager_id = m.id AND e.resignation_date IS NULL " +
                        "  AND (:tower IS NULL OR t.description = :tower)) " +
                        "ORDER BY m.name", nativeQuery = true)
        List<Employee> findDistinctManagersForEmployeeFilters(@Param("tower") String tower);

        @Query(value = "SELECT DISTINCT m.* FROM employees m " +
                        "WHERE m.resignation_date IS NULL " +
                        "AND EXISTS (SELECT 1 FROM employees e " +
                        "  LEFT JOIN tech_towers t ON e.tower = t.id " +
                        "  WHERE e.manager_id = m.id AND e.resignation_date IS NULL " +
                        "  AND e.id IN :ids " +
                        "  AND (:tower IS NULL OR t.description = :tower)) " +
                        "ORDER BY m.name", nativeQuery = true)
        List<Employee> findDistinctManagersForEmployeeFiltersByIds(
                        @Param("ids") List<Long> ids, @Param("tower") String tower);

        default List<Employee> findDistinctManagersForEmployeeFiltersFiltered(List<Long> ids, String tower) {
                if (ids == null)
                        return findDistinctManagersForEmployeeFilters(tower);
                return findDistinctManagersForEmployeeFiltersByIds(ids, tower);
        }

        // ===== Dashboard count queries =====

        // Count BENCH employees: no active PROJECT allocation this month
        @Query(value = "SELECT COUNT(*) FROM employees e " +
                        "WHERE e.resignation_date IS NULL " +
                        "AND NOT EXISTS (" +
                        "  SELECT 1 FROM allocations a WHERE a.employee_id = e.id " +
                        "  AND a.allocation_type = 'MATERNITY') " +
                        "AND NOT EXISTS (" +
                        "  SELECT 1 FROM allocations a WHERE a.employee_id = e.id " +
                        "  AND a.allocation_type = 'VACATION') " +
                        "AND NOT EXISTS (" +
                        "  SELECT 1 FROM allocations a " +
                        "  JOIN monthly_allocations ma ON ma.allocation_id = a.id " +
                        "  WHERE a.employee_id = e.id AND a.allocation_type = 'PROJECT' " +
                        "  AND ma.year = :year AND ma.month = :month AND ma.percentage > 0) " +
                        "AND NOT EXISTS (" +
                        "  SELECT 1 FROM allocations a WHERE a.employee_id = e.id " +
                        "  AND a.allocation_type = 'PROSPECT')", nativeQuery = true)
        long countBenchEmployees(@Param("year") int year, @Param("month") int month);

        @Query(value = "SELECT COUNT(*) FROM employees e " +
                        "WHERE e.resignation_date IS NULL AND e.id IN :ids " +
                        "AND NOT EXISTS (" +
                        "  SELECT 1 FROM allocations a WHERE a.employee_id = e.id " +
                        "  AND a.allocation_type = 'MATERNITY') " +
                        "AND NOT EXISTS (" +
                        "  SELECT 1 FROM allocations a WHERE a.employee_id = e.id " +
                        "  AND a.allocation_type = 'VACATION') " +
                        "AND NOT EXISTS (" +
                        "  SELECT 1 FROM allocations a " +
                        "  JOIN monthly_allocations ma ON ma.allocation_id = a.id " +
                        "  WHERE a.employee_id = e.id AND a.allocation_type = 'PROJECT' " +
                        "  AND ma.year = :year AND ma.month = :month AND ma.percentage > 0) " +
                        "AND NOT EXISTS (" +
                        "  SELECT 1 FROM allocations a WHERE a.employee_id = e.id " +
                        "  AND a.allocation_type = 'PROSPECT')", nativeQuery = true)
        long countBenchEmployeesByIds(@Param("ids") List<Long> ids, @Param("year") int year, @Param("month") int month);

        default long countBenchEmployeesFiltered(List<Long> ids, int year, int month) {
                if (ids == null)
                        return countBenchEmployees(year, month);
                return countBenchEmployeesByIds(ids, year, month);
        }

        // Count ACTIVE employees: have PROJECT allocation with % > 0 this month
        @Query(value = "SELECT COUNT(DISTINCT e.id) FROM employees e " +
                        "JOIN allocations a ON a.employee_id = e.id " +
                        "JOIN monthly_allocations ma ON ma.allocation_id = a.id " +
                        "WHERE e.resignation_date IS NULL " +
                        "AND a.allocation_type = 'PROJECT' " +
                        "AND ma.year = :year AND ma.month = :month AND ma.percentage > 0", nativeQuery = true)
        long countActiveAllocatedEmployees(@Param("year") int year, @Param("month") int month);

        @Query(value = "SELECT COUNT(DISTINCT e.id) FROM employees e " +
                        "JOIN allocations a ON a.employee_id = e.id " +
                        "JOIN monthly_allocations ma ON ma.allocation_id = a.id " +
                        "WHERE e.resignation_date IS NULL AND e.id IN :ids " +
                        "AND a.allocation_type = 'PROJECT' " +
                        "AND ma.year = :year AND ma.month = :month AND ma.percentage > 0", nativeQuery = true)
        long countActiveAllocatedEmployeesByIds(@Param("ids") List<Long> ids,
                        @Param("year") int year, @Param("month") int month);

        default long countActiveAllocatedEmployeesFiltered(List<Long> ids, int year, int month) {
                if (ids == null)
                        return countActiveAllocatedEmployees(year, month);
                return countActiveAllocatedEmployeesByIds(ids, year, month);
        }

        // Count PROSPECT employees: have PROSPECT allocation but NOT active
        @Query(value = "SELECT COUNT(*) FROM employees e " +
                        "WHERE e.resignation_date IS NULL " +
                        "AND EXISTS (SELECT 1 FROM allocations a WHERE a.employee_id = e.id " +
                        "  AND a.allocation_type = 'PROSPECT') " +
                        "AND NOT EXISTS (" +
                        "  SELECT 1 FROM allocations a " +
                        "  JOIN monthly_allocations ma ON ma.allocation_id = a.id " +
                        "  WHERE a.employee_id = e.id AND a.allocation_type = 'PROJECT' " +
                        "  AND ma.year = :year AND ma.month = :month AND ma.percentage > 0)", nativeQuery = true)
        long countProspectEmployees(@Param("year") int year, @Param("month") int month);

        @Query(value = "SELECT COUNT(*) FROM employees e " +
                        "WHERE e.resignation_date IS NULL AND e.id IN :ids " +
                        "AND EXISTS (SELECT 1 FROM allocations a WHERE a.employee_id = e.id " +
                        "  AND a.allocation_type = 'PROSPECT') " +
                        "AND NOT EXISTS (" +
                        "  SELECT 1 FROM allocations a " +
                        "  JOIN monthly_allocations ma ON ma.allocation_id = a.id " +
                        "  WHERE a.employee_id = e.id AND a.allocation_type = 'PROJECT' " +
                        "  AND ma.year = :year AND ma.month = :month AND ma.percentage > 0)", nativeQuery = true)
        long countProspectEmployeesByIds(@Param("ids") List<Long> ids,
                        @Param("year") int year, @Param("month") int month);

        default long countProspectEmployeesFiltered(List<Long> ids, int year, int month) {
                if (ids == null)
                        return countProspectEmployees(year, month);
                return countProspectEmployeesByIds(ids, year, month);
        }

        // Average allocation % for active employees
        @Query(value = "SELECT COALESCE(AVG(emp_total), 0) FROM (" +
                        "  SELECT SUM(ma.percentage) AS emp_total FROM employees e " +
                        "  JOIN allocations a ON a.employee_id = e.id " +
                        "  JOIN monthly_allocations ma ON ma.allocation_id = a.id " +
                        "  WHERE e.resignation_date IS NULL " +
                        "  AND a.allocation_type = 'PROJECT' " +
                        "  AND ma.year = :year AND ma.month = :month AND ma.percentage > 0 " +
                        "  GROUP BY e.id" +
                        ") sub", nativeQuery = true)
        double averageAllocationPercentage(@Param("year") int year, @Param("month") int month);

        @Query(value = "SELECT COALESCE(AVG(emp_total), 0) FROM (" +
                        "  SELECT SUM(ma.percentage) AS emp_total FROM employees e " +
                        "  JOIN allocations a ON a.employee_id = e.id " +
                        "  JOIN monthly_allocations ma ON ma.allocation_id = a.id " +
                        "  WHERE e.resignation_date IS NULL AND e.id IN :ids " +
                        "  AND a.allocation_type = 'PROJECT' " +
                        "  AND ma.year = :year AND ma.month = :month AND ma.percentage > 0 " +
                        "  GROUP BY e.id" +
                        ") sub", nativeQuery = true)
        double averageAllocationPercentageByIds(@Param("ids") List<Long> ids,
                        @Param("year") int year, @Param("month") int month);

        default double averageAllocationPercentageFiltered(List<Long> ids, int year, int month) {
                if (ids == null)
                        return averageAllocationPercentage(year, month);
                return averageAllocationPercentageByIds(ids, year, month);
        }

}
