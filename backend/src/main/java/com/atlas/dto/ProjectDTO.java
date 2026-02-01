package com.atlas.dto;

import com.atlas.entity.Project;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDTO {
    private Long id;

    @NotBlank(message = "Project ID is required")
    private String projectId;

    @NotBlank(message = "Project name is required")
    @Size(max = 255, message = "Project name must not exceed 255 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private String parentTower;
    private String tower;
    private LocalDate startDate;
    private LocalDate endDate;
    private Project.ProjectStatus status;
    private Long managerId;
    private String managerName;

    // Statistics
    private Integer allocatedEmployees;
    private Double averageAllocation;
}
