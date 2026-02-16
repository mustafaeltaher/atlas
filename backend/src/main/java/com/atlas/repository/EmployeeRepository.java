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
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee>, EmployeeRepositoryCustom {

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
                        "AND (CAST(:search AS text) IS NULL OR LOWER(e.name) LIKE :search) " +
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
                                        "AND (CAST(:search AS text) IS NULL OR LOWER(e.name) LIKE :search) " +
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
                        "AND (CAST(:search AS text) IS NULL OR LOWER(e.name) LIKE :search) " +
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
                                        "AND (CAST(:search AS text) IS NULL OR LOWER(e.name) LIKE :search) " +
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
                        "AND (CAST(:search AS text) IS NULL OR LOWER(e.name) LIKE :search) " +
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
                                        "AND (CAST(:search AS text) IS NULL OR LOWER(e.name) LIKE :search) " +
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
                        "AND (CAST(:search AS text) IS NULL OR LOWER(e.name) LIKE :search) " +
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
                                        "AND (CAST(:search AS text) IS NULL OR LOWER(e.name) LIKE :search) " +
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
                        "AND (CAST(:search AS text) IS NULL OR LOWER(e.name) LIKE :search) " +
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
                                        "AND (CAST(:search AS text) IS NULL OR LOWER(e.name) LIKE :search) " +
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
                        "AND (CAST(:search AS text) IS NULL OR LOWER(e.name) LIKE :search) " +
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
                                        "AND (CAST(:search AS text) IS NULL OR LOWER(e.name) LIKE :search) " +
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
                        "AND (CAST(:search AS text) IS NULL OR LOWER(e.name) LIKE :search) " +
                        "AND (CAST(:managerId AS bigint) IS NULL OR e.manager_id = :managerId) " +
                        "AND EXISTS (" +
                        "  SELECT 1 FROM allocations a " +
                        "  WHERE a.employee_id = e.id " +
                        "  AND CAST(a.allocation_type AS text) = :allocationType" +
                        ") " +
                        "ORDER BY e.name", countQuery = "SELECT COUNT(DISTINCT e.id) FROM employees e " +
                                        "WHERE e.resignation_date IS NULL " +
                                        "AND (CAST(:search AS text) IS NULL OR LOWER(e.name) LIKE :search) " +
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
                        "AND (CAST(:search AS text) IS NULL OR LOWER(e.name) LIKE :search) " +
                        "AND (CAST(:managerId AS bigint) IS NULL OR e.manager_id = :managerId) " +
                        "AND EXISTS (" +
                        "  SELECT 1 FROM allocations a " +
                        "  WHERE a.employee_id = e.id " +
                        "  AND CAST(a.allocation_type AS text) = :allocationType" +
                        ") " +
                        "ORDER BY e.name", countQuery = "SELECT COUNT(DISTINCT e.id) FROM employees e " +
                                        "WHERE e.resignation_date IS NULL " +
                                        "AND e.id IN :employeeIds " +
                                        "AND (CAST(:search AS text) IS NULL OR LOWER(e.name) LIKE :search) " +
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
                        "AND LOWER(m.name) LIKE :search " +
                        "AND EXISTS (SELECT 1 FROM Employee e WHERE e.manager = m AND e.resignationDate IS NULL) " +
                        "ORDER BY m.name")
        List<Employee> findDistinctManagers(@Param("search") String search);

        @Query("SELECT DISTINCT m FROM Employee m " +
                        "WHERE m.resignationDate IS NULL " +
                        "AND LOWER(m.name) LIKE :search " +
                        "AND EXISTS (SELECT 1 FROM Employee e WHERE e.manager = m AND e.resignationDate IS NULL AND e.id IN :ids) "
                        +
                        "ORDER BY m.name")
        List<Employee> findDistinctManagersByIds(@Param("ids") List<Long> ids, @Param("search") String search);

        default List<Employee> findDistinctManagersFiltered(List<Long> ids, String search) {
                if (ids == null)
                        return findDistinctManagers(search);
                return findDistinctManagersByIds(ids, search);
        }

        // 2. Standard allocation type filter (e.g. PROJECT, PROSPECT)
        @Query(value = "SELECT DISTINCT m.* FROM employees m " +
                        "WHERE m.resignation_date IS NULL " +
                        "AND LOWER(m.name) LIKE :search " +
                        "AND EXISTS (SELECT 1 FROM employees e " +
                        "  JOIN allocations a ON a.employee_id = e.id " +
                        "  WHERE e.manager_id = m.id AND e.resignation_date IS NULL " +
                        "  AND CAST(a.allocation_type AS text) = :type) " +
                        "ORDER BY m.name", nativeQuery = true)
        List<Employee> findDistinctManagersByAllocationType(@Param("type") String type, @Param("search") String search);

        @Query(value = "SELECT DISTINCT m.* FROM employees m " +
                        "WHERE m.resignation_date IS NULL " +
                        "AND LOWER(m.name) LIKE :search " +
                        "AND EXISTS (SELECT 1 FROM employees e " +
                        "  JOIN allocations a ON a.employee_id = e.id " +
                        "  WHERE e.manager_id = m.id AND e.resignation_date IS NULL AND e.id IN :ids " +
                        "  AND CAST(a.allocation_type AS text) = :type) " +
                        "ORDER BY m.name", nativeQuery = true)
        List<Employee> findDistinctManagersByAllocationTypeByIds(
                        @Param("ids") List<Long> ids, @Param("type") String type, @Param("search") String search);

        default List<Employee> findDistinctManagersByAllocationTypeFiltered(List<Long> ids, String type,
                        String search) {
                if (ids == null)
                        return findDistinctManagersByAllocationType(type, search);
                return findDistinctManagersByAllocationTypeByIds(ids, type, search);
        }

        // 3. BENCH: managers with employees who have no active PROJECT allocation this
        // month
        @Query(value = "SELECT DISTINCT m.* FROM employees m " +
                        "WHERE m.resignation_date IS NULL " +
                        "AND LOWER(m.name) LIKE :search " +
                        "AND EXISTS (" +
                        "  SELECT 1 FROM employees e WHERE e.manager_id = m.id AND e.resignation_date IS NULL " +
                        "  AND NOT EXISTS (" +
                        "    SELECT 1 FROM allocations a JOIN monthly_allocations ma ON ma.allocation_id = a.id " +
                        "    WHERE a.employee_id = e.id AND a.allocation_type = 'PROJECT' " +
                        "    AND ma.year = :year AND ma.month = :month AND ma.percentage > 0" +
                        "  )" +
                        ") ORDER BY m.name", nativeQuery = true)
        List<Employee> findDistinctManagersOfBenchEmployees(
                        @Param("year") int year, @Param("month") int month, @Param("search") String search);

        @Query(value = "SELECT DISTINCT m.* FROM employees m " +
                        "WHERE m.resignation_date IS NULL " +
                        "AND LOWER(m.name) LIKE :search " +
                        "AND EXISTS (" +
                        "  SELECT 1 FROM employees e WHERE e.manager_id = m.id AND e.resignation_date IS NULL " +
                        "  AND e.id IN :ids AND NOT EXISTS (" +
                        "    SELECT 1 FROM allocations a JOIN monthly_allocations ma ON ma.allocation_id = a.id " +
                        "    WHERE a.employee_id = e.id AND a.allocation_type = 'PROJECT' " +
                        "    AND ma.year = :year AND ma.month = :month AND ma.percentage > 0" +
                        "  )" +
                        ") ORDER BY m.name", nativeQuery = true)
        List<Employee> findDistinctManagersOfBenchEmployeesByIds(
                        @Param("ids") List<Long> ids,
                        @Param("year") int year, @Param("month") int month, @Param("search") String search);

        default List<Employee> findDistinctManagersOfBenchFiltered(List<Long> ids, int year, int month, String search) {
                if (ids == null)
                        return findDistinctManagersOfBenchEmployees(year, month, search);
                return findDistinctManagersOfBenchEmployeesByIds(ids, year, month, search);
        }

        // New method: Find managers of BENCH employees where EMPLOYEE matches search
        @Query(value = "SELECT DISTINCT m.* FROM employees m " +
                        "JOIN employees e ON e.manager_id = m.id " +
                        "WHERE m.resignation_date IS NULL AND e.resignation_date IS NULL " +
                        "AND (CAST(:search AS text) IS NULL OR LOWER(e.name) LIKE :search) " +
                        "AND (CAST(:managerSearch AS text) IS NULL OR LOWER(m.name) LIKE :managerSearch) " +
                        "AND NOT EXISTS (" +
                        "  SELECT 1 FROM allocations a JOIN monthly_allocations ma ON ma.allocation_id = a.id " +
                        "  WHERE a.employee_id = e.id AND a.allocation_type = 'PROJECT' " +
                        "  AND ma.year = :year AND ma.month = :month AND ma.percentage > 0" +
                        ") " +
                        "AND NOT EXISTS (SELECT 1 FROM allocations a WHERE a.employee_id = e.id AND a.allocation_type = 'PROSPECT') "
                        +
                        "AND NOT EXISTS (SELECT 1 FROM allocations a WHERE a.employee_id = e.id AND a.allocation_type = 'MATERNITY') "
                        +
                        "AND NOT EXISTS (SELECT 1 FROM allocations a WHERE a.employee_id = e.id AND a.allocation_type = 'VACATION') "
                        +
                        "ORDER BY m.name", nativeQuery = true)
        List<Employee> findDistinctManagersOfBenchByEmployeeSearch(
                        @Param("year") int year, @Param("month") int month,
                        @Param("search") String search, @Param("managerSearch") String managerSearch);

        @Query(value = "SELECT DISTINCT m.* FROM employees m " +
                        "JOIN employees e ON e.manager_id = m.id " +
                        "WHERE m.resignation_date IS NULL AND e.resignation_date IS NULL " +
                        "AND e.id IN :ids " +
                        "AND (CAST(:search AS text) IS NULL OR LOWER(e.name) LIKE :search) " +
                        "AND (CAST(:managerSearch AS text) IS NULL OR LOWER(m.name) LIKE :managerSearch) " +
                        "AND NOT EXISTS (" +
                        "  SELECT 1 FROM allocations a JOIN monthly_allocations ma ON ma.allocation_id = a.id " +
                        "  WHERE a.employee_id = e.id AND a.allocation_type = 'PROJECT' " +
                        "  AND ma.year = :year AND ma.month = :month AND ma.percentage > 0" +
                        ") " +
                        "AND NOT EXISTS (SELECT 1 FROM allocations a WHERE a.employee_id = e.id AND a.allocation_type = 'PROSPECT') "
                        +
                        "AND NOT EXISTS (SELECT 1 FROM allocations a WHERE a.employee_id = e.id AND a.allocation_type = 'MATERNITY') "
                        +
                        "AND NOT EXISTS (SELECT 1 FROM allocations a WHERE a.employee_id = e.id AND a.allocation_type = 'VACATION') "
                        +
                        "ORDER BY m.name", nativeQuery = true)
        List<Employee> findDistinctManagersOfBenchByEmployeeSearchByIds(
                        @Param("ids") List<Long> ids,
                        @Param("year") int year, @Param("month") int month,
                        @Param("search") String search, @Param("managerSearch") String managerSearch);

        default List<Employee> findDistinctManagersOfBenchByEmployeeSearchFiltered(List<Long> ids, int year, int month,
                        String search, String managerSearch) {
                // Handle empty list case - IN () clause fails in native SQL
                if (ids != null && ids.isEmpty()) {
                        return java.util.Collections.emptyList();
                }
                if (ids == null)
                        return findDistinctManagersOfBenchByEmployeeSearch(year, month, search, managerSearch);
                return findDistinctManagersOfBenchByEmployeeSearchByIds(ids, year, month, search, managerSearch);
        }

        // 4. ACTIVE: managers with employees who have an active PROJECT allocation this
        // month
        @Query(value = "SELECT DISTINCT m.* FROM employees m " +
                        "WHERE m.resignation_date IS NULL " +
                        "AND LOWER(m.name) LIKE :search " +
                        "AND EXISTS (" +
                        "  SELECT 1 FROM employees e WHERE e.manager_id = m.id AND e.resignation_date IS NULL " +
                        "  AND EXISTS (" +
                        "    SELECT 1 FROM allocations a JOIN monthly_allocations ma ON ma.allocation_id = a.id " +
                        "    WHERE a.employee_id = e.id AND a.allocation_type = 'PROJECT' " +
                        "    AND ma.year = :year AND ma.month = :month AND ma.percentage > 0" +
                        "  )" +
                        ") ORDER BY m.name", nativeQuery = true)
        List<Employee> findDistinctManagersOfActiveEmployees(
                        @Param("year") int year, @Param("month") int month, @Param("search") String search);

        @Query(value = "SELECT DISTINCT m.* FROM employees m " +
                        "WHERE m.resignation_date IS NULL " +
                        "AND LOWER(m.name) LIKE :search " +
                        "AND EXISTS (" +
                        "  SELECT 1 FROM employees e WHERE e.manager_id = m.id AND e.resignation_date IS NULL " +
                        "  AND e.id IN :ids AND EXISTS (" +
                        "    SELECT 1 FROM allocations a JOIN monthly_allocations ma ON ma.allocation_id = a.id " +
                        "    WHERE a.employee_id = e.id AND a.allocation_type = 'PROJECT' " +
                        "    AND ma.year = :year AND ma.month = :month AND ma.percentage > 0" +
                        "  )" +
                        ") ORDER BY m.name", nativeQuery = true)
        List<Employee> findDistinctManagersOfActiveEmployeesByIds(
                        @Param("ids") List<Long> ids,
                        @Param("year") int year, @Param("month") int month, @Param("search") String search);

        default List<Employee> findDistinctManagersOfActiveFiltered(List<Long> ids, int year, int month,
                        String search) {
                if (ids == null)
                        return findDistinctManagersOfActiveEmployees(year, month, search);
                return findDistinctManagersOfActiveEmployeesByIds(ids, year, month, search);
        }

        // Find managers of ACTIVE employees with employee name search (for faceted
        // search consistency)
        // Mirrors findDistinctManagersOfBenchByEmployeeSearchFiltered for ACTIVE status
        @Query(value = "SELECT DISTINCT m.* FROM employees m " +
                        "JOIN employees e ON e.manager_id = m.id " +
                        "WHERE m.resignation_date IS NULL AND e.resignation_date IS NULL " +
                        "AND (CAST(:search AS text) IS NULL OR LOWER(e.name) LIKE :search) " +
                        "AND (CAST(:managerSearch AS text) IS NULL OR LOWER(m.name) LIKE :managerSearch) " +
                        "AND EXISTS (" +
                        "  SELECT 1 FROM allocations a JOIN monthly_allocations ma ON ma.allocation_id = a.id " +
                        "  WHERE a.employee_id = e.id AND a.allocation_type = 'PROJECT' " +
                        "  AND ma.year = :year AND ma.month = :month AND ma.percentage > 0" +
                        ") " +
                        "ORDER BY m.name", nativeQuery = true)
        List<Employee> findDistinctManagersOfActiveByEmployeeSearch(
                        @Param("year") int year, @Param("month") int month,
                        @Param("search") String search, @Param("managerSearch") String managerSearch);

        @Query(value = "SELECT DISTINCT m.* FROM employees m " +
                        "JOIN employees e ON e.manager_id = m.id " +
                        "WHERE m.resignation_date IS NULL AND e.resignation_date IS NULL " +
                        "AND e.id IN :ids " +
                        "AND (CAST(:search AS text) IS NULL OR LOWER(e.name) LIKE :search) " +
                        "AND (CAST(:managerSearch AS text) IS NULL OR LOWER(m.name) LIKE :managerSearch) " +
                        "AND EXISTS (" +
                        "  SELECT 1 FROM allocations a JOIN monthly_allocations ma ON ma.allocation_id = a.id " +
                        "  WHERE a.employee_id = e.id AND a.allocation_type = 'PROJECT' " +
                        "  AND ma.year = :year AND ma.month = :month AND ma.percentage > 0" +
                        ") " +
                        "ORDER BY m.name", nativeQuery = true)
        List<Employee> findDistinctManagersOfActiveByEmployeeSearchByIds(
                        @Param("ids") List<Long> ids,
                        @Param("year") int year, @Param("month") int month,
                        @Param("search") String search, @Param("managerSearch") String managerSearch);

        default List<Employee> findDistinctManagersOfActiveByEmployeeSearchFiltered(List<Long> ids, int year, int month,
                        String search, String managerSearch) {
                // Handle empty list case - IN () clause fails in native SQL
                if (ids != null && ids.isEmpty()) {
                        return java.util.Collections.emptyList();
                }
                if (ids == null)
                        return findDistinctManagersOfActiveByEmployeeSearch(year, month, search, managerSearch);
                return findDistinctManagersOfActiveByEmployeeSearchByIds(ids, year, month, search, managerSearch);
        }

        // ===== Employee page filter dropdown queries =====

        // Distinct towers (for tower dropdown)

        @Query(value = "SELECT DISTINCT t.description FROM tech_towers t " +
                        "JOIN employees e ON e.tower = t.id " +
                        "WHERE e.resignation_date IS NULL " +
                        "AND (CAST(:managerId AS bigint) IS NULL OR e.manager_id = :managerId) " +
                        "AND (:search IS NULL OR LOWER(e.name) LIKE :search OR LOWER(e.email) LIKE :search) " +
                        "ORDER BY t.description", nativeQuery = true)
        List<String> findDistinctTowers(@Param("managerId") Long managerId, @Param("search") String search);

        @Query(value = "SELECT DISTINCT t.description FROM employees e " +
                        "JOIN tech_towers t ON e.tower = t.id " +
                        "WHERE e.resignation_date IS NULL AND e.id IN :ids " +
                        "AND (CAST(:managerId AS bigint) IS NULL OR e.manager_id = :managerId) " +
                        "AND (:search IS NULL OR LOWER(e.name) LIKE :search OR LOWER(e.email) LIKE :search) " +
                        "ORDER BY t.description", nativeQuery = true)
        List<String> findDistinctTowersByIds(@Param("ids") List<Long> ids, @Param("managerId") Long managerId,
                        @Param("search") String search);

        default List<String> findDistinctTowersFiltered(List<Long> ids, Long managerId, String search) {
                if (ids == null)
                        return findDistinctTowers(managerId, search);
                return findDistinctTowersByIds(ids, managerId, search);
        }

        // Distinct managers for employee page (managers whose reports match
        // tower/status filters)
        @Query(value = "SELECT DISTINCT m.* FROM employees m " +
                        "WHERE m.resignation_date IS NULL " +
                        "AND (:managerSearch IS NULL OR LOWER(m.name) LIKE :managerSearch) " +
                        "AND EXISTS (SELECT 1 FROM employees e " +
                        "  LEFT JOIN tech_towers t ON e.tower = t.id " +
                        "  WHERE e.manager_id = m.id AND e.resignation_date IS NULL " +
                        "  AND (:tower IS NULL OR t.description = :tower) " +
                        "  AND (:search IS NULL OR LOWER(e.name) LIKE :search OR LOWER(e.email) LIKE :search)) " +
                        "ORDER BY m.name", nativeQuery = true)
        List<Employee> findDistinctManagersForEmployeeFilters(@Param("tower") String tower,
                        @Param("search") String search,
                        @Param("managerSearch") String managerSearch);

        @Query(value = "SELECT DISTINCT m.* FROM employees m " +
                        "WHERE m.resignation_date IS NULL " +
                        "AND (:managerSearch IS NULL OR LOWER(m.name) LIKE :managerSearch) " +
                        "AND EXISTS (SELECT 1 FROM employees e " +
                        "  LEFT JOIN tech_towers t ON e.tower = t.id " +
                        "  WHERE e.manager_id = m.id AND e.resignation_date IS NULL " +
                        "  AND e.id IN :ids " +
                        "  AND (:tower IS NULL OR t.description = :tower) " +
                        "  AND (:search IS NULL OR LOWER(e.name) LIKE :search OR LOWER(e.email) LIKE :search)) " +
                        "ORDER BY m.name", nativeQuery = true)
        List<Employee> findDistinctManagersForEmployeeFiltersByIds(
                        @Param("ids") List<Long> ids, @Param("tower") String tower,
                        @Param("search") String search,
                        @Param("managerSearch") String managerSearch);

        default List<Employee> findDistinctManagersForEmployeeFiltersFiltered(List<Long> ids, String tower,
                        String search, String managerSearch) {
                if (ids == null)
                        return findDistinctManagersForEmployeeFilters(tower, search, managerSearch);
                return findDistinctManagersForEmployeeFiltersByIds(ids, tower, search, managerSearch);
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

        // ===== Managers for Allocations Page (with allocation type filter) =====
        // Split queries: one for when allocationType filter is provided, one for when
        // it's NULL (show all)

        // No allocation type filter - return all managers
        @Query(value = "SELECT DISTINCT m.* FROM allocations a " +
                        "JOIN employees e ON e.id = a.employee_id " +
                        "JOIN employees m ON m.id = e.manager_id " +
                        "WHERE e.manager_id IS NOT NULL " +
                        "AND e.resignation_date IS NULL " +
                        "AND m.resignation_date IS NULL " +
                        "AND LOWER(e.name) LIKE CAST(:search AS text) " +
                        "AND LOWER(m.name) LIKE CAST(:managerSearch AS text) " +
                        "ORDER BY m.name", nativeQuery = true)
        List<Employee> findDistinctManagersFromAllocationsAllTypes(
                        @Param("search") String search,
                        @Param("managerSearch") String managerSearch);

        @Query(value = "SELECT DISTINCT m.* FROM allocations a " +
                        "JOIN employees e ON e.id = a.employee_id " +
                        "JOIN employees m ON m.id = e.manager_id " +
                        "WHERE e.manager_id IS NOT NULL " +
                        "AND e.resignation_date IS NULL " +
                        "AND m.resignation_date IS NULL " +
                        "AND e.id IN :employeeIds " +
                        "AND LOWER(e.name) LIKE CAST(:search AS text) " +
                        "AND LOWER(m.name) LIKE CAST(:managerSearch AS text) " +
                        "ORDER BY m.name", nativeQuery = true)
        List<Employee> findDistinctManagersFromAllocationsAllTypesByIds(
                        @Param("employeeIds") List<Long> employeeIds,
                        @Param("search") String search,
                        @Param("managerSearch") String managerSearch);

        // With allocation type filter
        @Query(value = "SELECT DISTINCT m.* FROM allocations a " +
                        "JOIN employees e ON e.id = a.employee_id " +
                        "JOIN employees m ON m.id = e.manager_id " +
                        "WHERE e.manager_id IS NOT NULL " +
                        "AND e.resignation_date IS NULL " +
                        "AND m.resignation_date IS NULL " +
                        "AND CAST(a.allocation_type AS text) = CAST(:allocationType AS text) " +
                        "AND LOWER(e.name) LIKE CAST(:search AS text) " +
                        "AND LOWER(m.name) LIKE CAST(:managerSearch AS text) " +
                        "ORDER BY m.name", nativeQuery = true)
        List<Employee> findDistinctManagersFromAllocationsByType(
                        @Param("allocationType") String allocationType,
                        @Param("search") String search,
                        @Param("managerSearch") String managerSearch);

        @Query(value = "SELECT DISTINCT m.* FROM allocations a " +
                        "JOIN employees e ON e.id = a.employee_id " +
                        "JOIN employees m ON m.id = e.manager_id " +
                        "WHERE e.manager_id IS NOT NULL " +
                        "AND e.resignation_date IS NULL " +
                        "AND m.resignation_date IS NULL " +
                        "AND e.id IN :employeeIds " +
                        "AND CAST(a.allocation_type AS text) = CAST(:allocationType AS text) " +
                        "AND LOWER(e.name) LIKE CAST(:search AS text) " +
                        "AND LOWER(m.name) LIKE CAST(:managerSearch AS text) " +
                        "ORDER BY m.name", nativeQuery = true)
        List<Employee> findDistinctManagersFromAllocationsByTypeAndIds(
                        @Param("employeeIds") List<Long> employeeIds,
                        @Param("allocationType") String allocationType,
                        @Param("search") String search,
                        @Param("managerSearch") String managerSearch);

        default List<Employee> findDistinctManagersFromAllocationsFiltered(
                        String allocationType,
                        List<Long> employeeIds,
                        String search,
                        String managerSearch) {
                // Format search parameters with wildcards - NEVER pass NULL to avoid PostgreSQL
                // type inference issues
                // when same parameter is used multiple times in query
                String searchParam = (search != null && !search.trim().isEmpty())
                                ? "%" + search.trim().toLowerCase() + "%"
                                : "%"; // Match all instead of NULL
                String managerSearchParam = (managerSearch != null && !managerSearch.trim().isEmpty())
                                ? "%" + managerSearch.trim().toLowerCase() + "%"
                                : "%"; // Match all instead of NULL

                // Handle empty list case - IN () clause fails in native SQL
                if (employeeIds != null && employeeIds.isEmpty()) {
                        return java.util.Collections.emptyList();
                }

                // Route to appropriate query based on whether allocationType filter is present
                if (allocationType == null) {
                        // No type filter - show all
                        if (employeeIds == null) {
                                return findDistinctManagersFromAllocationsAllTypes(searchParam, managerSearchParam);
                        }
                        return findDistinctManagersFromAllocationsAllTypesByIds(employeeIds, searchParam,
                                        managerSearchParam);
                } else {
                        // Specific type filter
                        if (employeeIds == null) {
                                return findDistinctManagersFromAllocationsByType(allocationType, searchParam,
                                                managerSearchParam);
                        }
                        return findDistinctManagersFromAllocationsByTypeAndIds(employeeIds, allocationType, searchParam,
                                        managerSearchParam);
                }
        }

}
