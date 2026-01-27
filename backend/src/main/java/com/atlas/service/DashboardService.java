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
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final EmployeeService employeeService;
    private final ProjectRepository projectRepository;
    private final AllocationRepository allocationRepository;

    public DashboardStatsDTO getStats(User currentUser) {
        // Get filtered employees based on Hierarchy RBAC
        List<Employee> employees = employeeService.getAccessibleEmployees(currentUser);
        long activeEmployees = employees.stream().filter(Employee::getIsActive).count();

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
        double totalUtilization = 0;
        int utilizationCount = 0;

        for (Allocation allocation : allocations) {
            String util = allocation.getUtilizationForMonth(currentMonth);
            if (util != null) {
                if ("B".equalsIgnoreCase(util)) {
                    benchCount++;
                } else if ("P".equalsIgnoreCase(util)) {
                    prospectCount++;
                } else {
                    try {
                        totalUtilization += Double.parseDouble(util);
                        utilizationCount++;
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        double avgUtilization = utilizationCount > 0 ? (totalUtilization / utilizationCount) * 100 : 0.0;

        return DashboardStatsDTO.builder()
                .totalEmployees((long) employees.size())
                .activeEmployees(activeEmployees)
                .averageUtilization(Math.round(avgUtilization * 10.0) / 10.0)
                .benchCount(benchCount)
                .prospectCount(prospectCount)
                .activeProjects(activeProjects)
                .pendingProjects(0L)
                .employeeTrend(5.0)
                .utilizationTrend(2.5)
                .benchTrend(-3.0)
                .projectTrend(10.0)
                .build();
    }

    // Private filtering methods removed as logic is now inside getStats using
    // EmployeeService

}
