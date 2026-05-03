package com.MediTrack.meditrack_backend.Auth_Module.service;

import com.MediTrack.meditrack_backend.Auth_Module.dto.AuthResponse;
import com.MediTrack.meditrack_backend.Auth_Module.dto.LoginRequest;
import com.MediTrack.meditrack_backend.Auth_Module.dto.RegisterRequest;

/**
 * Defines the authentication operations exposed to controllers.
 */
public interface AuthService {
    /**
     * Registers a new user and returns the authentication payload.
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticates a user and returns the authentication payload.
     */
    AuthResponse login(LoginRequest request);

    /**
     * Handles logout for the current authentication approach.
     */
    void logout();
}
