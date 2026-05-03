package com.MediTrack.meditrack_backend.config;

import com.MediTrack.meditrack_backend.Auth_Module.entity.Role;
import com.MediTrack.meditrack_backend.Auth_Module.entity.User;
import com.MediTrack.meditrack_backend.Auth_Module.repository.RoleRepository;
import com.MediTrack.meditrack_backend.Auth_Module.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.email:admin@meditrack.local}")
    private String adminEmail;

    // ❗ IMPORTANT: remove silent failure fallback
    @Value("${app.admin.password}")
    private String adminPassword;

    @Bean
    public CommandLineRunner initializeRoles() {
        return args -> {
            createRoleIfMissing("ROLE_ADMIN");
            createRoleIfMissing("ROLE_BIOMED");
            createRoleIfMissing("ROLE_USER");
            createAdminIfMissing();
        };
    }

    private void createRoleIfMissing(String roleName) {
        if (!roleRepository.existsByRole(roleName)) {
            roleRepository.save(Role.builder().role(roleName).build());
        }
    }

    private void createAdminIfMissing() {
        if (userRepository.existsByUsername(adminUsername)) {
            return;
        }

        if (adminPassword == null || adminPassword.isBlank()) {
            throw new IllegalStateException("ADMIN PASSWORD is missing in environment (.env)");
        }

        Role adminRole = roleRepository.findByRole("ROLE_ADMIN")
                .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found"));

        Role userRole = roleRepository.findByRole("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("ROLE_USER not found"));

        User admin = User.builder()
                .username(adminUsername)
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .enabled(true)
                .deleted(false)
                .roles(new HashSet<>(Set.of(adminRole, userRole)))
                .build();

        userRepository.save(admin);
        System.out.println(">>> Admin user seeded: " + adminUsername);
    }
}