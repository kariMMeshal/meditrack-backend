package com.MediTrack.meditrack_backend.Auth_Module.service;

import com.MediTrack.meditrack_backend.Auth_Module.dto.AuthResponse;
import com.MediTrack.meditrack_backend.Auth_Module.dto.LoginRequest;
import com.MediTrack.meditrack_backend.Auth_Module.dto.RegisterRequest;
import com.MediTrack.meditrack_backend.Auth_Module.entity.RefreshToken;
import com.MediTrack.meditrack_backend.Auth_Module.entity.Role;
import com.MediTrack.meditrack_backend.Auth_Module.entity.User;
import com.MediTrack.meditrack_backend.Auth_Module.repository.RoleRepository;
import com.MediTrack.meditrack_backend.Auth_Module.repository.UserRepository;
import com.MediTrack.meditrack_backend.security.JwtService;
import com.MediTrack.meditrack_backend.security.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final LoginAttemptService loginAttemptService;
    private final AuditLogService auditLogService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Use generic messages — never confirm which field is taken
        if (userRepository.existsByUsername(request.getUsername()) ||
                userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Registration failed — please check your details");
        }

        Role defaultRole = roleRepository.findByRole("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .deleted(false)
                .roles(new HashSet<>(Set.of(defaultRole)))
                .build();

        User saved = userRepository.save(user);
        log.info("New user registered: {}", saved.getUsername());

        UserDetails userDetails = buildUserDetails(saved);
        String accessToken = jwtService.generateToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(saved);

        return buildAuthResponse(saved, accessToken, refreshToken.getToken());
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, String ip, String userAgent) {
        // Find user first — needed for lockout check before Spring Security authenticates
        User user = userRepository.findByUsername(request.getUsername()).orElse(null);

        // Check lockout BEFORE attempting authentication
        if (user != null && loginAttemptService.isLocked(user)) {
            auditLogService.loginFailure(request.getUsername(), ip, userAgent, "Account locked");
            // Generic error — don't reveal lockout status to potential attackers
            throw new BadCredentialsException("Authentication failed");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (AuthenticationException ex) {
            // Record failure — may trigger lockout
            if (user != null) {
                loginAttemptService.recordFailure(user, ip);
            }
            auditLogService.loginFailure(request.getUsername(), ip, userAgent, ex.getMessage());
            // Generic message — prevents user enumeration
            throw new BadCredentialsException("Authentication failed");
        }

        // Reload user after successful auth — state may have changed
        user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isDeleted() || !user.isEnabled()) {
            throw new BadCredentialsException("Authentication failed");
        }

        // Reset lockout counter on successful login
        loginAttemptService.recordSuccess(user);

        UserDetails userDetails = buildUserDetails(user);
        String accessToken = jwtService.generateToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        auditLogService.loginSuccess(user.getUsername(), ip, userAgent);
        log.info("Login successful — username={}, ip={}", user.getUsername(), ip);

        return buildAuthResponse(user, accessToken, refreshToken.getToken());
    }

    @Override
    @Transactional
    public AuthResponse refresh(String rawToken, String ip) {
        // Validate old token — throws if revoked or expired
        RefreshToken oldToken = refreshTokenService.validateRefreshToken(rawToken);
        User user = oldToken.getUser();

        // Rotate: revoke old, issue new refresh token
        refreshTokenService.revokeAllForUser(user);
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

        UserDetails userDetails = buildUserDetails(user);
        String newAccessToken = jwtService.generateToken(userDetails);

        auditLogService.tokenRefreshed(user.getUsername(), ip);
        log.info("Token rotated — username={}", user.getUsername());

        return buildAuthResponse(user, newAccessToken, newRefreshToken.getToken());
    }

    @Override
    @Transactional
    public void logout(String accessToken, String username, String ip, String userAgent) {
        // Revoke the specific refresh token — real server-side logout
        userRepository.findByUsername(username).ifPresent(user -> {
            refreshTokenService.revokeAllForUser(user);
            log.info("Logout — all refresh tokens revoked for user={}", username);
        });
        auditLogService.logout(username, ip, userAgent);
        long remainingTtlSeconds = jwtService.getRemainingExpirationSeconds(accessToken);

        tokenBlacklistService.blacklist(accessToken, remainingTtlSeconds);
        tokenBlacklistService.verifyBlackListed(accessToken,remainingTtlSeconds);
    }

    // ── Private Helpers ───────────────────────────────────────────────

    private UserDetails buildUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRoles().stream()
                        .map(Role::getRole).toArray(String[]::new))
                .disabled(!user.isEnabled())
                .build();
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresInMs(jwtService.getJwtExpirationMs())
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles().stream()
                        .map(Role::getRole).collect(Collectors.toSet()))
                .build();
    }
}