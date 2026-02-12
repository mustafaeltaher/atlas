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

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

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

        @Query("SELECT COUNT(DISTINCT a.project) FROM Allocation a WHERE a.employee IN :employees AND a.allocationType = :allocationType AND a.project.status = :projectStatus")
        long countActiveProjectsByEmployeesWithParams(
                        @Param("employees") List<Employee> employees,
                        @Param("allocationType") Allocation.AllocationType allocationType,
                        @Param("projectStatus") Project.ProjectStatus projectStatus);

        default long countActiveProjectsByEmployees(List<Employee> employees) {
                return countActiveProjectsByEmployeesWithParams(employees,
                                Allocation.AllocationType.PROJECT, Project.ProjectStatus.ACTIVE);
        }

        boolean existsByProjectId(String projectId);

        // Search with status filter (search param must be pre-formatted: lowercase with
        // % wildcards)
        @Query("SELECT p FROM Project p WHERE " +
                        "p.status = :status AND " +
                        "(:region IS NULL OR p.region = :region) AND " +
                        "(:search IS NULL OR LOWER(p.description) LIKE :search)")
        org.springframework.data.domain.Page<Project> searchProjectsWithStatus(
                        @Param("search") String search,
                        @Param("region") String region,
                        @Param("status") Project.ProjectStatus status,
                        org.springframework.data.domain.Pageable pageable);

        // Search without status filter
        @Query("SELECT p FROM Project p WHERE " +
                        "(:region IS NULL OR p.region = :region) AND " +
                        "(:search IS NULL OR LOWER(p.description) LIKE :search)")
        org.springframework.data.domain.Page<Project> searchProjectsNoStatus(
                        @Param("search") String search,
                        @Param("region") String region,
                        org.springframework.data.domain.Pageable pageable);

        default org.springframework.data.domain.Page<Project> searchProjects(
                        String search, String region, Project.ProjectStatus status,
                        org.springframework.data.domain.Pageable pageable) {
                if (status != null) {
                        return searchProjectsWithStatus(search, region, status, pageable);
                }
                return searchProjectsNoStatus(search, region, pageable);
        }

        // Search by IDs with status filter
        @Query(value = "SELECT p FROM Project p WHERE p.id IN :projectIds " +
                        "AND p.status = :status " +
                        "AND (:region IS NULL OR p.region = :region) " +
                        "AND (:search IS NULL OR LOWER(p.description) LIKE :search)", countQuery = "SELECT COUNT(p) FROM Project p WHERE p.id IN :projectIds "
                                        +
                                        "AND p.status = :status " +
                                        "AND (:region IS NULL OR p.region = :region) " +
                                        "AND (:search IS NULL OR LOWER(p.description) LIKE :search)")
        org.springframework.data.domain.Page<Project> searchProjectsByIdsWithStatus(
                        @Param("projectIds") List<Long> projectIds,
                        @Param("search") String search,
                        @Param("region") String region,
                        @Param("status") Project.ProjectStatus status,
                        org.springframework.data.domain.Pageable pageable);

        // Search by IDs without status filter
        @Query(value = "SELECT p FROM Project p WHERE p.id IN :projectIds " +
                        "AND (:region IS NULL OR p.region = :region) " +
                        "AND (:search IS NULL OR LOWER(p.description) LIKE :search)", countQuery = "SELECT COUNT(p) FROM Project p WHERE p.id IN :projectIds "
                                        +
                                        "AND (:region IS NULL OR p.region = :region) " +
                                        "AND (:search IS NULL OR LOWER(p.description) LIKE :search)")
        org.springframework.data.domain.Page<Project> searchProjectsByIdsNoStatus(
                        @Param("projectIds") List<Long> projectIds,
                        @Param("search") String search,
                        @Param("region") String region,
                        org.springframework.data.domain.Pageable pageable);

        default org.springframework.data.domain.Page<Project> searchProjectsByIds(
                        List<Long> projectIds, String search, String region,
                        Project.ProjectStatus status, org.springframework.data.domain.Pageable pageable) {
                if (status != null) {
                        return searchProjectsByIdsWithStatus(projectIds, search, region, status, pageable);
                }
                return searchProjectsByIdsNoStatus(projectIds, search, region, pageable);
        }

        // Faceted Search
        @Query("SELECT DISTINCT p.region FROM Project p WHERE p.region IS NOT NULL " +
                        "AND p.status = :status")
        List<String> findDistinctRegionsByStatusWithParam(@Param("status") Project.ProjectStatus status);

        @Query("SELECT DISTINCT p.region FROM Project p WHERE p.region IS NOT NULL")
        List<String> findDistinctRegionsAll();

        default List<String> findDistinctRegionsByStatus(Project.ProjectStatus status) {
                if (status != null) {
                        return findDistinctRegionsByStatusWithParam(status);
                }
                return findDistinctRegionsAll();
        }

        @Query("SELECT DISTINCT p.status FROM Project p WHERE " +
                        "(:region IS NULL OR p.region = :region)")
        List<Project.ProjectStatus> findDistinctStatusesByRegion(@Param("region") String region);
}
