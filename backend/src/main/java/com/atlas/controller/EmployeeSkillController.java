package com.atlas.controller;

import com.atlas.dto.AddSkillRequest;
import com.atlas.dto.EmployeeSkillDTO;
import com.atlas.dto.SkillDTO;
import com.atlas.entity.User;
import com.atlas.security.CustomUserDetailsService;
import com.atlas.service.EmployeeSkillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * REST controller for managing employee skill assignments.
 * Implements ABAC (Attribute-Based Access Control) via manager hierarchy.
 */
@RestController
@RequestMapping("/api/employees/{employeeId}/skills")
@RequiredArgsConstructor
public class EmployeeSkillController {

    private final EmployeeSkillService employeeSkillService;
    private final CustomUserDetailsService userDetailsService;

    /**
     * GET /api/employees/{employeeId}/skills
     * Get all skills currently assigned to an employee.
     *
     * @param employeeId the employee ID
     * @param authentication the authenticated user
     * @return 200 OK with list of assigned skills
     * @throws RuntimeException 403 Forbidden if access denied, 404 Not Found if employee not found
     */
    @GetMapping
    public ResponseEntity<List<EmployeeSkillDTO>> getEmployeeSkills(
            @PathVariable Long employeeId,
            Authentication authentication) {
        User currentUser = userDetailsService.getUserByUsername(authentication.getName());
        List<EmployeeSkillDTO> skills = employeeSkillService.getEmployeeSkills(employeeId, currentUser);
        return ResponseEntity.ok(skills);
    }

    /**
     * GET /api/employees/{employeeId}/skills/available
     * Get all skills that are available to add to an employee (not currently assigned).
     *
     * @param employeeId the employee ID
     * @param authentication the authenticated user
     * @return 200 OK with list of available skills
     * @throws RuntimeException 403 Forbidden if access denied, 404 Not Found if employee not found
     */
    @GetMapping("/available")
    public ResponseEntity<List<SkillDTO>> getAvailableSkills(
            @PathVariable Long employeeId,
            Authentication authentication) {
        User currentUser = userDetailsService.getUserByUsername(authentication.getName());
        List<SkillDTO> skills = employeeSkillService.getAvailableSkills(employeeId, currentUser);
        return ResponseEntity.ok(skills);
    }

    /**
     * POST /api/employees/{employeeId}/skills
     * Add a new skill to an employee with specified level and grade.
     *
     * @param employeeId the employee ID
     * @param request the skill assignment details (skillId, skillLevel, skillGrade)
     * @param authentication the authenticated user
     * @return 201 Created with the created skill assignment
     * @throws RuntimeException 400 Bad Request if duplicate skill,
     *                          403 Forbidden if access denied,
     *                          404 Not Found if employee or skill not found
     */
    @PostMapping
    public ResponseEntity<EmployeeSkillDTO> addSkillToEmployee(
            @PathVariable Long employeeId,
            @Valid @RequestBody AddSkillRequest request,
            Authentication authentication) {
        User currentUser = userDetailsService.getUserByUsername(authentication.getName());
        EmployeeSkillDTO created = employeeSkillService.addSkillToEmployee(employeeId, request, currentUser);

        // Return 201 Created with Location header
        URI location = URI.create("/api/employees/" + employeeId + "/skills/" + created.getSkillId());
        return ResponseEntity.created(location).body(created);
    }

    /**
     * DELETE /api/employees/{employeeId}/skills/{skillId}
     * Remove a skill from an employee.
     *
     * @param employeeId the employee ID
     * @param skillId the skill ID to remove
     * @param authentication the authenticated user
     * @return 204 No Content on success
     * @throws RuntimeException 403 Forbidden if access denied,
     *                          404 Not Found if employee or skill association not found
     */
    @DeleteMapping("/{skillId}")
    public ResponseEntity<Void> removeSkillFromEmployee(
            @PathVariable Long employeeId,
            @PathVariable Integer skillId,
            Authentication authentication) {
        User currentUser = userDetailsService.getUserByUsername(authentication.getName());
        employeeSkillService.removeSkillFromEmployee(employeeId, skillId, currentUser);
        return ResponseEntity.noContent().build();
    }
}
