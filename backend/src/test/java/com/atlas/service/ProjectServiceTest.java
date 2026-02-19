package com.atlas.service;

import com.atlas.dto.ProjectDTO;
import com.atlas.entity.Allocation;
import com.atlas.entity.Employee;
import com.atlas.entity.MonthlyAllocation;
import com.atlas.entity.Project;
import com.atlas.repository.AllocationRepository;
import com.atlas.repository.MonthlyAllocationRepository;
import com.atlas.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ProjectService DTO conversion methods.
 * Tests average allocation calculation for PROJECT allocations.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Project Service Tests")
public class ProjectServiceTest {

    @Mock
    private EmployeeService employeeService;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private AllocationRepository allocationRepository;

    @Mock
    private MonthlyAllocationRepository monthlyAllocationRepository;

    @InjectMocks
    private ProjectService projectService;

    private Project project;
    private int currentYear;
    private int currentMonth;

    @BeforeEach
    void setUp() {
        currentYear = LocalDate.now().getYear();
        currentMonth = LocalDate.now().getMonthValue();

        project = Project.builder()
                .id(1L)
                .projectId("PRJ-001")
                .description("Test Project")
                .status(Project.ProjectStatus.ACTIVE)
                .build();
    }

    /**
     * Sets up mock for batch monthly allocation query.
     * This simulates the database returning monthly allocations for the given allocations.
     */
    private void mockMonthlyAllocations(List<Allocation> allocations) {
        // Extract allocation IDs
        List<Long> allocationIds = allocations.stream()
                .map(Allocation::getId)
                .collect(Collectors.toList());

        // Build list of MonthlyAllocation entities that have percentages
        List<MonthlyAllocation> monthlyAllocations = allocations.stream()
                .filter(a -> !a.getMonthlyAllocations().isEmpty())
                .flatMap(a -> a.getMonthlyAllocations().stream())
                .filter(ma -> ma.getYear() == currentYear && ma.getMonth() == currentMonth)
                .collect(Collectors.toList());

        // Mock the batch query
        when(monthlyAllocationRepository.findByAllocationIdsAndYearAndMonth(
                allocationIds, currentYear, currentMonth))
                .thenReturn(monthlyAllocations);
    }

