package com.atlas.service;

import com.atlas.dto.DashboardStatsDTO;
import com.atlas.entity.Allocation;
import com.atlas.entity.Employee;
import com.atlas.entity.User;
import com.atlas.repository.AllocationRepository;
import com.atlas.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final EmployeeService employeeService;
    private final ProjectRepository projectRepository;
    private final AllocationRepository allocationRepository;

    public DashboardStatsDTO getStats(User currentUser) {
        List<Employee> employees = employeeService.getAccessibleEmployees(currentUser);
        long totalEmployees = employees.size();

        long activeProjects;
        if (currentUser.isTopLevel()) {
            activeProjects = projectRepository.countActiveProjects();
        } else {
            activeProjects = projectRepository.countActiveProjectsByEmployees(employees);
        }

        // Fetch ALL allocations for these employees to correctly determine status
        List<Allocation> allocations;
        if (currentUser.isTopLevel()) {
            allocations = allocationRepository.findAllWithEmployeeAndProject();
        } else {
            // Use the new method that returns ALL allocations (including
            // PROSPECT/BENCH/etc)
            allocations = allocationRepository.findAllocationsByEmployees(employees);
        }

        // Group allocations by employee for easier processing
        Map<Long, List<Allocation>> allocationsByEmployee = allocations.stream()
                .collect(Collectors.groupingBy(a -> a.getEmployee().getId()));

        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();

        long benchCount = 0;
        long prospectCount = 0;
        long activeEmployeeCount = 0;
        int totalAllocationPercentage = 0;
        int allocatedEmployeeCount = 0;

        for (Employee employee : employees) {
            List<Allocation> employeeAllocations = allocationsByEmployee.getOrDefault(employee.getId(),
                    Collections.emptyList());

            // Determine status similar to EmployeeService.toDTO logic
            boolean isResigned = employee.getResignationDate() != null;
            boolean isMaternity = employeeAllocations.stream()
                    .anyMatch(a -> a.getAllocationType() == Allocation.AllocationType.MATERNITY);
            boolean isVacation = employeeAllocations.stream()
                    .anyMatch(a -> a.getAllocationType() == Allocation.AllocationType.VACATION);

            boolean hasActiveProject = false;
            boolean hasProspect = false;
            int employeeTotalAllocation = 0;

            for (Allocation allocation : employeeAllocations) {
                if (allocation.getAllocationType() == Allocation.AllocationType.PROJECT) {
                    Integer alloc = allocation.getAllocationForYearMonth(currentYear, currentMonth);
                    if (alloc != null && alloc > 0) {
                        employeeTotalAllocation += alloc;
                        hasActiveProject = true;
                    }
                } else if (allocation.getAllocationType() == Allocation.AllocationType.PROSPECT) {
                    hasProspect = true;
                }
            }

            // Stats calculation
            if (hasActiveProject) {
                activeEmployeeCount++;
                totalAllocationPercentage += employeeTotalAllocation;
                allocatedEmployeeCount++;
            } else if (hasProspect) {
                // Only count as prospect if NOT active
                prospectCount++;
            } else {
                // Bench logic: Not active, not prospect, and not (Resigned OR Maternity OR
                // Vacation)
                if (!isResigned && !isMaternity && !isVacation) {
                    benchCount++;
                }
            }
        }

        double avgAllocation = allocatedEmployeeCount > 0 ? (double) totalAllocationPercentage / allocatedEmployeeCount
                : 0.0;

        return DashboardStatsDTO.builder()
                .totalEmployees(totalEmployees)
                .activeEmployees(activeEmployeeCount)
                .averageAllocation(Math.round(avgAllocation * 10.0) / 10.0)
                .benchCount(benchCount) // Now correctly calculated
                .prospectCount(prospectCount)
                .activeProjects(activeProjects)
                .pendingProjects(0L) // Placeholder or implement if needed
                .employeeTrend(5.0) // Dummy trend data
                .allocationTrend(2.5)
                .benchTrend(-3.0)
                .projectTrend(10.0)
                .build();
    }
}
