package com.atlas.service;

import com.atlas.dto.AddSkillRequest;
import com.atlas.dto.EmployeeSkillDTO;
import com.atlas.dto.SkillDTO;
import com.atlas.entity.Employee;
import com.atlas.entity.EmployeeSkill;
import com.atlas.entity.Skill;
import com.atlas.entity.User;
import com.atlas.repository.EmployeeRepository;
import com.atlas.repository.EmployeeSkillRepository;
import com.atlas.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing employee skill assignments.
 * Implements ABAC (Attribute-Based Access Control) via manager hierarchy.
 */
@Service
@RequiredArgsConstructor
public class EmployeeSkillService {

    private final EmployeeRepository employeeRepository;
    private final SkillRepository skillRepository;
    private final EmployeeSkillRepository employeeSkillRepository;
    private final EmployeeService employeeService;

    /**
     * Get all skills currently assigned to an employee.
     *
     * @param employeeId the employee ID
     * @param currentUser the authenticated user
     * @return list of assigned skills with metadata
     * @throws RuntimeException if employee not found or access denied
     */
    @Transactional(readOnly = true)
    public List<EmployeeSkillDTO> getEmployeeSkills(Long employeeId, User currentUser) {
        validateAccess(employeeId, currentUser);

        return employeeSkillRepository.findByEmployeeId(employeeId).stream()
                .map(this::toEmployeeSkillDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all skills that are available to add to an employee (not currently assigned).
     *
     * @param employeeId the employee ID
     * @param currentUser the authenticated user
     * @return list of available skills
     * @throws RuntimeException if employee not found or access denied
     */
    @Transactional(readOnly = true)
    public List<SkillDTO> getAvailableSkills(Long employeeId, User currentUser) {
        validateAccess(employeeId, currentUser);

        return skillRepository.findAvailableSkillsForEmployee(employeeId).stream()
                .map(this::toSkillDTO)
                .collect(Collectors.toList());
    }

    /**
     * Add a new skill to an employee with specified level and grade.
     *
     * @param employeeId the employee ID
     * @param request the skill assignment details (skillId, skillLevel, skillGrade)
     * @param currentUser the authenticated user
     * @return the created skill assignment DTO
     * @throws RuntimeException if employee/skill not found, access denied, or duplicate skill
     */
    @Transactional
    public EmployeeSkillDTO addSkillToEmployee(Long employeeId, AddSkillRequest request, User currentUser) {
        validateAccess(employeeId, currentUser);

        // Validate employee exists
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + employeeId));

        // Validate skill exists
        Skill skill = skillRepository.findById(request.getSkillId())
                .orElseThrow(() -> new RuntimeException("Skill not found: " + request.getSkillId()));

        // Check for duplicate assignment
        if (employeeSkillRepository.existsByEmployeeIdAndSkillId(employeeId, request.getSkillId())) {
            throw new RuntimeException("Skill " + request.getSkillId() + " is already assigned to employee " + employeeId);
        }

        // Create new skill assignment
        EmployeeSkill employeeSkill = EmployeeSkill.builder()
                .employee(employee)
                .skill(skill)
                .skillLevel(request.getSkillLevel())
                .skillGrade(request.getSkillGrade())
                .build();

        EmployeeSkill saved = employeeSkillRepository.save(employeeSkill);
        return toEmployeeSkillDTO(saved);
    }

    /**
     * Remove a skill from an employee.
     *
     * @param employeeId the employee ID
     * @param skillId the skill ID to remove
     * @param currentUser the authenticated user
     * @throws RuntimeException if employee not found, access denied, or skill not assigned
     */
    @Transactional
    public void removeSkillFromEmployee(Long employeeId, Integer skillId, User currentUser) {
        validateAccess(employeeId, currentUser);

        // Validate employee exists
        if (!employeeRepository.existsById(employeeId)) {
            throw new RuntimeException("Employee not found: " + employeeId);
        }

        // Find and delete the skill assignment
        EmployeeSkill employeeSkill = employeeSkillRepository.findByEmployeeIdAndSkillId(employeeId, skillId)
                .orElseThrow(() -> new RuntimeException("Skill " + skillId + " is not assigned to employee " + employeeId));

        employeeSkillRepository.delete(employeeSkill);
    }

    /**
     * Validate that the current user has access to the employee via ABAC (manager hierarchy).
     *
     * @param employeeId the employee ID
     * @param currentUser the authenticated user
     * @throws RuntimeException if access is denied
     */
    private void validateAccess(Long employeeId, User currentUser) {
        List<Long> accessibleIds = employeeService.getAccessibleEmployeeIds(currentUser);
        if (!accessibleIds.contains(employeeId)) {
            throw new RuntimeException("Access denied to employee: " + employeeId);
        }
    }

    /**
     * Convert EmployeeSkill entity to EmployeeSkillDTO.
     *
     * @param employeeSkill the entity
     * @return the DTO
     */
    private EmployeeSkillDTO toEmployeeSkillDTO(EmployeeSkill employeeSkill) {
        return EmployeeSkillDTO.builder()
                .id(employeeSkill.getId())
                .skillId(employeeSkill.getSkill().getId())
                .skillDescription(employeeSkill.getSkill().getDescription())
                .skillLevel(employeeSkill.getSkillLevel() != null ? employeeSkill.getSkillLevel().name() : null)
                .skillGrade(employeeSkill.getSkillGrade() != null ? employeeSkill.getSkillGrade().name() : null)
                .build();
    }

    /**
     * Convert Skill entity to SkillDTO.
     *
     * @param skill the entity
     * @return the DTO
     */
    private SkillDTO toSkillDTO(Skill skill) {
        return SkillDTO.builder()
                .id(skill.getId())
                .description(skill.getDescription())
                .towerDescription(skill.getTower() != null ? skill.getTower().getDescription() : null)
                .build();
    }
}
