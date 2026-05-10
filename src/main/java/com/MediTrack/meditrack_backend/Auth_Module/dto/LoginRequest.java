package com.MediTrack.meditrack_backend.Auth_Module.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Username is required")
    @Size(max = 100, message = "Username too long")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(max = 200, message = "Password too long")
    private String password;
}