package com.atlas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeAllocationSummaryDTO {
    private Long employeeId;
    private String employeeName;
    private String employeeOracleId;
    private Double totalAllocationPercentage;
    private int projectCount;
    private List<AllocationDTO> allocations;
}
