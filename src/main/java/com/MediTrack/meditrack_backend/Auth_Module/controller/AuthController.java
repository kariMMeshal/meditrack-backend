package com.MediTrack.meditrack_backend.Auth_Module.controller;

import com.MediTrack.meditrack_backend.Auth_Module.dto.*;
import com.MediTrack.meditrack_backend.Auth_Module.service.AuthService;
import com.MediTrack.meditrack_backend.Auth_Module.service.RefreshTokenService;
import com.MediTrack.meditrack_backend.security.LockOutService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request ,  HttpServletResponse response) {
        AuthResult result = authService.register(request);
        setRefreshTokenCookie(response, result.refreshToken().getToken());
        System.out.println("Registered user: " + result.response());
        return ResponseEntity.ok(result.response());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest, HttpServletResponse response) {
        String ip = extractClientIp(httpRequest);

        if (lockOutService.isLocked(request.getUsername())) {
            long secondsLeft = lockOutService.remainingLockoutSeconds(request.getUsername());
            return ResponseEntity.status(429).body(Map.of(
                    "status", 429,
                    "error", "Too Many Requests",
                    "message", "Account temporarily locked. Try again in " + secondsLeft + " seconds."
            ));
        }
        AuthResult result = authService.login(request, ip, httpRequest.getHeader("User-Agent"));
        setRefreshTokenCookie(response, result.refreshToken().getToken());
        System.out.println("Logged in user: " + result.response());
        return ResponseEntity.ok(result.response());
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            HttpServletRequest request,
            HttpServletResponse response) {

        String refreshToken = extractRefreshTokenFromCookie(request);

        AuthResult result = authService.refresh(
                refreshToken,
                extractClientIp(request)
        );

        setRefreshTokenCookie(response, result.refreshToken().getToken());

        return ResponseEntity.ok(result.response());
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestBody(required = false) LogoutRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        String accessToken = extractAccessToken(httpRequest, request);

        authService.logout(
                accessToken,
                authentication.getName(),
                extractClientIp(httpRequest),
                httpRequest.getHeader("User-Agent")
        );

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

    // ---------------- COOKIE HELPERS ----------------

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true) // production only
                .path("/api/auth/refresh")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/api/auth/refresh")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            throw new RuntimeException("No cookies found");
        }

        for (Cookie cookie : request.getCookies()) {
            if ("refreshToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        throw new RuntimeException("Refresh token cookie not found");
    }

    private String extractAccessToken(HttpServletRequest request,
                                      LogoutRequest body) {

        // 1. Try Authorization header first (Bearer)
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // 2. Fallback to body
        if (body != null && body.getAccessToken() != null) {
            return body.getAccessToken();
        }

        throw new RuntimeException("Access token not provided");
    }
}