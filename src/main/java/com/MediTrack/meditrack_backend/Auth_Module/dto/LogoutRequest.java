package com.MediTrack.meditrack_backend.Auth_Module.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LogoutRequest {

    @NotBlank(message = "Access token is required for logout")
    private String accessToken;
}