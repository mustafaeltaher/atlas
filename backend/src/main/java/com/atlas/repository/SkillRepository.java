package com.atlas.repository;

import com.atlas.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Integer> {

    /**
     * Find all skills that are NOT currently assigned to a specific employee.
     * Used for populating the "add skill" dropdown.
     *
     * @param employeeId the employee ID
     * @return list of available skills
     */
    @Query("SELECT s FROM Skill s WHERE s.id NOT IN " +
           "(SELECT es.skill.id FROM EmployeeSkill es WHERE es.employee.id = :employeeId) " +
           "ORDER BY s.description ASC")
    List<Skill> findAvailableSkillsForEmployee(@Param("employeeId") Long employeeId);
}
