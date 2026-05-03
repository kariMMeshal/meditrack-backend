package com.MediTrack.meditrack_backend.Auth_Module.service;

import com.MediTrack.meditrack_backend.Auth_Module.dto.RoleDTO;
import com.MediTrack.meditrack_backend.Auth_Module.entity.Role;
import com.MediTrack.meditrack_backend.Auth_Module.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
/**
 * Implements role creation and retrieval logic.
 */
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    @Transactional
    /**
     * Normalizes the role name and saves it if it does not already exist.
     */
    public RoleDTO createRole(RoleDTO request) {
        String normalizedRole = normalizeRole(request.getRole());
        if (roleRepository.existsByRole(normalizedRole)) {
            throw new RuntimeException("Role already exists");
        }
        Role role = Role.builder().role(normalizedRole).build();
        return toDTO(roleRepository.save(role));
    }

    @Override
    @Transactional(readOnly = true)
    /**
     * Returns all stored roles.
     */
    public List<RoleDTO> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    /**
     * Returns one role by id.
     */
    public RoleDTO getRoleById(Integer id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        return toDTO(role);
    }

    @Override
    @Transactional
    /**
     * Deletes a role by id.
     */
    public void deleteRole(Integer id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        roleRepository.delete(role);
    }

    /**
     * Ensures role names always use uppercase ROLE_ format.
     */
    private String normalizeRole(String role) {
        String value = role.trim().toUpperCase();
        return value.startsWith("ROLE_") ? value : "ROLE_" + value;
    }

    /**
     * Converts the entity to a DTO.
     */
    private RoleDTO toDTO(Role role) {
        return RoleDTO.builder()
                .id(role.getId())
                .role(role.getRole())
                .build();
    }
}
