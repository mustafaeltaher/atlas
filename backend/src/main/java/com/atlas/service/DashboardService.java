package com.atlas.service;

import com.atlas.dto.DashboardStatsDTO;
import com.atlas.entity.Allocation;
import com.atlas.entity.Employee;
import com.atlas.entity.User;
import com.atlas.repository.AllocationRepository;
import com.atlas.repository.EmployeeRepository;
import com.atlas.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final EmployeeService employeeService;
    private final ProjectRepository projectRepository;
    private final AllocationRepository allocationRepository;

    public DashboardStatsDTO getStats(User currentUser) {
        // Get filtered employees based on Hierarchy RBAC
        List<Employee> employees = employeeService.getAccessibleEmployees(currentUser);
        long totalEmployees = employees.size();
        Set<Long> activeEmployeesIds = new HashSet<>();
        // long activeEmployees =
        // employees.stream().filter(Employee::getIsActive).count();

        // Get filtered projects based on Hierarchy RBAC
        long activeProjects;
        if (currentUser.getRole() == User.Role.SYSTEM_ADMIN || currentUser.getRole() == User.Role.EXECUTIVE) {
            activeProjects = projectRepository.countActiveProjects();
        } else {
            activeProjects = projectRepository.countActiveProjectsByEmployees(employees);
        }

        // Get filtered allocations based on Hierarchy RBAC
        List<Allocation> allocations;
        if (currentUser.getRole() == User.Role.SYSTEM_ADMIN || currentUser.getRole() == User.Role.EXECUTIVE) {
            allocations = allocationRepository.findAll();
        } else {
            allocations = allocationRepository.findActiveByEmployees(employees);
        }

        int currentMonth = LocalDate.now().getMonthValue();

        long benchCount = 0;
        long prospectCount = 0;
        double totalAllocation = 0;
        int allocationCount = 0;

        for (Allocation allocation : allocations) {
            String alloc = allocation.getAllocationForMonth(currentMonth);
            if (alloc != null) {
                if ("B".equalsIgnoreCase(alloc)) {
                    benchCount++;
                } else if ("P".equalsIgnoreCase(alloc)) {
                    prospectCount++;
                } else {
                    try {
                        // Only count allocation if it's a number and the employee is not already
                        // counted for allocation
                        if (Character.isDigit(alloc.charAt(0))) {
                            totalAllocation += Double.parseDouble(alloc);
                            allocationCount++;
                            if (!activeEmployeesIds.contains(allocation.getEmployee().getId())) {
                                activeEmployeesIds.add(allocation.getEmployee().getId());
                            }
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        // Update activeEmployees based on those with actual allocations, if that's the
        // intent
        // If activeEmployees was meant to be based on employee status, then this line
        // should be removed or adjusted.
        // For now, keeping the original activeEmployees count from the stream.
        // activeEmployees = activeEmployeesIds.size(); // This line would change the
        // meaning of activeEmployees

        // The original activeEmployees variable was based on employee status.
        // The instruction implies a change to how activeEmployees is calculated,
        // potentially using activeEmployeesIds.
        // For now, we'll use the count from activeEmployeesIds as per the implied
        // change.
        long activeEmployees = activeEmployeesIds.size();

        double avgAllocation = allocationCount > 0 ? (totalAllocation / allocationCount) * 100 : 0.0;

        return DashboardStatsDTO.builder()
                .totalEmployees(totalEmployees)
                .activeEmployees(activeEmployees)
                .averageAllocation(Math.round(avgAllocation * 10.0) / 10.0)
                .benchCount(benchCount)
                .prospectCount(prospectCount)
                .activeProjects(activeProjects)
                .pendingProjects(0L)
                .employeeTrend(5.0) // Mock trend data
                .allocationTrend(2.5)
                .benchTrend(-3.0)
                .projectTrend(10.0)
                .build();
    }

    // Private filtering methods removed as logic is now inside getStats using
    // EmployeeService

}
