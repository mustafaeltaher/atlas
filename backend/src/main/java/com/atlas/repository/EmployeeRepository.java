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

        // Find BENCH employees: no active PROJECT allocation in current year/month
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
                        "  AND ma.percentage > 0" +
                        ") " +
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
                                        "  AND ma.percentage > 0" +
                                        ")", nativeQuery = true)
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
                        "  AND ma.percentage > 0" +
                        ") " +
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
                                        "  AND ma.percentage > 0" +
                                        ")", nativeQuery = true)
        Page<Employee> findBenchEmployeesByIds(
                        @Param("employeeIds") List<Long> employeeIds,
                        @Param("search") String search,
                        @Param("managerId") Long managerId,
                        @Param("currentYear") int currentYear,
                        @Param("currentMonth") int currentMonth,
                        Pageable pageable);

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
}
