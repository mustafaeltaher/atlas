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

    @Query("SELECT a FROM Allocation a WHERE a.project.id = :projectId AND a.status = 'ACTIVE'")
    List<Allocation> findActiveByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT a FROM Allocation a WHERE a.project.parentTower = :parentTower")
    List<Allocation> findByParentTower(@Param("parentTower") String parentTower);

    @Query("SELECT a FROM Allocation a WHERE a.project.tower = :tower")
    List<Allocation> findByTower(@Param("tower") String tower);

    @Query("SELECT a FROM Allocation a JOIN a.project p WHERE p.id IN :projectIds")
    List<Allocation> findByProjectIdIn(@Param("projectIds") List<Long> projectIds);

    @Query("SELECT a FROM Allocation a WHERE a.employee.parentTower = :parentTower AND a.status = 'ACTIVE'")
    List<Allocation> findActiveByParentTower(@Param("parentTower") String parentTower);

    @Query("SELECT a FROM Allocation a WHERE a.employee.tower = :tower AND a.status = 'ACTIVE'")
    List<Allocation> findActiveByTower(@Param("tower") String tower);

    @Query("SELECT a FROM Allocation a WHERE a.employee IN :employees AND a.status = 'ACTIVE'")
    List<Allocation> findActiveByEmployees(@Param("employees") List<Employee> employees);

    boolean existsByEmployeeAndProject(Employee employee, Project project);

    // Pagination methods
    @Query("SELECT a FROM Allocation a WHERE a.status = 'ACTIVE'")
    org.springframework.data.domain.Page<Allocation> findByStatusActive(
            org.springframework.data.domain.Pageable pageable);

    // Search with pagination and filters
    @Query("SELECT a FROM Allocation a WHERE " +
            "(:status IS NULL OR a.status = :status) AND " +
            "(:search IS NULL OR LOWER(CAST(a.employee.name AS string)) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR "
            +
            "LOWER(CAST(a.project.name AS string)) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))")
    org.springframework.data.domain.Page<Allocation> searchAllocations(
            @Param("search") String search,
            @Param("status") Allocation.AllocationStatus status,
            org.springframework.data.domain.Pageable pageable);
}
