package com.atlas.controller;

import com.atlas.dto.DashboardStatsDTO;
import com.atlas.entity.User;
import com.atlas.security.CustomUserDetailsService;
import com.atlas.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final CustomUserDetailsService userDetailsService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getStats(Authentication authentication) {
        User currentUser = userDetailsService.getUserByUsername(authentication.getName());
        return ResponseEntity.ok(dashboardService.getStats(currentUser));
    }
}
