package com.MediTrack.meditrack_backend.Auth_Module.dto;

import com.MediTrack.meditrack_backend.Auth_Module.entity.RefreshToken;

public record AuthResult(AuthResponse response, RefreshToken refreshToken) {
}
