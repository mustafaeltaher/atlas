package com.atlas.service;

import com.atlas.dto.EmployeeDTO;
import com.atlas.entity.Allocation;
import com.atlas.entity.Employee;
import com.atlas.entity.MonthlyAllocation;
import com.atlas.entity.Project;
import com.atlas.repository.AllocationRepository;
import com.atlas.repository.EmployeeRepository;
import com.atlas.repository.EmployeeSkillRepository;
import com.atlas.repository.MonthlyAllocationRepository;
import com.atlas.repository.TechTowerRepository;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Unit tests for EmployeeService DTO conversion methods.
 * Tests total allocation calculation for PROJECT and PROSPECT allocations.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Employee Service Tests")
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AllocationRepository allocationRepository;

    @Mock
    private EmployeeSkillRepository employeeSkillRepository;

    @Mock
    private TechTowerRepository techTowerRepository;

    @Mock
    private MonthlyAllocationRepository monthlyAllocationRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee;
    private Project project;
    private int currentYear;
    private int currentMonth;

    @BeforeEach
    void setUp() {
        currentYear = LocalDate.now().getYear();
        currentMonth = LocalDate.now().getMonthValue();

        employee = Employee.builder()
                .id(1L)
                .oracleId(1000)
                .name("Test Employee")
                .email("test@atlas.com")
                .build();

        project = Project.builder()
                .id(1L)
                .projectId("PRJ-001")
                .description("Test Project")
                .build();

        // Mock skill repository to return empty list
        when(employeeSkillRepository.findByEmployeeId(anyLong())).thenReturn(List.of());
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
    @DisplayName("Should count PROJECT allocation in total allocation")
    void shouldCountProjectAllocationInTotal() throws Exception {
        // Given: Employee with one PROJECT allocation at 75%
        Allocation projectAllocation = createAllocation(Allocation.AllocationType.PROJECT, 75);
        List<Allocation> allocations = List.of(projectAllocation);
        mockMonthlyAllocations(allocations);

        // When: Convert to DTO
        EmployeeDTO dto = invokeToDTO(employee, allocations);

        // Then: Total allocation should be 75%
        assertThat(dto.getTotalAllocation()).isEqualTo(75.0);
        assertThat(dto.getAllocationStatus()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("Should count PROSPECT allocation in total allocation")
    void shouldCountProspectAllocationInTotal() throws Exception {
        // Given: Employee with one PROSPECT allocation at 50%
        Allocation prospectAllocation = createAllocation(Allocation.AllocationType.PROSPECT, 50);
        List<Allocation> allocations = List.of(prospectAllocation);
        mockMonthlyAllocations(allocations);

        // When: Convert to DTO
        EmployeeDTO dto = invokeToDTO(employee, allocations);

        // Then: Total allocation should be 50% (PROSPECT allocations have percentages!)
        assertThat(dto.getTotalAllocation()).isEqualTo(50.0);
        assertThat(dto.getAllocationStatus()).isEqualTo("PROSPECT");
    }

    @Test
    @DisplayName("Should sum PROJECT and PROSPECT allocations in total")
    void shouldSumProjectAndProspectAllocations() throws Exception {
        // Given: Employee with PROJECT (75%) and PROSPECT (25%) allocations
        Allocation projectAllocation = createAllocation(Allocation.AllocationType.PROJECT, 75);
        Allocation prospectAllocation = createAllocation(Allocation.AllocationType.PROSPECT, 25);
        List<Allocation> allocations = List.of(projectAllocation, prospectAllocation);
        mockMonthlyAllocations(allocations);

        // When: Convert to DTO
        EmployeeDTO dto = invokeToDTO(employee, allocations);

        // Then: Total allocation should be 100% (75 + 25)
        assertThat(dto.getTotalAllocation()).isEqualTo(100.0);
        // Has PROJECT allocation, so status is ACTIVE (not PROSPECT)
        assertThat(dto.getAllocationStatus()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("Should ignore MATERNITY allocation in total percentage")
    void shouldIgnoreMaternityAllocationInTotal() throws Exception {
        // Given: Employee with MATERNITY allocation (no percentage)
        Allocation maternityAllocation = createAllocation(Allocation.AllocationType.MATERNITY, null);
        List<Allocation> allocations = List.of(maternityAllocation);
        mockMonthlyAllocations(allocations);

        // When: Convert to DTO
        EmployeeDTO dto = invokeToDTO(employee, allocations);

        // Then: Total allocation should be 0% (MATERNITY doesn't count)
        assertThat(dto.getTotalAllocation()).isEqualTo(0.0);
        assertThat(dto.getStatus()).isEqualTo("MATERNITY");
    }

    @Test
    @DisplayName("Should ignore VACATION allocation in total percentage")
    void shouldIgnoreVacationAllocationInTotal() throws Exception {
        // Given: Employee with VACATION allocation (no percentage)
        Allocation vacationAllocation = createAllocation(Allocation.AllocationType.VACATION, null);
        List<Allocation> allocations = List.of(vacationAllocation);
        mockMonthlyAllocations(allocations);

        // When: Convert to DTO
        EmployeeDTO dto = invokeToDTO(employee, allocations);

        // Then: Total allocation should be 0% (VACATION doesn't count)
        assertThat(dto.getTotalAllocation()).isEqualTo(0.0);
        assertThat(dto.getStatus()).isEqualTo("VACATION");
    }

    @Test
    @DisplayName("Should handle PROJECT allocation with no current month percentage")
    void shouldHandleProjectAllocationWithNoCurrentMonthPercentage() throws Exception {
        // Given: PROJECT allocation but no monthly allocation for current month
        Allocation projectAllocation = createAllocation(Allocation.AllocationType.PROJECT, null);
        List<Allocation> allocations = List.of(projectAllocation);
        mockMonthlyAllocations(allocations);

        // When: Convert to DTO
        EmployeeDTO dto = invokeToDTO(employee, allocations);

        // Then: Total allocation should be 0%
        assertThat(dto.getTotalAllocation()).isEqualTo(0.0);
        // No active percentage, so status is BENCH
        assertThat(dto.getAllocationStatus()).isEqualTo("BENCH");
    }

    @Test
    @DisplayName("Should handle multiple PROJECT allocations")
    void shouldHandleMultipleProjectAllocations() throws Exception {
        // Given: Multiple PROJECT allocations (50% + 25%)
        Allocation project1 = createAllocation(Allocation.AllocationType.PROJECT, 50);
        Allocation project2 = createAllocation(Allocation.AllocationType.PROJECT, 25);
        List<Allocation> allocations = List.of(project1, project2);
        mockMonthlyAllocations(allocations);

        // When: Convert to DTO
        EmployeeDTO dto = invokeToDTO(employee, allocations);

        // Then: Total allocation should sum to 75%
        assertThat(dto.getTotalAllocation()).isEqualTo(75.0);
        assertThat(dto.getAllocationStatus()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("Should handle zero percentage in current month")
    void shouldHandleZeroPercentageInCurrentMonth() throws Exception {
        // Given: PROJECT allocation with 0% in current month
        Allocation projectAllocation = createAllocation(Allocation.AllocationType.PROJECT, 0);
        List<Allocation> allocations = List.of(projectAllocation);
        mockMonthlyAllocations(allocations);

        // When: Convert to DTO
        EmployeeDTO dto = invokeToDTO(employee, allocations);

        // Then: Total allocation should be 0%
        assertThat(dto.getTotalAllocation()).isEqualTo(0.0);
        assertThat(dto.getAllocationStatus()).isEqualTo("BENCH");
    }

    // Helper methods

    private Allocation createAllocation(Allocation.AllocationType type, Integer percentage) {
        Allocation allocation = Allocation.builder()
                .id((long) (Math.random() * 1000))
                .employee(employee)
                .project(project)
                .allocationType(type)
                .startDate(LocalDate.now().minusMonths(6))
                .endDate(LocalDate.now().plusMonths(6))
                .monthlyAllocations(new ArrayList<>())
                .build();

        // Add current month allocation if percentage is provided
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
    private EmployeeDTO invokeToDTO(Employee employee, List<Allocation> allocations) throws Exception {
        Method toDTOMethod = EmployeeService.class.getDeclaredMethod("toDTO", Employee.class, List.class);
        toDTOMethod.setAccessible(true);
        return (EmployeeDTO) toDTOMethod.invoke(employeeService, employee, allocations);
    }
}