    @Test
    @DisplayName("Should calculate average allocation for single employee")
    void shouldCalculateAverageForSingleEmployee() throws Exception {
        // Given: Project with one employee allocated at 75%
        Allocation allocation = createAllocation(1L, 75);
        List<Allocation> allocations = List.of(allocation);
        mockMonthlyAllocations(allocations);

        // When: Convert to DTO
        ProjectDTO dto = invokeToDTO(project, allocations);

        // Then: Average allocation should be 75% (one employee at 75%)
        assertThat(dto.getAverageAllocation()).isEqualTo(75.0);
        assertThat(dto.getAllocatedEmployees()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should calculate average allocation for multiple employees")
    void shouldCalculateAverageForMultipleEmployees() throws Exception {
        // Given: Project with three employees (100%, 50%, 25%)
        Allocation alloc1 = createAllocation(1L, 100);
        Allocation alloc2 = createAllocation(2L, 50);
        Allocation alloc3 = createAllocation(3L, 25);
        List<Allocation> allocations = List.of(alloc1, alloc2, alloc3);
        mockMonthlyAllocations(allocations);

        // When: Convert to DTO
        ProjectDTO dto = invokeToDTO(project, allocations);

        // Then: Average allocation should be (100 + 50 + 25) / 3 = 58.33%
        assertThat(dto.getAverageAllocation()).isEqualTo(175.0 / 3.0);
        assertThat(dto.getAllocatedEmployees()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should exclude zero percentages from average calculation")
    void shouldExcludeZeroPercentagesFromAverage() throws Exception {
        // Given: Project with employees (100%, 0%, 50%)
        Allocation alloc1 = createAllocation(1L, 100);
        Allocation alloc2 = createAllocation(2L, 0);
        Allocation alloc3 = createAllocation(3L, 50);
        List<Allocation> allocations = List.of(alloc1, alloc2, alloc3);
        mockMonthlyAllocations(allocations);

        // When: Convert to DTO
        ProjectDTO dto = invokeToDTO(project, allocations);

        // Then: Average should be (100 + 50) / 2 = 75% (excludes zero)
        assertThat(dto.getAverageAllocation()).isEqualTo(75.0);
        assertThat(dto.getAllocatedEmployees()).isEqualTo(3); // Total count includes all
    }

    @Test
    @DisplayName("Should exclude null percentages from average calculation")
    void shouldExcludeNullPercentagesFromAverage() throws Exception {
        // Given: Project with employees (100%, null, 50%)
        Allocation alloc1 = createAllocation(1L, 100);
        Allocation alloc2 = createAllocation(2L, null);
        Allocation alloc3 = createAllocation(3L, 50);
        List<Allocation> allocations = List.of(alloc1, alloc2, alloc3);
        mockMonthlyAllocations(allocations);

        // When: Convert to DTO
        ProjectDTO dto = invokeToDTO(project, allocations);

        // Then: Average should be (100 + 50) / 2 = 75% (excludes null)
        assertThat(dto.getAverageAllocation()).isEqualTo(75.0);
        assertThat(dto.getAllocatedEmployees()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should return zero average when all percentages are zero or null")
    void shouldReturnZeroWhenAllPercentagesAreZeroOrNull() throws Exception {
        // Given: Project with employees (0%, null, 0%)
        Allocation alloc1 = createAllocation(1L, 0);
        Allocation alloc2 = createAllocation(2L, null);
        Allocation alloc3 = createAllocation(3L, 0);
        List<Allocation> allocations = List.of(alloc1, alloc2, alloc3);
        mockMonthlyAllocations(allocations);

        // When: Convert to DTO
        ProjectDTO dto = invokeToDTO(project, allocations);

        // Then: Average should be 0% (no valid percentages)
        assertThat(dto.getAverageAllocation()).isEqualTo(0.0);
        assertThat(dto.getAllocatedEmployees()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should return zero average when no allocations exist")
    void shouldReturnZeroWhenNoAllocations() throws Exception {
        // Given: Project with no allocations
        List<Allocation> allocations = List.of();
        // No need to mock - empty list skips repository call

        // When: Convert to DTO
        ProjectDTO dto = invokeToDTO(project, allocations);

        // Then: Average should be 0%
        assertThat(dto.getAverageAllocation()).isEqualTo(0.0);
        assertThat(dto.getAllocatedEmployees()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should use batch query to fetch current month allocations")
    void shouldUseBatchQueryToFetchCurrentMonth() throws Exception {
        // Given: Multiple allocations
        Allocation alloc1 = createAllocation(1L, 75);
        Allocation alloc2 = createAllocation(2L, 50);
        List<Allocation> allocations = List.of(alloc1, alloc2);
        mockMonthlyAllocations(allocations);

        // When: Convert to DTO
        ProjectDTO dto = invokeToDTO(project, allocations);

        // Then: Should use batch query and calculate correct average
        assertThat(dto.getAverageAllocation()).isEqualTo(62.5); // (75 + 50) / 2
        // Verify no lazy loading occurred (implicit - if lazy loading happened, mock wouldn't be hit)
    }

    @Test
    @DisplayName("Should handle project with only PROSPECT allocations")
    void shouldHandleProspectAllocations() throws Exception {
        // Given: Project with PROSPECT allocations (they have percentages too)
        Allocation alloc1 = createAllocationWithType(1L, 100, Allocation.AllocationType.PROSPECT);
        Allocation alloc2 = createAllocationWithType(2L, 50, Allocation.AllocationType.PROSPECT);
        List<Allocation> allocations = List.of(alloc1, alloc2);
        mockMonthlyAllocations(allocations);

        // When: Convert to DTO
        ProjectDTO dto = invokeToDTO(project, allocations);

        // Then: Should calculate average correctly
        assertThat(dto.getAverageAllocation()).isEqualTo(75.0); // (100 + 50) / 2
        assertThat(dto.getAllocatedEmployees()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should handle over-allocation scenarios (>100% per employee)")
    void shouldHandleOverAllocation() throws Exception {
        // Given: Employees allocated more than 100% (realistic scenario)
        Allocation alloc1 = createAllocation(1L, 120);
        Allocation alloc2 = createAllocation(2L, 110);
        List<Allocation> allocations = List.of(alloc1, alloc2);
        mockMonthlyAllocations(allocations);

        // When: Convert to DTO
        ProjectDTO dto = invokeToDTO(project, allocations);

        // Then: Should calculate average correctly even with >100%
        assertThat(dto.getAverageAllocation()).isEqualTo(115.0); // (120 + 110) / 2
        assertThat(dto.getAllocatedEmployees()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should calculate correct total before averaging")
    void shouldCalculateCorrectTotalBeforeAveraging() throws Exception {
        // Given: Project with 5 employees at different percentages
        Allocation alloc1 = createAllocation(1L, 100);
        Allocation alloc2 = createAllocation(2L, 75);
        Allocation alloc3 = createAllocation(3L, 50);
        Allocation alloc4 = createAllocation(4L, 25);
        Allocation alloc5 = createAllocation(5L, 10);
        List<Allocation> allocations = List.of(alloc1, alloc2, alloc3, alloc4, alloc5);
        mockMonthlyAllocations(allocations);

        // When: Convert to DTO
        ProjectDTO dto = invokeToDTO(project, allocations);

        // Then: Total = 100+75+50+25+10 = 260, Average = 260/5 = 52.0
        assertThat(dto.getAverageAllocation()).isEqualTo(52.0);
        assertThat(dto.getAllocatedEmployees()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should handle mix of PROJECT and PROSPECT allocations")
    void shouldHandleMixedAllocationTypes() throws Exception {
        // Given: Mix of PROJECT and PROSPECT allocations
        Allocation projectAlloc1 = createAllocationWithType(1L, 100, Allocation.AllocationType.PROJECT);
        Allocation projectAlloc2 = createAllocationWithType(2L, 75, Allocation.AllocationType.PROJECT);
        Allocation prospect1 = createAllocationWithType(3L, 50, Allocation.AllocationType.PROSPECT);
        Allocation prospect2 = createAllocationWithType(4L, 25, Allocation.AllocationType.PROSPECT);
        List<Allocation> allocations = List.of(projectAlloc1, projectAlloc2, prospect1, prospect2);
        mockMonthlyAllocations(allocations);

        // When: Convert to DTO
        ProjectDTO dto = invokeToDTO(this.project, allocations);

        // Then: Should sum all types: (100+75+50+25)/4 = 62.5
        assertThat(dto.getAverageAllocation()).isEqualTo(62.5);
        assertThat(dto.getAllocatedEmployees()).isEqualTo(4);
    }

    @Test
    @DisplayName("Should handle single employee with partial allocation")
    void shouldHandleSingleEmployeePartialAllocation() throws Exception {
        // Given: Single employee at 33%
        Allocation alloc = createAllocation(1L, 33);
        List<Allocation> allocations = List.of(alloc);
        mockMonthlyAllocations(allocations);

        // When: Convert to DTO
        ProjectDTO dto = invokeToDTO(project, allocations);

        // Then: Average equals the single value
        assertThat(dto.getAverageAllocation()).isEqualTo(33.0);
        assertThat(dto.getAllocatedEmployees()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should exclude MATERNITY allocations from average (no percentage)")
    void shouldExcludeMaternityFromAverage() throws Exception {
        // Given: Mix of PROJECT and MATERNITY (MATERNITY has no percentage)
        Allocation projectAlloc = createAllocationWithType(1L, 100, Allocation.AllocationType.PROJECT);
        Allocation maternity = createAllocationWithType(2L, null, Allocation.AllocationType.MATERNITY);
        List<Allocation> allocations = List.of(projectAlloc, maternity);
        mockMonthlyAllocations(allocations);

        // When: Convert to DTO
        ProjectDTO dto = invokeToDTO(this.project, allocations);

        // Then: Only PROJECT counted: 100/1 = 100.0
        assertThat(dto.getAverageAllocation()).isEqualTo(100.0);
        assertThat(dto.getAllocatedEmployees()).isEqualTo(2); // Total count includes all
    }

    @Test
    @DisplayName("Should exclude VACATION allocations from average (no percentage)")
    void shouldExcludeVacationFromAverage() throws Exception {
        // Given: Mix of PROJECT and VACATION (VACATION has no percentage)
        Allocation projectAlloc1 = createAllocationWithType(1L, 80, Allocation.AllocationType.PROJECT);
        Allocation projectAlloc2 = createAllocationWithType(2L, 60, Allocation.AllocationType.PROJECT);
        Allocation vacation = createAllocationWithType(3L, null, Allocation.AllocationType.VACATION);
        List<Allocation> allocations = List.of(projectAlloc1, projectAlloc2, vacation);
        mockMonthlyAllocations(allocations);

        // When: Convert to DTO
        ProjectDTO dto = invokeToDTO(this.project, allocations);

        // Then: Only PROJECTs counted: (80+60)/2 = 70.0
        assertThat(dto.getAverageAllocation()).isEqualTo(70.0);
        assertThat(dto.getAllocatedEmployees()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should calculate average with high precision for odd divisions")
    void shouldCalculateAverageWithHighPrecision() throws Exception {
        // Given: Allocations that result in repeating decimals
        Allocation alloc1 = createAllocation(1L, 100);
        Allocation alloc2 = createAllocation(2L, 100);
        Allocation alloc3 = createAllocation(3L, 100);
        List<Allocation> allocations = List.of(alloc1, alloc2, alloc3);
        mockMonthlyAllocations(allocations);

        // When: Convert to DTO
        ProjectDTO dto = invokeToDTO(project, allocations);

        // Then: 300/3 = 100.0 (exact)
        assertThat(dto.getAverageAllocation()).isEqualTo(100.0);

        // Edge case: 100/3 = 33.333...
        Allocation single = createAllocation(1L, 100);
        Allocation alloc2_2 = createAllocation(2L, 0); // Will be excluded
        Allocation alloc3_2 = createAllocation(3L, 0); // Will be excluded
        List<Allocation> allocations2 = List.of(single, alloc2_2, alloc3_2);
        mockMonthlyAllocations(allocations2);
        ProjectDTO dto2 = invokeToDTO(project, allocations2);
        assertThat(dto2.getAverageAllocation()).isEqualTo(100.0);
    }

    @Test
    @DisplayName("Should count only positive percentages in average denominator")
    void shouldCountOnlyPositivePercentagesInDenominator() throws Exception {
        // Given: 10 allocations, but only 3 have positive percentages
        List<Allocation> allocations = new ArrayList<>();
        allocations.add(createAllocation(1L, 90));
        allocations.add(createAllocation(2L, 60));
        allocations.add(createAllocation(3L, 30));
        allocations.add(createAllocation(4L, 0));
        allocations.add(createAllocation(5L, 0));
        allocations.add(createAllocation(6L, null));
        allocations.add(createAllocation(7L, null));
        allocations.add(createAllocation(8L, 0));
        allocations.add(createAllocation(9L, null));
        allocations.add(createAllocation(10L, 0));
        mockMonthlyAllocations(allocations);

        // When: Convert to DTO
        ProjectDTO dto = invokeToDTO(project, allocations);

        // Then: Average = (90+60+30)/3 = 60.0 (not divided by 10)
        assertThat(dto.getAverageAllocation()).isEqualTo(60.0);
        assertThat(dto.getAllocatedEmployees()).isEqualTo(10); // Total count
    }

    // Helper methods

    private Allocation createAllocation(Long employeeId, Integer percentage) {
        return createAllocationWithType(employeeId, percentage, Allocation.AllocationType.PROJECT);
    }

    private Allocation createAllocationWithType(Long employeeId, Integer percentage, Allocation.AllocationType type) {
        Employee emp = Employee.builder()
                .id(employeeId)
                .oracleId(1000 + employeeId.intValue())
                .name("Employee " + employeeId)
                .email("emp" + employeeId + "@atlas.com")
                .build();

        Allocation allocation = Allocation.builder()
                .id((long) (Math.random() * 1000))
                .employee(emp)
                .project(project)
                .allocationType(type)
                .startDate(LocalDate.now().minusMonths(6))
                .endDate(LocalDate.now().plusMonths(6))
                .monthlyAllocations(new ArrayList<>())
                .build();

        // Add current month allocation if percentage is provided and > 0
        if (percentage != null && percentage > 0) {
            MonthlyAllocation monthlyAlloc = MonthlyAllocation.builder()
                    .id((long) (Math.random() * 1000))
                    .allocation(allocation)
                    .year(currentYear)
                    .month(currentMonth)
                    .percentage(percentage)
                    .build();
            allocation.getMonthlyAllocations().add(monthlyAlloc);
        }

        return allocation;
    }

    /**
     * Invokes the private toDTO method using reflection for testing purposes.
     */
    private ProjectDTO invokeToDTO(Project project, List<Allocation> allocations) throws Exception {
        Method toDTOMethod = ProjectService.class.getDeclaredMethod("toDTO", Project.class, List.class);
        toDTOMethod.setAccessible(true);
        return (ProjectDTO) toDTOMethod.invoke(projectService, project, allocations);
    }
}
