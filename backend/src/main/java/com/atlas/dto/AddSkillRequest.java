package com.atlas.dto;

import com.atlas.entity.EmployeeSkill;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

/**
 * Request body for adding a skill to an employee.
 * All fields are required to ensure complete skill profile data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddSkillRequest {
    /**
     * ID of the skill to assign (REQUIRED).
     * Must reference an existing skill in the system.
     */
    @NotNull(message = "Skill ID is required")
    private Integer skillId;

    /**
     * Skill importance level (REQUIRED).
     * - PRIMARY: Core skill for the employee's role
     * - SECONDARY: Supporting or supplementary skill
     */
    @NotNull(message = "Skill level is required")
    private EmployeeSkill.SkillLevel skillLevel;

    /**
     * Proficiency level (REQUIRED).
     * - ADVANCED: Expert-level proficiency
     * - INTERMEDIATE: Working proficiency
     * - BEGINNER: Learning or basic proficiency
     */
    @NotNull(message = "Skill grade is required")
    private EmployeeSkill.SkillGrade skillGrade;
}
