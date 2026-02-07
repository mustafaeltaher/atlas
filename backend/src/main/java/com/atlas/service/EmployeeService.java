package com.atlas.service;

import com.atlas.dto.EmployeeDTO;
import com.atlas.entity.Allocation;
import com.atlas.entity.Employee;
import com.atlas.entity.User;
import com.atlas.repository.AllocationRepository;
import com.atlas.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final AllocationRepository allocationRepository;

    public List<EmployeeDTO> getAllEmployees(User currentUser) {
        List<Employee> employees = getFilteredEmployees(currentUser);
        Map<Long, List<Allocation>> allocationsByEmployee = batchFetchAllocations(employees);
        return employees.stream()
                .map(e -> toDTO(e, allocationsByEmployee.getOrDefault(e.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
    }

    // Paginated version with search and dynamic status filtering
    public org.springframework.data.domain.Page<EmployeeDTO> getAllEmployees(User currentUser,
            org.springframework.data.domain.Pageable pageable, String search, Long managerId, String tower,
            String status) {

        // Normalize filters
        String searchParam = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        String towerParam = (tower != null && !tower.trim().isEmpty()) ? tower.trim() : null;
        String statusParam = (status != null && !status.trim().isEmpty()) ? status.trim() : null;

        List<Long> accessibleIds = null;

        if (currentUser.getRole() != User.Role.SYSTEM_ADMIN && currentUser.getRole() != User.Role.EXECUTIVE) {
            // For non-admin managers: get accessible employee IDs first
            List<Employee> accessibleEmployees = getAccessibleEmployees(currentUser);
            if (accessibleEmployees.isEmpty()) {
                return new org.springframework.data.domain.PageImpl<>(List.of(), pageable, 0);
            }
            accessibleIds = accessibleEmployees.stream()
                    .map(Employee::getId)
                    .collect(Collectors.toList());
        }

        // Create Specification
        org.springframework.data.jpa.domain.Specification<Employee> spec = com.atlas.specification.EmployeeSpecification
                .withFilters(searchParam, towerParam, managerId, statusParam, accessibleIds);

        // Execute Query
        org.springframework.data.domain.Page<Employee> employeePage = employeeRepository.findAll(spec, pageable);

        // Batch fetch allocations for DTO conversion
        Map<Long, List<Allocation>> allocationsByEmployee = batchFetchAllocations(employeePage.getContent());
        return employeePage.map(e -> toDTO(e, allocationsByEmployee.getOrDefault(e.getId(), Collections.emptyList())));
    }

    public EmployeeDTO getEmployeeById(Long id, User currentUser) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + id));

        // Check if user has access to this employee based on RBAC
        if (!hasAccessToEmployee(currentUser, employee)) {
            throw new RuntimeException("Access denied to employee: " + id);
        }

        return toDTO(employee);
    }

    public List<Employee> getAccessibleEmployees(User user) {
        if (user.getRole() == User.Role.SYSTEM_ADMIN || user.getRole() == User.Role.EXECUTIVE) {
            return employeeRepository.findByIsActiveTrueWithManager();
        }

        Employee userEmployee = user.getEmployee();
        if (userEmployee == null) {
            return List.of();
        }

        // Load all active employees in one query, then walk the tree in memory
        List<Employee> allActive = employeeRepository.findByIsActiveTrueWithManager();
        Map<Long, List<Employee>> reportsByManager = new HashMap<>();
        for (Employee e : allActive) {
            if (e.getManager() != null) {
                reportsByManager.computeIfAbsent(e.getManager().getId(), k -> new java.util.ArrayList<>()).add(e);
            }
        }

        List<Employee> result = new java.util.ArrayList<>();
        result.add(userEmployee);
        collectReports(userEmployee.getId(), reportsByManager, result);
        return result;
    }

    private void collectReports(Long managerId, Map<Long, List<Employee>> reportsByManager, List<Employee> result) {
        List<Employee> directs = reportsByManager.getOrDefault(managerId, Collections.emptyList());
        for (Employee report : directs) {
            result.add(report);
            collectReports(report.getId(), reportsByManager, result);
        }
    }

    private List<Employee> getFilteredEmployees(User user) {
        return getAccessibleEmployees(user);
    }

    private boolean hasAccessToEmployee(User user, Employee employee) {
        if (user.getRole() == User.Role.SYSTEM_ADMIN || user.getRole() == User.Role.EXECUTIVE) {
            return true;
        }

        Employee userEmployee = user.getEmployee();
        if (userEmployee == null) {
            return false;
        }

        // Users can always access their own profile
        if (userEmployee.getId().equals(employee.getId())) {
            return true;
        }

        // Check if the employee is in the manager's hierarchy
        return switch (user.getRole()) {
            case HEAD, DEPARTMENT_MANAGER, TEAM_LEAD ->
                isInHierarchy(userEmployee.getId(), employee);
            default -> false;
        };
    }

    private boolean isInHierarchy(Long managerId, Employee employee) {
        // Check if employee reports to this manager (directly or indirectly)
        Employee currentManager = employee.getManager();
        while (currentManager != null) {
            if (currentManager.getId().equals(managerId)) {
                return true;
            }
            currentManager = currentManager.getManager();
        }
        return false;
    }

    private Map<Long, List<Allocation>> batchFetchAllocations(List<Employee> employees) {
        if (employees.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> ids = employees.stream().map(Employee::getId).collect(Collectors.toList());
        return allocationRepository.findActiveByEmployeeIds(ids).stream()
                .collect(Collectors.groupingBy(a -> a.getEmployee().getId()));
    }

    private EmployeeDTO toDTO(Employee employee) {
        List<Allocation> allocations = allocationRepository.findActiveByEmployeeId(employee.getId());
        return toDTO(employee, allocations);
    }

    private EmployeeDTO toDTO(Employee employee, List<Allocation> allocations) {
        int currentMonth = LocalDate.now().getMonthValue();

        boolean hasActive = false;
        boolean hasProspect = false;
        double totalAllocation = 0.0;

        for (Allocation allocation : allocations) {
            String alloc = allocation.getAllocationForMonth(currentMonth);
            if (alloc != null) {
                if (alloc.equalsIgnoreCase("B")) {
                    // Bench - ignored for status calculation purposes
                } else if (alloc.equalsIgnoreCase("P")) {
                    hasProspect = true;
                } else {
                    try {
                        totalAllocation += Double.parseDouble(alloc);
                        hasActive = true;
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        String status = "BENCH";
        if (hasActive) {
            status = "ACTIVE";
        } else if (hasProspect) {
            status = "PROSPECT";
        }

        return EmployeeDTO.builder()
                .id(employee.getId())
                .oracleId(employee.getOracleId())
                .name(employee.getName())
                .gender(employee.getGender())
                .grade(employee.getGrade())
                .jobLevel(employee.getJobLevel())
                .title(employee.getTitle())
                .primarySkill(employee.getPrimarySkill())
                .secondarySkill(employee.getSecondarySkill())
                .hiringType(employee.getHiringType())
                .location(employee.getLocation())
                .legalEntity(employee.getLegalEntity())
                .costCenter(employee.getCostCenter())
                .nationality(employee.getNationality())
                .hireDate(employee.getHireDate())
                .resignationDate(employee.getResignationDate())
                .email(employee.getEmail())
                .parentTower(employee.getParentTower())
                .tower(employee.getTower())
                .futureManager(employee.getFutureManager())
                .managerId(employee.getManager() != null ? employee.getManager().getId() : null)
                .managerName(employee.getManager() != null ? employee.getManager().getName() : null)
                .isActive(employee.getIsActive())
                .totalAllocation(totalAllocation * 100)
                .allocationStatus(status)
                .build();
    }

    public long countActiveEmployees() {
        return employeeRepository.countActiveEmployees();
    }

    public List<String> getDistinctParentTowers() {
        return employeeRepository.findDistinctParentTowers();
    }

    public List<String> getDistinctStatuses(User currentUser, Long managerId, String tower) {
        return getFilteredEmployeeStream(currentUser, managerId, tower, null)
                .map(EmployeeDTO::getAllocationStatus)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<String> getDistinctTowers(User currentUser, Long managerId, String status) {
        return getFilteredEmployeeStream(currentUser, managerId, null, status)
                .map(EmployeeDTO::getTower)
                .filter(t -> t != null && !t.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<EmployeeDTO> getAccessibleManagers(User currentUser, String tower, String status) {
        // For managers list: we want to find managers who have employees matching the
        // filters
        // This is slightly different from "find employees matching filters"
        // But previously we defined it as: "Managers who have accessible reports
        // matching the criteria"

        List<Employee> accessible = getAccessibleEmployees(currentUser);

        // 1. Identify which employees match the criteria
        List<EmployeeDTO> matchingEmployees = getFilteredEmployeeStream(currentUser, null, tower, status)
                .collect(Collectors.toList());

        // 2. Collect their managers
        return matchingEmployees.stream()
                .map(dto -> {
                    // We need the Employee entity to get the manager, but DTO only has ID/Name
                    // Map back from filtered ID to original list
                    return accessible.stream().filter(e -> e.getId().equals(dto.getId())).findFirst().orElse(null);
                })
                .filter(e -> e != null && e.getManager() != null)
                .map(Employee::getManager)
                .distinct()
                .filter(m -> m.getIsActive()) // ensure manager is active
                .map(m -> EmployeeDTO.builder()
                        .id(m.getId())
                        .name(m.getName())
                        .oracleId(m.getOracleId())
                        .build())
                .sorted((m1, m2) -> m1.getName().compareTo(m2.getName()))
                .collect(Collectors.toList());
    }

    private java.util.stream.Stream<EmployeeDTO> getFilteredEmployeeStream(User currentUser, Long managerId,
            String tower, String status) {
        List<Employee> accessible = getAccessibleEmployees(currentUser);

        // Filter by structural attributes first
        List<Employee> structurallyFiltered = accessible.stream()
                .filter(e -> managerId == null || (e.getManager() != null && e.getManager().getId().equals(managerId)))
                .filter(e -> tower == null || (e.getTower() != null && e.getTower().equals(tower)))
                .collect(Collectors.toList());

        // Batch fetch allocations for these employees
        Map<Long, List<Allocation>> allocationsByEmployee = batchFetchAllocations(structurallyFiltered);

        // Map to DTO (calculates status) and filter by status
        return structurallyFiltered.stream()
                .map(e -> toDTO(e, allocationsByEmployee.getOrDefault(e.getId(), Collections.emptyList())))
                .filter(dto -> status == null || dto.getAllocationStatus().equalsIgnoreCase(status));
    }

    @Transactional
    public Employee createEmployee(Employee employee) {
        if (employeeRepository.existsByEmail(employee.getEmail())) {
            throw new RuntimeException("Employee with email already exists: " + employee.getEmail());
        }
        return employeeRepository.save(employee);
    }
}
