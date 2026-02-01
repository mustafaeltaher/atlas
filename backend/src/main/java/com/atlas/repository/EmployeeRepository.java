package com.atlas.repository;

import com.atlas.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

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
    @Query(value = "SELECT e FROM Employee e LEFT JOIN FETCH e.manager WHERE e.isActive = true",
            countQuery = "SELECT COUNT(e) FROM Employee e WHERE e.isActive = true")
    org.springframework.data.domain.Page<Employee> findByIsActiveTrue(
            org.springframework.data.domain.Pageable pageable);

    // Search with pagination
    @Query(value = "SELECT e FROM Employee e LEFT JOIN FETCH e.manager WHERE e.isActive = true AND " +
            "(LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(e.primarySkill) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(e.tower) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%')))",
            countQuery = "SELECT COUNT(e) FROM Employee e WHERE e.isActive = true AND " +
            "(LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(e.primarySkill) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(e.tower) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    org.springframework.data.domain.Page<Employee> searchActiveEmployees(
            @Param("search") String search,
            org.springframework.data.domain.Pageable pageable);

    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.manager WHERE e.isActive = true")
    List<Employee> findByIsActiveTrueWithManager();
}
