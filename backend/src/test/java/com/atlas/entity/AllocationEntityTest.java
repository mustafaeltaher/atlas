package com.atlas.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Allocation entity helper methods.
 * Tests the in-memory filtering logic for monthly allocations.
 */
@DisplayName("Allocation Entity Tests")
public class AllocationEntityTest {

    private Allocation allocation;
    private Employee employee;
    private Project project;

    @BeforeEach
    void setUp() {
        // Create test employee
        employee = Employee.builder()
                .id(1L)
                .oracleId(1000)
                .name("Test Employee")
                .email("test@atlas.com")
                .build();

        // Create test project
        project = Project.builder()
                .id(1L)
                .projectId("PRJ-001")
                .description("Test Project")
                .build();

        // Create allocation with empty monthly allocations
        allocation = Allocation.builder()
                .id(1L)
                .employee(employee)
                .project(project)
                .allocationType(Allocation.AllocationType.PROJECT)
                .startDate(LocalDate.now().minusMonths(3))
                .endDate(LocalDate.now().plusMonths(3))
                .build();
    }

    @Test
    @DisplayName("Should return null when no monthly allocation exists for given year/month")
    void shouldReturnNullWhenNoMonthlyAllocationExists() {
        // When: Get allocation for a year/month with no data
        Integer result = allocation.getAllocationForYearMonth(2025, 6);

        // Then: Should return null
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should return correct percentage when monthly allocation exists")
    void shouldReturnCorrectPercentageWhenMonthlyAllocationExists() {
        // Given: Allocation with monthly data
        MonthlyAllocation monthlyAlloc = MonthlyAllocation.builder()
                .id(1L)
                .allocation(allocation)
                .year(2025)
                .month(6)
                .percentage(75)
                .build();
        allocation.getMonthlyAllocations().add(monthlyAlloc);

        // When: Get allocation for that year/month
        Integer result = allocation.getAllocationForYearMonth(2025, 6);

        // Then: Should return correct percentage
        assertThat(result).isEqualTo(75);
    }

    @Test
    @DisplayName("Should return correct percentage when multiple monthly allocations exist")
    void shouldReturnCorrectPercentageWithMultipleMonthlyAllocations() {
        // Given: Allocation with multiple monthly data points
        MonthlyAllocation monthlyAlloc1 = MonthlyAllocation.builder()
                .id(1L)
                .allocation(allocation)
                .year(2025)
                .month(5)
                .percentage(50)
                .build();
        MonthlyAllocation monthlyAlloc2 = MonthlyAllocation.builder()
                .id(2L)
                .allocation(allocation)
                .year(2025)
                .month(6)
                .percentage(100)
                .build();
        MonthlyAllocation monthlyAlloc3 = MonthlyAllocation.builder()
                .id(3L)
                .allocation(allocation)
                .year(2025)
                .month(7)
                .percentage(25)
                .build();
        allocation.getMonthlyAllocations().add(monthlyAlloc1);
        allocation.getMonthlyAllocations().add(monthlyAlloc2);
        allocation.getMonthlyAllocations().add(monthlyAlloc3);

        // When: Get allocation for June 2025
        Integer result = allocation.getAllocationForYearMonth(2025, 6);

        // Then: Should return correct percentage for June only
        assertThat(result).isEqualTo(100);
    }

    @Test
    @DisplayName("Should distinguish between different years")
    void shouldDistinguishBetweenDifferentYears() {
        // Given: Same month but different years
        MonthlyAllocation monthlyAlloc2024 = MonthlyAllocation.builder()
                .id(1L)
                .allocation(allocation)
                .year(2024)
                .month(6)
                .percentage(50)
                .build();
        MonthlyAllocation monthlyAlloc2025 = MonthlyAllocation.builder()
                .id(2L)
                .allocation(allocation)
                .year(2025)
                .month(6)
                .percentage(100)
                .build();
        allocation.getMonthlyAllocations().add(monthlyAlloc2024);
        allocation.getMonthlyAllocations().add(monthlyAlloc2025);

        // When: Get allocations for different years
        Integer result2024 = allocation.getAllocationForYearMonth(2024, 6);
        Integer result2025 = allocation.getAllocationForYearMonth(2025, 6);

        // Then: Should return correct percentages for each year
        assertThat(result2024).isEqualTo(50);
        assertThat(result2025).isEqualTo(100);
    }

    @Test
    @DisplayName("Should return null for year/month combination that doesn't exist")
    void shouldReturnNullForNonExistentYearMonth() {
        // Given: Allocation with some monthly data
        MonthlyAllocation monthlyAlloc = MonthlyAllocation.builder()
                .id(1L)
                .allocation(allocation)
                .year(2025)
                .month(6)
                .percentage(100)
                .build();
        allocation.getMonthlyAllocations().add(monthlyAlloc);

        // When: Get allocation for different month
        Integer result = allocation.getAllocationForYearMonth(2025, 7);

        // Then: Should return null
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should handle zero percentage correctly")
    void shouldHandleZeroPercentageCorrectly() {
        // Given: Allocation with 0% monthly allocation
        MonthlyAllocation monthlyAlloc = MonthlyAllocation.builder()
                .id(1L)
                .allocation(allocation)
                .year(2025)
                .month(6)
                .percentage(0)
                .build();
        allocation.getMonthlyAllocations().add(monthlyAlloc);

        // When: Get allocation for that year/month
        Integer result = allocation.getAllocationForYearMonth(2025, 6);

        // Then: Should return 0, not null
        assertThat(result).isEqualTo(0);
    }

    @Test
    @DisplayName("Should work with current year and month")
    void shouldWorkWithCurrentYearAndMonth() {
        // Given: Current year and month
        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();

        MonthlyAllocation monthlyAlloc = MonthlyAllocation.builder()
                .id(1L)
                .allocation(allocation)
                .year(currentYear)
                .month(currentMonth)
                .percentage(80)
                .build();
        allocation.getMonthlyAllocations().add(monthlyAlloc);

        // When: Get allocation for current year/month
        Integer result = allocation.getAllocationForYearMonth(currentYear, currentMonth);

        // Then: Should return correct percentage
        assertThat(result).isEqualTo(80);
    }

    @Test
    @DisplayName("Should filter correctly when many monthly allocations span multiple years")
    void shouldFilterCorrectlyWithManyMonthlyAllocations() {
        // Given: Many monthly allocations across 2 years (24 months)
        for (int year = 2024; year <= 2025; year++) {
            for (int month = 1; month <= 12; month++) {
                int percentage = (year * 100) + month; // Unique value per year/month
                MonthlyAllocation monthlyAlloc = MonthlyAllocation.builder()
                        .id((long) ((year - 2024) * 12 + month))
                        .allocation(allocation)
                        .year(year)
                        .month(month)
                        .percentage(percentage)
                        .build();
                allocation.getMonthlyAllocations().add(monthlyAlloc);
            }
        }

        // When: Get specific allocations
        Integer result2024Jan = allocation.getAllocationForYearMonth(2024, 1);
        Integer result2025Dec = allocation.getAllocationForYearMonth(2025, 12);
        Integer result2024Jun = allocation.getAllocationForYearMonth(2024, 6);

        // Then: Should correctly filter each one
        assertThat(result2024Jan).isEqualTo(2024 * 100 + 1);  // 202401
        assertThat(result2025Dec).isEqualTo(2025 * 100 + 12); // 202512
        assertThat(result2024Jun).isEqualTo(2024 * 100 + 6);  // 202406
    }
}
