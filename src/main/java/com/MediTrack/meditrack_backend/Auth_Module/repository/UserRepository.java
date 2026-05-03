package com.MediTrack.meditrack_backend.Auth_Module.repository;

import com.MediTrack.meditrack_backend.Auth_Module.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Provides database access methods for user accounts.
 */
public interface UserRepository extends JpaRepository<User, Integer> {
    /**
     * Finds a user by username.
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by email.
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks whether a username already exists.
     */
    boolean existsByUsername(String username);

    /**
     * Checks whether an email already exists.
     */
    boolean existsByEmail(String email);
}
