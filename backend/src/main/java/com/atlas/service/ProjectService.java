package com.atlas.service;

import com.atlas.dto.ProjectDTO;
import com.atlas.entity.Allocation;
import com.atlas.entity.Employee;
import com.atlas.entity.Project;
import com.atlas.entity.User;
import com.atlas.repository.AllocationRepository;
import com.atlas.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
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

    public List<ProjectDTO> getAllProjects(User currentUser) {
        List<Project> projects = getFilteredProjects(currentUser);
        Map<Long, List<Allocation>> allocationsByProject = batchFetchAllocations(projects);
        return projects.stream()
                .map(p -> toDTO(p, allocationsByProject.getOrDefault(p.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
    }

    // Paginated version with search and filters - uses database-level pagination
    public org.springframework.data.domain.Page<ProjectDTO> getAllProjects(User currentUser,
            org.springframework.data.domain.Pageable pageable, String search, String tower, String status) {

        Project.ProjectStatus statusEnum = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                statusEnum = Project.ProjectStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid status, ignore filter
            }
        }

        String searchParam = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        String towerParam = (tower != null && !tower.trim().isEmpty()) ? tower.trim() : null;

        if (currentUser.getRole() == User.Role.SYSTEM_ADMIN || currentUser.getRole() == User.Role.EXECUTIVE) {
            // DATABASE-LEVEL pagination for admins with all filters
            org.springframework.data.domain.Page<Project> projectPage = projectRepository.searchProjects(
                    searchParam, towerParam, statusEnum, pageable);
            Map<Long, List<Allocation>> allocationsByProject = batchFetchAllocations(projectPage.getContent());
            return projectPage
                    .map(p -> toDTO(p, allocationsByProject.getOrDefault(p.getId(), Collections.emptyList())));
        }

        // For non-admin managers: get accessible project IDs first
        List<Project> accessibleProjects = getFilteredProjects(currentUser);
        if (accessibleProjects.isEmpty()) {
            return new org.springframework.data.domain.PageImpl<>(List.of(), pageable, 0);
        }

        List<Long> accessibleIds = accessibleProjects.stream()
                .map(Project::getId)
                .collect(Collectors.toList());

        // DATABASE-LEVEL pagination for non-admin managers with all filters
        org.springframework.data.domain.Page<Project> projectPage = projectRepository.searchProjectsByIds(
                accessibleIds, searchParam, towerParam, statusEnum, pageable);
        Map<Long, List<Allocation>> allocationsByProject = batchFetchAllocations(projectPage.getContent());
        return projectPage.map(p -> toDTO(p, allocationsByProject.getOrDefault(p.getId(), Collections.emptyList())));
    }

    public List<String> getDistinctTowers(Project.ProjectStatus status) {
        return projectRepository.findDistinctTowersByStatus(status);
    }

    public List<String> getDistinctStatuses(String tower) {
        return projectRepository.findDistinctStatusesByTower(tower).stream()
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
                .name(projectDTO.getName())
                .description(projectDTO.getDescription())
                .parentTower(projectDTO.getParentTower())
                .tower(projectDTO.getTower())
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

        project.setName(projectDTO.getName());
        project.setDescription(projectDTO.getDescription());
        project.setParentTower(projectDTO.getParentTower());
        project.setTower(projectDTO.getTower());
        project.setStartDate(projectDTO.getStartDate());
        project.setEndDate(projectDTO.getEndDate());
        project.setStatus(projectDTO.getStatus());

        project = projectRepository.save(project);
        return toDTO(project);
    }

    private List<Project> getFilteredProjects(User user) {
        if (user.getRole() == User.Role.SYSTEM_ADMIN || user.getRole() == User.Role.EXECUTIVE) {
            return projectRepository.findActiveProjects();
        }

        List<Employee> employees = employeeService.getAccessibleEmployees(user);
        if (employees.isEmpty()) {
            return List.of();
        }

        return projectRepository.findActiveProjectsByEmployees(employees);
    }

    private boolean hasAccessToProject(User user, Project project) {
        if (user.getRole() == User.Role.SYSTEM_ADMIN || user.getRole() == User.Role.EXECUTIVE) {
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
        return allocationRepository.findActiveByProjectIds(ids).stream()
                .collect(Collectors.groupingBy(a -> a.getProject().getId()));
    }

    private ProjectDTO toDTO(Project project) {
        List<Allocation> allocations = allocationRepository.findActiveByProjectId(project.getId());
        return toDTO(project, allocations);
    }

    private ProjectDTO toDTO(Project project, List<Allocation> allocations) {
        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();
        double totalAllocation = 0.0;
        int count = 0;

        for (Allocation allocation : allocations) {
            Double alloc = allocation.getAllocationForYearMonth(currentYear, currentMonth);
            if (alloc != null && alloc > 0) {
                totalAllocation += alloc;
                count++;
            }
        }

        double avgAllocation = count > 0 ? (totalAllocation / count) * 100 : 0.0;

        return ProjectDTO.builder()
                .id(project.getId())
                .projectId(project.getProjectId())
                .name(project.getName())
                .description(project.getDescription())
                .parentTower(project.getParentTower())
                .tower(project.getTower())
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
