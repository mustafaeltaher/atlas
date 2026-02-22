package com.atlas.repository;

import com.atlas.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

        Optional<User> findByUsername(String username);

        Optional<User> findByUsernameIgnoreCase(String username);

        @org.springframework.data.jpa.repository.Query("SELECT u FROM User u JOIN u.employee e " +
                        "WHERE e.resignationDate IS NULL " +
                        "AND e.jobLevel IN :allowedLevels " +
                        "AND (:excludedIds IS NULL OR e.id NOT IN :excludedIds) " +
                        "AND (:search IS NULL OR LOWER(u.username) LIKE :search OR LOWER(e.name) LIKE :search) " +
                        "ORDER BY e.name")
        java.util.List<User> findPotentialDelegateUsers(
                        @org.springframework.data.repository.query.Param("allowedLevels") java.util.List<com.atlas.entity.Employee.JobLevel> allowedLevels,
                        @org.springframework.data.repository.query.Param("excludedIds") java.util.List<Long> excludedIds,
                        @org.springframework.data.repository.query.Param("search") String search);

        @org.springframework.data.jpa.repository.Query("SELECT u FROM User u JOIN u.employee e " +
                        "WHERE e.resignationDate IS NULL " +
                        "AND e.manager.id = :managerId " +
                        "AND (:excludedIds IS NULL OR e.id NOT IN :excludedIds) " +
                        "AND (:search IS NULL OR LOWER(u.username) LIKE :search OR LOWER(e.name) LIKE :search) " +
                        "ORDER BY e.name")
        java.util.List<User> findPotentialDelegatesDirectReports(
                        @org.springframework.data.repository.query.Param("managerId") Long managerId,
                        @org.springframework.data.repository.query.Param("excludedIds") java.util.List<Long> excludedIds,
                        @org.springframework.data.repository.query.Param("search") String search);
}
