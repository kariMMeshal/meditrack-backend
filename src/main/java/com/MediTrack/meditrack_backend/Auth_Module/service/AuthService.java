package com.MediTrack.meditrack_backend.Auth_Module.service;

import com.MediTrack.meditrack_backend.Auth_Module.dto.AuthResult;
import com.MediTrack.meditrack_backend.Auth_Module.dto.LoginRequest;
import com.MediTrack.meditrack_backend.Auth_Module.dto.RegisterRequest;

public interface AuthService {

    AuthResult register(RegisterRequest request);

    /**
     * Authenticates user, enforces lockout, records audit event.
     * @param request login credentials
     * @param ip      client IP address for audit log
     * @param userAgent browser/client identifier for audit log
     */
    AuthResult login(LoginRequest request, String ip, String userAgent);

    /**
     * Validates refresh token, rotates it, and issues a new access token.
     * @param refreshToken the opaque refresh token from a previous login
     */
    AuthResult refresh(String refreshToken, String ip);

    /**
     * Revokes the refresh token server-side — real logout.
     * @param refreshToken the token to invalidate
     */
    void logout(String refreshToken, String username, String ip, String userAgent);
}