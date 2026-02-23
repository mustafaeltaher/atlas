package com.atlas.specification;

import com.atlas.entity.Allocation;
import com.atlas.entity.Employee;
import com.atlas.entity.Project;
import com.atlas.repository.AllocationRepository;
import com.atlas.repository.EmployeeRepository;
import com.atlas.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for AllocationSpecification, focusing on month/year filtering logic.
 * Tests verify that date range predicates correctly filter allocations based on:
 * - Allocations active during selected month
 * - Allocations with NULL endDate (ongoing)
 * - Allocations starting or ending mid-month
 * - Allocations completely outside the date range
 */
@DataJpaTest
class AllocationSpecificationTest {

    @Autowired
    private AllocationRepository allocationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ProjectRepository projectRepository;

    private Employee testEmployee;
    private Project testProject;

    @BeforeEach
    void setUp() {
        // Create test employee
        testEmployee = Employee.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .oracleId(12345)
                .build();
        testEmployee = employeeRepository.save(testEmployee);

        // Create test project
        testProject = Project.builder()
                .description("Test Project")
                .region("US")
                .build();
        testProject = projectRepository.save(testProject);
    }

    @Test
    void byMonthYear_validMonthYear_filtersCorrectly() {
        // Given: An allocation active during February 2026
        Allocation allocation = createAllocation(
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 28)
        );

        // When: Filter allocations for February 2026
        Specification<Allocation> spec = AllocationSpecification.withFilters(
                null, null, null, List.of(testEmployee.getId()), 2026, 2
        );
        List<Allocation> result = allocationRepository.findAll(spec);

