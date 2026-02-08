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

    @NotNull(message = "Project ID is required")
    private Long projectId;

    private String projectName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Allocation.AllocationStatus status;

    // Current month allocation (for display)
    private Double currentMonthAllocation;
    private Double allocationPercentage;

    // Year for creating/editing allocations
    private Integer year;

    // Monthly allocations (normalized structure)
    private List<MonthlyAllocationDTO> monthlyAllocations;
}
