package com.MediTrack.meditrack_backend.Auth_Module.repository;

import com.MediTrack.meditrack_backend.Auth_Module.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Provides database access methods for roles.
 */
public interface RoleRepository extends JpaRepository<Role, Integer> {
    /**
     * Finds a role by its name.
     */
    Optional<Role> findByRole(String role);

    /**
     * Checks whether a role already exists.
     */
    boolean existsByRole(String role);
}
