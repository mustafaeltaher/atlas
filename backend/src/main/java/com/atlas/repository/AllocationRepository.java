package com.atlas.repository;

import com.atlas.entity.Allocation;
import com.atlas.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AllocationRepository extends JpaRepository<Allocation, Long> {

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

        // With allocationType filter
        @Query(value = "SELECT a FROM Allocation a JOIN FETCH a.employee e LEFT JOIN FETCH a.project p " +
                        "WHERE a.allocationType = :allocationType " +
                        "AND (:managerId IS NULL OR e.manager.id = :managerId) " +
                        "AND (:search IS NULL OR LOWER(e.name) LIKE :search " +
                        "OR (p IS NOT NULL AND LOWER(p.description) LIKE :search))", countQuery = "SELECT COUNT(a) FROM Allocation a JOIN a.employee e LEFT JOIN a.project p "
                                        +
                                        "WHERE a.allocationType = :allocationType " +
                                        "AND (:managerId IS NULL OR e.manager.id = :managerId) " +
                                        "AND (:search IS NULL OR LOWER(e.name) LIKE :search " +
                                        "OR (p IS NOT NULL AND LOWER(p.description) LIKE :search))")
        org.springframework.data.domain.Page<Allocation> findAllocationsWithTypeFilter(
                        @Param("search") String search,
                        @Param("allocationType") Allocation.AllocationType allocationType,
                        @Param("managerId") Long managerId,
                        org.springframework.data.domain.Pageable pageable);

        // Without allocationType filter
        @Query(value = "SELECT a FROM Allocation a JOIN FETCH a.employee e LEFT JOIN FETCH a.project p " +
                        "WHERE (:managerId IS NULL OR e.manager.id = :managerId) " +
                        "AND (:search IS NULL OR LOWER(e.name) LIKE :search " +
                        "OR (p IS NOT NULL AND LOWER(p.description) LIKE :search))", countQuery = "SELECT COUNT(a) FROM Allocation a JOIN a.employee e LEFT JOIN a.project p "
                                        +
                                        "WHERE (:managerId IS NULL OR e.manager.id = :managerId) " +
                                        "AND (:search IS NULL OR LOWER(e.name) LIKE :search " +
                                        "OR (p IS NOT NULL AND LOWER(p.description) LIKE :search))")
        org.springframework.data.domain.Page<Allocation> findAllocationsNoTypeFilter(
                        @Param("search") String search,
                        @Param("managerId") Long managerId,
                        org.springframework.data.domain.Pageable pageable);

        default org.springframework.data.domain.Page<Allocation> findAllocationsWithFilters(
                        String search, Allocation.AllocationType allocationType, Long managerId,
                        org.springframework.data.domain.Pageable pageable) {
                if (allocationType != null) {
                        return findAllocationsWithTypeFilter(search, allocationType, managerId, pageable);
                }
                return findAllocationsNoTypeFilter(search, managerId, pageable);
        }

        // With allocationType filter by employee IDs
        @Query(value = "SELECT a FROM Allocation a JOIN FETCH a.employee e LEFT JOIN FETCH a.project p " +
                        "WHERE e.id IN :employeeIds " +
                        "AND a.allocationType = :allocationType " +
                        "AND (:managerId IS NULL OR e.manager.id = :managerId) " +
                        "AND (:search IS NULL OR LOWER(e.name) LIKE :search " +
                        "OR (p IS NOT NULL AND LOWER(p.description) LIKE :search))", countQuery = "SELECT COUNT(a) FROM Allocation a JOIN a.employee e LEFT JOIN a.project p "
                                        +
                                        "WHERE e.id IN :employeeIds " +
                                        "AND a.allocationType = :allocationType " +
                                        "AND (:managerId IS NULL OR e.manager.id = :managerId) " +
                                        "AND (:search IS NULL OR LOWER(e.name) LIKE :search " +
                                        "OR (p IS NOT NULL AND LOWER(p.description) LIKE :search))")
        org.springframework.data.domain.Page<Allocation> findAllocationsByIdsWithTypeFilter(
                        @Param("employeeIds") List<Long> employeeIds,
                        @Param("search") String search,
                        @Param("allocationType") Allocation.AllocationType allocationType,
                        @Param("managerId") Long managerId,
                        org.springframework.data.domain.Pageable pageable);

        // Without allocationType filter by employee IDs
        @Query(value = "SELECT a FROM Allocation a JOIN FETCH a.employee e LEFT JOIN FETCH a.project p " +
                        "WHERE e.id IN :employeeIds " +
                        "AND (:managerId IS NULL OR e.manager.id = :managerId) " +
                        "AND (:search IS NULL OR LOWER(e.name) LIKE :search " +
                        "OR (p IS NOT NULL AND LOWER(p.description) LIKE :search))", countQuery = "SELECT COUNT(a) FROM Allocation a JOIN a.employee e LEFT JOIN a.project p "
                                        +
                                        "WHERE e.id IN :employeeIds " +
                                        "AND (:managerId IS NULL OR e.manager.id = :managerId) " +
                                        "AND (:search IS NULL OR LOWER(e.name) LIKE :search " +
                                        "OR (p IS NOT NULL AND LOWER(p.description) LIKE :search))")
        org.springframework.data.domain.Page<Allocation> findAllocationsByIdsNoTypeFilter(
                        @Param("employeeIds") List<Long> employeeIds,
                        @Param("search") String search,
                        @Param("managerId") Long managerId,
                        org.springframework.data.domain.Pageable pageable);

        default org.springframework.data.domain.Page<Allocation> findAllocationsWithFiltersByEmployeeIds(
                        List<Long> employeeIds, String search, Allocation.AllocationType allocationType,
                        Long managerId, org.springframework.data.domain.Pageable pageable) {
                if (allocationType != null) {
                        return findAllocationsByIdsWithTypeFilter(employeeIds, search, allocationType, managerId,
                                        pageable);
                }
                return findAllocationsByIdsNoTypeFilter(employeeIds, search, managerId, pageable);
        }

        @Query("SELECT DISTINCT a.allocationType FROM Allocation a JOIN a.employee e WHERE " +
                        "(:managerId IS NULL OR e.manager.id = :managerId)")
        List<Allocation.AllocationType> findDistinctAllocationTypesByManager(@Param("managerId") Long managerId);
}
