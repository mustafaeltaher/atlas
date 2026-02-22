package com.atlas.dto;

import com.atlas.entity.Project;
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

    private String projectId;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private Project.ProjectType projectType;
    private String region;
    private String vertical;
    private LocalDate startDate;
    private LocalDate endDate;
    private Project.ProjectStatus status;
    private Long managerId;
    private String managerName;

    // Statistics
    private Integer allocatedEmployees;
    private Double averageAllocation;
}
