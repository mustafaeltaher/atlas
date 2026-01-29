package com.atlas.controller;

import com.atlas.dto.EmployeeDTO;
import com.atlas.entity.User;
import com.atlas.security.CustomUserDetailsService;
import com.atlas.service.EmployeeService;
import com.atlas.service.ExcelImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final ExcelImportService excelImportService;
    private final CustomUserDetailsService userDetailsService;

    @GetMapping
    public ResponseEntity<org.springframework.data.domain.Page<EmployeeDTO>> getAllEmployees(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        User currentUser = userDetailsService.getUserByUsername(authentication.getName());
        return ResponseEntity.ok(employeeService.getAllEmployees(currentUser,
                org.springframework.data.domain.PageRequest.of(page, size), search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> getEmployeeById(@PathVariable Long id, Authentication authentication) {
        User currentUser = userDetailsService.getUserByUsername(authentication.getName());
        return ResponseEntity.ok(employeeService.getEmployeeById(id, currentUser));
    }

    @GetMapping("/towers")
    public ResponseEntity<Map<String, List<String>>> getTowers() {
        return ResponseEntity.ok(Map.of(
                "parentTowers", employeeService.getDistinctParentTowers(),
                "towers", employeeService.getDistinctTowers()));
    }

    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importEmployees(@RequestParam("file") MultipartFile file) {
        try {
            int imported = excelImportService.importEmployees(file);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "imported", imported,
                    "message", "Successfully imported " + imported + " employees"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Import failed: " + e.getMessage()));
        }
    }
}
