package com.atlas.service;

import com.atlas.dto.ProjectDTO;
import com.atlas.entity.Allocation;
import com.atlas.entity.Employee;
import com.atlas.entity.MonthlyAllocation;
import com.atlas.entity.Project;
import com.atlas.entity.User;
import com.atlas.repository.AllocationRepository;
import com.atlas.repository.MonthlyAllocationRepository;
import com.atlas.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final EmployeeService employeeService;
    private final ProjectRepository projectRepository;
    private final AllocationRepository allocationRepository;
    private final MonthlyAllocationRepository monthlyAllocationRepository;

    public List<ProjectDTO> getAllProjects(User currentUser) {
        List<Project> projects = getFilteredProjects(currentUser);
        Map<Long, List<Allocation>> allocationsByProject = batchFetchAllocations(projects);
        return projects.stream()
                .map(p -> toDTO(p, allocationsByProject.getOrDefault(p.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
    }

    // Paginated version with search and filters - uses database-level pagination
    public Page<ProjectDTO> getAllProjects(User currentUser,
            Pageable pageable, String search, String region, String status) {

        Project.ProjectStatus statusEnum = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                statusEnum = Project.ProjectStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid status, ignore filter
            }
        }

        String regionParam = (region != null && !region.trim().isEmpty()) ? region.trim() : null;

        List<Long> projectIds = getFilteredProjectIds(currentUser);
        if (projectIds != null && projectIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        org.springframework.data.jpa.domain.Specification<Project> spec = com.atlas.specification.ProjectSpecification
                .withFilters(statusEnum, regionParam, search, projectIds);

        Page<Project> projectPage = projectRepository.findAll(spec, pageable);
        Map<Long, List<Allocation>> allocationsByProject = batchFetchAllocations(projectPage.getContent());
        return projectPage.map(p -> toDTO(p, allocationsByProject.getOrDefault(p.getId(), Collections.emptyList())));
    }

    public List<String> getDistinctRegions(Project.ProjectStatus status, String search, User user) {
        List<Long> filteredIds = getFilteredProjectIds(user);

        // Use custom repository method for DB-level distinct region selection
        // This avoids in-memory distinct and sorting operations on large result sets
        return projectRepository.findDistinctRegionsByProjectSpec(status, search, filteredIds);
    }

    public List<String> getDistinctStatuses(String region, String search, User user) {
        String searchParam = (search != null && !search.trim().isEmpty())
                ? "%" + search.trim().toLowerCase() + "%"
                : null;
        List<Long> filteredIds = getFilteredProjectIds(user);

        List<Project.ProjectStatus> statuses;
        if (filteredIds == null) {
            statuses = projectRepository.findDistinctStatuses(region, searchParam);
        } else {
            statuses = projectRepository.findDistinctStatusesByIds(filteredIds, region, searchParam);
        }

        return statuses.stream()
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    public ProjectDTO getProjectById(Long id, User currentUser) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found: " + id));

        if (!hasAccessToProject(currentUser, project)) {
            throw new RuntimeException("Access denied to project: " + id);
        }

        return toDTO(project);
    }

    @Transactional
    public ProjectDTO createProject(ProjectDTO projectDTO) {
        if (projectRepository.existsByProjectId(projectDTO.getProjectId())) {
            throw new RuntimeException("Project ID already exists: " + projectDTO.getProjectId());
        }

        Project project = Project.builder()
                .projectId(projectDTO.getProjectId())
                .description(projectDTO.getDescription())
                .projectType(projectDTO.getProjectType() != null ? projectDTO.getProjectType()
                        : Project.ProjectType.PROJECT)
                .region(projectDTO.getRegion())
                .vertical(projectDTO.getVertical())
                .startDate(projectDTO.getStartDate())
                .endDate(projectDTO.getEndDate())
                .status(projectDTO.getStatus() != null ? projectDTO.getStatus() : Project.ProjectStatus.ACTIVE)
                .build();

        project = projectRepository.save(project);
        return toDTO(project);
    }

    @Transactional
    public ProjectDTO updateProject(Long id, ProjectDTO projectDTO) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found: " + id));

        // Only update editable fields: status, startDate, endDate
        if (projectDTO.getStatus() != null) {
            project.setStatus(projectDTO.getStatus());
        }
        if (projectDTO.getStartDate() != null) {
            project.setStartDate(projectDTO.getStartDate());
        }
        if (projectDTO.getEndDate() != null) {
            project.setEndDate(projectDTO.getEndDate());
        }

        project = projectRepository.save(project);
        return toDTO(project);
    }

    /**
     * Returns accessible project IDs for access control filtering.
     * Returns null for top-level users (no filter), or List of IDs for
     * non-top-level.
     */
    private List<Long> getFilteredProjectIds(User user) {
        if (user.isTopLevel()) {
            return null;
        }
        List<Employee> employees = employeeService.getAccessibleEmployees(user);
        if (employees.isEmpty()) {
            return List.of();
        }
        return projectRepository.findProjectsByEmployees(employees).stream()
                .map(Project::getId).collect(Collectors.toList());
    }

    private List<Project> getFilteredProjects(User user) {
        if (user.isTopLevel()) {
            return projectRepository.findActiveProjects();
        }
        List<Employee> employees = employeeService.getAccessibleEmployees(user);
        if (employees.isEmpty()) {
            return List.of();
        }
        return projectRepository.findProjectsByEmployees(employees);
    }

    private boolean hasAccessToProject(User user, Project project) {
        if (user.isTopLevel()) {
            return true;
        }

        List<Project> accessibleProjects = getFilteredProjects(user);
        return accessibleProjects.stream().anyMatch(p -> p.getId().equals(project.getId()));
    }

    private Map<Long, List<Allocation>> batchFetchAllocations(List<Project> projects) {
        if (projects.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> ids = projects.stream().map(Project::getId).collect(Collectors.toList());
        return allocationRepository.findProjectAllocationsByProjectIds(ids).stream()
                .collect(Collectors.groupingBy(a -> a.getProject().getId()));
    }

    private ProjectDTO toDTO(Project project) {
        List<Allocation> allocations = allocationRepository.findProjectAllocationsByProjectId(project.getId());
        return toDTO(project, allocations);
    }

    private ProjectDTO toDTO(Project project, List<Allocation> allocations) {
        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();

        // Batch-fetch monthly allocations for current month (database-level filtering)
        // This avoids N lazy loads and in-memory filtering
        List<Long> allocationIds = allocations.stream()
                .map(Allocation::getId)
                .collect(Collectors.toList());

        Map<Long, Integer> currentMonthAllocations = Collections.emptyMap();
        if (!allocationIds.isEmpty()) {
            currentMonthAllocations = monthlyAllocationRepository
                    .findByAllocationIdsAndYearAndMonth(allocationIds, currentYear, currentMonth)
                    .stream()
                    .collect(Collectors.toMap(
                            ma -> ma.getAllocation().getId(),
                            MonthlyAllocation::getPercentage,
                            (a, b) -> a));
        }

        // Calculate average allocation across employees with >0% allocation
        int totalAllocation = 0;
        int count = 0;

        for (Allocation allocation : allocations) {
            Integer alloc = currentMonthAllocations.get(allocation.getId());
            if (alloc != null && alloc > 0) {
                totalAllocation += alloc;
                count++;
            }
        }

        double avgAllocation = count > 0 ? (double) totalAllocation / count : 0.0;

        return ProjectDTO.builder()
                .id(project.getId())
                .projectId(project.getProjectId())
                .description(project.getDescription())
                .projectType(project.getProjectType())
                .region(project.getRegion())
                .vertical(project.getVertical())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .status(project.getStatus())
                .managerId(project.getManager() != null ? project.getManager().getId() : null)
                .managerName(project.getManager() != null ? project.getManager().getName() : null)
                .allocatedEmployees(allocations.size())
                .averageAllocation(avgAllocation)
                .build();
    }

    public long countActiveProjects() {
        return projectRepository.countActiveProjects();
    }
}
