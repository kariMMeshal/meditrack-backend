package com.MediTrack.meditrack_backend.controller;

import com.MediTrack.meditrack_backend.model.dto.UserDTO;
import com.MediTrack.meditrack_backend.model.dto.auth.ChangePasswordRequest;
import com.MediTrack.meditrack_backend.model.dto.auth.ResetPasswordRequest;
import com.MediTrack.meditrack_backend.model.dto.auth.UpdateProfileRequest;
import com.MediTrack.meditrack_backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
/**
 * Exposes admin and self-service endpoints for managing users.
 */
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    /**
     * Creates a new user with default account settings.
     */
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    /**
     * Returns all non-deleted users.
     */
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    /**
     * Returns one user by id.
     */
    public ResponseEntity<UserDTO> getUserById(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    /**
     * Updates the main profile fields of a user.
     */
    public ResponseEntity<UserDTO> updateUser(@PathVariable Integer id, @Valid @RequestBody UserDTO request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    /**
     * Soft-deletes a user and disables the account.
     */
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    /**
     * Enables a user account that is not deleted.
     */
    public ResponseEntity<UserDTO> activateUser(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.activateUser(id));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    /**
     * Disables an active user account.
     */
    public ResponseEntity<UserDTO> deactivateUser(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.deactivateUser(id));
    }

    @PutMapping("/me/profile")
    /**
     * Lets the authenticated user update their own username and email.
     */
    public ResponseEntity<UserDTO> updateProfile(@Valid @RequestBody UpdateProfileRequest request, Authentication authentication) {
        return ResponseEntity.ok(userService.updateProfile(authentication.getName(), request));
    }

    @PutMapping("/me/password")
    /**
     * Lets the authenticated user change their own password.
     */
    public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody ChangePasswordRequest request, Authentication authentication) {
        userService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    @PutMapping("/{id}/password/reset")
    @PreAuthorize("hasRole('ADMIN')")
    /**
     * Lets an admin reset a user's password.
     */
    public ResponseEntity<Map<String, String>> resetPassword(@PathVariable Integer id, @Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(id, request);
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }

    @PostMapping("/{userId}/roles/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    /**
     * Assigns a role to a user.
     */
    public ResponseEntity<UserDTO> assignRole(@PathVariable Integer userId, @PathVariable Integer roleId) {
        return ResponseEntity.ok(userService.assignRole(userId, roleId));
    }

    @DeleteMapping("/{userId}/roles/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    /**
     * Removes a role from a user.
     */
    public ResponseEntity<UserDTO> removeRole(@PathVariable Integer userId, @PathVariable Integer roleId) {
        return ResponseEntity.ok(userService.removeRole(userId, roleId));
    }

    @GetMapping("/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    /**
     * Returns the role names assigned to a user.
     */
    public ResponseEntity<Set<String>> getUserRoles(@PathVariable Integer userId) {
        return ResponseEntity.ok(userService.getUserRoles(userId));
    }
}
