package com.MediTrack.meditrack_backend.model.dto;

import com.MediTrack.meditrack_backend.model.enitity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {
    private Integer id;
    private String username;
    private String email;
    private String password;
    private boolean enabled;
    private boolean deleted;
    private Set<Role> roles;
}
