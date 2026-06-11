package com.MediTrack.meditrack_backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Maintains a blacklist of revoked access tokens in Redis.
 * TTL matches the token's remaining lifetime — entry auto-expires
 * exactly when the token would have expired anyway, keeping Redis lean.
 */
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final String PREFIX = "blacklist:token:";
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Blacklists a token for the given remaining TTL in seconds.
     * Call this on logout or password change.
     */
    public void blacklist(String token, long remainingTtlSeconds) {
        if (remainingTtlSeconds > 0) {
            redisTemplate.opsForValue()
                    .set(PREFIX + token, "revoked", Duration.ofSeconds(remainingTtlSeconds));
        }
    }

    /** Returns true if this token has been explicitly revoked. */
    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + token));
    }

}