package com.atlas.service;

import com.atlas.dto.EmployeeDTO;
import com.atlas.entity.Allocation;
import com.atlas.entity.Employee;
import com.atlas.entity.EmployeeSkill;
import com.atlas.entity.User;
import com.atlas.repository.AllocationRepository;
import com.atlas.repository.EmployeeRepository;
import com.atlas.repository.EmployeeSkillRepository;
import com.atlas.specification.EmployeeSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final AllocationRepository allocationRepository;
    private final EmployeeSkillRepository employeeSkillRepository;

    public List<EmployeeDTO> getAllEmployees(User currentUser) {
        List<Employee> employees = getFilteredEmployees(currentUser);
        Map<Long, List<Allocation>> allocationsByEmployee = batchFetchAllocations(employees);
        return employees.stream()
                .map(e -> toDTO(e, allocationsByEmployee.getOrDefault(e.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
    }

    // Paginated version with search and dynamic status filtering - uses DB-level pagination
    public Page<EmployeeDTO> getAllEmployees(User currentUser,
            Pageable pageable, String search, Long managerId, String tower,
            String status) {

        // Normalize filters
        String searchParam = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        String towerParam = (tower != null && !tower.trim().isEmpty()) ? tower.trim() : null;
        String statusParam = (status != null && !status.trim().isEmpty()) ? status.trim() : null;

        // Get accessible employee IDs for non-top-level users
        List<Long> accessibleIds = null;
        if (!currentUser.isTopLevel()) {
            List<Employee> accessibleEmployees = getAccessibleEmployees(currentUser);
            if (accessibleEmployees.isEmpty()) {
                return new PageImpl<>(List.of(), pageable, 0);
            }
            accessibleIds = accessibleEmployees.stream()
                    .map(Employee::getId)
                    .collect(Collectors.toList());
        }

        // DB-level pagination with all filters applied via Specification
        Page<Employee> employeePage = employeeRepository.findAll(
                EmployeeSpecification.withFilters(searchParam, towerParam, managerId, statusParam, accessibleIds),
                pageable);

        // Batch fetch allocations for page content only
        Map<Long, List<Allocation>> allocationsByEmployee = batchFetchAllocations(employeePage.getContent());

        return employeePage.map(e -> toDTO(e, allocationsByEmployee.getOrDefault(e.getId(), Collections.emptyList())));
    }

    public EmployeeDTO getEmployeeById(Long id, User currentUser) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + id));

        // Check if user has access to this employee based on hierarchy
        if (!hasAccessToEmployee(currentUser, employee)) {
            throw new RuntimeException("Access denied to employee: " + id);
        }

        return toDTO(employee);
    }

    public List<Employee> getAccessibleEmployees(User user) {
        if (user.isTopLevel()) {
            return employeeRepository.findAllWithManager();
        }

        Employee userEmployee = user.getEmployee();
        if (userEmployee == null) {
            return List.of();
        }

        // Load all employees in one query, then walk the tree in memory
        List<Employee> all = employeeRepository.findAllWithManager();
        Map<Long, List<Employee>> reportsByManager = new HashMap<>();
        for (Employee e : all) {
            if (e.getManager() != null) {
                reportsByManager.computeIfAbsent(e.getManager().getId(), k -> new ArrayList<>()).add(e);
            }
        }

        List<Employee> result = new ArrayList<>();
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
        if (user.isTopLevel()) {
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

        // Check if the employee is in the user's hierarchy
        return isInHierarchy(userEmployee.getId(), employee);
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
        return allocationRepository.findByEmployeeIdsWithDetails(ids).stream()
                .collect(Collectors.groupingBy(a -> a.getEmployee().getId()));
    }

    private EmployeeDTO toDTO(Employee employee) {
        List<Allocation> allocations = allocationRepository.findByEmployeeIdWithDetails(employee.getId());
        return toDTO(employee, allocations);
    }

    private EmployeeDTO toDTO(Employee employee, List<Allocation> allocations) {
        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();

        // Derive employee status from allocations and resignationDate
        String employeeStatus;
        if (employee.getResignationDate() != null) {
            employeeStatus = "RESIGNED";
        } else if (allocations.stream().anyMatch(a -> a.getAllocationType() == Allocation.AllocationType.MATERNITY)) {
            employeeStatus = "MATERNITY";
        } else if (allocations.stream().anyMatch(a -> a.getAllocationType() == Allocation.AllocationType.VACATION)) {
            employeeStatus = "VACATION";
        } else {
            employeeStatus = "ACTIVE";
        }

        // Derive allocation status from PROJECT/PROSPECT allocations
        boolean hasActive = false;
        boolean hasProspect = false;
        int totalAllocation = 0;

        for (Allocation allocation : allocations) {
            if (allocation.getAllocationType() == Allocation.AllocationType.PROSPECT) {
                hasProspect = true;
            } else if (allocation.getAllocationType() == Allocation.AllocationType.PROJECT) {
                Integer alloc = allocation.getAllocationForYearMonth(currentYear, currentMonth);
                if (alloc != null && alloc > 0) {
                    totalAllocation += alloc;
                    hasActive = true;
                }
            }
        }

        // Determine allocation status
        String allocationStatus;
        if ("RESIGNED".equals(employeeStatus) || "MATERNITY".equals(employeeStatus)
                || "VACATION".equals(employeeStatus)) {
            // These employees are not considered bench
            allocationStatus = hasActive ? "ACTIVE" : (hasProspect ? "PROSPECT" : null);
        } else {
            if (hasActive) {
                allocationStatus = "ACTIVE";
            } else if (hasProspect) {
                allocationStatus = "PROSPECT";
            } else {
                allocationStatus = "BENCH";
            }
        }

        // Tower info from TechTower entity
        String towerName = null;
        String parentTowerName = null;
        Integer towerId = null;
        if (employee.getTower() != null) {
            towerId = employee.getTower().getId();
            towerName = employee.getTower().getDescription();
            if (employee.getTower().getParentTower() != null) {
                parentTowerName = employee.getTower().getParentTower().getDescription();
            }
        }

        // Skills
        List<EmployeeDTO.EmployeeSkillDTO> skillDTOs = employeeSkillRepository.findByEmployeeId(employee.getId())
                .stream()
                .map(es -> EmployeeDTO.EmployeeSkillDTO.builder()
                        .skillName(es.getSkill().getDescription())
                        .skillLevel(es.getSkillLevel() != null ? es.getSkillLevel().name() : null)
                        .skillGrade(es.getSkillGrade() != null ? es.getSkillGrade().name() : null)
                        .build())
                .collect(Collectors.toList());

        return EmployeeDTO.builder()
                .id(employee.getId())
                .oracleId(employee.getOracleId())
                .name(employee.getName())
                .gender(employee.getGender() != null ? employee.getGender().name() : null)
                .grade(employee.getGrade())
                .jobLevel(employee.getJobLevel() != null ? employee.getJobLevel().name() : null)
                .title(employee.getTitle())
                .hiringType(employee.getHiringType() != null ? employee.getHiringType().name() : null)
                .location(employee.getLocation())
                .legalEntity(employee.getLegalEntity())
                .costCenter(employee.getCostCenter())
                .nationality(employee.getNationality())
                .hireDate(employee.getHireDate())
                .resignationDate(employee.getResignationDate())
                .reasonOfLeave(employee.getReasonOfLeave())
                .email(employee.getEmail())
                .towerId(towerId)
                .towerName(towerName)
                .parentTowerName(parentTowerName)
                .managerId(employee.getManager() != null ? employee.getManager().getId() : null)
                .managerName(employee.getManager() != null ? employee.getManager().getName() : null)
                .skills(skillDTOs)
                .status(employeeStatus)
                .totalAllocation((double) totalAllocation)
                .allocationStatus(allocationStatus)
                .build();
    }

    public long countActiveEmployees() {
        return employeeRepository.countActiveEmployees();
    }

    public List<String> getDistinctStatuses(User currentUser, Long managerId, String tower) {
        List<EmployeeDTO> employees = getFilteredEmployeeStream(currentUser, managerId, tower, null)
                .collect(Collectors.toList());

        Set<String> statuses = new LinkedHashSet<>();

        for (EmployeeDTO emp : employees) {
            // Add employee status if not ACTIVE
            if (emp.getStatus() != null && !"ACTIVE".equals(emp.getStatus())) {
                statuses.add(emp.getStatus());
            }
            // Add allocation status if present (for ACTIVE employees)
            if (emp.getAllocationStatus() != null && "ACTIVE".equals(emp.getStatus())) {
                statuses.add(emp.getAllocationStatus());
            }
        }

        return new ArrayList<>(statuses);
    }

    public List<String> getDistinctTowers(User currentUser, Long managerId, String status) {
        return getFilteredEmployeeStream(currentUser, managerId, null, status)
                .map(EmployeeDTO::getTowerName)
                .filter(t -> t != null && !t.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<EmployeeDTO> getAccessibleManagers(User currentUser, String tower, String status) {
        List<Employee> accessible = getAccessibleEmployees(currentUser);

        // Identify which employees match the criteria
        List<EmployeeDTO> matchingEmployees = getFilteredEmployeeStream(currentUser, null, tower, status)
                .collect(Collectors.toList());

        // Collect their managers
        return matchingEmployees.stream()
                .map(dto -> accessible.stream()
                        .filter(e -> e.getId().equals(dto.getId()))
                        .findFirst()
                        .orElse(null))
                .filter(e -> e != null && e.getManager() != null)
                .map(Employee::getManager)
                .distinct()
                .filter(m -> m.getResignationDate() == null) // ensure manager is active (no resignation)
                .map(m -> EmployeeDTO.builder()
                        .id(m.getId())
                        .name(m.getName())
                        .oracleId(m.getOracleId())
                        .build())
                .sorted((m1, m2) -> m1.getName().compareTo(m2.getName()))
                .collect(Collectors.toList());
    }

    private Stream<EmployeeDTO> getFilteredEmployeeStream(User currentUser, Long managerId,
            String tower, String status) {
        List<Employee> accessible = getAccessibleEmployees(currentUser);

        // Filter by structural attributes first
        List<Employee> structurallyFiltered = accessible.stream()
                .filter(e -> managerId == null
                        || (e.getManager() != null && e.getManager().getId().equals(managerId)))
                .filter(e -> tower == null
                        || (e.getTower() != null && e.getTower().getDescription() != null
                                && e.getTower().getDescription().equals(tower)))
                .collect(Collectors.toList());

        // Batch fetch allocations for these employees
        Map<Long, List<Allocation>> allocationsByEmployee = batchFetchAllocations(structurallyFiltered);

        // Map to DTO (calculates status) and filter by status
        return structurallyFiltered.stream()
                .map(e -> toDTO(e, allocationsByEmployee.getOrDefault(e.getId(), Collections.emptyList())))
                .filter(dto -> {
                    if (status == null) return true;
                    // ACTIVE, BENCH, PROSPECT are allocation-level statuses
                    if ("ACTIVE".equalsIgnoreCase(status)
                            || "BENCH".equalsIgnoreCase(status)
                            || "PROSPECT".equalsIgnoreCase(status)) {
                        return status.equalsIgnoreCase(dto.getAllocationStatus());
                    }
                    // MATERNITY, VACATION, RESIGNED are employee-level statuses
                    return status.equalsIgnoreCase(dto.getStatus());
                });
    }

    @Transactional
    public Employee createEmployee(Employee employee) {
        if (employeeRepository.existsByEmail(employee.getEmail())) {
            throw new RuntimeException("Employee with email already exists: " + employee.getEmail());
        }
        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee updateEmployee(Long id, Employee updatedEmployee) {
        Employee existing = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + id));

        // Update fields as needed
        return employeeRepository.save(existing);
    }
}
