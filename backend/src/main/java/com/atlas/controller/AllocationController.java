package com.atlas.controller;

import com.atlas.dto.AllocationDTO;
import com.atlas.entity.User;
import com.atlas.security.CustomUserDetailsService;
import com.atlas.service.AllocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/allocations")
@RequiredArgsConstructor
public class AllocationController {

    private final AllocationService allocationService;
    private final CustomUserDetailsService userDetailsService;

    @GetMapping
    public ResponseEntity<List<AllocationDTO>> getAllAllocations(Authentication authentication) {
        User currentUser = userDetailsService.getUserByUsername(authentication.getName());
        return ResponseEntity.ok(allocationService.getAllAllocations(currentUser));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AllocationDTO> getAllocationById(@PathVariable Long id) {
        return ResponseEntity.ok(allocationService.getAllocationById(id));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<AllocationDTO>> getAllocationsByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(allocationService.getAllocationsByEmployee(employeeId));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<AllocationDTO>> getAllocationsByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(allocationService.getAllocationsByProject(projectId));
    }

    @PostMapping
    public ResponseEntity<AllocationDTO> createAllocation(@RequestBody AllocationDTO allocationDTO) {
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
