package com.MediTrack.meditrack_backend.Auth_Module.service;

import com.MediTrack.meditrack_backend.Auth_Module.dto.UserDTO;
import com.MediTrack.meditrack_backend.Auth_Module.dto.ChangePasswordRequest;
import com.MediTrack.meditrack_backend.Auth_Module.dto.ResetPasswordRequest;
import com.MediTrack.meditrack_backend.Auth_Module.dto.UpdateProfileRequest;

import java.util.List;
import java.util.Set;

/**
 * Defines user management and self-service account operations.
 */
public interface UserService {
    /**
     * Creates a new user.
     */
    UserDTO createUser(UserDTO request);

    /**
     * Returns all active users.
     */
    List<UserDTO> getAllUsers();

    /**
     * Returns one user by id.
     */
    UserDTO getUserById(Integer id);

    /**
     * Updates the basic details of a user.
     */
    UserDTO updateUser(Integer id, UserDTO request);

    /**
     * Soft-deletes a user account.
     */
    void deleteUser(Integer id);

    /**
     * Enables a user account.
     */
    UserDTO activateUser(Integer id);

    /**
     * Disables a user account.
     */
    UserDTO deactivateUser(Integer id);

    /**
     * Updates the profile of the authenticated user.
     */
    UserDTO updateProfile(String authenticatedUsername, UpdateProfileRequest request);

    /**
     * Changes the password of the authenticated user.
     */
    void changePassword(String authenticatedUsername, ChangePasswordRequest request);

    /**
     * Resets the password of a user.
     */
    void resetPassword(Integer userId, ResetPasswordRequest request);

    /**
     * Adds a role to a user.
     */
    UserDTO assignRole(Integer userId, Integer roleId);

    /**
     * Removes a role from a user.
     */
    UserDTO removeRole(Integer userId, Integer roleId);

    /**
     * Returns the roles assigned to a user.
     */
    Set<String> getUserRoles(Integer userId);
}
