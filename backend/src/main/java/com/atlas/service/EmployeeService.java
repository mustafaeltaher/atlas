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

    // Paginated version with search
    public org.springframework.data.domain.Page<EmployeeDTO> getAllEmployees(User currentUser,
            org.springframework.data.domain.Pageable pageable, String search, Long managerId) {
        if (currentUser.getRole() == User.Role.SYSTEM_ADMIN || currentUser.getRole() == User.Role.EXECUTIVE) {
            if (managerId != null) {
                // Manager filter takes priority - use in-memory filtering
                List<Employee> allActive = employeeRepository.findByIsActiveTrueWithManager();
                allActive = filterByManager(allActive, managerId);
                return paginateAndConvert(allActive, search, pageable);
            }
            // Direct database pagination for admins
            org.springframework.data.domain.Page<Employee> employeePage;
            if (search != null && !search.trim().isEmpty()) {
                employeePage = employeeRepository.searchActiveEmployees(search.trim(), pageable);
            } else {
                employeePage = employeeRepository.findByIsActiveTrue(pageable);
            }
            Map<Long, List<Allocation>> allocationsByEmployee = batchFetchAllocations(employeePage.getContent());
            return employeePage.map(e -> toDTO(e, allocationsByEmployee.getOrDefault(e.getId(), Collections.emptyList())));
        }

        // For managers: fetch all accessible employees, then filter and paginate
        List<Employee> allAccessible = getAccessibleEmployees(currentUser);

        if (managerId != null) {
            allAccessible = filterByManager(allAccessible, managerId);
        }

        return paginateAndConvert(allAccessible, search, pageable);
    }

    private List<Employee> filterByManager(List<Employee> employees, Long managerId) {
        return employees.stream()
                .filter(e -> e.getManager() != null && e.getManager().getId().equals(managerId))
                .collect(Collectors.toList());
    }

    private org.springframework.data.domain.Page<EmployeeDTO> paginateAndConvert(
            List<Employee> employees, String search, org.springframework.data.domain.Pageable pageable) {
        // Apply search filter if provided
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase().trim();
            employees = employees.stream()
                    .filter(e -> (e.getName() != null && e.getName().toLowerCase().contains(searchLower)) ||
                            (e.getPrimarySkill() != null && e.getPrimarySkill().toLowerCase().contains(searchLower)) ||
                            (e.getTower() != null && e.getTower().toLowerCase().contains(searchLower)) ||
                            (e.getEmail() != null && e.getEmail().toLowerCase().contains(searchLower)))
                    .collect(Collectors.toList());
        }

        // Manual pagination from the filtered list
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), employees.size());
        List<Employee> pageEmployees = start < employees.size() ? employees.subList(start, end) : List.of();

        Map<Long, List<Allocation>> allocationsByEmployee = batchFetchAllocations(pageEmployees);
        List<EmployeeDTO> pageContent = pageEmployees.stream()
                .map(e -> toDTO(e, allocationsByEmployee.getOrDefault(e.getId(), Collections.emptyList())))
                .collect(Collectors.toList());

        return new org.springframework.data.domain.PageImpl<>(
                pageContent,
                pageable,
                employees.size());
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

        double totalAllocation = 0.0;
        String status = "BENCH";

        for (Allocation allocation : allocations) {
            String alloc = allocation.getAllocationForMonth(currentMonth);
            if (alloc != null) {
                if (alloc.equalsIgnoreCase("B")) {
                    status = "BENCH";
                } else if (alloc.equalsIgnoreCase("P")) {
                    if (status.equals("BENCH")) {
                        status = "PROSPECT";
                    }
                } else {
                    try {
                        totalAllocation += Double.parseDouble(alloc);
                        status = "ACTIVE";
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
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
                .currentAllocation(totalAllocation * 100)
                .allocationStatus(status)
                .build();
    }

    public long countActiveEmployees() {
        return employeeRepository.countActiveEmployees();
    }

    public List<String> getDistinctParentTowers() {
        return employeeRepository.findDistinctParentTowers();
    }

    public List<String> getDistinctTowers() {
        return employeeRepository.findDistinctTowers();
    }

    public List<EmployeeDTO> getAccessibleManagers(User currentUser) {
        List<Employee> managers;
        switch (currentUser.getRole()) {
            case SYSTEM_ADMIN, EXECUTIVE:
                managers = employeeRepository.findActiveManagers();
                break;
            case HEAD:
                if (currentUser.getEmployee() != null && currentUser.getEmployee().getParentTower() != null) {
                    managers = employeeRepository.findActiveManagersByParentTower(currentUser.getEmployee().getParentTower());
                } else {
                    managers = List.of();
                }
                break;
            case DEPARTMENT_MANAGER:
                if (currentUser.getEmployee() != null && currentUser.getEmployee().getTower() != null) {
                    managers = employeeRepository.findActiveManagersByTower(currentUser.getEmployee().getTower());
                } else {
                    managers = List.of();
                }
                break;
            case TEAM_LEAD:
                if (currentUser.getEmployee() != null) {
                    managers = List.of(currentUser.getEmployee());
                } else {
                    managers = List.of();
                }
                break;
            default:
                managers = List.of();
        }
        return managers.stream()
                .map(m -> EmployeeDTO.builder().id(m.getId()).name(m.getName()).oracleId(m.getOracleId()).build())
                .collect(Collectors.toList());
    }

    @Transactional
    public Employee createEmployee(Employee employee) {
        if (employeeRepository.existsByEmail(employee.getEmail())) {
            throw new RuntimeException("Employee with email already exists: " + employee.getEmail());
        }
        return employeeRepository.save(employee);
    }
}
