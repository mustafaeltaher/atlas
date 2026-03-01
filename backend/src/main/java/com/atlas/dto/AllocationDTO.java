package com.atlas.dto;

import com.atlas.entity.Allocation;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocationDTO {
    private Long id;

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    private String employeeName;
    private String employeeOracleId;

    private Long projectId; // nullable for Vacation/Maternity
    private String projectName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Allocation.AllocationType allocationType;

    // Current month allocation (for display)
    private Integer currentMonthAllocation;
    private Double allocationPercentage;

    // Month-by-month mode
    // If null or empty: single percentage mode
    // If populated: month-by-month mode
    private List<MonthlyAllocationDTO> monthlyAllocations;

    // Year for creating/editing allocations
    private Integer year;
}
