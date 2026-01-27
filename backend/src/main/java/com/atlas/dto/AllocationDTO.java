package com.atlas.dto;

import com.atlas.entity.Allocation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocationDTO {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private Long projectId;
    private String projectName;
    private String confirmedAssignment;
    private String prospectAssignment;
    private LocalDate startDate;
    private LocalDate endDate;
    private Allocation.AllocationStatus status;

    // Current month utilization
    private String currentMonthUtilization;
    private Double utilizationPercentage; // Numeric value for progress bar

    // Monthly utilization (for editing)
    private String janUtilization;
    private String febUtilization;
    private String marUtilization;
    private String aprUtilization;
    private String mayUtilization;
    private String junUtilization;
    private String julUtilization;
    private String augUtilization;
    private String sepUtilization;
    private String octUtilization;
    private String novUtilization;
    private String decUtilization;
}
