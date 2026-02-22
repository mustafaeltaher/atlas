package com.atlas.service;

import com.atlas.dto.DashboardStatsDTO;
import com.atlas.entity.User;
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
    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;

    public DashboardStatsDTO getStats(User currentUser) {
        List<Long> accessibleIds = employeeService.getAccessibleEmployeeIds(currentUser);

        long totalEmployees;
        if (accessibleIds == null) {
            totalEmployees = employeeRepository.countActiveEmployees();
        } else {
            totalEmployees = accessibleIds.size();
        }

        long activeProjects;
        if (accessibleIds == null) {
            activeProjects = projectRepository.countActiveProjects();
        } else {
            activeProjects = projectRepository.countActiveProjectsByEmployeeIds(accessibleIds);
        }

        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();

        long benchCount = employeeRepository.countBenchEmployeesFiltered(accessibleIds, currentYear, currentMonth);
        long activeEmployeeCount = employeeRepository.countActiveAllocatedEmployeesFiltered(accessibleIds, currentYear,
                currentMonth);
        long prospectCount = employeeRepository.countProspectEmployeesFiltered(accessibleIds, currentYear,
                currentMonth);
        double avgAllocation = employeeRepository.averageAllocationPercentageFiltered(accessibleIds, currentYear,
                currentMonth);

        return DashboardStatsDTO.builder()
                .totalEmployees(totalEmployees)
                .activeEmployees(activeEmployeeCount)
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
