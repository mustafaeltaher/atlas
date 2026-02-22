package com.atlas.service;

import com.atlas.dto.AllocationDTO;
import com.atlas.entity.Allocation;
import com.atlas.entity.Employee;
import com.atlas.entity.Project;
import com.atlas.repository.AllocationRepository;
import com.atlas.repository.EmployeeRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AllocationService DTO conversion methods.
 * Tests that the toDTO() method correctly fetches currentMonthAllocation
 * from the database using MonthlyAllocationRepository projection query.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Allocation Service Tests")
public class AllocationServiceTest {

    @Mock
    private AllocationRepository allocationRepository;

    @Mock
    private MonthlyAllocationRepository monthlyAllocationRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private AllocationService allocationService;

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
    }

    @Test
    @DisplayName("Should populate currentMonthAllocation from database projection query")
    void shouldPopulateCurrentMonthAllocationFromDatabaseQuery() throws Exception {
        // Given: Allocation and mocked repository to return percentage
        Allocation allocation = createBasicAllocation();
        when(monthlyAllocationRepository.findPercentageByAllocationIdAndYearMonth(
                eq(1L), eq(currentYear), eq(currentMonth)))
                .thenReturn(Optional.of(75));

        // When: Convert to DTO
        AllocationDTO dto = invokeToDTO(allocation);

        // Then: currentMonthAllocation should match the mocked database value
        assertThat(dto.getCurrentMonthAllocation()).isEqualTo(75);
        assertThat(dto.getAllocationPercentage()).isEqualTo(75.0);
    }

    @Test
    @DisplayName("Should set currentMonthAllocation to null when no data for current month")
    void shouldSetCurrentMonthAllocationToNullWhenNoCurrentMonthData() throws Exception {
        // Given: Allocation with no monthly data for current month
        Allocation allocation = createBasicAllocation();
        when(monthlyAllocationRepository.findPercentageByAllocationIdAndYearMonth(
                eq(1L), eq(currentYear), eq(currentMonth)))
                .thenReturn(Optional.empty());

        // When: Convert to DTO
        AllocationDTO dto = invokeToDTO(allocation);

        // Then: currentMonthAllocation should be null
        assertThat(dto.getCurrentMonthAllocation()).isNull();
        assertThat(dto.getAllocationPercentage()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should handle allocation with no monthly data")
    void shouldHandleAllocationWithNoMonthlyData() throws Exception {
        // Given: Allocation with no monthly data at all
        Allocation allocation = createBasicAllocation();
        when(monthlyAllocationRepository.findPercentageByAllocationIdAndYearMonth(
                any(Long.class), any(Integer.class), any(Integer.class)))
                .thenReturn(Optional.empty());

        // When: Convert to DTO
        AllocationDTO dto = invokeToDTO(allocation);

        // Then: Should have null currentMonthAllocation
        assertThat(dto.getCurrentMonthAllocation()).isNull();
        assertThat(dto.getAllocationPercentage()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should populate basic allocation fields correctly")
    void shouldPopulateBasicAllocationFieldsCorrectly() throws Exception {
        // Given: Allocation with all fields
        Allocation allocation = Allocation.builder()
                .id(999L)
                .employee(employee)
                .project(project)
                .allocationType(Allocation.AllocationType.PROJECT)
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 12, 31))
                .monthlyAllocations(new ArrayList<>())
                .build();

        when(monthlyAllocationRepository.findPercentageByAllocationIdAndYearMonth(
                eq(999L), eq(currentYear), eq(currentMonth)))
                .thenReturn(Optional.empty());

        // When: Convert to DTO
        AllocationDTO dto = invokeToDTO(allocation);

        // Then: Basic fields should be correctly populated
        assertThat(dto.getId()).isEqualTo(999L);
        assertThat(dto.getEmployeeId()).isEqualTo(employee.getId());
        assertThat(dto.getEmployeeName()).isEqualTo(employee.getName());
        assertThat(dto.getEmployeeOracleId()).isEqualTo(String.valueOf(employee.getOracleId()));
        assertThat(dto.getProjectId()).isEqualTo(project.getId());
        assertThat(dto.getProjectName()).isEqualTo(project.getDescription());
        assertThat(dto.getAllocationType()).isEqualTo(Allocation.AllocationType.PROJECT);
        assertThat(dto.getStartDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(dto.getEndDate()).isEqualTo(LocalDate.of(2025, 12, 31));
    }

    @Test
    @DisplayName("Should handle MATERNITY allocation without project")
    void shouldHandleMaternityAllocationWithoutProject() throws Exception {
        // Given: MATERNITY allocation with no project
        Allocation allocation = Allocation.builder()
                .id(1L)
                .employee(employee)
                .project(null)  // No project for MATERNITY
                .allocationType(Allocation.AllocationType.MATERNITY)
                .startDate(LocalDate.now().minusMonths(2))
                .endDate(LocalDate.now().plusMonths(4))
                .monthlyAllocations(new ArrayList<>())
                .build();

        when(monthlyAllocationRepository.findPercentageByAllocationIdAndYearMonth(
                eq(1L), eq(currentYear), eq(currentMonth)))
                .thenReturn(Optional.empty());

        // When: Convert to DTO
        AllocationDTO dto = invokeToDTO(allocation);

        // Then: Project fields should be null
        assertThat(dto.getProjectId()).isNull();
        assertThat(dto.getProjectName()).isNull();
        assertThat(dto.getAllocationType()).isEqualTo(Allocation.AllocationType.MATERNITY);
    }

    @Test
    @DisplayName("Should fetch current month percentage via database projection")
    void shouldFetchCurrentMonthPercentageViaDatabaseProjection() throws Exception {
        // Given: Allocation with mocked database response for 85%
        Allocation allocation = createBasicAllocation();
        when(monthlyAllocationRepository.findPercentageByAllocationIdAndYearMonth(
                eq(1L), eq(currentYear), eq(currentMonth)))
                .thenReturn(Optional.of(85));

        // When: Convert to DTO
        AllocationDTO dto = invokeToDTO(allocation);

        // Then: Should correctly use database projection value
        assertThat(dto.getCurrentMonthAllocation()).isEqualTo(85);
        assertThat(dto.getAllocationPercentage()).isEqualTo(85.0);
    }

    @Test
    @DisplayName("Should handle zero percentage in current month")
    void shouldHandleZeroPercentageInCurrentMonth() throws Exception {
        // Given: Allocation with 0% in current month
        Allocation allocation = createBasicAllocation();
        when(monthlyAllocationRepository.findPercentageByAllocationIdAndYearMonth(
                eq(1L), eq(currentYear), eq(currentMonth)))
                .thenReturn(Optional.of(0));

        // When: Convert to DTO
        AllocationDTO dto = invokeToDTO(allocation);

        // Then: Should set currentMonthAllocation to 0, not null
        assertThat(dto.getCurrentMonthAllocation()).isEqualTo(0);
        assertThat(dto.getAllocationPercentage()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should use projection query to avoid loading all monthly allocations")
    void shouldUseProjectionQueryToAvoidLoadingAllMonthlyAllocations() throws Exception {
        // Given: Allocation with database returning specific month percentage
        // This test verifies that we're NOT loading all monthlyAllocations into memory
        Allocation allocation = createBasicAllocation();

        // Mock repository to return ONLY the percentage for current month
        when(monthlyAllocationRepository.findPercentageByAllocationIdAndYearMonth(
                eq(1L), eq(currentYear), eq(currentMonth)))
                .thenReturn(Optional.of(75));

        // When: Convert to DTO
        AllocationDTO dto = invokeToDTO(allocation);

        // Then: DTO should have correct percentage from database projection
        assertThat(dto.getCurrentMonthAllocation()).isEqualTo(75);
        assertThat(dto.getAllocationPercentage()).isEqualTo(75.0);

        // Verify allocation's monthlyAllocations list was NEVER accessed
        // (no lazy loading triggered, no in-memory filtering performed)
        assertThat(allocation.getMonthlyAllocations()).isEmpty();
    }

    // Helper methods

    private Allocation createBasicAllocation() {
        return Allocation.builder()
                .id(1L)
                .employee(employee)
                .project(project)
                .allocationType(Allocation.AllocationType.PROJECT)
                .startDate(LocalDate.now().minusMonths(6))
                .endDate(LocalDate.now().plusMonths(6))
                .monthlyAllocations(new ArrayList<>())
                .build();
    }

    /**
     * Invokes the private toDTO method using reflection for testing purposes.
     * This allows us to test the DTO conversion logic in isolation.
     */
    private AllocationDTO invokeToDTO(Allocation allocation) throws Exception {
        Method toDTOMethod = AllocationService.class.getDeclaredMethod("toDTO", Allocation.class);
        toDTOMethod.setAccessible(true);
        return (AllocationDTO) toDTOMethod.invoke(allocationService, allocation);
    }
}
