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
import com.atlas.specification.AllocationSpecification;
import com.atlas.specification.EmployeeSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
            Pageable pageable, String search, String allocationType, Long managerId,
            Integer year, Integer month) {

        // Default to current month if not provided
        if (year == null) {
            year = LocalDate.now().getYear();
        }
        if (month == null) {
            month = LocalDate.now().getMonthValue();
        }

        Allocation.AllocationType allocationTypeEnum = null;
        if (allocationType != null && !allocationType.trim().isEmpty()) {
            try {
                allocationTypeEnum = Allocation.AllocationType.valueOf(allocationType.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid allocation type, ignore filter
            }
        }

        String searchParam = (search != null && !search.trim().isEmpty()) ? search.trim()
                : null;

        List<Long> accessibleIds = employeeService.getAccessibleEmployeeIds(currentUser);
        if (accessibleIds != null && accessibleIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        Specification<Allocation> spec = AllocationSpecification
                .withFilters(allocationTypeEnum, managerId, searchParam, accessibleIds, year, month);

        Page<Allocation> allocationPage = allocationRepository.findAll(spec, pageable);
        return allocationPage.map(this::toDTO);
    }

    public Page<EmployeeAllocationSummaryDTO> getGroupedAllocations(User currentUser,
            Pageable pageable, String search, String allocationType, Long managerId,
            Integer year, Integer month) {

        // Default to current month if not provided
        if (year == null) {
            year = LocalDate.now().getYear();
        }
        if (month == null) {
            month = LocalDate.now().getMonthValue();
        }

        final boolean isBenchFilter = "BENCH".equalsIgnoreCase(allocationType);

        Allocation.AllocationType temp = null;
        if (!isBenchFilter && allocationType != null && !allocationType.trim().isEmpty()) {
            try {
                temp = Allocation.AllocationType.valueOf(allocationType.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Not a valid type, ignore
            }
        }
        final Allocation.AllocationType filterTypeEnum = temp;

        String searchParam = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        int currentYear = year;
        int currentMonth = month;

        List<Long> accessibleIds = employeeService.getAccessibleEmployeeIds(currentUser);
        if (accessibleIds != null && accessibleIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // Build employee filter specification
        Specification<Employee> spec;

        if (isBenchFilter) {
            // BENCH: employees with no allocations
            spec = EmployeeSpecification.withFilters(
                    searchParam, null, managerId, "BENCH", accessibleIds, null, currentYear, currentMonth);
        } else if (filterTypeEnum != null) {
            // Allocation type filter: get employees who have at least one allocation of this type
            // Use base filters (search, manager, access) + allocation type existence check
            spec = EmployeeSpecification.baseFiltersWithAllocationType(
                    searchParam, managerId, accessibleIds, filterTypeEnum, currentYear, currentMonth);
        } else {
            // No type filter: get all employees
            spec = EmployeeSpecification.withFilters(
                    searchParam, null, managerId, null, accessibleIds, null, currentYear, currentMonth);
        }

        Page<Employee> employeePage = employeeRepository.findAll(spec, pageable);

        List<Employee> employees = employeePage.getContent();
        if (employees.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, employeePage.getTotalElements());
        }

        // Batch-fetch allocations for the employees on this page
        List<Long> employeeIds = employees.stream()
                .map(Employee::getId)
                .collect(Collectors.toList());

        Specification<Allocation> allocationSpec;
        if (isBenchFilter) {
            allocationSpec = null; // Bench employees have no active allocations
        } else {
            allocationSpec = AllocationSpecification.withFilters(
                    filterTypeEnum, null, null, employeeIds, currentYear, currentMonth);
        }

        List<Allocation> allocations = isBenchFilter ? List.of() : allocationRepository.findAll(allocationSpec);

        // Get allocation IDs for batch-fetching monthly allocations
        List<Long> allocationIds = allocations.stream()
                .map(Allocation::getId)
                .collect(Collectors.toList());

        // Batch-fetch monthly allocations for current year/month
        Map<Long, Integer> currentMonthAllocations = allocationIds.isEmpty() ? java.util.Map.of()
                : monthlyAllocationRepository
                        .findByAllocationIdsAndYearAndMonth(allocationIds, currentYear, currentMonth)
                        .stream()
                        .collect(Collectors.toMap(
                                ma -> ma.getAllocation().getId(),
                                ma -> ma.getPercentage(),
                                (a, b) -> a));

        // Group allocations by employee ID
        Map<Long, List<Allocation>> allocationsByEmployee = allocations.stream()
                .collect(Collectors.groupingBy(a -> a.getEmployee().getId()));

        // Fetch distinct project counts directly from the DB, filtered by allocation type
        // For BENCH, filterTypeEnum is null, so we won't count any projects (BENCH employees have no allocations)
        // For PROJECT/PROSPECT/etc., we count only projects matching that type
        String allocationTypeString = filterTypeEnum != null ? filterTypeEnum.name() : null;
        Map<Long, Long> projectCountMap = monthlyAllocationRepository
                .findDistinctProjectCountByEmployeeIdsAndYearMonth(employeeIds, currentYear, currentMonth, allocationTypeString)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]));

        // Build summary DTOs for each employee on the page
        List<EmployeeAllocationSummaryDTO> summaries = employees.stream()
                .map(emp -> {
                    // Allocations are perfectly filtered from the DB mapping
                    List<Allocation> filteredAllocations = allocationsByEmployee
                            .getOrDefault(emp.getId(), List.of());

                    List<AllocationDTO> allocationDTOs = filteredAllocations.stream()
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
                            .employeeEmail(emp.getEmail())
                            .employeeOracleId(emp.getOracleId() != null ? String.valueOf(emp.getOracleId()) : null)
                            .managerName(emp.getManager() != null ? emp.getManager().getName() : null)
                            .totalAllocationPercentage(totalPercentage)
                            .projectCount(projectCountMap.getOrDefault(emp.getId(), 0L).intValue())
                            .allocations(allocationDTOs)
                            .build();
                }).collect(Collectors.toList());

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

    public List<AllocationDTO> getAllocationsByEmployee(Long employeeId, Integer year, Integer month,
            User currentUser) {
        if (year == null)
            year = LocalDate.now().getYear();
        if (month == null)
            month = LocalDate.now().getMonthValue();

        final int targetYear = year;
        final int targetMonth = month;

        List<Allocation> allEmployeeAllocations = allocationRepository.findByEmployeeIdWithDetails(employeeId).stream()
                .filter(a -> hasAccessToAllocation(currentUser, a))
                .collect(Collectors.toList());

        List<Long> allocationIds = allEmployeeAllocations.stream()
                .map(Allocation::getId)
                .collect(Collectors.toList());

        Map<Long, Integer> currentMonthAllocations = allocationIds.isEmpty() ? Map.of()
                : monthlyAllocationRepository
                        .findByAllocationIdsAndYearAndMonth(allocationIds, targetYear, targetMonth)
                        .stream()
                        .collect(Collectors.toMap(
                                ma -> ma.getAllocation().getId(),
                                MonthlyAllocation::getPercentage,
                                (a, b) -> a));

        return allEmployeeAllocations.stream()
                .map(a -> toDTOWithCurrentMonth(a, targetYear, targetMonth, currentMonthAllocations))
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

        // Project is required for PROJECT and PROSPECT types, nullable for
        // VACATION/MATERNITY
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

        // Create monthly allocations for the entire date range if percentage provided
        if (dto.getMonthlyAllocations() != null && !dto.getMonthlyAllocations().isEmpty()
                && (allocationType == Allocation.AllocationType.PROJECT
                        || allocationType == Allocation.AllocationType.PROSPECT)) {
            for (MonthlyAllocationDTO monthDto : dto.getMonthlyAllocations()) {
                if (monthDto.getPercentage() == null || monthDto.getPercentage() < 1
                        || monthDto.getPercentage() > 100) {
                    throw new RuntimeException(
                            "Invalid percentage for " + monthDto.getMonth() + "/" + monthDto.getYear()
                                    + ". Must be between 1 and 100.");
                }

                MonthlyAllocation monthlyAlloc = MonthlyAllocation.builder()
                        .allocation(allocation)
                        .year(monthDto.getYear())
                        .month(monthDto.getMonth())
                        .percentage(monthDto.getPercentage())
                        .build();
                monthlyAllocationRepository.save(monthlyAlloc);
            }
        } else if (dto.getCurrentMonthAllocation() != null &&
                (allocationType == Allocation.AllocationType.PROJECT
                        || allocationType == Allocation.AllocationType.PROSPECT)) {
            validateAllocationPercentage(dto.getCurrentMonthAllocation());

            LocalDate current = allocation.getStartDate();
            LocalDate end = allocation.getEndDate();

            if (current != null && end != null) {
                // Normalize to start of month to ensure we cover all months
                current = current.withDayOfMonth(1);
                LocalDate endMonth = end.withDayOfMonth(1);

                while (!current.isAfter(endMonth)) {
                    MonthlyAllocation monthlyAlloc = MonthlyAllocation.builder()
                            .allocation(allocation)
                            .year(current.getYear())
                            .month(current.getMonthValue())
                            .percentage(dto.getCurrentMonthAllocation())
                            .build();
                    monthlyAllocationRepository.save(monthlyAlloc);
                    current = current.plusMonths(1);
                }
            } else {
                // Fallback: Create for current month if dates are missing
                int year = dto.getYear() != null ? dto.getYear() : LocalDate.now().getYear();
                int month = LocalDate.now().getMonthValue();

                MonthlyAllocation monthlyAlloc = MonthlyAllocation.builder()
                        .allocation(allocation)
                        .year(year)
                        .month(month)
                        .percentage(dto.getCurrentMonthAllocation())
                        .build();
                monthlyAllocationRepository.save(monthlyAlloc);
            }
        }

        allocation = allocationRepository.findByIdWithDetails(allocation.getId()).orElse(allocation);
        return toDTO(allocation);
    }

    private void validateAllocationPercentage(Integer percentage) {
        if (percentage == null) {
            return;
        }
        if (percentage < 1 || percentage > 100) {
            throw new RuntimeException("Invalid allocation percentage. Must be between 1 and 100.");
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

        if (dto.getMonthlyAllocations() != null && !dto.getMonthlyAllocations().isEmpty()
                && (allocation.getAllocationType() == Allocation.AllocationType.PROJECT
                        || allocation.getAllocationType() == Allocation.AllocationType.PROSPECT)) {
            for (MonthlyAllocationDTO monthDto : dto.getMonthlyAllocations()) {
                if (monthDto.getPercentage() == null || monthDto.getPercentage() < 1
                        || monthDto.getPercentage() > 100) {
                    throw new RuntimeException("Invalid percentage. Must be between 1 and 100.");
                }

                MonthlyAllocation existing = monthlyAllocationRepository
                        .findByAllocationIdAndYearAndMonth(id, monthDto.getYear(), monthDto.getMonth())
                        .orElse(null);

                if (existing != null) {
                    existing.setPercentage(monthDto.getPercentage());
                    monthlyAllocationRepository.save(existing);
                } else {
                    MonthlyAllocation newMonthlyAlloc = MonthlyAllocation.builder()
                            .allocation(allocation)
                            .year(monthDto.getYear())
                            .month(monthDto.getMonth())
                            .percentage(monthDto.getPercentage())
                            .build();
                    monthlyAllocationRepository.save(newMonthlyAlloc);
                }
            }
        } else if (dto.getCurrentMonthAllocation() != null
                && (allocation.getAllocationType() == Allocation.AllocationType.PROJECT
                        || allocation.getAllocationType() == Allocation.AllocationType.PROSPECT)) {
            validateAllocationPercentage(dto.getCurrentMonthAllocation());

            LocalDate current = allocation.getStartDate();
            LocalDate end = allocation.getEndDate();

            if (current != null && end != null) {
                current = current.withDayOfMonth(1);
                LocalDate endMonth = end.withDayOfMonth(1);

                final int currentSystemYear = LocalDate.now().getYear();
                final int currentSystemMonth = LocalDate.now().getMonthValue();

                while (!current.isAfter(endMonth)) {
                    final int yearLocal = current.getYear();
                    final int monthLocal = current.getMonthValue();

                    // Check if the iterating month is strictly in the past
                    boolean isPastMonth = (yearLocal < currentSystemYear) ||
                            (yearLocal == currentSystemYear && monthLocal < currentSystemMonth);

                    if (!isPastMonth) {
                        MonthlyAllocation existing = monthlyAllocationRepository
                                .findByAllocationIdAndYearAndMonth(id, yearLocal, monthLocal)
                                .orElse(null);

                        if (existing != null) {
                            existing.setPercentage(dto.getCurrentMonthAllocation());
                            monthlyAllocationRepository.save(existing);
                        } else {
                            MonthlyAllocation newMonthlyAlloc = MonthlyAllocation.builder()
                                    .allocation(allocation)
                                    .year(yearLocal)
                                    .month(monthLocal)
                                    .percentage(dto.getCurrentMonthAllocation())
                                    .build();
                            monthlyAllocationRepository.save(newMonthlyAlloc);
                        }
                    }
                    current = current.plusMonths(1);
                }
            } else {
                int currentYear = LocalDate.now().getYear();
                int currentMonth = LocalDate.now().getMonthValue();

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

        // Fetch ONLY the percentage value from database (Projection query)
        // This avoids lazy loading ALL monthlyAllocations and filtering in-memory
        Integer currentMonthAlloc = monthlyAllocationRepository
                .findPercentageByAllocationIdAndYearMonth(allocation.getId(), currentYear, currentMonth)
                .orElse(null);
        Double allocationPercentage = currentMonthAlloc != null ? (double) currentMonthAlloc : 0.0;

        List<MonthlyAllocationDTO> monthlyAllocations = allocation.getMonthlyAllocations() != null
                ? allocation.getMonthlyAllocations().stream()
                        .map(ma -> MonthlyAllocationDTO.builder()
                                .id(ma.getId())
                                .allocationId(allocation.getId())
                                .year(ma.getYear())
                                .month(ma.getMonth())
                                .percentage(ma.getPercentage())
                                .build())
                        .collect(Collectors.toList())
                : new ArrayList<>();

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
                .monthlyAllocations(monthlyAllocations)
                .build();
    }

    private AllocationDTO toDTOWithCurrentMonth(Allocation allocation, int year, int month,
            Map<Long, Integer> currentMonthAllocations) {
        Integer currentMonthAlloc = currentMonthAllocations.get(allocation.getId());
        Double allocationPercentage = currentMonthAlloc != null ? (double) currentMonthAlloc : 0.0;

        List<MonthlyAllocationDTO> monthlyAllocations = allocation.getMonthlyAllocations() != null
                ? allocation.getMonthlyAllocations().stream()
                        .map(ma -> MonthlyAllocationDTO.builder()
                                .id(ma.getId())
                                .allocationId(allocation.getId())
                                .year(ma.getYear())
                                .month(ma.getMonth())
                                .percentage(ma.getPercentage())
                                .build())
                        .collect(Collectors.toList())
                : new ArrayList<>();

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
                .monthlyAllocations(monthlyAllocations)
                .build();
    }

    public List<Map<String, Object>> getManagersForAllocations(User currentUser, String allocationType, String search,
            String managerSearch, Integer year, Integer month) {
        // Default to current month if not provided
        if (year == null) {
            year = LocalDate.now().getYear();
        }
        if (month == null) {
            month = LocalDate.now().getMonthValue();
        }

        List<Long> accessibleIds = employeeService.getAccessibleEmployeeIds(currentUser);

        String searchTerm = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        String managerSearchTerm = (managerSearch != null && !managerSearch.trim().isEmpty())
                ? managerSearch.trim()
                : null;

        // Use EmployeeSpecification to filter employees by allocation criteria
        // Then select their distinct managers at DB level (same pattern as employees
        // page)
        // statusParam can be BENCH or allocation type (PROJECT, PROSPECT, etc.)
        String statusParam = allocationType;

        // Pass year/month so manager dropdown reflects the selected month
        // This ensures managers shown have subordinates with allocations in the
        // selected month
        List<Employee> distinctManagers = employeeRepository.findDistinctManagersByEmployeeSpec(
                searchTerm, null, null, statusParam, accessibleIds, managerSearchTerm, year, month);

        return distinctManagers.stream()
                .map(m -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", m.getId());
                    map.put("name", m.getName());
                    return map;
                })
                .collect(Collectors.toList());
    }

    public List<String> getDistinctAllocationTypes(User currentUser, Long managerId, String search,
            String allocationType,
            Integer year, Integer month) {
        // Default to current month if not provided
        if (year == null) {
            year = LocalDate.now().getYear();
        }
        if (month == null) {
            month = LocalDate.now().getMonthValue();
        }

        String searchParam = (search != null && !search.trim().isEmpty()) ? search.trim() : null;

        // Get accessible employee IDs for ABAC filtering
        List<Long> accessibleIds = employeeService.getAccessibleEmployeeIds(currentUser);

        // Use custom repository method for DB-level distinct allocation types
        // This avoids in-memory distinct operations on large result sets
        List<Allocation.AllocationType> types = allocationRepository.findDistinctAllocationTypesBySpec(
                managerId, searchParam, accessibleIds, year, month);

        // Convert to list of strings (PROJECT, PROSPECT, VACATION, MATERNITY)
        List<String> typeNames = types.stream()
                .map(Allocation.AllocationType::name)
                .collect(Collectors.toList());

        // Only add BENCH if:
        // 1. No allocation type filter is selected (show all available types), OR
        // 2. Current filter is BENCH (keep BENCH in dropdown when it's selected)
        // Don't add BENCH when filtering by PROJECT/PROSPECT/etc. (faceted search)
        boolean shouldIncludeBench = (allocationType == null || allocationType.trim().isEmpty()
                || "BENCH".equalsIgnoreCase(allocationType));

        if (shouldIncludeBench && (accessibleIds == null || !accessibleIds.isEmpty())) {
            String searchWithWildcards = (search != null && !search.trim().isEmpty())
                    ? "%" + search.trim().toLowerCase() + "%"
                    : null;

            // Use paginated query with page size 1 just to check if any bench employees
            // exist
            // Note: year/month parameters are accepted but BENCH employees should be
            // visible
            // across all months since they have no allocations (per constitution)
            Specification<Employee> benchSpec = EmployeeSpecification
                    .withFilters(
                            searchWithWildcards, null, managerId, "BENCH", accessibleIds, null, year, month);

            Page<Employee> benchPage = employeeRepository.findAll(benchSpec, Pageable.ofSize(1));

            if (benchPage.getTotalElements() > 0) {
                typeNames.add("BENCH");
            }
        }

        return typeNames;
    }

    public List<String> getAvailableMonths(User currentUser, String allocationType, Long managerId, String search) {
        List<Long> accessibleIds = employeeService.getAccessibleEmployeeIds(currentUser);

        if (accessibleIds != null && accessibleIds.isEmpty()) {
            return List.of();
        }

        // BENCH employees have no allocation records, so show unlimited months
        // Generate 10 years back + 5 years forward = 180 months total
        if (allocationType != null && "BENCH".equalsIgnoreCase(allocationType)) {
            return generateAllMonths(120, 60);
        }

        // For PROJECT, PROSPECT, MATERNITY, VACATION - show only months with actual
        // allocations
        Allocation.AllocationType allocationTypeEnum = null;
        if (allocationType != null && !allocationType.trim().isEmpty()) {
            try {
                allocationTypeEnum = Allocation.AllocationType.valueOf(allocationType.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid type, ignore
            }
        }

        String searchParam = (search != null && !search.trim().isEmpty()) ? search.trim() : null;

        // Get distinct year-month combinations from allocations matching filters
        List<String> availableMonths = allocationRepository.findDistinctAvailableMonths(
                allocationTypeEnum, managerId, searchParam, accessibleIds);

        // If no specific allocation type filter, show unlimited months
        // User can navigate to any time period - anyone without allocations appears as
        // BENCH
        if (allocationType == null || allocationType.trim().isEmpty()) {
            // Same unlimited range as BENCH: 10 years back + 5 years forward
            return generateAllMonths(120, 60);
        }

        return availableMonths;
    }

    /**
     * Generates a list of year-month strings in "YYYY-MM" format.
     *
     * @param monthsBack    Number of months before current month
     * @param monthsForward Number of months after current month
     * @return List of year-month strings
     */
    private List<String> generateAllMonths(int monthsBack, int monthsForward) {
        List<String> months = new ArrayList<>();
        LocalDate current = LocalDate.now().minusMonths(monthsBack);
        LocalDate end = LocalDate.now().plusMonths(monthsForward);

        while (!current.isAfter(end)) {
            months.add(String.format("%04d-%02d", current.getYear(), current.getMonthValue()));
            current = current.plusMonths(1);
        }

        return months;
    }
}
