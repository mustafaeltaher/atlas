package com.atlas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for displaying skill information (e.g., in available skills dropdown).
 * Represents a skill with its associated tech tower.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillDTO {
    /**
     * Unique skill identifier.
     */
    private Integer id;

    /**
     * Skill name or description (e.g., "Java Programming", "AWS Cloud").
     */
    private String description;

    /**
     * Associated tech tower/domain name (e.g., "Backend Development").
     * Nullable if skill has no associated tower.
     */
    private String towerDescription;
}
