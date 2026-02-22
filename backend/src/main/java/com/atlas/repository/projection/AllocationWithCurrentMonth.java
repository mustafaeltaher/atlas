package com.atlas.repository.projection;

import com.atlas.entity.Allocation;
import com.atlas.entity.Employee;
import com.atlas.entity.Project;

import java.time.LocalDate;

/**
 * Projection interface for Allocation with current month data only.
 * Used to optimize queries by fetching only the current month's allocation percentage
 * instead of loading all monthly allocations into memory.
 *
 * This approach reduces data fetching by ~92% (1 month vs 24 months of data).
 */
public interface AllocationWithCurrentMonth {
    Long getId();
    Employee getEmployee();
    Project getProject();
    Allocation.AllocationType getAllocationType();
    LocalDate getStartDate();
    LocalDate getEndDate();

    /**
     * Current month's allocation percentage.
     * Will be null if no allocation exists for the current month.
     */
    Integer getCurrentMonthPercentage();
}
