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

    // Current month allocation
    private String currentMonthAllocation;
    private Double allocationPercentage; // Numeric value for progress bar

    // Monthly allocation (for editing)
    private String janAllocation;
    private String febAllocation;
    private String marAllocation;
    private String aprAllocation;
    private String mayAllocation;
    private String junAllocation;
    private String julAllocation;
    private String augAllocation;
    private String sepAllocation;
    private String octAllocation;
    private String novAllocation;
    private String decAllocation;
}
