package com.atlas.dto;

import com.atlas.entity.Project;
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
    private String name;
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
    private Double averageUtilization;
}
