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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final EmployeeService employeeService;
    private final ProjectRepository projectRepository;
    private final AllocationRepository allocationRepository;

    public List<ProjectDTO> getAllProjects(User currentUser) {
        List<Project> projects = getFilteredProjects(currentUser);
        return projects.stream()
                .map(this::toDTO)
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

    private ProjectDTO toDTO(Project project) {
        List<Allocation> allocations = allocationRepository.findActiveByProjectId(project.getId());

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
