package com.atlas.controller;

import com.atlas.dto.DelegateRequest;
import com.atlas.dto.DelegateResponse;
import com.atlas.service.DelegateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delegates")
@RequiredArgsConstructor
public class DelegateController {

    private final DelegateService delegateService;

    // Get list of users I have delegated access to (I am the delegator)
    @GetMapping("/my-delegates")
    public ResponseEntity<List<DelegateResponse>> getMyDelegates() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(delegateService.getMyDelegates(username));
    }

    // Get list of accounts I can impersonate (I am the delegate)
    @GetMapping("/available-accounts")
    public ResponseEntity<List<DelegateResponse>> getAvailableAccounts() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(delegateService.getAvailableAccounts(username));
    }

    // Grant access to someone
    @PostMapping
    public ResponseEntity<DelegateResponse> grantAccess(@Valid @RequestBody DelegateRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(delegateService.grantAccess(username, request));
    }

    @GetMapping("/potential")
    public ResponseEntity<List<com.atlas.dto.EmployeeDTO>> getPotentialDelegates(
            @RequestParam(required = false) String search) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(delegateService.getPotentialDelegates(username, search));
    }

    // Revoke access
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revokeAccess(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        delegateService.revokeAccess(username, id);
        return ResponseEntity.ok().build();
    }
}
