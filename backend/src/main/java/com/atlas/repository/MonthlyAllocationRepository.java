package com.atlas.repository;

import com.atlas.entity.MonthlyAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonthlyAllocationRepository extends JpaRepository<MonthlyAllocation, Long> {

    Optional<MonthlyAllocation> findByAllocationIdAndYearAndMonth(Long allocationId, Integer year, Integer month);

    @Query("SELECT ma FROM MonthlyAllocation ma WHERE ma.allocation.id IN :allocationIds AND ma.year = :year AND ma.month = :month")
    List<MonthlyAllocation> findByAllocationIdsAndYearAndMonth(
            @Param("allocationIds") List<Long> allocationIds,
            @Param("year") Integer year,
            @Param("month") Integer month);

    /**
     * Fetches ONLY the percentage value for a specific allocation, year, and month.
     * This is a Projection query that avoids loading the entire MonthlyAllocation entity,
     * eliminating the need for in-memory filtering.
     *
     * @param allocationId The allocation ID
     * @param year The year
     * @param month The month (1-12)
     * @return Optional containing the percentage, or empty if no data exists for that month
     */
    @Query("SELECT ma.percentage FROM MonthlyAllocation ma WHERE ma.allocation.id = :allocationId AND ma.year = :year AND ma.month = :month")
    Optional<Integer> findPercentageByAllocationIdAndYearMonth(
            @Param("allocationId") Long allocationId,
            @Param("year") Integer year,
            @Param("month") Integer month);
}
