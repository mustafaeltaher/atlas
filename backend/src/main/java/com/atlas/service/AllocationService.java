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
            Pageable pageable, String search, String allocationType, Long managerId) {

        Allocation.AllocationType allocationTypeEnum = null;
        if (allocationType != null && !allocationType.trim().isEmpty()) {
            try {
                allocationTypeEnum = Allocation.AllocationType.valueOf(allocationType.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid allocation type, ignore filter
            }
        }

        String searchParam = (search != null && !search.trim().isEmpty()) ? "%" + search.trim().toLowerCase() + "%" : null;

        if (currentUser.isTopLevel()) {
            Page<Allocation> allocationPage = allocationRepository
                    .findAllocationsWithFilters(searchParam, allocationTypeEnum, managerId, pageable);
            return allocationPage.map(this::toDTO);
        }

        // For non-top-level users: get accessible employee IDs first
        List<Employee> accessibleEmployees = employeeService.getAccessibleEmployees(currentUser);
        if (accessibleEmployees.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        List<Long> accessibleIds = accessibleEmployees.stream()
                .map(Employee::getId)
                .collect(Collectors.toList());

        Page<Allocation> allocationPage = allocationRepository
                .findAllocationsWithFiltersByEmployeeIds(accessibleIds, searchParam, allocationTypeEnum, managerId,
                        pageable);
        return allocationPage.map(this::toDTO);
    }

    public Page<EmployeeAllocationSummaryDTO> getGroupedAllocations(User currentUser,
            Pageable pageable, String search, String allocationType, Long managerId) {

        boolean isBenchFilter = "BENCH".equalsIgnoreCase(allocationType);
        boolean isActiveFilter = "ACTIVE".equalsIgnoreCase(allocationType);
        // Check if it's a standard allocation type (PROJECT, PROSPECT, VACATION, MATERNITY)
        boolean isStandardTypeFilter = false;
        if (!isBenchFilter && !isActiveFilter && allocationType != null && !allocationType.trim().isEmpty()) {
            try {
                Allocation.AllocationType.valueOf(allocationType.toUpperCase());
                isStandardTypeFilter = true;
            } catch (IllegalArgumentException e) {
                // Not a valid type, ignore
            }
        }

        String searchParam = (search != null && !search.trim().isEmpty()) ? "%" + search.trim().toLowerCase() + "%" : null;
        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();

        Page<Employee> employeePage;

        if (currentUser.isTopLevel()) {
            if (isBenchFilter) {
                employeePage = employeeRepository.findBenchEmployees(searchParam, managerId, currentYear,
                        currentMonth, pageable);
            } else if (isActiveFilter) {
                employeePage = employeeRepository.findActiveAllocatedEmployees(searchParam, managerId,
                        currentYear, currentMonth, pageable);
            } else if (isStandardTypeFilter) {
                employeePage = employeeRepository.findEmployeesByAllocationType(searchParam, managerId,
                        allocationType.toUpperCase(), pageable);
            } else {
                employeePage = employeeRepository.findEmployeesForAllocationView(
                        searchParam, managerId, pageable);
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
            } else if (isStandardTypeFilter) {
                employeePage = employeeRepository.findEmployeesByAllocationTypeByIds(accessibleIds,
                        searchParam, managerId, allocationType.toUpperCase(), pageable);
            } else {
                employeePage = employeeRepository.findEmployeesForAllocationViewByIds(
                        accessibleIds, searchParam, managerId, pageable);
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
        Map<Long, Integer> currentMonthAllocations = monthlyAllocationRepository
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

                    // For current month view: only PROJECT allocations with valid percentage
                    List<Allocation> projectAllocations = empAllocations.stream()
                            .filter(a -> a.getAllocationType() == Allocation.AllocationType.PROJECT)
                            .filter(a -> {
                                Integer percentage = currentMonthAllocations.get(a.getId());
                                return percentage != null && percentage > 0;
                            })
                            .collect(Collectors.toList());

                    List<AllocationDTO> allocationDTOs = projectAllocations.stream()
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
                            .employeeOracleId(emp.getOracleId() != null ? String.valueOf(emp.getOracleId()) : null)
                            .totalAllocationPercentage(totalPercentage)
                            .projectCount(projectAllocations.size())
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

        Allocation.AllocationType allocationType = dto.getAllocationType() != null
                ? dto.getAllocationType()
                : Allocation.AllocationType.PROJECT;

        // Project is required for PROJECT and PROSPECT types, nullable for VACATION/MATERNITY
        Project project = null;
        if (allocationType == Allocation.AllocationType.PROJECT
                || allocationType == Allocation.AllocationType.PROSPECT) {
            if (dto.getProjectId() == null) {
                throw new RuntimeException(
                        "Project is required for " + allocationType.name() + " allocations.");
            }
            project = projectRepository.findById(dto.getProjectId())
                    .orElseThrow(() -> new RuntimeException("Project not found: " + dto.getProjectId()));
        } else if (dto.getProjectId() != null) {
            // Allow optional project for VACATION/MATERNITY but don't require it
            project = projectRepository.findById(dto.getProjectId()).orElse(null);
        }

        Allocation allocation = Allocation.builder()
                .employee(employee)
                .project(project)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .allocationType(allocationType)
                .monthlyAllocations(new ArrayList<>())
                .build();

        allocation = allocationRepository.save(allocation);

        // Create monthly allocation for current month if percentage provided
        if (dto.getCurrentMonthAllocation() != null && allocationType == Allocation.AllocationType.PROJECT) {
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

        // If monthly allocations list is provided, create them (only for PROJECT allocations)
        if (dto.getMonthlyAllocations() != null && !dto.getMonthlyAllocations().isEmpty()) {
            if (allocationType == Allocation.AllocationType.PROSPECT) {
                throw new RuntimeException("PROSPECT allocations cannot have monthly allocation percentages. " +
                        "Change allocation type to PROJECT to add monthly allocations.");
            }
            for (MonthlyAllocationDTO maDTO : dto.getMonthlyAllocations()) {
                Integer pct = maDTO.getPercentage();
                validateAllocationPercentage(pct);

                MonthlyAllocation monthlyAlloc = MonthlyAllocation.builder()
                        .allocation(allocation)
                        .year(maDTO.getYear())
                        .month(maDTO.getMonth())
                        .percentage(pct)
                        .build();
                monthlyAllocationRepository.save(monthlyAlloc);
            }
        }

        return toDTO(allocation);
    }

    private void validateAllocationPercentage(Integer percentage) {
        if (percentage == null) {
            return;
        }
        if (percentage != 25 && percentage != 50 && percentage != 75 && percentage != 100) {
            throw new RuntimeException(
                    "Invalid allocation percentage. Allowed values are: 25, 50, 75, 100.");
        }
    }

    @Transactional
    public AllocationDTO updateAllocation(Long id, AllocationDTO dto) {
        Allocation allocation = allocationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Allocation not found: " + id));

        // Allocation type is immutable after creation
        if (dto.getAllocationType() != null && dto.getAllocationType() != allocation.getAllocationType()) {
            throw new RuntimeException(
                    "Cannot change allocation type. Allocation type is immutable after creation. " +
                            "Delete this allocation and create a new one with the desired type.");
        }

        // Update current month's allocation value
        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();

        if (dto.getCurrentMonthAllocation() != null) {
            if (allocation.getAllocationType() == Allocation.AllocationType.PROSPECT) {
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
        if (user.isTopLevel()) {
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
        if (user.isTopLevel()) {
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

        // Get current month allocation (Integer from helper)
        Integer currentMonthAlloc = allocation.getAllocationForYearMonth(currentYear, currentMonth);
        Double allocationPercentage = currentMonthAlloc != null ? (double) currentMonthAlloc : 0.0;

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
                .employeeOracleId(
                        allocation.getEmployee().getOracleId() != null
                                ? String.valueOf(allocation.getEmployee().getOracleId())
                                : null)
                .projectId(allocation.getProject() != null ? allocation.getProject().getId() : null)
                .projectName(allocation.getProject() != null ? allocation.getProject().getDescription() : null)
                .startDate(allocation.getStartDate())
                .endDate(allocation.getEndDate())
                .allocationType(allocation.getAllocationType())
                .currentMonthAllocation(currentMonthAlloc)
                .allocationPercentage(allocationPercentage)
                .monthlyAllocations(monthlyDTOs)
                .build();
    }

    private AllocationDTO toDTOWithCurrentMonth(Allocation allocation, int year, int month,
            Map<Long, Integer> currentMonthAllocations) {
        Integer currentMonthAlloc = currentMonthAllocations.get(allocation.getId());
        Double allocationPercentage = currentMonthAlloc != null ? (double) currentMonthAlloc : 0.0;

        return AllocationDTO.builder()
                .id(allocation.getId())
                .employeeId(allocation.getEmployee().getId())
                .employeeName(allocation.getEmployee().getName())
                .employeeOracleId(
                        allocation.getEmployee().getOracleId() != null
                                ? String.valueOf(allocation.getEmployee().getOracleId())
                                : null)
                .projectId(allocation.getProject() != null ? allocation.getProject().getId() : null)
                .projectName(allocation.getProject() != null ? allocation.getProject().getDescription() : null)
                .startDate(allocation.getStartDate())
                .endDate(allocation.getEndDate())
                .allocationType(allocation.getAllocationType())
                .currentMonthAllocation(currentMonthAlloc)
                .allocationPercentage(allocationPercentage)
                .build();
    }

    public List<Map<String, Object>> getManagersForAllocations(User currentUser, String allocationType) {
        List<Employee> accessible = employeeService.getAccessibleEmployees(currentUser);
        if (accessible.isEmpty()) {
            return List.of();
        }

        // Batch-fetch allocations
        Map<Long, List<Allocation>> allocationsByEmployee = new java.util.HashMap<>();
        List<Long> ids = accessible.stream().map(Employee::getId).collect(Collectors.toList());
        allocationRepository.findByEmployeeIdsWithDetails(ids)
                .forEach(a -> allocationsByEmployee
                        .computeIfAbsent(a.getEmployee().getId(), k -> new ArrayList<>())
                        .add(a));

        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();

        // Filter employees by allocation type
        List<Employee> filtered = accessible.stream()
                .filter(e -> e.getResignationDate() == null)
                .filter(e -> {
                    if (allocationType == null || allocationType.trim().isEmpty()) {
                        return true;
                    }
                    List<Allocation> empAllocs = allocationsByEmployee.getOrDefault(e.getId(), List.of());
                    if ("BENCH".equalsIgnoreCase(allocationType)) {
                        return empAllocs.stream()
                                .filter(a -> a.getAllocationType() == Allocation.AllocationType.PROJECT)
                                .noneMatch(a -> {
                                    Integer pct = a.getAllocationForYearMonth(currentYear, currentMonth);
                                    return pct != null && pct > 0;
                                });
                    }
                    if ("ACTIVE".equalsIgnoreCase(allocationType)) {
                        return empAllocs.stream()
                                .filter(a -> a.getAllocationType() == Allocation.AllocationType.PROJECT)
                                .anyMatch(a -> {
                                    Integer pct = a.getAllocationForYearMonth(currentYear, currentMonth);
                                    return pct != null && pct > 0;
                                });
                    }
                    try {
                        Allocation.AllocationType type = Allocation.AllocationType.valueOf(allocationType.toUpperCase());
                        return empAllocs.stream().anyMatch(a -> a.getAllocationType() == type);
                    } catch (IllegalArgumentException ex) {
                        return true;
                    }
                })
                .collect(Collectors.toList());

        // Collect distinct managers from filtered employees
        return filtered.stream()
                .filter(e -> e.getManager() != null)
                .map(Employee::getManager)
                .filter(m -> m.getResignationDate() == null)
                .distinct()
                .sorted((a, b) -> a.getName().compareTo(b.getName()))
                .map(m -> {
                    Map<String, Object> map = new java.util.LinkedHashMap<>();
                    map.put("id", m.getId());
                    map.put("name", m.getName());
                    return map;
                })
                .collect(Collectors.toList());
    }

    public List<String> getDistinctAllocationTypes(Long managerId) {
        List<String> types = allocationRepository.findDistinctAllocationTypesByManager(managerId).stream()
                .map(Allocation.AllocationType::name)
                .collect(Collectors.toCollection(ArrayList::new));
        // BENCH is a derived state (no active PROJECT allocation), always include it
        if (!types.contains("BENCH")) {
            types.add("BENCH");
        }
        return types;
    }
}
