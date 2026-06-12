package com.MediTrack.meditrack_backend.Auth_Module.controller;

import com.MediTrack.meditrack_backend.Auth_Module.dto.*;
import com.MediTrack.meditrack_backend.Auth_Module.service.AuthService;
import com.MediTrack.meditrack_backend.security.LockOutService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final LockOutService lockOutService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        String ip = extractClientIp(httpRequest);

        if (lockOutService.isLocked(request.getUsername())) {
            long secondsLeft = lockOutService.remainingLockoutSeconds(request.getUsername());
            return ResponseEntity.status(429).body(Map.of(
                    "status", 429,
                    "error", "Too Many Requests",
                    "message", "Account temporarily locked. Try again in " + secondsLeft + " seconds."
            ));
        }

        return ResponseEntity.ok(authService.login(request, ip, httpRequest.getHeader("User-Agent")));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody RefreshRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authService.refresh(
                request.getRefreshToken(),
                extractClientIp(httpRequest)));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @Valid @RequestBody LogoutRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        authService.logout(
                request.getAccessToken(),
                authentication.getName(),
                extractClientIp(httpRequest),
                httpRequest.getHeader("User-Agent"));
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    /**
     * Extracts real client IP — handles reverse proxy and load balancer headers.
     */
    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}