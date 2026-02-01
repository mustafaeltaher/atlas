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

    // Paginated version with search and filters
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
            // Direct database pagination for admins
            org.springframework.data.domain.Page<Project> projectPage = projectRepository.searchProjects(
                    searchParam, towerParam, statusEnum, pageable);
            Map<Long, List<Allocation>> allocationsByProject = batchFetchAllocations(projectPage.getContent());
            return projectPage.map(p -> toDTO(p, allocationsByProject.getOrDefault(p.getId(), Collections.emptyList())));
        }

        // For managers: fetch all accessible projects, then filter and paginate
        List<Project> allAccessible = getFilteredProjects(currentUser);

        // Apply search and filters
        final String searchLower = searchParam != null ? searchParam.toLowerCase() : null;
        final String towerFinal = towerParam;
        final Project.ProjectStatus statusFinal = statusEnum;

        allAccessible = allAccessible.stream()
                .filter(p -> searchLower == null ||
                        (p.getName() != null && p.getName().toLowerCase().contains(searchLower)))
                .filter(p -> towerFinal == null ||
                        (p.getTower() != null && p.getTower().equals(towerFinal)))
                .filter(p -> statusFinal == null || p.getStatus() == statusFinal)
                .collect(Collectors.toList());

        // Manual pagination from the filtered list
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allAccessible.size());
        List<Project> pageProjects = start < allAccessible.size() ? allAccessible.subList(start, end) : List.of();

        Map<Long, List<Allocation>> allocationsByProject = batchFetchAllocations(pageProjects);
        List<ProjectDTO> pageContent = pageProjects.stream()
                .map(p -> toDTO(p, allocationsByProject.getOrDefault(p.getId(), Collections.emptyList())))
                .collect(Collectors.toList());

        return new org.springframework.data.domain.PageImpl<>(
                pageContent,
                pageable,
                allAccessible.size());
    }

    public List<String> getDistinctTowers() {
        return projectRepository.findDistinctTowers();
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
        int currentMonth = LocalDate.now().getMonthValue();
        double totalAllocation = 0.0;
        int count = 0;

        for (Allocation allocation : allocations) {
            String alloc = allocation.getAllocationForMonth(currentMonth);
            if (alloc != null && Character.isDigit(alloc.charAt(0))) {
                try {
                    totalAllocation += Double.parseDouble(alloc);
                    count++;
                } catch (NumberFormatException ignored) {
                }
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
