package com.atlas.repository;

import com.atlas.entity.Allocation;
import com.atlas.entity.Employee;
import com.atlas.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AllocationRepository extends JpaRepository<Allocation, Long> {

    List<Allocation> findByEmployee(Employee employee);

    List<Allocation> findByProject(Project project);

    List<Allocation> findByEmployeeId(Long employeeId);

    List<Allocation> findByProjectId(Long projectId);

    List<Allocation> findByStatus(Allocation.AllocationStatus status);

    @Query("SELECT a FROM Allocation a WHERE a.employee.id = :employeeId AND a.status = 'ACTIVE'")
    List<Allocation> findActiveByEmployeeId(@Param("employeeId") Long employeeId);

    @Query("SELECT a FROM Allocation a JOIN FETCH a.employee JOIN FETCH a.project WHERE a.project.id = :projectId AND a.status = 'ACTIVE'")
    List<Allocation> findActiveByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT a FROM Allocation a JOIN FETCH a.employee JOIN FETCH a.project WHERE a.project.parentTower = :parentTower")
    List<Allocation> findByParentTower(@Param("parentTower") String parentTower);

    @Query("SELECT a FROM Allocation a JOIN FETCH a.employee JOIN FETCH a.project WHERE a.project.tower = :tower")
    List<Allocation> findByTower(@Param("tower") String tower);

    @Query("SELECT a FROM Allocation a JOIN FETCH a.employee JOIN FETCH a.project WHERE a.project.id IN :projectIds")
    List<Allocation> findByProjectIdIn(@Param("projectIds") List<Long> projectIds);

    @Query("SELECT a FROM Allocation a JOIN FETCH a.employee JOIN FETCH a.project WHERE a.employee.parentTower = :parentTower AND a.status = 'ACTIVE'")
    List<Allocation> findActiveByParentTower(@Param("parentTower") String parentTower);

    @Query("SELECT a FROM Allocation a JOIN FETCH a.employee JOIN FETCH a.project WHERE a.employee.tower = :tower AND a.status = 'ACTIVE'")
    List<Allocation> findActiveByTower(@Param("tower") String tower);

    @Query("SELECT a FROM Allocation a JOIN FETCH a.employee JOIN FETCH a.project WHERE a.employee IN :employees AND a.status = 'ACTIVE'")
    List<Allocation> findActiveByEmployees(@Param("employees") List<Employee> employees);

    @Query("SELECT a FROM Allocation a WHERE a.employee.id IN :employeeIds AND a.status = 'ACTIVE'")
    List<Allocation> findActiveByEmployeeIds(@Param("employeeIds") List<Long> employeeIds);

    @Query("SELECT a FROM Allocation a WHERE a.project.id IN :projectIds AND a.status = 'ACTIVE'")
    List<Allocation> findActiveByProjectIds(@Param("projectIds") List<Long> projectIds);

    @Query("SELECT a FROM Allocation a JOIN FETCH a.employee JOIN FETCH a.project")
    List<Allocation> findAllWithEmployeeAndProject();

    @Query("SELECT a FROM Allocation a JOIN FETCH a.employee JOIN FETCH a.project WHERE a.employee.id = :employeeId")
    List<Allocation> findByEmployeeIdWithDetails(@Param("employeeId") Long employeeId);

    @Query("SELECT a FROM Allocation a JOIN FETCH a.employee JOIN FETCH a.project WHERE a.project.id = :projectId")
    List<Allocation> findByProjectIdWithDetails(@Param("projectId") Long projectId);

    @Query("SELECT a FROM Allocation a JOIN FETCH a.employee JOIN FETCH a.project WHERE a.id = :id")
    java.util.Optional<Allocation> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT a FROM Allocation a JOIN FETCH a.employee JOIN FETCH a.project WHERE a.employee.id IN :employeeIds")
    List<Allocation> findByEmployeeIdsWithDetails(@Param("employeeIds") List<Long> employeeIds);

    boolean existsByEmployeeAndProject(Employee employee, Project project);

    // Database-level paginated query with all filters for admin/executive
    @Query(value = "SELECT a FROM Allocation a JOIN FETCH a.employee e JOIN FETCH a.project p " +
            "WHERE (:status IS NULL OR a.status = :status) " +
            "AND (:managerId IS NULL OR e.manager.id = :managerId) " +
            "AND (:search IS NULL OR LOWER(CAST(e.name AS string)) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
            "OR LOWER(CAST(p.name AS string)) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))",
            countQuery = "SELECT COUNT(a) FROM Allocation a JOIN a.employee e JOIN a.project p " +
            "WHERE (:status IS NULL OR a.status = :status) " +
            "AND (:managerId IS NULL OR e.manager.id = :managerId) " +
            "AND (:search IS NULL OR LOWER(CAST(e.name AS string)) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
            "OR LOWER(CAST(p.name AS string)) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))")
    org.springframework.data.domain.Page<Allocation> findAllocationsWithFilters(
            @Param("search") String search,
            @Param("status") Allocation.AllocationStatus status,
            @Param("managerId") Long managerId,
            org.springframework.data.domain.Pageable pageable);

    // Database-level paginated query with all filters for non-admin (employee ID restriction)
    @Query(value = "SELECT a FROM Allocation a JOIN FETCH a.employee e JOIN FETCH a.project p " +
            "WHERE e.id IN :employeeIds " +
            "AND (:status IS NULL OR a.status = :status) " +
            "AND (:managerId IS NULL OR e.manager.id = :managerId) " +
            "AND (:search IS NULL OR LOWER(CAST(e.name AS string)) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
            "OR LOWER(CAST(p.name AS string)) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))",
            countQuery = "SELECT COUNT(a) FROM Allocation a JOIN a.employee e JOIN a.project p " +
            "WHERE e.id IN :employeeIds " +
            "AND (:status IS NULL OR a.status = :status) " +
            "AND (:managerId IS NULL OR e.manager.id = :managerId) " +
            "AND (:search IS NULL OR LOWER(CAST(e.name AS string)) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
            "OR LOWER(CAST(p.name AS string)) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))")
    org.springframework.data.domain.Page<Allocation> findAllocationsWithFiltersByEmployeeIds(
            @Param("employeeIds") List<Long> employeeIds,
            @Param("search") String search,
            @Param("status") Allocation.AllocationStatus status,
            @Param("managerId") Long managerId,
            org.springframework.data.domain.Pageable pageable);

    // Pagination methods
    @Query("SELECT a FROM Allocation a WHERE a.status = 'ACTIVE'")
    org.springframework.data.domain.Page<Allocation> findByStatusActive(
            org.springframework.data.domain.Pageable pageable);

    // Search with pagination and filters
    @Query(value = "SELECT a FROM Allocation a JOIN FETCH a.employee JOIN FETCH a.project WHERE " +
            "(:status IS NULL OR a.status = :status) AND " +
            "(:search IS NULL OR LOWER(CAST(a.employee.name AS string)) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR "
            +
            "LOWER(CAST(a.project.name AS string)) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))",
            countQuery = "SELECT COUNT(a) FROM Allocation a WHERE " +
            "(:status IS NULL OR a.status = :status) AND " +
            "(:search IS NULL OR LOWER(CAST(a.employee.name AS string)) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR "
            +
            "LOWER(CAST(a.project.name AS string)) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))")
    org.springframework.data.domain.Page<Allocation> searchAllocations(
            @Param("search") String search,
            @Param("status") Allocation.AllocationStatus status,
            org.springframework.data.domain.Pageable pageable);
}
