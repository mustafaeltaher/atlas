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
         * This is a Projection query that avoids loading the entire MonthlyAllocation
         * entity,
         * eliminating the need for in-memory filtering.
         *
         * @param allocationId The allocation ID
         * @param year         The year
         * @param month        The month (1-12)
         * @return Optional containing the percentage, or empty if no data exists for
         *         that month
         */
        @Query("SELECT ma.percentage FROM MonthlyAllocation ma WHERE ma.allocation.id = :allocationId AND ma.year = :year AND ma.month = :month")
        Optional<Integer> findPercentageByAllocationIdAndYearMonth(
                        @Param("allocationId") Long allocationId,
                        @Param("year") Integer year,
                        @Param("month") Integer month);

        /**
         * Counts the number of distinct active projects (percentage > 0) per employee
         * for a given month, optionally filtered by allocation type.
         * Returned array contains: [0] = Employee ID (Long), [1] = Project Count (Long)
         *
         * @param employeeIds     List of employee IDs to count projects for
         * @param year            The year to filter by
         * @param month           The month to filter by (1-12)
         * @param allocationType  Optional allocation type filter (PROJECT, PROSPECT, etc.). If null, counts all types.
         * @return List of Object arrays with [employeeId, projectCount]
         */
        @Query("SELECT ma.allocation.employee.id, COUNT(DISTINCT ma.allocation.project.id) " +
                        "FROM MonthlyAllocation ma " +
                        "WHERE ma.allocation.employee.id IN :employeeIds " +
                        "AND ma.year = :year " +
                        "AND ma.month = :month " +
                        "AND ma.percentage > 0 " +
                        "AND (:allocationType IS NULL OR CAST(ma.allocation.allocationType AS string) = :allocationType) " +
                        "GROUP BY ma.allocation.employee.id")
        List<Object[]> findDistinctProjectCountByEmployeeIdsAndYearMonth(
                        @Param("employeeIds") List<Long> employeeIds,
                        @Param("year") Integer year,
                        @Param("month") Integer month,
                        @Param("allocationType") String allocationType);
}
