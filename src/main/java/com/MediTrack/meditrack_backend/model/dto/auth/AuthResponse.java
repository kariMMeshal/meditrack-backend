package com.MediTrack.meditrack_backend.model.dto.auth;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String tokenType;
    private long expiresInMs;
    private Integer userId;
    private String username;
    private String email;
    private Set<String> roles;
}
