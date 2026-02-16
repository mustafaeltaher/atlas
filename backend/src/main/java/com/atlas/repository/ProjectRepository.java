package com.atlas.repository;

import com.atlas.entity.Allocation;
import com.atlas.entity.Employee;
import com.atlas.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface ProjectRepository extends JpaSpecificationExecutor<Project>, JpaRepository<Project, Long>, ProjectRepositoryCustom {

        Optional<Project> findByProjectId(String projectId);

        List<Project> findByStatus(Project.ProjectStatus status);

        long countByStatus(Project.ProjectStatus status);

        default List<Project> findActiveProjects() {
                return findByStatus(Project.ProjectStatus.ACTIVE);
        }

        default long countActiveProjects() {
                return countByStatus(Project.ProjectStatus.ACTIVE);
        }

        @Query("SELECT DISTINCT a.project FROM Allocation a WHERE a.employee IN :employees AND a.allocationType = :allocationType AND a.project.status = :projectStatus")
        List<Project> findActiveProjectsByEmployeesWithParams(
                        @Param("employees") List<Employee> employees,
                        @Param("allocationType") Allocation.AllocationType allocationType,
                        @Param("projectStatus") Project.ProjectStatus projectStatus);

        default List<Project> findActiveProjectsByEmployees(List<Employee> employees) {
                return findActiveProjectsByEmployeesWithParams(employees,
                                Allocation.AllocationType.PROJECT, Project.ProjectStatus.ACTIVE);
        }

        @Query("SELECT DISTINCT a.project FROM Allocation a WHERE a.employee IN :employees AND a.allocationType = :allocationType")
        List<Project> findProjectsByEmployeesWithParams(
                        @Param("employees") List<Employee> employees,
                        @Param("allocationType") Allocation.AllocationType allocationType);

        default List<Project> findProjectsByEmployees(List<Employee> employees) {
                return findProjectsByEmployeesWithParams(employees,
                                Allocation.AllocationType.PROJECT);
        }

        @Query("SELECT COUNT(DISTINCT a.project) FROM Allocation a WHERE a.employee IN :employees AND a.allocationType = :allocationType AND a.project.status = :projectStatus")
        long countActiveProjectsByEmployeesWithParams(
                        @Param("employees") List<Employee> employees,
                        @Param("allocationType") Allocation.AllocationType allocationType,
                        @Param("projectStatus") Project.ProjectStatus projectStatus);

        default long countActiveProjectsByEmployees(List<Employee> employees) {
                return countActiveProjectsByEmployeesWithParams(employees,
                                Allocation.AllocationType.PROJECT, Project.ProjectStatus.ACTIVE);
        }

        @Query(value = "SELECT COUNT(DISTINCT a.project_id) FROM allocations a " +
                        "JOIN projects p ON p.id = a.project_id " +
                        "WHERE a.employee_id IN :employeeIds " +
                        "AND CAST(a.allocation_type AS text) = 'PROJECT' " +
                        "AND CAST(p.status AS text) = 'ACTIVE'", nativeQuery = true)
        long countActiveProjectsByEmployeeIds(@Param("employeeIds") List<Long> employeeIds);

        boolean existsByProjectId(String projectId);

        // Faceted Search - Statuses
        @Query("SELECT DISTINCT p.status FROM Project p " +
                        "WHERE (:region IS NULL OR p.region = :region)")
        List<Project.ProjectStatus> findDistinctStatusesByRegion(@Param("region") String region);

        // Unified routing: accept nullable projectIds (null = no filter)
        @Query("SELECT DISTINCT p.status FROM Project p " +
                        "WHERE (:region IS NULL OR p.region = :region) " +
                        "AND (:search IS NULL OR LOWER(p.description) LIKE :search OR LOWER(p.projectId) LIKE :search) "
                        +
                        "ORDER BY p.status")
        List<Project.ProjectStatus> findDistinctStatuses(@Param("region") String region,
                        @Param("search") String search);

        @Query("SELECT DISTINCT p.status FROM Project p " +
                        "WHERE p.id IN :ids " +
                        "AND (:region IS NULL OR p.region = :region) " +
                        "AND (:search IS NULL OR LOWER(p.description) LIKE :search OR LOWER(p.projectId) LIKE :search) "
                        +
                        "ORDER BY p.status")
        List<Project.ProjectStatus> findDistinctStatusesByIds(@Param("ids") List<Long> ids,
                        @Param("region") String region,
                        @Param("search") String search);
}
