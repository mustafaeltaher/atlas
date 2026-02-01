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
        List<Employee> employees = employeeService.getAccessibleEmployees(currentUser);
        long totalEmployees = employees.size();
        Set<Long> activeEmployeesIds = new HashSet<>();

        long activeProjects;
        if (currentUser.getRole() == User.Role.SYSTEM_ADMIN || currentUser.getRole() == User.Role.EXECUTIVE) {
            activeProjects = projectRepository.countActiveProjects();
        } else {
            activeProjects = projectRepository.countActiveProjectsByEmployees(employees);
        }

        List<Allocation> allocations;
        if (currentUser.getRole() == User.Role.SYSTEM_ADMIN || currentUser.getRole() == User.Role.EXECUTIVE) {
            allocations = allocationRepository.findAllWithEmployeeAndProject();
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
                        if (Character.isDigit(alloc.charAt(0))) {
                            totalAllocation += Double.parseDouble(alloc);
                            allocationCount++;
                            activeEmployeesIds.add(allocation.getEmployee().getId());
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

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
                .employeeTrend(5.0)
                .allocationTrend(2.5)
                .benchTrend(-3.0)
                .projectTrend(10.0)
                .build();
    }
}
