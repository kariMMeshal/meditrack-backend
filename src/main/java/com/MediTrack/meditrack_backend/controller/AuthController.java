package com.MediTrack.meditrack_backend.controller;

import com.MediTrack.meditrack_backend.model.dto.auth.AuthResponse;
import com.MediTrack.meditrack_backend.model.dto.auth.LoginRequest;
import com.MediTrack.meditrack_backend.model.dto.auth.RegisterRequest;
import com.MediTrack.meditrack_backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
/**
 * Handles authentication endpoints such as register, login, and logout.
 */
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    /**
     * Creates a new user account and returns a JWT for the new user.
     */
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    /**
     * Authenticates a user and returns a JWT if the credentials are valid.
     */
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    /**
     * Returns a logout response for JWT-based clients.
     */
    public ResponseEntity<Map<String, String>> logout() {
        authService.logout();
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}
