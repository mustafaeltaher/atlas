package com.atlas.repository;

import com.atlas.entity.EmployeeSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeSkillRepository extends JpaRepository<EmployeeSkill, Integer> {

    /**
     * Find all skills assigned to a specific employee.
     *
     * @param employeeId the employee ID
     * @return list of employee skill associations
     */
    List<EmployeeSkill> findByEmployeeId(Long employeeId);

    /**
     * Find a specific skill assignment for an employee.
     *
     * @param employeeId the employee ID
     * @param skillId the skill ID
     * @return optional employee skill association
     */
    Optional<EmployeeSkill> findByEmployeeIdAndSkillId(Long employeeId, Integer skillId);

    /**
     * Check if an employee already has a specific skill assigned.
     *
     * @param employeeId the employee ID
     * @param skillId the skill ID
     * @return true if the skill is already assigned
     */
    boolean existsByEmployeeIdAndSkillId(Long employeeId, Integer skillId);
}
