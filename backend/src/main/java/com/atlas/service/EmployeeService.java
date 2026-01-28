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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final AllocationRepository allocationRepository;

    public List<EmployeeDTO> getAllEmployees(User currentUser) {
        List<Employee> employees = getFilteredEmployees(currentUser);
        return employees.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
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
            // Full visibility
            return employeeRepository.findByIsActiveTrue();
        }

        Employee userEmployee = user.getEmployee();
        if (userEmployee == null) {
            return List.of();
        }

        // Hierarchy-based visibility: get all direct and indirect reports
        // Including the user themselves
        List<Employee> employees = new java.util.ArrayList<>();
        employees.add(userEmployee);
        employees.addAll(getAllReportsRecursively(userEmployee.getId()));
        return employees;
    }

    private List<Employee> getFilteredEmployees(User user) {
        return getAccessibleEmployees(user);
    }

    private List<Employee> getAllReportsRecursively(Long managerId) {
        List<Employee> allReports = new java.util.ArrayList<>();
        List<Employee> directReports = employeeRepository.findActiveByManagerId(managerId);

        for (Employee report : directReports) {
            allReports.add(report);
            // Recursively get reports of this employee
            allReports.addAll(getAllReportsRecursively(report.getId()));
        }

        return allReports;
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

    private EmployeeDTO toDTO(Employee employee) {
        // Calculate current month allocation
        int currentMonth = LocalDate.now().getMonthValue();
        List<Allocation> allocations = allocationRepository.findActiveByEmployeeId(employee.getId());

        double totalAllocation = 0.0;
        String status = "BENCH";

        for (Allocation allocation : allocations) {
            String alloc = allocation.getAllocationForMonth(currentMonth);
            if (alloc != null) {
                if (alloc.equalsIgnoreCase("B")) {
                    // Bench - no allocation
                    status = "BENCH";
                } else if (alloc.equalsIgnoreCase("P")) {
                    // Prospect - potential allocation
                    if (status.equals("BENCH")) {
                        status = "PROSPECT";
                    }
                } else {
                    // Active allocation
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

    @Transactional
    public Employee createEmployee(Employee employee) {
        if (employeeRepository.existsByEmail(employee.getEmail())) {
            throw new RuntimeException("Employee with email already exists: " + employee.getEmail());
        }
        return employeeRepository.save(employee);
    }
}
