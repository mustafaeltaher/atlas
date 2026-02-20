package com.atlas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for displaying an employee's assigned skill with proficiency metadata.
 * Represents a skill association with level and grade information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSkillDTO {
    /**
     * Unique identifier for the employee-skill association.
     */
    private Integer id;

    /**
     * ID of the assigned skill.
     */
    private Integer skillId;

    /**
     * Name of the assigned skill for display (e.g., "Java Programming").
     */
    private String skillDescription;

    /**
     * Skill importance level.
     * - PRIMARY: Core skill for the employee's role
     * - SECONDARY: Supporting or supplementary skill
     * - null: Level not specified (legacy data only)
     */
    private String skillLevel;

    /**
     * Proficiency level.
     * - ADVANCED: Expert-level proficiency
     * - INTERMEDIATE: Working proficiency
     * - BEGINNER: Learning or basic proficiency
     * - null: Grade not specified (legacy data only)
     */
    private String skillGrade;
}
