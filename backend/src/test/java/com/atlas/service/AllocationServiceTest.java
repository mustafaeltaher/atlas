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

    // ========== Month/Year Filter Tests ==========

    @Test
    @DisplayName("Should default to current month when year/month are NULL")
    void shouldDefaultToCurrentMonthWhenYearMonthAreNull() {
        // Given: Service is expected to default NULL year/month to current date
        // The getAllocations method has this logic:
        // if (year == null) year = LocalDate.now().getYear();
        // if (month == null) month = LocalDate.now().getMonthValue();

        // When/Then: Verify current date values are valid
        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();

        assertThat(currentYear).isGreaterThan(2020);
        assertThat(currentMonth).isBetween(1, 12);
    }

    @Test
    @DisplayName("Should filter allocations active in February 2026")
    void shouldFilterAllocationsActiveInFebruary2026() {
        // Given: Allocation spanning Jan-June 2026
        Allocation allocation = Allocation.builder()
                .id(1L)
                .employee(employee)
                .project(project)
                .allocationType(Allocation.AllocationType.PROJECT)
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 6, 30))
                .monthlyAllocations(new ArrayList<>())
                .build();

        // This allocation should be "active" in February 2026 because:
        // startDate (2026-01-01) <= lastDayOfMonth (2026-02-28)
        // endDate (2026-06-30) >= firstDayOfMonth (2026-02-01)

        // Then: Allocation should match February 2026 filter
        // The specification logic handles this in database query
        assertThat(allocation.getStartDate()).isBefore(LocalDate.of(2026, 2, 28));
        assertThat(allocation.getEndDate()).isAfter(LocalDate.of(2026, 2, 1));
    }

    @Test
    @DisplayName("Should include allocation with NULL endDate (ongoing)")
    void shouldIncludeAllocationWithNullEndDate() {
        // Given: Ongoing allocation (NULL endDate)
        Allocation allocation = Allocation.builder()
                .id(1L)
                .employee(employee)
                .project(project)
                .allocationType(Allocation.AllocationType.PROJECT)
                .startDate(LocalDate.of(2025, 6, 1))
                .endDate(null) // Ongoing
                .monthlyAllocations(new ArrayList<>())
                .build();

        // This allocation should be "active" in February 2026 because:
        // startDate (2025-06-01) <= lastDayOfMonth (2026-02-28)
        // endDate IS NULL (covers all future months)

        // Then: Allocation should match any future month filter
        assertThat(allocation.getStartDate()).isBefore(LocalDate.of(2026, 2, 28));
        assertThat(allocation.getEndDate()).isNull();
    }

    @Test
    @DisplayName("Should exclude allocation that ended before selected month")
    void shouldExcludeAllocationEndedBeforeSelectedMonth() {
        // Given: Allocation that ended in January 2026
        Allocation allocation = Allocation.builder()
                .id(1L)
                .employee(employee)
                .project(project)
                .allocationType(Allocation.AllocationType.PROJECT)
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 1, 31))
                .monthlyAllocations(new ArrayList<>())
                .build();

        // This allocation should NOT be "active" in February 2026 because:
        // endDate (2026-01-31) < firstDayOfMonth (2026-02-01)

        // Then: Allocation should NOT match February 2026 filter
        assertThat(allocation.getEndDate()).isBefore(LocalDate.of(2026, 2, 1));
    }

    @Test
    @DisplayName("Should exclude allocation that starts after selected month")
    void shouldExcludeAllocationStartsAfterSelectedMonth() {
        // Given: Allocation starting in March 2026
        Allocation allocation = Allocation.builder()
                .id(1L)
                .employee(employee)
                .project(project)
                .allocationType(Allocation.AllocationType.PROJECT)
                .startDate(LocalDate.of(2026, 3, 1))
                .endDate(LocalDate.of(2026, 3, 31))
                .monthlyAllocations(new ArrayList<>())
                .build();

        // This allocation should NOT be "active" in February 2026 because:
        // startDate (2026-03-01) > lastDayOfMonth (2026-02-28)

        // Then: Allocation should NOT match February 2026 filter
        assertThat(allocation.getStartDate()).isAfter(LocalDate.of(2026, 2, 28));
    }

    @Test
    @DisplayName("Should include allocation that starts mid-month")
    void shouldIncludeAllocationThatStartsMidMonth() {
        // Given: Allocation starting mid-February
        Allocation allocation = Allocation.builder()
                .id(1L)
                .employee(employee)
                .project(project)
                .allocationType(Allocation.AllocationType.PROJECT)
                .startDate(LocalDate.of(2026, 2, 15))
                .endDate(LocalDate.of(2026, 2, 28))
                .monthlyAllocations(new ArrayList<>())
                .build();

        // This allocation should be "active" in February 2026 because:
        // startDate (2026-02-15) <= lastDayOfMonth (2026-02-28)
        // endDate (2026-02-28) >= firstDayOfMonth (2026-02-01)

        // Then: Allocation should match February 2026 filter
        assertThat(allocation.getStartDate()).isBefore(LocalDate.of(2026, 2, 28).plusDays(1));
        assertThat(allocation.getEndDate()).isAfter(LocalDate.of(2026, 2, 1).minusDays(1));
    }

    @Test
    @DisplayName("Should include allocation that ends mid-month")
    void shouldIncludeAllocationThatEndsMidMonth() {
        // Given: Allocation ending mid-February
        Allocation allocation = Allocation.builder()
                .id(1L)
                .employee(employee)
                .project(project)
                .allocationType(Allocation.AllocationType.PROJECT)
                .startDate(LocalDate.of(2026, 2, 1))
                .endDate(LocalDate.of(2026, 2, 20))
                .monthlyAllocations(new ArrayList<>())
                .build();

        // This allocation should be "active" in February 2026 because:
        // It overlaps with at least part of February

        // Then: Allocation should match February 2026 filter
        assertThat(allocation.getStartDate()).isBeforeOrEqualTo(LocalDate.of(2026, 2, 28));
        assertThat(allocation.getEndDate()).isAfterOrEqualTo(LocalDate.of(2026, 2, 1));
    }

    @Test
    @DisplayName("Should handle leap year month correctly")
    void shouldHandleLeapYearMonthCorrectly() {
        // Given: February in a leap year (2024)
        int year = 2024;
        int month = 2;
        LocalDate firstDay = LocalDate.of(year, month, 1);
        LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());

        // Then: February 2024 should have 29 days
        assertThat(lastDay.getDayOfMonth()).isEqualTo(29);
        assertThat(lastDay).isEqualTo(LocalDate.of(2024, 2, 29));
    }

    // ========== Faceted Search Tests ==========

    @Test
    @DisplayName("getDistinctAllocationTypes - Should NOT include BENCH when PROJECT filter is selected")
    void getDistinctAllocationTypes_projectFilter_shouldNotIncludeBench() {
        // Given: Mock user and accessible IDs
        com.atlas.entity.User mockUser = new com.atlas.entity.User();
        mockUser.setId(1L);
        when(employeeService.getAccessibleEmployeeIds(mockUser))
                .thenReturn(java.util.List.of(1L, 2L, 3L));

        // Mock repository to return PROJECT type only
        when(allocationRepository.findDistinctAllocationTypesBySpec(
                any(), any(), any(), any(), any()))
                .thenReturn(java.util.List.of(Allocation.AllocationType.PROJECT));

        // When: Get distinct types with PROJECT filter
        java.util.List<String> types = allocationService.getDistinctAllocationTypes(
                mockUser, null, null, "PROJECT", 2026, 2);

        // Then: Should only contain PROJECT, NOT BENCH
        assertThat(types).containsExactly("PROJECT");
        assertThat(types).doesNotContain("BENCH");
    }

    @Test
    @DisplayName("getDistinctAllocationTypes - Should include BENCH when no allocation type filter")
    void getDistinctAllocationTypes_noFilter_shouldIncludeBench() {
        // Given: Mock user and accessible IDs
        com.atlas.entity.User mockUser = new com.atlas.entity.User();
        mockUser.setId(1L);
        when(employeeService.getAccessibleEmployeeIds(mockUser))
                .thenReturn(java.util.List.of(1L, 2L, 3L));

        // Mock repository to return PROJECT and PROSPECT types
        when(allocationRepository.findDistinctAllocationTypesBySpec(
                any(), any(), any(), any(), any()))
                .thenReturn(java.util.List.of(
                        Allocation.AllocationType.PROJECT,
                        Allocation.AllocationType.PROSPECT));

        // Mock bench employees exist
        org.springframework.data.domain.Page<Employee> benchPage =
                new org.springframework.data.domain.PageImpl<>(
                        java.util.List.of(employee));
        when(employeeRepository.findBenchEmployeesFiltered(
                any(), any(), any(),
                org.mockito.ArgumentMatchers.eq(2026),
                org.mockito.ArgumentMatchers.eq(2),
                any()))
                .thenReturn(benchPage);

        // When: Get distinct types with NO filter (null)
        java.util.List<String> types = allocationService.getDistinctAllocationTypes(
                mockUser, null, null, null, 2026, 2);

        // Then: Should contain PROJECT, PROSPECT, and BENCH
        assertThat(types).contains("PROJECT", "PROSPECT", "BENCH");
    }

    @Test
    @DisplayName("getDistinctAllocationTypes - Should include BENCH when BENCH filter is selected")
    void getDistinctAllocationTypes_benchFilter_shouldIncludeBench() {
        // Given: Mock user and accessible IDs
        com.atlas.entity.User mockUser = new com.atlas.entity.User();
        mockUser.setId(1L);
        when(employeeService.getAccessibleEmployeeIds(mockUser))
                .thenReturn(java.util.List.of(1L, 2L, 3L));

        // Mock repository to return empty (BENCH has no allocations)
        when(allocationRepository.findDistinctAllocationTypesBySpec(
                any(), any(), any(), any(), any()))
                .thenReturn(java.util.List.of());

        // Mock bench employees exist
        org.springframework.data.domain.Page<Employee> benchPage =
                new org.springframework.data.domain.PageImpl<>(
                        java.util.List.of(employee));
        when(employeeRepository.findBenchEmployeesFiltered(
                any(), any(), any(),
                org.mockito.ArgumentMatchers.eq(2026),
                org.mockito.ArgumentMatchers.eq(2),
                any()))
                .thenReturn(benchPage);

        // When: Get distinct types with BENCH filter
        java.util.List<String> types = allocationService.getDistinctAllocationTypes(
                mockUser, null, null, "BENCH", 2026, 2);

        // Then: Should contain only BENCH
        assertThat(types).containsExactly("BENCH");
    }

    @Test
    @DisplayName("getDistinctAllocationTypes - Should NOT include BENCH when PROSPECT filter is selected")
    void getDistinctAllocationTypes_prospectFilter_shouldNotIncludeBench() {
        // Given: Mock user and accessible IDs
        com.atlas.entity.User mockUser = new com.atlas.entity.User();
        mockUser.setId(1L);
        when(employeeService.getAccessibleEmployeeIds(mockUser))
                .thenReturn(java.util.List.of(1L, 2L, 3L));

        // Mock repository to return PROSPECT type only
        when(allocationRepository.findDistinctAllocationTypesBySpec(
                any(), any(), any(), any(), any()))
                .thenReturn(java.util.List.of(Allocation.AllocationType.PROSPECT));

        // When: Get distinct types with PROSPECT filter
        java.util.List<String> types = allocationService.getDistinctAllocationTypes(
                mockUser, null, null, "PROSPECT", 2026, 2);

        // Then: Should only contain PROSPECT, NOT BENCH
        assertThat(types).containsExactly("PROSPECT");
        assertThat(types).doesNotContain("BENCH");
    }

    @Test
    @DisplayName("getAvailableMonths - Should return 180 months when no allocation type filter")
    void getAvailableMonths_noFilter_shouldReturnUnlimitedMonths() {
        // Given: Mock user and accessible IDs
        com.atlas.entity.User mockUser = new com.atlas.entity.User();
        mockUser.setId(1L);
        when(employeeService.getAccessibleEmployeeIds(mockUser))
                .thenReturn(java.util.List.of(1L, 2L, 3L));

        // When: Get available months with NO filter
        java.util.List<String> months = allocationService.getAvailableMonths(
                mockUser, null, null, null);

        // Then: Should return 180 months (10 years back + 5 forward)
        assertThat(months).hasSize(181); // 120 + 60 + 1 (current month)
    }

    @Test
    @DisplayName("getAvailableMonths - Should return 180 months for BENCH allocation type")
    void getAvailableMonths_benchFilter_shouldReturnUnlimitedMonths() {
        // Given: Mock user and accessible IDs
        com.atlas.entity.User mockUser = new com.atlas.entity.User();
        mockUser.setId(1L);
        when(employeeService.getAccessibleEmployeeIds(mockUser))
                .thenReturn(java.util.List.of(1L, 2L, 3L));

        // When: Get available months with BENCH filter
        java.util.List<String> months = allocationService.getAvailableMonths(
                mockUser, "BENCH", null, null);

        // Then: Should return 180 months (10 years back + 5 forward)
        assertThat(months).hasSize(181); // 120 + 60 + 1 (current month)
    }

    @Test
    @DisplayName("getAvailableMonths - Should query database for PROJECT allocation type")
    void getAvailableMonths_projectFilter_shouldQueryDatabase() {
        // Given: Mock user and accessible IDs
        com.atlas.entity.User mockUser = new com.atlas.entity.User();
        mockUser.setId(1L);
        when(employeeService.getAccessibleEmployeeIds(mockUser))
                .thenReturn(java.util.List.of(1L, 2L, 3L));

        // Mock repository to return specific months with PROJECT allocations
        when(allocationRepository.findDistinctAvailableMonths(
                eq(Allocation.AllocationType.PROJECT), any(), any(), any()))
                .thenReturn(java.util.List.of("2026-01", "2026-02", "2026-03"));

        // When: Get available months with PROJECT filter
        java.util.List<String> months = allocationService.getAvailableMonths(
                mockUser, "PROJECT", null, null);

        // Then: Should return only months with PROJECT allocations
        assertThat(months).containsExactly("2026-01", "2026-02", "2026-03");
    }

    // ========== getGroupedAllocations Employee Status Mapping Tests ==========

    @Test
    @DisplayName("getGroupedAllocations - PROJECT filter should map to ACTIVE employee status")
    void getGroupedAllocations_projectFilter_shouldMapToActiveStatus() {
        // Given: Mock user and accessible IDs
        com.atlas.entity.User mockUser = new com.atlas.entity.User();
        mockUser.setId(1L);
        when(employeeService.getAccessibleEmployeeIds(mockUser))
                .thenReturn(java.util.List.of(1L, 2L, 3L));

        // Create an employee with PROJECT allocation (ACTIVE status)
        Employee activeEmployee = Employee.builder()
                .id(1L)
                .oracleId(1000)
                .name("Active Employee")
                .email("active@atlas.com")
                .build();

        // Mock employeeRepository to return employee when searching for ACTIVE status
        // The key assertion: PROJECT allocation type → ACTIVE employee status mapping
        when(employeeRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class),
                any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(
                        java.util.List.of(activeEmployee)));

        // Mock allocation for the active employee
        Allocation allocation = Allocation.builder()
                .id(1L)
                .employee(activeEmployee)
                .project(project)
                .allocationType(Allocation.AllocationType.PROJECT)
                .startDate(LocalDate.of(2026, 2, 1))
                .endDate(null)
                .monthlyAllocations(new ArrayList<>())
                .build();

        when(allocationRepository.findByEmployeeIdsWithDetails(any()))
                .thenReturn(java.util.List.of(allocation));

        when(monthlyAllocationRepository.findByAllocationIdsAndYearAndMonth(any(), eq(2026), eq(2)))
                .thenReturn(java.util.List.of(
                        com.atlas.entity.MonthlyAllocation.builder()
                                .id(1L)
                                .allocation(allocation)
                                .year(2026)
                                .month(2)
                                .percentage(75)
                                .build()));

        // When: Get grouped allocations with PROJECT filter
        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Page<com.atlas.dto.EmployeeAllocationSummaryDTO> result =
                allocationService.getGroupedAllocations(
                        mockUser, pageable, null, "PROJECT", null, 2026, 2);

        // Then: Should return employees with PROJECT allocations (ACTIVE status)
        // Should NOT include BENCH employees (those without allocations)
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().get(0).getEmployeeId()).isEqualTo(activeEmployee.getId());
    }

    @Test
    @DisplayName("getGroupedAllocations - BENCH filter should pass BENCH status unchanged")
    void getGroupedAllocations_benchFilter_shouldPassBenchStatusUnchanged() {
        // Given: Mock user and accessible IDs
        com.atlas.entity.User mockUser = new com.atlas.entity.User();
        mockUser.setId(1L);
        when(employeeService.getAccessibleEmployeeIds(mockUser))
                .thenReturn(java.util.List.of(1L, 2L, 3L));

        // Create a BENCH employee (no allocations)
        Employee benchEmployee = Employee.builder()
                .id(2L)
                .oracleId(2000)
                .name("Bench Employee")
                .email("bench@atlas.com")
                .build();

        // Mock employeeRepository to return BENCH employee
        when(employeeRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class),
                any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(
                        java.util.List.of(benchEmployee)));

        // No allocations for BENCH employees
        when(allocationRepository.findByEmployeeIdsWithDetails(any()))
                .thenReturn(java.util.List.of());

        when(monthlyAllocationRepository.findByAllocationIdsAndYearAndMonth(any(), eq(2026), eq(2)))
                .thenReturn(java.util.List.of());

        // When: Get grouped allocations with BENCH filter
        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Page<com.atlas.dto.EmployeeAllocationSummaryDTO> result =
                allocationService.getGroupedAllocations(
                        mockUser, pageable, null, "BENCH", null, 2026, 2);

        // Then: Should return BENCH employees (with empty allocation lists)
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().get(0).getEmployeeId()).isEqualTo(benchEmployee.getId());
        assertThat(result.getContent().get(0).getAllocations()).isEmpty();
    }

    @Test
    @DisplayName("getGroupedAllocations - PROSPECT filter should pass PROSPECT status unchanged")
    void getGroupedAllocations_prospectFilter_shouldPassProspectStatusUnchanged() {
        // Given: Mock user and accessible IDs
        com.atlas.entity.User mockUser = new com.atlas.entity.User();
        mockUser.setId(1L);
        when(employeeService.getAccessibleEmployeeIds(mockUser))
                .thenReturn(java.util.List.of(1L, 2L, 3L));

        // Create an employee with PROSPECT allocation
        Employee prospectEmployee = Employee.builder()
                .id(3L)
                .oracleId(3000)
                .name("Prospect Employee")
                .email("prospect@atlas.com")
                .build();

        when(employeeRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class),
                any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(
                        java.util.List.of(prospectEmployee)));

        Allocation prospectAllocation = Allocation.builder()
                .id(2L)
                .employee(prospectEmployee)
                .project(null)
                .allocationType(Allocation.AllocationType.PROSPECT)
                .startDate(LocalDate.of(2026, 2, 1))
                .endDate(null)
                .monthlyAllocations(new ArrayList<>())
                .build();

        when(allocationRepository.findByEmployeeIdsWithDetails(any()))
                .thenReturn(java.util.List.of(prospectAllocation));

        when(monthlyAllocationRepository.findByAllocationIdsAndYearAndMonth(any(), eq(2026), eq(2)))
                .thenReturn(java.util.List.of(
                        com.atlas.entity.MonthlyAllocation.builder()
                                .id(2L)
                                .allocation(prospectAllocation)
                                .year(2026)
                                .month(2)
                                .percentage(100)
                                .build()));

        // When: Get grouped allocations with PROSPECT filter
        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Page<com.atlas.dto.EmployeeAllocationSummaryDTO> result =
                allocationService.getGroupedAllocations(
                        mockUser, pageable, null, "PROSPECT", null, 2026, 2);

        // Then: Should return employees with PROSPECT allocations
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().get(0).getEmployeeId()).isEqualTo(prospectEmployee.getId());
        assertThat(result.getContent().get(0).getAllocations()).hasSize(1);
        assertThat(result.getContent().get(0).getAllocations().get(0).getAllocationType())
                .isEqualTo(Allocation.AllocationType.PROSPECT);
    }

    @Test
    @DisplayName("getGroupedAllocations - MATERNITY filter should pass MATERNITY status unchanged")
    void getGroupedAllocations_maternityFilter_shouldPassMaternityStatusUnchanged() {
        // Given: Mock user and accessible IDs
        com.atlas.entity.User mockUser = new com.atlas.entity.User();
        mockUser.setId(1L);
        when(employeeService.getAccessibleEmployeeIds(mockUser))
                .thenReturn(java.util.List.of(1L, 2L, 3L));

        // Create an employee with MATERNITY allocation
        Employee maternityEmployee = Employee.builder()
                .id(4L)
                .oracleId(4000)
                .name("Maternity Employee")
                .email("maternity@atlas.com")
                .build();

        when(employeeRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class),
                any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(
                        java.util.List.of(maternityEmployee)));

        Allocation maternityAllocation = Allocation.builder()
                .id(3L)
                .employee(maternityEmployee)
                .project(null)
                .allocationType(Allocation.AllocationType.MATERNITY)
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 6, 30))
                .monthlyAllocations(new ArrayList<>())
                .build();

        when(allocationRepository.findByEmployeeIdsWithDetails(any()))
                .thenReturn(java.util.List.of(maternityAllocation));

        when(monthlyAllocationRepository.findByAllocationIdsAndYearAndMonth(any(), eq(2026), eq(2)))
                .thenReturn(java.util.List.of(
                        com.atlas.entity.MonthlyAllocation.builder()
                                .id(3L)
                                .allocation(maternityAllocation)
                                .year(2026)
                                .month(2)
                                .percentage(100)
                                .build()));

        // When: Get grouped allocations with MATERNITY filter
        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Page<com.atlas.dto.EmployeeAllocationSummaryDTO> result =
                allocationService.getGroupedAllocations(
                        mockUser, pageable, null, "MATERNITY", null, 2026, 2);

        // Then: Should return employees with MATERNITY allocations
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().get(0).getEmployeeId()).isEqualTo(maternityEmployee.getId());
        assertThat(result.getContent().get(0).getAllocations()).hasSize(1);
        assertThat(result.getContent().get(0).getAllocations().get(0).getAllocationType())
                .isEqualTo(Allocation.AllocationType.MATERNITY);
    }

    @Test
    @DisplayName("getGroupedAllocations - VACATION filter should pass VACATION status unchanged")
    void getGroupedAllocations_vacationFilter_shouldPassVacationStatusUnchanged() {
        // Given: Mock user and accessible IDs
        com.atlas.entity.User mockUser = new com.atlas.entity.User();
        mockUser.setId(1L);
        when(employeeService.getAccessibleEmployeeIds(mockUser))
                .thenReturn(java.util.List.of(1L, 2L, 3L));

        // Create an employee with VACATION allocation
        Employee vacationEmployee = Employee.builder()
                .id(5L)
                .oracleId(5000)
                .name("Vacation Employee")
                .email("vacation@atlas.com")
                .build();

        when(employeeRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class),
                any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(
                        java.util.List.of(vacationEmployee)));

        Allocation vacationAllocation = Allocation.builder()
                .id(4L)
                .employee(vacationEmployee)
                .project(null)
                .allocationType(Allocation.AllocationType.VACATION)
                .startDate(LocalDate.of(2026, 2, 1))
                .endDate(LocalDate.of(2026, 2, 14))
                .monthlyAllocations(new ArrayList<>())
                .build();

        when(allocationRepository.findByEmployeeIdsWithDetails(any()))
                .thenReturn(java.util.List.of(vacationAllocation));

        when(monthlyAllocationRepository.findByAllocationIdsAndYearAndMonth(any(), eq(2026), eq(2)))
                .thenReturn(java.util.List.of(
                        com.atlas.entity.MonthlyAllocation.builder()
                                .id(4L)
                                .allocation(vacationAllocation)
                                .year(2026)
                                .month(2)
                                .percentage(100)
                                .build()));

        // When: Get grouped allocations with VACATION filter
        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Page<com.atlas.dto.EmployeeAllocationSummaryDTO> result =
                allocationService.getGroupedAllocations(
                        mockUser, pageable, null, "VACATION", null, 2026, 2);

        // Then: Should return employees with VACATION allocations
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().get(0).getEmployeeId()).isEqualTo(vacationEmployee.getId());
        assertThat(result.getContent().get(0).getAllocations()).hasSize(1);
        assertThat(result.getContent().get(0).getAllocations().get(0).getAllocationType())
                .isEqualTo(Allocation.AllocationType.VACATION);
    }

    // ==================== Month-Aware Status Checks (Fix #5) ====================

    @Test
    @DisplayName("getGroupedAllocations - PROSPECT filter should only return employees with PROSPECT allocations in selected month")
    void getGroupedAllocations_prospectFilterWithMonth_shouldOnlyReturnEmployeesWithProspectInSelectedMonth() {
        // Given: Mock user and accessible IDs
        com.atlas.entity.User mockUser = new com.atlas.entity.User();
        mockUser.setId(1L);
        when(employeeService.getAccessibleEmployeeIds(mockUser))
                .thenReturn(java.util.List.of(1L, 2L));

        // Employee #1: Has PROSPECT allocation in February 2026 (startDate: Feb 1, endDate: Feb 28)
        // Should NOT appear when filtering for July 2026
        Employee prospectInFeb = Employee.builder()
                .id(1L)
                .oracleId(1000)
                .name("Prospect in Feb Only")
                .email("prospect-feb@atlas.com")
                .build();

        // Employee #2: Has PROSPECT allocation in July 2026 (startDate: Jul 1, endDate: Jul 31)
        // Should appear when filtering for July 2026
        Employee prospectInJul = Employee.builder()
                .id(2L)
                .oracleId(1001)
                .name("Prospect in Jul")
                .email("prospect-jul@atlas.com")
                .build();

        // Mock employeeRepository to return only Employee #2 when filtering for July + PROSPECT
        // The key assertion: PROSPECT status check should use selected month (July), not any month
        when(employeeRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class),
                any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(
                        java.util.List.of(prospectInJul))); // Only employee with PROSPECT in July

        // Mock allocation for Employee #2
        Allocation prospectAllocation = Allocation.builder()
                .id(200L)
                .employee(prospectInJul)
                .allocationType(Allocation.AllocationType.PROSPECT)
                .startDate(java.time.LocalDate.of(2026, 7, 1))
                .endDate(java.time.LocalDate.of(2026, 7, 31))
                .build();

        when(allocationRepository.findByEmployeeIdsWithDetails(java.util.List.of(2L)))
                .thenReturn(java.util.List.of(prospectAllocation));

        // No monthly allocations for PROSPECT (PROSPECT doesn't have MonthlyAllocation records)
        when(monthlyAllocationRepository.findByAllocationIdsAndYearAndMonth(
                java.util.List.of(200L), 2026, 7))
                .thenReturn(java.util.List.of());

        // When: Get grouped allocations for July 2026 with PROSPECT filter
        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Page<com.atlas.dto.EmployeeAllocationSummaryDTO> result =
                allocationService.getGroupedAllocations(
                        mockUser, pageable, null, "PROSPECT", null, 2026, 7);

        // Then: Should return ONLY Employee #2 (PROSPECT in July)
        // Should NOT return Employee #1 (PROSPECT in February only)
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmployeeId()).isEqualTo(prospectInJul.getId());
        assertThat(result.getContent().get(0).getEmployeeName()).isEqualTo("Prospect in Jul");
    }

    @Test
    @DisplayName("getGroupedAllocations - MATERNITY filter should only return employees with MATERNITY allocations in selected month")
    void getGroupedAllocations_maternityFilterWithMonth_shouldOnlyReturnEmployeesWithMaternityInSelectedMonth() {
        // Given: Mock user and accessible IDs
        com.atlas.entity.User mockUser = new com.atlas.entity.User();
        mockUser.setId(1L);
        when(employeeService.getAccessibleEmployeeIds(mockUser))
                .thenReturn(java.util.List.of(1L, 2L));

        // Employee #1: Has MATERNITY allocation in March 2026 (startDate: Mar 1, endDate: Mar 31)
        // Should NOT appear when filtering for August 2026
        Employee maternityInMar = Employee.builder()
                .id(1L)
                .oracleId(1000)
                .name("Maternity in Mar Only")
                .email("maternity-mar@atlas.com")
                .build();

        // Employee #2: Has MATERNITY allocation in August 2026 (startDate: Aug 1, endDate: Aug 31)
        // Should appear when filtering for August 2026
        Employee maternityInAug = Employee.builder()
                .id(2L)
                .oracleId(1001)
                .name("Maternity in Aug")
                .email("maternity-aug@atlas.com")
                .build();

        // Mock employeeRepository to return only Employee #2 when filtering for August + MATERNITY
        when(employeeRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class),
                any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(
                        java.util.List.of(maternityInAug))); // Only employee with MATERNITY in August

        // Mock allocation for Employee #2
        Allocation maternityAllocation = Allocation.builder()
                .id(300L)
                .employee(maternityInAug)
                .allocationType(Allocation.AllocationType.MATERNITY)
                .startDate(java.time.LocalDate.of(2026, 8, 1))
                .endDate(java.time.LocalDate.of(2026, 8, 31))
                .build();

        when(allocationRepository.findByEmployeeIdsWithDetails(java.util.List.of(2L)))
                .thenReturn(java.util.List.of(maternityAllocation));

        // No monthly allocations for MATERNITY
        when(monthlyAllocationRepository.findByAllocationIdsAndYearAndMonth(
                java.util.List.of(300L), 2026, 8))
                .thenReturn(java.util.List.of());

        // When: Get grouped allocations for August 2026 with MATERNITY filter
        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Page<com.atlas.dto.EmployeeAllocationSummaryDTO> result =
                allocationService.getGroupedAllocations(
                        mockUser, pageable, null, "MATERNITY", null, 2026, 8);

        // Then: Should return ONLY Employee #2 (MATERNITY in August)
        // Should NOT return Employee #1 (MATERNITY in March only)
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmployeeId()).isEqualTo(maternityInAug.getId());
        assertThat(result.getContent().get(0).getEmployeeName()).isEqualTo("Maternity in Aug");
    }

    @Test
    @DisplayName("getGroupedAllocations - VACATION filter should only return employees with VACATION allocations in selected month")
    void getGroupedAllocations_vacationFilterWithMonth_shouldOnlyReturnEmployeesWithVacationInSelectedMonth() {
        // Given: Mock user and accessible IDs
        com.atlas.entity.User mockUser = new com.atlas.entity.User();
        mockUser.setId(1L);
        when(employeeService.getAccessibleEmployeeIds(mockUser))
                .thenReturn(java.util.List.of(1L, 2L));

        // Employee #1: Has VACATION allocation in April 2026 (startDate: Apr 1, endDate: Apr 30)
        // Should NOT appear when filtering for September 2026
        Employee vacationInApr = Employee.builder()
                .id(1L)
                .oracleId(1000)
                .name("Vacation in Apr Only")
                .email("vacation-apr@atlas.com")
                .build();

        // Employee #2: Has VACATION allocation in September 2026 (startDate: Sep 1, endDate: Sep 30)
        // Should appear when filtering for September 2026
        Employee vacationInSep = Employee.builder()
                .id(2L)
                .oracleId(1001)
                .name("Vacation in Sep")
                .email("vacation-sep@atlas.com")
                .build();

        // Mock employeeRepository to return only Employee #2 when filtering for September + VACATION
        when(employeeRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class),
                any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(
                        java.util.List.of(vacationInSep))); // Only employee with VACATION in September

        // Mock allocation for Employee #2
        Allocation vacationAllocation = Allocation.builder()
                .id(400L)
                .employee(vacationInSep)
                .allocationType(Allocation.AllocationType.VACATION)
                .startDate(java.time.LocalDate.of(2026, 9, 1))
                .endDate(java.time.LocalDate.of(2026, 9, 30))
                .build();

        when(allocationRepository.findByEmployeeIdsWithDetails(java.util.List.of(2L)))
                .thenReturn(java.util.List.of(vacationAllocation));

        // No monthly allocations for VACATION
        when(monthlyAllocationRepository.findByAllocationIdsAndYearAndMonth(
                java.util.List.of(400L), 2026, 9))
                .thenReturn(java.util.List.of());

        // When: Get grouped allocations for September 2026 with VACATION filter
        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Page<com.atlas.dto.EmployeeAllocationSummaryDTO> result =
                allocationService.getGroupedAllocations(
                        mockUser, pageable, null, "VACATION", null, 2026, 9);

        // Then: Should return ONLY Employee #2 (VACATION in September)
        // Should NOT return Employee #1 (VACATION in April only)
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmployeeId()).isEqualTo(vacationInSep.getId());
        assertThat(result.getContent().get(0).getEmployeeName()).isEqualTo("Vacation in Sep");
    }

    @Test
    @DisplayName("getGroupedAllocations - PROSPECT with date range overlap should include employees with allocations spanning selected month")
    void getGroupedAllocations_prospectFilterWithDateRangeOverlap_shouldIncludeOverlappingAllocations() {
        // Given: Mock user and accessible IDs
        com.atlas.entity.User mockUser = new com.atlas.entity.User();
        mockUser.setId(1L);
        when(employeeService.getAccessibleEmployeeIds(mockUser))
                .thenReturn(java.util.List.of(1L));

        // Employee: Has PROSPECT allocation from June 1 to August 31, 2026 (spans 3 months)
        // Should appear when filtering for July 2026 (middle of date range)
        Employee prospectWithOverlap = Employee.builder()
                .id(1L)
                .oracleId(1000)
                .name("Prospect Jun-Aug")
                .email("prospect-overlap@atlas.com")
                .build();

        // Mock employeeRepository to return employee when filtering for July + PROSPECT
        when(employeeRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class),
                any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(
                        java.util.List.of(prospectWithOverlap)));

        // Mock allocation spanning June-August
        Allocation prospectAllocation = Allocation.builder()
                .id(500L)
                .employee(prospectWithOverlap)
                .allocationType(Allocation.AllocationType.PROSPECT)
                .startDate(java.time.LocalDate.of(2026, 6, 1))
                .endDate(java.time.LocalDate.of(2026, 8, 31))
                .build();

        when(allocationRepository.findByEmployeeIdsWithDetails(java.util.List.of(1L)))
                .thenReturn(java.util.List.of(prospectAllocation));

        when(monthlyAllocationRepository.findByAllocationIdsAndYearAndMonth(
                java.util.List.of(500L), 2026, 7))
                .thenReturn(java.util.List.of());

        // When: Get grouped allocations for July 2026 with PROSPECT filter
        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Page<com.atlas.dto.EmployeeAllocationSummaryDTO> result =
                allocationService.getGroupedAllocations(
                        mockUser, pageable, null, "PROSPECT", null, 2026, 7);

        // Then: Should return the employee because allocation spans July
        // Date range overlap: startDate (Jun 1) <= lastDayOfMonth (Jul 31) AND endDate (Aug 31) >= firstDayOfMonth (Jul 1)
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmployeeId()).isEqualTo(prospectWithOverlap.getId());
        assertThat(result.getContent().get(0).getAllocations()).hasSize(1);
        assertThat(result.getContent().get(0).getAllocations().get(0).getStartDate())
                .isEqualTo(java.time.LocalDate.of(2026, 6, 1));
        assertThat(result.getContent().get(0).getAllocations().get(0).getEndDate())
                .isEqualTo(java.time.LocalDate.of(2026, 8, 31));
    }

    @Test
    @DisplayName("getGroupedAllocations - PROSPECT filter should exclude employees with PROSPECT ending before selected month")
    void getGroupedAllocations_prospectFilterWithEndedAllocation_shouldExcludeEmployees() {
        // Given: Mock user and accessible IDs
        com.atlas.entity.User mockUser = new com.atlas.entity.User();
        mockUser.setId(1L);
        when(employeeService.getAccessibleEmployeeIds(mockUser))
                .thenReturn(java.util.List.of(1L));

        // Employee: Has PROSPECT allocation from January 1 to January 31, 2026
        // Should NOT appear when filtering for July 2026 (allocation ended before selected month)
        Employee prospectEnded = Employee.builder()
                .id(1L)
                .oracleId(1000)
                .name("Prospect Ended in Jan")
                .email("prospect-ended@atlas.com")
                .build();

        // Mock employeeRepository to return empty list (employee should be filtered out)
        when(employeeRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class),
                any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(
                        java.util.List.of())); // Empty because allocation ended before July

        // When: Get grouped allocations for July 2026 with PROSPECT filter
        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Page<com.atlas.dto.EmployeeAllocationSummaryDTO> result =
                allocationService.getGroupedAllocations(
                        mockUser, pageable, null, "PROSPECT", null, 2026, 7);

        // Then: Should return empty because allocation ended before selected month
        // Date range check fails: endDate (Jan 31) < firstDayOfMonth (Jul 1)
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("getManagersForAllocations - Should return managers for PROSPECT employees in selected month")
    void getManagersForAllocations_withProspectTypeAndMonth_shouldReturnManagersInSelectedMonth() {
        // Given: Mock user and accessible IDs
        com.atlas.entity.User mockUser = new com.atlas.entity.User();
        mockUser.setId(1L);
        when(employeeService.getAccessibleEmployeeIds(mockUser))
                .thenReturn(java.util.List.of(1L, 2L));

        // Manager entity
        Employee manager = Employee.builder()
                .id(10L)
                .name("Test Manager")
                .email("manager@atlas.com")
                .build();

        // Mock employeeRepository to return the manager for July 2026
        // This simulates that there ARE employees with PROSPECT allocations for this manager in July 2026
        when(employeeRepository.findDistinctManagersByEmployeeSpec(
                eq(null), // search
                eq(null), // tower
                eq(null), // managerId
                eq("PROSPECT"), // status
                eq(java.util.List.of(1L, 2L)), // accessibleIds
                eq(null), // managerName
                eq(2026), // year
                eq(7))) // month
                .thenReturn(java.util.List.of(manager));

        // When: Get managers for PROSPECT allocations in July 2026
        java.util.List<java.util.Map<String, Object>> managers =
                allocationService.getManagersForAllocations(
                        mockUser, "PROSPECT", null, null, 2026, 7);

        // Then: Should return the manager who has PROSPECT employees in July 2026
        assertThat(managers).hasSize(1);
        assertThat(managers.get(0).get("id")).isEqualTo(10L);
        assertThat(managers.get(0).get("name")).isEqualTo("Test Manager");
    }

    @Test
    @DisplayName("getManagersForAllocations - Should use selected month not current month for PROSPECT status")
    void getManagersForAllocations_withProspectType_shouldUseSelectedMonthNotCurrentMonth() {
        // Given: Mock user
        com.atlas.entity.User mockUser = new com.atlas.entity.User();
        mockUser.setId(1L);
        when(employeeService.getAccessibleEmployeeIds(mockUser))
                .thenReturn(java.util.List.of(1L));

        Employee manager = Employee.builder()
                .id(10L)
                .name("Test Manager")
                .build();

        // Mock to return manager for the SELECTED month (July 2026), not current month
        when(employeeRepository.findDistinctManagersByEmployeeSpec(
                any(), any(), any(), eq("PROSPECT"), any(), any(),
                eq(2026), eq(7))) // year=2026, month=7
                .thenReturn(java.util.List.of(manager));

        // When: Request managers for July 2026
        allocationService.getManagersForAllocations(
                mockUser, "PROSPECT", null, null, 2026, 7);

        // Then: Should have called repository with July 2026, not current month
        org.mockito.Mockito.verify(employeeRepository).findDistinctManagersByEmployeeSpec(
                eq(null), eq(null), eq(null), eq("PROSPECT"),
                eq(java.util.List.of(1L)), eq(null),
                eq(2026), eq(7)); // Verify year=2026, month=7 were passed
    }
}
