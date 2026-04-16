package com.MediTrack.meditrack_backend.service;

import com.MediTrack.meditrack_backend.model.dto.auth.AuthResponse;
import com.MediTrack.meditrack_backend.model.dto.auth.LoginRequest;
import com.MediTrack.meditrack_backend.model.dto.auth.RegisterRequest;

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
