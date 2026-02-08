package com.atlas.service;

import com.atlas.dto.AllocationDTO;
import com.atlas.dto.EmployeeAllocationSummaryDTO;
import com.atlas.dto.MonthlyAllocationDTO;
import com.atlas.entity.Allocation;
import com.atlas.entity.Employee;
import com.atlas.entity.MonthlyAllocation;
import com.atlas.entity.Project;
import com.atlas.entity.User;
import com.atlas.repository.AllocationRepository;
import com.atlas.repository.EmployeeRepository;
import com.atlas.repository.MonthlyAllocationRepository;
import com.atlas.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AllocationService {

    private final AllocationRepository allocationRepository;
    private final MonthlyAllocationRepository monthlyAllocationRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;
    private final EmployeeService employeeService;

    public List<AllocationDTO> getAllAllocations(User currentUser) {
        List<Allocation> allocations = getFilteredAllocations(currentUser);
        return allocations.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Paginated version with search and filters - uses database-level pagination
    public Page<AllocationDTO> getAllAllocations(User currentUser,
            Pageable pageable, String search, String status, Long managerId) {

        Allocation.AllocationStatus statusEnum = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                statusEnum = Allocation.AllocationStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid status, ignore filter
            }
        }

        String searchParam = (search != null && !search.trim().isEmpty()) ? search.trim() : null;

        if (currentUser.getRole() == User.Role.SYSTEM_ADMIN || currentUser.getRole() == User.Role.EXECUTIVE) {
            Page<Allocation> allocationPage = allocationRepository
                    .findAllocationsWithFilters(searchParam, statusEnum, managerId, pageable);
            return allocationPage.map(this::toDTO);
        }

        // For non-admin managers: get accessible employee IDs first
        List<Employee> accessibleEmployees = employeeService.getAccessibleEmployees(currentUser);
        if (accessibleEmployees.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        List<Long> accessibleIds = accessibleEmployees.stream()
                .map(Employee::getId)
                .collect(Collectors.toList());

        Page<Allocation> allocationPage = allocationRepository
                .findAllocationsWithFiltersByEmployeeIds(accessibleIds, searchParam, statusEnum, managerId, pageable);
        return allocationPage.map(this::toDTO);
    }

    public Page<EmployeeAllocationSummaryDTO> getGroupedAllocations(User currentUser,
            Pageable pageable, String search, String status, Long managerId) {

        boolean isBenchFilter = "BENCH".equalsIgnoreCase(status);
        boolean isActiveFilter = "ACTIVE".equalsIgnoreCase(status);

        String searchParam = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();

        Page<Employee> employeePage;

        if (currentUser.getRole() == User.Role.SYSTEM_ADMIN || currentUser.getRole() == User.Role.EXECUTIVE) {
            if (isBenchFilter) {
                employeePage = employeeRepository.findBenchEmployees(searchParam, managerId, currentYear,
                        currentMonth, pageable);
            } else if (isActiveFilter) {
                employeePage = employeeRepository.findActiveAllocatedEmployees(searchParam, managerId,
                        currentYear, currentMonth, pageable);
            } else {
                employeePage = employeeRepository.findEmployeesForAllocationView(
                        searchParam, managerId, null, null, null, pageable);
            }
        } else {
            List<Employee> accessible = employeeService.getAccessibleEmployees(currentUser);
            if (accessible.isEmpty()) {
                return new PageImpl<>(List.of(), pageable, 0);
            }
            List<Long> accessibleIds = accessible.stream()
                    .map(Employee::getId).collect(Collectors.toList());

            if (isBenchFilter) {
                employeePage = employeeRepository.findBenchEmployeesByIds(accessibleIds, searchParam,
                        managerId, currentYear, currentMonth, pageable);
            } else if (isActiveFilter) {
                employeePage = employeeRepository.findActiveAllocatedEmployeesByIds(accessibleIds,
                        searchParam, managerId, currentYear, currentMonth, pageable);
            } else {
                employeePage = employeeRepository.findEmployeesForAllocationViewByIds(
                        accessibleIds, searchParam, managerId, null, pageable);
            }
        }

        List<Employee> employees = employeePage.getContent();
        if (employees.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, employeePage.getTotalElements());
        }

        // Batch-fetch allocations for the employees on this page
        List<Long> employeeIds = employees.stream()
                .map(Employee::getId)
                .collect(Collectors.toList());
        List<Allocation> allocations = allocationRepository.findByEmployeeIdsWithDetails(employeeIds);

        // Get allocation IDs for batch-fetching monthly allocations
        List<Long> allocationIds = allocations.stream()
                .map(Allocation::getId)
                .collect(Collectors.toList());

        // Batch-fetch monthly allocations for current year/month
        Map<Long, Double> currentMonthAllocations = monthlyAllocationRepository
                .findByAllocationIdsAndYearAndMonth(allocationIds, currentYear, currentMonth)
                .stream()
                .collect(Collectors.toMap(
                        ma -> ma.getAllocation().getId(),
                        MonthlyAllocation::getPercentage,
                        (a, b) -> a));

        // Group allocations by employee ID
        Map<Long, List<Allocation>> allocationsByEmployee = allocations.stream()
                .collect(Collectors.groupingBy(a -> a.getEmployee().getId()));

        // Build summary DTOs for each employee on the page
        List<EmployeeAllocationSummaryDTO> summaries = employees.stream()
                .map(emp -> {
                    List<Allocation> empAllocations = allocationsByEmployee
                            .getOrDefault(emp.getId(), List.of());

                    // For current month view: only ACTIVE allocations with valid percentage
                    List<Allocation> activeAllocations = empAllocations.stream()
                            .filter(a -> a.getStatus() == Allocation.AllocationStatus.ACTIVE)
                            .filter(a -> {
                                Double percentage = currentMonthAllocations.get(a.getId());
                                return percentage != null && percentage > 0;
                            })
                            .collect(Collectors.toList());

                    List<AllocationDTO> allocationDTOs = activeAllocations.stream()
                            .map(a -> toDTOWithCurrentMonth(a, currentYear, currentMonth, currentMonthAllocations))
                            .collect(Collectors.toList());

                    double totalPercentage = allocationDTOs.stream()
                            .mapToDouble(dto -> dto.getAllocationPercentage() != null
                                    ? dto.getAllocationPercentage()
                                    : 0.0)
                            .sum();

                    return EmployeeAllocationSummaryDTO.builder()
                            .employeeId(emp.getId())
                            .employeeName(emp.getName())
                            .employeeOracleId(emp.getOracleId())
                            .totalAllocationPercentage(totalPercentage)
                            .projectCount(activeAllocations.size())
                            .allocations(allocationDTOs)
                            .build();
                })
                .collect(Collectors.toList());

        return new PageImpl<>(summaries, pageable, employeePage.getTotalElements());
    }

    public AllocationDTO getAllocationById(Long id, User currentUser) {
        Allocation allocation = allocationRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Allocation not found: " + id));
        if (!hasAccessToAllocation(currentUser, allocation)) {
            throw new RuntimeException("Access denied to allocation: " + id);
        }
        return toDTO(allocation);
    }

    public List<AllocationDTO> getAllocationsByEmployee(Long employeeId, User currentUser) {
        return allocationRepository.findByEmployeeIdWithDetails(employeeId).stream()
                .filter(a -> hasAccessToAllocation(currentUser, a))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<AllocationDTO> getAllocationsByProject(Long projectId, User currentUser) {
        return allocationRepository.findByProjectIdWithDetails(projectId).stream()
                .filter(a -> hasAccessToAllocation(currentUser, a))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public AllocationDTO createAllocation(AllocationDTO dto) {
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found: " + dto.getEmployeeId()));

        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found: " + dto.getProjectId()));

        Allocation.AllocationStatus status = dto.getStatus() != null ? dto.getStatus()
                : Allocation.AllocationStatus.ACTIVE;

        Allocation allocation = Allocation.builder()
                .employee(employee)
                .project(project)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .status(status)
                .monthlyAllocations(new ArrayList<>())
                .build();

        allocation = allocationRepository.save(allocation);

        // Create monthly allocation for current month if percentage provided
        if (dto.getCurrentMonthAllocation() != null && status == Allocation.AllocationStatus.ACTIVE) {
            int year = dto.getYear() != null ? dto.getYear() : LocalDate.now().getYear();
            int month = LocalDate.now().getMonthValue();

            validateAllocationPercentage(dto.getCurrentMonthAllocation());

            MonthlyAllocation monthlyAlloc = MonthlyAllocation.builder()
                    .allocation(allocation)
                    .year(year)
                    .month(month)
                    .percentage(dto.getCurrentMonthAllocation())
                    .build();
            monthlyAllocationRepository.save(monthlyAlloc);
        }

        // If monthly allocations list is provided, create them (only for ACTIVE
        // allocations)
        if (dto.getMonthlyAllocations() != null && !dto.getMonthlyAllocations().isEmpty()) {
            if (status == Allocation.AllocationStatus.PROSPECT) {
                throw new RuntimeException("PROSPECT allocations cannot have monthly allocation percentages. " +
                        "Change status to ACTIVE to add monthly allocations.");
            }
            for (MonthlyAllocationDTO maDTO : dto.getMonthlyAllocations()) {
                validateAllocationPercentage(maDTO.getPercentage());

                MonthlyAllocation monthlyAlloc = MonthlyAllocation.builder()
                        .allocation(allocation)
                        .year(maDTO.getYear())
                        .month(maDTO.getMonth())
                        .percentage(maDTO.getPercentage())
                        .build();
                monthlyAllocationRepository.save(monthlyAlloc);
            }
        }

        return toDTO(allocation);
    }

    private void validateAllocationPercentage(Double percentage) {
        if (percentage == null) {
            return;
        }
        if (percentage != 0.25 && percentage != 0.5 && percentage != 0.75 && percentage != 1.0) {
            throw new RuntimeException(
                    "Invalid allocation percentage. Allowed values are: 0.25 (25%), 0.5 (50%), 0.75 (75%), 1 (100%).");
        }
    }

    @Transactional
    public AllocationDTO updateAllocation(Long id, AllocationDTO dto) {
        Allocation allocation = allocationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Allocation not found: " + id));

        // Status is immutable after creation
        if (dto.getStatus() != null && dto.getStatus() != allocation.getStatus()) {
            throw new RuntimeException("Cannot change allocation status. Status is immutable after creation. " +
                    "Delete this allocation and create a new one with the desired status.");
        }

        // Update current month's allocation value
        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();

        if (dto.getCurrentMonthAllocation() != null) {
            if (allocation.getStatus() == Allocation.AllocationStatus.PROSPECT) {
                // Prospect allocations don't have percentage values
            } else {
                validateAllocationPercentage(dto.getCurrentMonthAllocation());

                // Find or create monthly allocation for current month
                MonthlyAllocation existing = monthlyAllocationRepository
                        .findByAllocationIdAndYearAndMonth(id, currentYear, currentMonth)
                        .orElse(null);

                if (existing != null) {
                    existing.setPercentage(dto.getCurrentMonthAllocation());
                    monthlyAllocationRepository.save(existing);
                } else {
                    MonthlyAllocation newMonthlyAlloc = MonthlyAllocation.builder()
                            .allocation(allocation)
                            .year(currentYear)
                            .month(currentMonth)
                            .percentage(dto.getCurrentMonthAllocation())
                            .build();
                    monthlyAllocationRepository.save(newMonthlyAlloc);
                }
            }
        }

        allocation = allocationRepository.save(allocation);
        return toDTO(allocation);
    }

    @Transactional
    public void deleteAllocation(Long id) {
        if (!allocationRepository.existsById(id)) {
            throw new RuntimeException("Allocation not found: " + id);
        }
        // Monthly allocations will be deleted automatically due to CascadeType.ALL
        allocationRepository.deleteById(id);
    }

    private List<Allocation> getFilteredAllocations(User user) {
        if (user.getRole() == User.Role.SYSTEM_ADMIN || user.getRole() == User.Role.EXECUTIVE) {
            return allocationRepository.findAllWithEmployeeAndProject();
        }

        List<Employee> accessible = employeeService.getAccessibleEmployees(user);
        if (accessible.isEmpty()) {
            return List.of();
        }

        List<Long> ids = accessible.stream().map(Employee::getId).collect(Collectors.toList());
        return allocationRepository.findByEmployeeIdsWithDetails(ids);
    }

    private boolean hasAccessToAllocation(User user, Allocation allocation) {
        if (user.getRole() == User.Role.SYSTEM_ADMIN || user.getRole() == User.Role.EXECUTIVE) {
            return true;
        }

        var userEmployee = user.getEmployee();
        if (userEmployee == null) {
            return false;
        }

        Employee allocationEmployee = allocation.getEmployee();
        if (userEmployee.getId().equals(allocationEmployee.getId())) {
            return true;
        }

        Employee mgr = allocationEmployee.getManager();
        while (mgr != null) {
            if (mgr.getId().equals(userEmployee.getId())) {
                return true;
            }
            mgr = mgr.getManager();
        }
        return false;
    }

    private AllocationDTO toDTO(Allocation allocation) {
        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();

        // Get current month allocation
        Double currentMonthAlloc = allocation.getAllocationForYearMonth(currentYear, currentMonth);
        Double allocationPercentage = currentMonthAlloc != null ? currentMonthAlloc * 100 : 0.0;

        // Map monthly allocations
        List<MonthlyAllocationDTO> monthlyDTOs = allocation.getMonthlyAllocations().stream()
                .map(ma -> MonthlyAllocationDTO.builder()
                        .id(ma.getId())
                        .allocationId(allocation.getId())
                        .year(ma.getYear())
                        .month(ma.getMonth())
                        .percentage(ma.getPercentage())
                        .build())
                .collect(Collectors.toList());

        return AllocationDTO.builder()
                .id(allocation.getId())
                .employeeId(allocation.getEmployee().getId())
                .employeeName(allocation.getEmployee().getName())
                .employeeOracleId(allocation.getEmployee().getOracleId())
                .projectId(allocation.getProject().getId())
                .projectName(allocation.getProject().getName())
                .startDate(allocation.getStartDate())
                .endDate(allocation.getEndDate())
                .status(allocation.getStatus())
                .currentMonthAllocation(currentMonthAlloc)
                .allocationPercentage(allocationPercentage)
                .monthlyAllocations(monthlyDTOs)
                .build();
    }

    private AllocationDTO toDTOWithCurrentMonth(Allocation allocation, int year, int month,
            Map<Long, Double> currentMonthAllocations) {
        Double currentMonthAlloc = currentMonthAllocations.get(allocation.getId());
        Double allocationPercentage = currentMonthAlloc != null ? currentMonthAlloc * 100 : 0.0;

        return AllocationDTO.builder()
                .id(allocation.getId())
                .employeeId(allocation.getEmployee().getId())
                .employeeName(allocation.getEmployee().getName())
                .employeeOracleId(allocation.getEmployee().getOracleId())
                .projectId(allocation.getProject().getId())
                .projectName(allocation.getProject().getName())
                .startDate(allocation.getStartDate())
                .endDate(allocation.getEndDate())
                .status(allocation.getStatus())
                .currentMonthAllocation(currentMonthAlloc)
                .allocationPercentage(allocationPercentage)
                .build();
    }

    public List<String> getDistinctStatuses(Long managerId) {
        return List.of("ACTIVE", "BENCH");
    }
}
