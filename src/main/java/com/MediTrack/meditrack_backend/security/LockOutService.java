package com.MediTrack.meditrack_backend.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class LockOutService {

    private static final String ATTEMPTS_KEY = "login:attempts:";
    private static final String LOCKED_KEY   = "login:locked:";

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${security.max-login-attempts:5}")
    private int maxAttempts;

    @Getter
    @Value("${security.lockout-minutes:15}")
    private int lockoutMinutes;

    public boolean isLocked(String username) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(LOCKED_KEY + username));
    }

    /**
     * Records a failed attempt.
     * @return true if this failure JUST caused the account to be locked
     *         (used by caller to fire a one-time alert)
     */
    public boolean recordFailure(String username) {
        String attemptsKey = ATTEMPTS_KEY + username;

        Long attempts = redisTemplate.opsForValue().increment(attemptsKey);

        if (attempts != null && attempts == 1) {
            redisTemplate.expire(attemptsKey, Duration.ofMinutes(lockoutMinutes));
        }

        if (attempts != null && attempts >= maxAttempts) {
            redisTemplate.opsForValue()
                    .set(LOCKED_KEY + username, "locked", Duration.ofMinutes(lockoutMinutes));
            redisTemplate.delete(attemptsKey);
            log.warn("Account LOCKED in Redis — username={}, lockoutMinutes={}", username, lockoutMinutes);
            return true; // ← just transitioned into locked state
        }

        log.info("Failed login attempt {}/{} — username={}", attempts, maxAttempts, username);
        return false;
    }

    public void recordSuccess(String username) {
        redisTemplate.delete(ATTEMPTS_KEY + username);
        redisTemplate.delete(LOCKED_KEY + username);
    }

    public long remainingLockoutSeconds(String username) {
        Long ttl = redisTemplate.getExpire(LOCKED_KEY + username);
        return ttl != null ? ttl : 0;
    }

}