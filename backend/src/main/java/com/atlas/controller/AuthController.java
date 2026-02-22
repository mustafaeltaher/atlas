package com.atlas.controller;

import com.atlas.dto.ChangePasswordRequest;
import com.atlas.dto.LoginRequest;
import com.atlas.dto.LoginResponse;
import com.atlas.entity.User;
import com.atlas.repository.UserRepository;
import com.atlas.security.CustomUserDetailsService;
import com.atlas.security.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.atlas.service.DelegateService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final DelegateService delegateService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);

        User user = userDetailsService.getUserByUsername(loginRequest.getUsername());

        LoginResponse response = LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .isTopLevel(user.isTopLevel())
                .employeeName(user.getEmployee().getName())
                .employeeId(user.getEmployee().getId())
                .isImpersonating(false)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/impersonate")
    public ResponseEntity<? extends Object> impersonate(@Valid @RequestBody com.atlas.dto.ImpersonateRequest request) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        String targetUsername = request.getTargetUsername();

        // 1. Check if I have permission to impersonate this user
        // Exception: Super Admin might bypass this (optional), but for now we stick to
        // delegation
        if (!delegateService.canImpersonate(currentUsername, targetUsername)) {
            // Fallback: Check if user is Super Admin?
            // For now, let's enforce delegation strictly as per design
            return ResponseEntity.status(403)
                    .body(Map.of("message", "You do not have permission to impersonate this user."));
        }

        // 2. Generate Impersonation Token
        String token = tokenProvider.generateImpersonationToken(targetUsername, currentUsername);

        // 3. Build Response (similar to login)
        User targetUser = userDetailsService.getUserByUsername(targetUsername);

        LoginResponse response = LoginResponse.builder()
                .token(token)
                .username(targetUser.getUsername())
                .email(targetUser.getEmail())
                .isTopLevel(targetUser.isTopLevel())
                .employeeName(targetUser.getEmployee().getName())
                .employeeId(targetUser.getEmployee().getId())
                .isImpersonating(true)
                .impersonatorUsername(currentUsername)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<LoginResponse> getCurrentUser(
            @RequestHeader(value = "Authorization", required = false) String bearerToken) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userDetailsService.getUserByUsername(username);

        boolean isImpersonating = false;
        String impersonatorUsername = null;

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            impersonatorUsername = tokenProvider.getImpersonatorFromToken(token);
            isImpersonating = impersonatorUsername != null;
        }

        LoginResponse response = LoginResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .isTopLevel(user.isTopLevel())
                .employeeName(user.getEmployee().getName())
                .employeeId(user.getEmployee().getId())
                .isImpersonating(isImpersonating)
                .impersonatorUsername(impersonatorUsername)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userDetailsService.getUserByUsername(username);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Current password is incorrect"));
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }
}
