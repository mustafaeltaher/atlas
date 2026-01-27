package com.atlas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    private Long totalEmployees;
    private Long activeEmployees;
    private Double averageUtilization;
    private Long benchCount;
    private Long prospectCount;
    private Long activeProjects;
    private Long pendingProjects;

    // Trend data (compared to previous month)
    private Double employeeTrend;
    private Double utilizationTrend;
    private Double benchTrend;
    private Double projectTrend;
}
