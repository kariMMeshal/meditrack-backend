package com.MediTrack.meditrack_backend.Auth_Module.service;

import com.MediTrack.meditrack_backend.Auth_Module.dto.RoleDTO;

import java.util.List;

/**
 * Defines role management operations.
 */
public interface RoleService {
    /**
     * Creates a new role.
     */
    RoleDTO createRole(RoleDTO request);

    /**
     * Returns all roles.
     */
    List<RoleDTO> getAllRoles();

    /**
     * Returns one role by id.
     */
    RoleDTO getRoleById(Integer id);

    /**
     * Deletes a role.
     */
    void deleteRole(Integer id);
}
