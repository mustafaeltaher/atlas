package com.atlas.service;

import com.atlas.dto.AllocationDTO;
import com.atlas.dto.EmployeeAllocationSummaryDTO;
import com.atlas.entity.Allocation;
import com.atlas.entity.Employee;
import com.atlas.entity.Project;
import com.atlas.entity.User;
import com.atlas.repository.AllocationRepository;
import com.atlas.repository.EmployeeRepository;
import com.atlas.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AllocationService {

    private final AllocationRepository allocationRepository;
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
    public org.springframework.data.domain.Page<AllocationDTO> getAllAllocations(User currentUser,
            org.springframework.data.domain.Pageable pageable, String search, String status, Long managerId) {

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
            // DATABASE-LEVEL pagination for admin/executive with all filters
            org.springframework.data.domain.Page<Allocation> allocationPage = allocationRepository
                    .findAllocationsWithFilters(
                            searchParam, statusEnum, managerId, pageable);
            return allocationPage.map(this::toDTO);
        }

        // For non-admin managers: get accessible employee IDs first
        List<Employee> accessibleEmployees = employeeService.getAccessibleEmployees(currentUser);
        if (accessibleEmployees.isEmpty()) {
            return new org.springframework.data.domain.PageImpl<>(List.of(), pageable, 0);
        }

        List<Long> accessibleIds = accessibleEmployees.stream()
                .map(Employee::getId)
                .collect(Collectors.toList());

        // DATABASE-LEVEL pagination for non-admin managers with all filters
        org.springframework.data.domain.Page<Allocation> allocationPage = allocationRepository
                .findAllocationsWithFiltersByEmployeeIds(
                        accessibleIds, searchParam, statusEnum, managerId, pageable);
        return allocationPage.map(this::toDTO);
    }

    public Page<EmployeeAllocationSummaryDTO> getGroupedAllocations(User currentUser,
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

        Page<Employee> employeePage;
        if (currentUser.getRole() == User.Role.SYSTEM_ADMIN || currentUser.getRole() == User.Role.EXECUTIVE) {
            // Admin/Executive: use the full view with no ID restriction
            employeePage = employeeRepository.findEmployeesForAllocationView(
                    searchParam, managerId, statusEnum, null, null, pageable);
        } else {
            // Other roles: restrict to employees in the reporting chain
            List<Employee> accessible = employeeService.getAccessibleEmployees(currentUser);
            if (accessible.isEmpty()) {
                return new PageImpl<>(List.of(), pageable, 0);
            }
            List<Long> accessibleIds = accessible.stream()
                    .map(Employee::getId).collect(Collectors.toList());
            employeePage = employeeRepository.findEmployeesForAllocationViewByIds(
                    accessibleIds, searchParam, managerId, statusEnum, pageable);
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

        // Group allocations by employee ID
        Map<Long, List<Allocation>> allocationsByEmployee = allocations.stream()
                .collect(Collectors.groupingBy(a -> a.getEmployee().getId()));

        // Build summary DTOs for each employee on the page
        final Allocation.AllocationStatus statusFinal = statusEnum;
        List<EmployeeAllocationSummaryDTO> summaries = employees.stream()
                .map(emp -> {
                    List<Allocation> empAllocations = allocationsByEmployee
                            .getOrDefault(emp.getId(), List.of());

                    // Apply status filter to allocations if specified
                    if (statusFinal != null) {
                        empAllocations = empAllocations.stream()
                                .filter(a -> a.getStatus() == statusFinal)
                                .collect(Collectors.toList());
                    }

                    List<AllocationDTO> allocationDTOs = empAllocations.stream()
                            .map(this::toDTO)
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
                            .projectCount(empAllocations.size())
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

        Allocation allocation = Allocation.builder()
                .employee(employee)
                .project(project)
                .confirmedAssignment(dto.getConfirmedAssignment())
                .prospectAssignment(dto.getProspectAssignment())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .status(dto.getStatus() != null ? dto.getStatus() : Allocation.AllocationStatus.ACTIVE)
                .janAllocation(dto.getJanAllocation())
                .febAllocation(dto.getFebAllocation())
                .marAllocation(dto.getMarAllocation())
                .aprAllocation(dto.getAprAllocation())
                .mayAllocation(dto.getMayAllocation())
                .junAllocation(dto.getJunAllocation())
                .julAllocation(dto.getJulAllocation())
                .augAllocation(dto.getAugAllocation())
                .sepAllocation(dto.getSepAllocation())
                .octAllocation(dto.getOctAllocation())
                .novAllocation(dto.getNovAllocation())
                .decAllocation(dto.getDecAllocation())
                .build();

        allocation = allocationRepository.save(allocation);
        return toDTO(allocation);
    }

    @Transactional
    public AllocationDTO updateAllocation(Long id, AllocationDTO dto) {
        Allocation allocation = allocationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Allocation not found: " + id));

        // Only update the current month's allocation value
        int currentMonth = LocalDate.now().getMonthValue();
        String newValue = dto.getCurrentMonthAllocation();
        if (newValue != null) {
            allocation.setAllocationForMonth(currentMonth, newValue);
        }

        allocation = allocationRepository.save(allocation);
        return toDTO(allocation);
    }

    @Transactional
    public void deleteAllocation(Long id) {
        if (!allocationRepository.existsById(id)) {
            throw new RuntimeException("Allocation not found: " + id);
        }
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

        // Walk up the chain from the allocation's employee to check if current user is
        // an ancestor
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
        return AllocationDTO.builder()
                .id(allocation.getId())
                .employeeId(allocation.getEmployee().getId())
                .employeeName(allocation.getEmployee().getName())
                .employeeOracleId(allocation.getEmployee().getOracleId())
                .projectId(allocation.getProject().getId())
                .projectName(allocation.getProject().getName())
                .confirmedAssignment(allocation.getConfirmedAssignment())
                .prospectAssignment(allocation.getProspectAssignment())
                .startDate(allocation.getStartDate())
                .endDate(allocation.getEndDate())
                .status(allocation.getStatus())
                .currentMonthAllocation(calculateCurrentMonthAllocation(allocation))
                .allocationPercentage(calculateAllocationPercentage(allocation))
                .janAllocation(allocation.getJanAllocation())
                .febAllocation(allocation.getFebAllocation())
                .marAllocation(allocation.getMarAllocation())
                .aprAllocation(allocation.getAprAllocation())
                .mayAllocation(allocation.getMayAllocation())
                .junAllocation(allocation.getJunAllocation())
                .julAllocation(allocation.getJulAllocation())
                .augAllocation(allocation.getAugAllocation())
                .sepAllocation(allocation.getSepAllocation())
                .octAllocation(allocation.getOctAllocation())
                .novAllocation(allocation.getNovAllocation())
                .decAllocation(allocation.getDecAllocation())
                .build();
    }

    private String calculateCurrentMonthAllocation(Allocation allocation) {
        int currentMonth = LocalDate.now().getMonthValue();
        return allocation.getAllocationForMonth(currentMonth);
    }

    private double calculateAllocationPercentage(Allocation allocation) {
        String currentAllocation = calculateCurrentMonthAllocation(allocation);
        double percentage = 0.0;
        if (currentAllocation != null && !currentAllocation.equalsIgnoreCase("B")
                && !currentAllocation.equalsIgnoreCase("P")) {
            try {
                percentage = Double.parseDouble(currentAllocation) * 100;
            } catch (NumberFormatException ignored) {
                // Log or handle the exception if necessary
            }
        }
        return percentage;
    }

    public List<String> getDistinctStatuses(Long managerId) {
        return allocationRepository.findDistinctStatusesByManager(managerId).stream()
                .map(Enum::name)
                .collect(Collectors.toList());
    }
}
