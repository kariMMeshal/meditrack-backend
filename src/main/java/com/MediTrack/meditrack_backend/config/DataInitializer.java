package com.MediTrack.meditrack_backend.config;

import com.MediTrack.meditrack_backend.model.enitity.Role;
import com.MediTrack.meditrack_backend.model.enitity.User;
import com.MediTrack.meditrack_backend.repository.RoleRepository;
import com.MediTrack.meditrack_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
/**
 * Seeds default roles and an admin account when the application starts.
 */
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    /**
     * Creates startup data required for auth and user management.
     */
    public CommandLineRunner initializeRoles() {
        return args -> {
            createRoleIfMissing("ROLE_ADMIN");
            createRoleIfMissing("ROLE_BIOMED");
            createRoleIfMissing("ROLE_USER");
            createAdminIfMissing();
        };
    }

    /**
     * Saves a role only when it does not already exist.
     */
    private void createRoleIfMissing(String roleName) {
        if (!roleRepository.existsByRole(roleName)) {
            roleRepository.save(Role.builder().role(roleName).build());
        }
    }

    /**
     * Creates the default admin account if it is missing.
     */
    private void createAdminIfMissing() {
        if (userRepository.existsByUsername("admin")) {
            return;
        }

        Role adminRole = roleRepository.findByRole("ROLE_ADMIN")
                .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found"));
        Role userRole = roleRepository.findByRole("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("ROLE_USER not found"));

        User admin = User.builder()
                .username("admin")
                .email("admin@meditrack.local")
                .password(passwordEncoder.encode("Admin@12345"))
                .enabled(true)
                .deleted(false)
                .roles(new HashSet<>(Set.of(adminRole, userRole)))
                .build();
        userRepository.save(admin);
    }
}
