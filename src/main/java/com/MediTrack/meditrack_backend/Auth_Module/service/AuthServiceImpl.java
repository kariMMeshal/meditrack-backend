package com.MediTrack.meditrack_backend.Auth_Module.service;

import com.MediTrack.meditrack_backend.Alerts_Module.service.AlertGenerator;
import com.MediTrack.meditrack_backend.Auth_Module.dto.AuthResponse;
import com.MediTrack.meditrack_backend.Auth_Module.dto.AuthResult;
import com.MediTrack.meditrack_backend.Auth_Module.dto.LoginRequest;
import com.MediTrack.meditrack_backend.Auth_Module.dto.RegisterRequest;
import com.MediTrack.meditrack_backend.Auth_Module.entity.RefreshToken;
import com.MediTrack.meditrack_backend.Auth_Module.entity.Role;
import com.MediTrack.meditrack_backend.Auth_Module.entity.User;
import com.MediTrack.meditrack_backend.Auth_Module.repository.RoleRepository;
import com.MediTrack.meditrack_backend.Auth_Module.repository.UserRepository;
import com.MediTrack.meditrack_backend.security.JwtService;
import com.MediTrack.meditrack_backend.security.LockOutService;
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
    private final AuditLogService auditLogService;
    private final TokenBlacklistService tokenBlacklistService;
    private final LockOutService lockOutService;
    private final AlertGenerator alertGenerator;

    @Override
    @Transactional
    public AuthResult register(RegisterRequest request) {
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
        return new AuthResult(buildAuthResponse(saved, accessToken), refreshToken);
    }

    @Override
    @Transactional
    public AuthResult login(LoginRequest request, String ip, String userAgent) {

        String identifier = request.getUsername();

        // 1. Lockout check (before DB/auth)
        if (lockOutService.isLocked(identifier)) {
            auditLogService.loginFailure(identifier, ip, userAgent, "Account locked");
            throw new BadCredentialsException("Authentication failed");
        }

        // 2. Resolve user by username OR email
        User user = userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier))
                .orElseThrow(() -> new BadCredentialsException("Authentication failed"));

        // 3. Authenticate using Spring Security (MUST use username)
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getUsername(),
                            request.getPassword()
                    )
            );
        } catch (AuthenticationException ex) {

            boolean justLocked = lockOutService.recordFailure(identifier);

            alertGenerator.failedLoginAttempt(identifier, ip);

            if (justLocked) {
                alertGenerator.accountLocked(
                        identifier,
                        ip,
                        lockOutService.getLockoutMinutes()
                );
            }

            auditLogService.loginFailure(identifier, ip, userAgent, ex.getMessage());
            throw new BadCredentialsException("Authentication failed");
        }

        // 4. Check account status
        if (user.isDeleted() || !user.isEnabled()) {
            throw new BadCredentialsException("Authentication failed");
        }

        // 5. Success → reset lockout
        lockOutService.recordSuccess(identifier);

        // 6. Generate tokens
        UserDetails userDetails = buildUserDetails(user);

        String accessToken = jwtService.generateToken(userDetails);

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        // 7. Audit log
        auditLogService.loginSuccess(user.getUsername(), ip, userAgent);

        // 8. Return result
        return new AuthResult(
                buildAuthResponse(user, accessToken),
                refreshToken
        );
    }

    @Override
    @Transactional
    public AuthResult refresh(String rawToken, String ip) {
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

        return new AuthResult( buildAuthResponse(user, newAccessToken), newRefreshToken);
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

    private AuthResponse buildAuthResponse(User user, String accessToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
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