package com.MediTrack.meditrack_backend.Auth_Module.service;

import com.MediTrack.meditrack_backend.Auth_Module.dto.AuthResponse;
import com.MediTrack.meditrack_backend.Auth_Module.dto.LoginRequest;
import com.MediTrack.meditrack_backend.Auth_Module.dto.RegisterRequest;
import com.MediTrack.meditrack_backend.Auth_Module.entity.Role;
import com.MediTrack.meditrack_backend.Auth_Module.entity.User;
import com.MediTrack.meditrack_backend.Auth_Module.repository.RoleRepository;
import com.MediTrack.meditrack_backend.Auth_Module.repository.UserRepository;
import com.MediTrack.meditrack_backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
/**
 * Implements registration and login using Spring Security and JWT tokens.
 */
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    @Transactional
    /**
     * Creates a new user, assigns the default role, and returns a JWT.
     */
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already in use");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use");
        }

        Role defaultRole = roleRepository.findByRole("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default role ROLE_USER not found"));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .deleted(false)
                .roles(new HashSet<>(Set.of(defaultRole)))
                .build();
        User saved = userRepository.save(user);

        UserDetails securityUser = org.springframework.security.core.userdetails.User.builder()
                .username(saved.getUsername())
                .password(saved.getPassword())
                .authorities(saved.getRoles().stream().map(Role::getRole).toArray(String[]::new))
                .disabled(!saved.isEnabled())
                .build();
        String token = jwtService.generateToken(securityUser);
        return buildAuthResponse(saved, token);
    }

    @Override
    @Transactional(readOnly = true)
    /**
     * Authenticates an existing user and returns a fresh JWT.
     */
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.isDeleted()) {
            throw new RuntimeException("User not found");
        }
        if (!user.isEnabled()) {
            throw new RuntimeException("User exists but is not enabled");
        }
        String token = jwtService.generateToken(userDetails);
        return buildAuthResponse(user, token);
    }

    @Override
    /**
     * JWT logout is stateless, so there is no server-side session to clear.
     */
    public void logout() {
        // Stateless JWT logout is handled on client side by deleting the token.
    }

    /**
     * Builds the auth response returned after registration or login.
     */
    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresInMs(jwtService.getJwtExpirationMs())
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(Role::getRole).collect(Collectors.toSet()))
                .build();
    }
}
