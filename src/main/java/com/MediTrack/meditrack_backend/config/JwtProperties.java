package com.MediTrack.meditrack_backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
/**
 * Holds JWT configuration values loaded from application properties.
 */
public record JwtProperties(String secret, long expiration) {}
