package com.MediTrack.meditrack_backend.Auth_Module.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
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

    @NotBlank(message = "Password is required")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private Boolean enabled;
    private Boolean deleted;
    private Set<String> roles;
}
