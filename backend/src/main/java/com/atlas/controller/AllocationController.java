package com.atlas.controller;

import com.atlas.dto.AllocationDTO;
import com.atlas.dto.EmployeeAllocationSummaryDTO;
import com.atlas.entity.User;
import com.atlas.security.CustomUserDetailsService;
import com.atlas.service.AllocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/allocations")
@RequiredArgsConstructor
public class AllocationController {

    private static final int MAX_PAGE_SIZE = 100;

    private final AllocationService allocationService;
    private final CustomUserDetailsService userDetailsService;

    @GetMapping
    public ResponseEntity<org.springframework.data.domain.Page<AllocationDTO>> getAllAllocations(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String allocationType,
            @RequestParam(required = false) Long managerId) {
        page = Math.max(0, page);
        size = Math.max(1, Math.min(size, MAX_PAGE_SIZE));
        User currentUser = userDetailsService.getUserByUsername(authentication.getName());
        return ResponseEntity.ok(allocationService.getAllAllocations(currentUser,
                org.springframework.data.domain.PageRequest.of(page, size), search, allocationType, managerId));
    }

    @GetMapping("/grouped")
    public ResponseEntity<Page<EmployeeAllocationSummaryDTO>> getGroupedAllocations(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String allocationType,
            @RequestParam(required = false) Long managerId) {
        page = Math.max(0, page);
        size = Math.max(1, Math.min(size, MAX_PAGE_SIZE));
        User currentUser = userDetailsService.getUserByUsername(authentication.getName());
        return ResponseEntity.ok(allocationService.getGroupedAllocations(currentUser,
                PageRequest.of(page, size), search, allocationType, managerId));
    }

    @GetMapping("/managers")
    public ResponseEntity<List<Map<String, Object>>> getAllocationManagers(
            Authentication authentication,
            @RequestParam(required = false) String allocationType) {
        User currentUser = userDetailsService.getUserByUsername(authentication.getName());
        return ResponseEntity.ok(allocationService.getManagersForAllocations(currentUser, allocationType));
    }

    @GetMapping("/allocation-types")
    public ResponseEntity<List<String>> getAllocationTypes(@RequestParam(required = false) Long managerId) {
        return ResponseEntity.ok(allocationService.getDistinctAllocationTypes(managerId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AllocationDTO> getAllocationById(@PathVariable Long id, Authentication authentication) {
        User currentUser = userDetailsService.getUserByUsername(authentication.getName());
        return ResponseEntity.ok(allocationService.getAllocationById(id, currentUser));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<AllocationDTO>> getAllocationsByEmployee(@PathVariable Long employeeId,
            Authentication authentication) {
        User currentUser = userDetailsService.getUserByUsername(authentication.getName());
        return ResponseEntity.ok(allocationService.getAllocationsByEmployee(employeeId, currentUser));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<AllocationDTO>> getAllocationsByProject(@PathVariable Long projectId,
            Authentication authentication) {
        User currentUser = userDetailsService.getUserByUsername(authentication.getName());
        return ResponseEntity.ok(allocationService.getAllocationsByProject(projectId, currentUser));
    }

    @PostMapping
    public ResponseEntity<AllocationDTO> createAllocation(@Valid @RequestBody AllocationDTO allocationDTO) {
        return ResponseEntity.ok(allocationService.createAllocation(allocationDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AllocationDTO> updateAllocation(@PathVariable Long id,
            @RequestBody AllocationDTO allocationDTO) {
        return ResponseEntity.ok(allocationService.updateAllocation(id, allocationDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAllocation(@PathVariable Long id) {
        allocationService.deleteAllocation(id);
        return ResponseEntity.ok().build();
    }
}
