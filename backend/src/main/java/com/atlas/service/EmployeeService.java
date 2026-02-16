package com.atlas.service;

import com.atlas.dto.EmployeeDTO;
import com.atlas.entity.Allocation;
import com.atlas.entity.Employee;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    // Paginated version with search and dynamic status filtering - uses DB-level
    // pagination
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
        Employee currentEmployee = user.getEmployee();
        if (currentEmployee == null) {
            return Collections.emptyList();
        }
        // Unified ABAC: Everyone sees their subtree (themselves + descendants)
        // using efficient recursive DB query
        List<Long> ids = employeeRepository.findAllSubordinateIds(currentEmployee.getId());
        return employeeRepository.findAllById(ids);
    }

    /**
     * Returns accessible employee IDs for access control filtering.
     * Unified ABAC: Returns ID list for ALL users (admins included).
     */
    public List<Long> getAccessibleEmployeeIds(User user) {
        Employee currentEmployee = user.getEmployee();
        if (currentEmployee == null) {
            return Collections.emptyList();
        }
        return employeeRepository.findAllSubordinateIds(currentEmployee.getId());
    }

    private List<Employee> getFilteredEmployees(User user) {
        return getAccessibleEmployees(user);
    }

    private boolean hasAccessToEmployee(User user, Employee employee) {
        Employee userEmployee = user.getEmployee();
        if (userEmployee == null) {
            return false;
        }

        // Users can always access their own profile
        if (userEmployee.getId().equals(employee.getId())) {
            return true;
        }

        // Hierarchy check using unified recursive query
        // This implicitly handles admins (their ID list contains everyone)
        List<Long> accessibleIds = getAccessibleEmployeeIds(user);
        return accessibleIds.contains(employee.getId());
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

    public List<String> getDistinctStatuses(User currentUser, Long managerId, String tower, String search) {
        List<Long> accessibleIds = getAccessibleEmployeeIds(currentUser);
        String towerParam = (tower != null && !tower.trim().isEmpty()) ? tower.trim() : null;
        String searchParam = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        List<String> allStatuses = List.of("ACTIVE", "BENCH", "PROSPECT", "MATERNITY", "VACATION", "RESIGNED");
        List<String> result = new ArrayList<>();
        for (String s : allStatuses) {
            long count = employeeRepository.count(
                    EmployeeSpecification.withFilters(searchParam, towerParam, managerId, s, accessibleIds));
            if (count > 0) {
                result.add(s);
            }
        }
        return result;
    }

    public List<String> getDistinctTowers(User currentUser, Long managerId, String status, String search) {
        List<Long> accessibleIds = getAccessibleEmployeeIds(currentUser);
        String searchParam = (search != null && !search.trim().isEmpty())
                ? "%" + search.trim().toLowerCase() + "%"
                : "%";
        return employeeRepository.findDistinctTowersFiltered(accessibleIds, managerId, searchParam);
    }

    public List<EmployeeDTO> getAccessibleManagers(User currentUser, String tower, String status, String search) {
        List<Long> accessibleIds = getAccessibleEmployeeIds(currentUser);
        String towerParam = (tower != null && !tower.trim().isEmpty()) ? tower.trim() : null;
        String statusParam = (status != null && !status.trim().isEmpty()) ? status.trim() : null;
        String searchParam = (search != null && !search.trim().isEmpty())
                ? "%" + search.trim().toLowerCase() + "%"
                : "%";

        List<Employee> managers;
        if (statusParam == null) {
            // No status filter — use the optimized tower-only query
            managers = employeeRepository.findDistinctManagersForEmployeeFiltersFiltered(accessibleIds, towerParam,
                    searchParam);
        } else {
            // Status filter active — find matching employees via spec, then extract their
            // managers
            var spec = EmployeeSpecification.withFilters(searchParam, towerParam, null, statusParam, accessibleIds);
            List<Employee> matchingEmployees = employeeRepository.findAll(spec);
            Map<Long, Employee> uniqueManagers = new java.util.LinkedHashMap<>();
            for (Employee e : matchingEmployees) {
                if (e.getManager() != null && e.getManager().getResignationDate() == null) {
                    uniqueManagers.putIfAbsent(e.getManager().getId(), e.getManager());
                }
            }
            managers = new ArrayList<>(uniqueManagers.values());
            managers.sort(java.util.Comparator.comparing(Employee::getName));
        }
        return managers.stream()
                .map(m -> EmployeeDTO.builder()
                        .id(m.getId())
                        .name(m.getName())
                        .oracleId(m.getOracleId())
                        .build())
                .collect(Collectors.toList());
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
