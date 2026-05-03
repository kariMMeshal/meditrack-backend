package com.MediTrack.meditrack_backend.Auth_Module.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @NotBlank(message = "username is required")
    private String username;

    @Email(message = "email format is invalid")
    @NotBlank(message = "email is required")
    private String email;
}
