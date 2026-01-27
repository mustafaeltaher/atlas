package com.atlas.repository;

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

    List<Project> findByParentTower(String parentTower);

    List<Project> findByTower(String tower);

    @Query("SELECT p FROM Project p WHERE p.status = 'ACTIVE'")
    List<Project> findActiveProjects();

    @Query("SELECT p FROM Project p WHERE p.parentTower = :parentTower AND p.status = 'ACTIVE'")
    List<Project> findActiveByParentTower(@Param("parentTower") String parentTower);

    @Query("SELECT p FROM Project p WHERE p.tower = :tower AND p.status = 'ACTIVE'")
    List<Project> findActiveByTower(@Param("tower") String tower);

    @Query("SELECT COUNT(p) FROM Project p WHERE p.status = 'ACTIVE'")
    long countActiveProjects();

    @Query("SELECT COUNT(p) FROM Project p WHERE p.parentTower = :parentTower AND p.status = 'ACTIVE'")
    long countActiveByParentTower(@Param("parentTower") String parentTower);

    @Query("SELECT COUNT(p) FROM Project p WHERE p.tower = :tower AND p.status = 'ACTIVE'")
    long countActiveByTower(@Param("tower") String tower);

    @Query("SELECT DISTINCT a.project FROM Allocation a WHERE a.employee IN :employees AND a.status = 'ACTIVE' AND a.project.status = 'ACTIVE'")
    List<Project> findActiveProjectsByEmployees(@Param("employees") List<Employee> employees);

    @Query("SELECT COUNT(DISTINCT a.project) FROM Allocation a WHERE a.employee IN :employees AND a.status = 'ACTIVE' AND a.project.status = 'ACTIVE'")
    long countActiveProjectsByEmployees(@Param("employees") List<Employee> employees);

    boolean existsByProjectId(String projectId);
}