        // Then: Allocation should be included
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(allocation.getId());
    }

    @Test
    void byMonthYear_nullEndDate_includesOngoingAllocations() {
        // Given: An ongoing allocation (NULL endDate) that started before February 2026
        Allocation ongoingAllocation = createAllocation(
                LocalDate.of(2026, 1, 15),
                null  // Ongoing, no end date
        );

        // When: Filter allocations for February 2026
        Specification<Allocation> spec = AllocationSpecification.withFilters(
                null, null, null, List.of(testEmployee.getId()), 2026, 2
        );
        List<Allocation> result = allocationRepository.findAll(spec);

        // Then: Ongoing allocation should be included
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(ongoingAllocation.getId());
    }

    @Test
    void byMonthYear_allocationEndedBeforeMonth_excludes() {
        // Given: An allocation that ended before February 2026
        createAllocation(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31)  // Ended in January
        );

        // When: Filter allocations for February 2026
        Specification<Allocation> spec = AllocationSpecification.withFilters(
                null, null, null, List.of(testEmployee.getId()), 2026, 2
        );
        List<Allocation> result = allocationRepository.findAll(spec);

        // Then: Allocation should be excluded
        assertThat(result).isEmpty();
    }

    @Test
    void byMonthYear_allocationStartsAfterMonth_excludes() {
        // Given: An allocation that starts after February 2026
        createAllocation(
                LocalDate.of(2026, 3, 1),  // Starts in March
                LocalDate.of(2026, 3, 31)
        );

        // When: Filter allocations for February 2026
        Specification<Allocation> spec = AllocationSpecification.withFilters(
                null, null, null, List.of(testEmployee.getId()), 2026, 2
        );
        List<Allocation> result = allocationRepository.findAll(spec);

        // Then: Allocation should be excluded
        assertThat(result).isEmpty();
    }

    @Test
    void byMonthYear_allocationStartsMidMonth_includes() {
        // Given: An allocation that starts mid-February and extends beyond
        Allocation allocation = createAllocation(
                LocalDate.of(2026, 2, 15),  // Starts mid-February
                LocalDate.of(2026, 3, 15)
        );

        // When: Filter allocations for February 2026
        Specification<Allocation> spec = AllocationSpecification.withFilters(
                null, null, null, List.of(testEmployee.getId()), 2026, 2
        );
        List<Allocation> result = allocationRepository.findAll(spec);

        // Then: Allocation should be included
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(allocation.getId());
    }

    @Test
    void byMonthYear_allocationEndsMidMonth_includes() {
        // Given: An allocation that started before February and ends mid-February
        Allocation allocation = createAllocation(
                LocalDate.of(2026, 1, 15),
                LocalDate.of(2026, 2, 15)  // Ends mid-February
        );

        // When: Filter allocations for February 2026
        Specification<Allocation> spec = AllocationSpecification.withFilters(
                null, null, null, List.of(testEmployee.getId()), 2026, 2
        );
        List<Allocation> result = allocationRepository.findAll(spec);

        // Then: Allocation should be included
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(allocation.getId());
    }

    @Test
    void byMonthYear_allocationSpansEntireMonth_includes() {
        // Given: An allocation that spans the entire month of February
        Allocation allocation = createAllocation(
                LocalDate.of(2026, 1, 1),   // Started before February
                LocalDate.of(2026, 3, 31)   // Ends after February
        );

        // When: Filter allocations for February 2026
        Specification<Allocation> spec = AllocationSpecification.withFilters(
                null, null, null, List.of(testEmployee.getId()), 2026, 2
        );
        List<Allocation> result = allocationRepository.findAll(spec);

        // Then: Allocation should be included
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(allocation.getId());
    }

    @Test
    void byMonthYear_leapYear_handlesCorrectly() {
        // Given: Allocations in February 2024 (leap year - 29 days)
        Allocation allocation = createAllocation(
                LocalDate.of(2024, 2, 1),
                LocalDate.of(2024, 2, 29)  // Feb 29 exists in leap year
        );

        // When: Filter allocations for February 2024
        Specification<Allocation> spec = AllocationSpecification.withFilters(
                null, null, null, List.of(testEmployee.getId()), 2024, 2
        );
        List<Allocation> result = allocationRepository.findAll(spec);

        // Then: Allocation should be included
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(allocation.getId());
    }

    @Test
    void byMonthYear_multipleAllocations_filtersCorrectly() {
        // Given: Multiple allocations with different date ranges
        Allocation feb2026 = createAllocation(
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 28)
        );

        createAllocation(  // January - should be excluded
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31)
        );

        Allocation ongoing = createAllocation(  // Ongoing - should be included
                LocalDate.of(2026, 1, 15),
                null
        );

        createAllocation(  // March - should be excluded
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31)
        );

        // When: Filter allocations for February 2026
        Specification<Allocation> spec = AllocationSpecification.withFilters(
                null, null, null, List.of(testEmployee.getId()), 2026, 2
        );
        List<Allocation> result = allocationRepository.findAll(spec);

        // Then: Only February and ongoing allocations should be included
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Allocation::getId)
                .containsExactlyInAnyOrder(feb2026.getId(), ongoing.getId());
    }

    @Test
    void byMonthYear_combinedWithTypeFilter_filtersCorrectly() {
        // Given: PROJECT and PROSPECT allocations in February 2026
        Allocation projectAllocation = createAllocation(
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 28),
                Allocation.AllocationType.PROJECT
        );

        createAllocation(  // PROSPECT - should be excluded by type filter
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 28),
                Allocation.AllocationType.PROSPECT
        );

        // When: Filter allocations for PROJECT type in February 2026
        Specification<Allocation> spec = AllocationSpecification.withFilters(
                Allocation.AllocationType.PROJECT,
                null,
                null,
                List.of(testEmployee.getId()),
                2026,
                2
        );
        List<Allocation> result = allocationRepository.findAll(spec);

        // Then: Only PROJECT allocation should be included
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(projectAllocation.getId());
        assertThat(result.get(0).getAllocationType()).isEqualTo(Allocation.AllocationType.PROJECT);
    }

    // Helper methods

    private Allocation createAllocation(LocalDate startDate, LocalDate endDate) {
        return createAllocation(startDate, endDate, Allocation.AllocationType.PROJECT);
    }

    private Allocation createAllocation(LocalDate startDate, LocalDate endDate, Allocation.AllocationType type) {
        Allocation allocation = Allocation.builder()
                .employee(testEmployee)
                .project(testProject)
                .startDate(startDate)
                .endDate(endDate)
                .allocationType(type)
                .build();
        return allocationRepository.save(allocation);
    }
}
