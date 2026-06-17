package com.MediTrack.meditrack_backend.Auth_Module.service;

import com.MediTrack.meditrack_backend.Alerts_Module.service.AlertGenerator;
import com.MediTrack.meditrack_backend.Auth_Module.dto.UserDTO;
import com.MediTrack.meditrack_backend.Auth_Module.dto.ChangePasswordRequest;
import com.MediTrack.meditrack_backend.Auth_Module.dto.ResetPasswordRequest;
import com.MediTrack.meditrack_backend.Auth_Module.dto.UpdateProfileRequest;
import com.MediTrack.meditrack_backend.Auth_Module.entity.Role;
import com.MediTrack.meditrack_backend.Auth_Module.entity.User;
import com.MediTrack.meditrack_backend.Auth_Module.repository.RoleRepository;
import com.MediTrack.meditrack_backend.Auth_Module.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
  //Implements admin user management and account self-service features.
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AlertGenerator alertGenerator;

    @Override
    @Transactional
    //Creates a new enabled user with the default role and starter password.
    public UserDTO createUser(UserDTO request) {
        validateUniqueFields(request.getUsername(), request.getEmail(), null);

        Set<Role> roles = new HashSet<>();

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            for (String roleName : request.getRoles()) {
                Role role = roleRepository.findByRole(roleName)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
                roles.add(role);
            }
        } else {
            Role defaultRole = roleRepository.findByRole("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Role not found: ROLE_USER"));
            roles.add(defaultRole);
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(request.getEnabled() != null ? request.getEnabled() : true)
                .deleted(request.getDeleted() != null ? request.getDeleted() : false)
                .roles(roles)
                .build();

        User savedUser = userRepository.save(user);

        String performedBy = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        alertGenerator.userCreated(
                savedUser.getId(),
                savedUser.getUsername(),
                performedBy
        );

        return toDTO(savedUser);
    }

    @Override
    // Returns all users that have not been soft-deleted.
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .filter(user -> !user.isDeleted())
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
     // Returns one active user by id.
    public UserDTO getUserById(Integer id) {
        return toDTO(findActiveUserById(id));
    }

    @Override
    @Transactional
    //Updates username, email, and enabled status for an existing user.
    public UserDTO updateUser(Integer id, UserDTO request) {
        User user = findActiveUserById(id);
        validateUniqueFields(request.getUsername(), request.getEmail(), id);
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }
        return toDTO(userRepository.save(user));
    }

    @Override
    @Transactional
     // Marks a user as deleted and disables the account.
    public void deleteUser(Integer id) {
        User user = findActiveUserById(id);
        user.setDeleted(true);
        user.setEnabled(false);
        userRepository.save(user);
        alertGenerator.userDeleted(
                user.getId(),
                user.getUsername(),
                SecurityContextHolder.getContext().getAuthentication().getName()
        );
    }

    @Override
    @Transactional
     // Enables a user account if the user was not deleted.
    public UserDTO activateUser(Integer id) {
        User user = findAnyUserById(id);
        if (user.isDeleted()) {
            throw new RuntimeException("Cannot activate a deleted user");
        }
        user.setEnabled(true);
        return toDTO(userRepository.save(user));
    }

    @Override
    @Transactional
     //Disables an active user account.
    public UserDTO deactivateUser(Integer id) {
        User user = findActiveUserById(id);
        user.setEnabled(false);
        return toDTO(userRepository.save(user));
    }

    @Override
    @Transactional
     // Updates the authenticated user's own profile details.
    public UserDTO updateProfile(String authenticatedUsername, UpdateProfileRequest request) {
        User user = userRepository.findByUsername(authenticatedUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));
        validateUniqueFields(request.getUsername(), request.getEmail(), user.getId());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        return toDTO(userRepository.save(user));
    }

    @Override
    @Transactional
     // Verifies the current password before saving a new encoded password.
    public void changePassword(String authenticatedUsername, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(authenticatedUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            throw new RuntimeException("New password must be different from current password");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
     // Lets an admin replace a user's password directly.
    public void resetPassword(Integer userId, ResetPasswordRequest request) {
        User user = findActiveUserById(userId);
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
     // Adds a role to a user.
    public UserDTO assignRole(Integer userId, Integer roleId) {
        User user = findActiveUserById(userId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.getRoles().add(role);

        User saved = userRepository.save(user);

        String performedBy = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        alertGenerator.roleAssigned(
                saved.getId(),
                saved.getUsername(),
                role.getRole(),
                performedBy
        );

        return toDTO(saved);
    }

    @Override
    @Transactional
    public UserDTO removeRole(Integer userId, Integer roleId) {
        User user = findActiveUserById(userId);
        if (user.getRoles().size() == 1) {
            throw new RuntimeException("User must have at least one role");
        }
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.getRoles().removeIf(r -> r.getId().equals(roleId));

        User saved = userRepository.save(user);

        String performedBy = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        alertGenerator.roleRemoved(
                saved.getId(),
                saved.getUsername(),
                role.getRole(),
                performedBy
        );

        return toDTO(saved);
    }

    @Override
     // Returns role names for one user.
    public Set<String> getUserRoles(Integer userId) {
        User user = findActiveUserById(userId);
        return user.getRoles().stream().map(Role::getRole).collect(Collectors.toSet());
    }

     // Returns a user only if the account exists and is not deleted.
    private User findActiveUserById(Integer id) {
        User user = findAnyUserById(id);
        if (user.isDeleted()) {
            throw new RuntimeException("User not found");
        }
        return user;
    }

    //Returns a user even if the account is disabled or deleted.
    private User findAnyUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

     // Prevents duplicate usernames and emails.
    private void validateUniqueFields(String username, String email, Integer existingUserId) {
        userRepository.findByUsername(username).ifPresent(user -> {
            if (!user.getId().equals(existingUserId)) {
                throw new RuntimeException("Username is already in use");
            }
        });
        userRepository.findByEmail(email).ifPresent(user -> {
            if (!user.getId().equals(existingUserId)) {
                throw new RuntimeException("Email is already in use");
            }
        });
    }


    //  Converts the entity to the API response shape.
    private UserDTO toDTO(User user) {
        Set<String> roleNames = user.getRoles() == null
                ? Set.of()
                : user.getRoles().stream().map(Role::getRole).collect(Collectors.toSet());
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .enabled(user.isEnabled())
                .deleted(user.isDeleted())
                .roles(roleNames)
                .build();
    }
}
