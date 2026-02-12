package com.atlas.controller;

import com.atlas.dto.ProjectDTO;
import com.atlas.entity.User;
import com.atlas.security.CustomUserDetailsService;
import com.atlas.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private static final int MAX_PAGE_SIZE = 100;

    private final ProjectService projectService;
    private final CustomUserDetailsService userDetailsService;

    @GetMapping
    public ResponseEntity<org.springframework.data.domain.Page<ProjectDTO>> getAllProjects(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String status) {
        page = Math.max(0, page);
        size = Math.max(1, Math.min(size, MAX_PAGE_SIZE));
        User currentUser = userDetailsService.getUserByUsername(authentication.getName());
        return ResponseEntity.ok(projectService.getAllProjects(currentUser,
                org.springframework.data.domain.PageRequest.of(page, size), search, region, status));
    }

    @GetMapping("/regions")
    public ResponseEntity<java.util.List<String>> getRegions(@RequestParam(required = false) String status) {
        com.atlas.entity.Project.ProjectStatus projectStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                projectStatus = com.atlas.entity.Project.ProjectStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                // ignore invalid status
            }
        }
        return ResponseEntity.ok(projectService.getDistinctRegions(projectStatus));
    }

    @GetMapping("/statuses")
    public ResponseEntity<java.util.List<String>> getStatuses(@RequestParam(required = false) String region) {
        return ResponseEntity.ok(projectService.getDistinctStatuses(region));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDTO> getProjectById(@PathVariable Long id, Authentication authentication) {
        User currentUser = userDetailsService.getUserByUsername(authentication.getName());
        return ResponseEntity.ok(projectService.getProjectById(id, currentUser));
    }

    @PostMapping
    public ResponseEntity<ProjectDTO> createProject(@Valid @RequestBody ProjectDTO projectDTO) {
        return ResponseEntity.ok(projectService.createProject(projectDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectDTO> updateProject(@PathVariable Long id, @Valid @RequestBody ProjectDTO projectDTO) {
        return ResponseEntity.ok(projectService.updateProject(id, projectDTO));
    }
}
