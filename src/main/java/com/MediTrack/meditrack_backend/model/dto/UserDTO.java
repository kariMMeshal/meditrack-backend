package com.MediTrack.meditrack_backend.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "username is required")
    private String username;

    @Email(message = "email format is invalid")
    @NotBlank(message = "email is required")
    private String email;
    private boolean enabled;
    private boolean deleted;
    private Set<String> roles;
}
