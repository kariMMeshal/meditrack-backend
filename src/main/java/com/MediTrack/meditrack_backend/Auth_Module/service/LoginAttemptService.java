package com.MediTrack.meditrack_backend.Auth_Module.service;

import com.MediTrack.meditrack_backend.Auth_Module.entity.User;
import com.MediTrack.meditrack_backend.Auth_Module.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Enforces account lockout after repeated failed login attempts.
 *
 * Strategy:
 * - Track failedLoginAttempts on the User entity (survives restarts)
 * - After MAX_ATTEMPTS, lock the account for LOCKOUT_MINUTES
 * - On successful login, reset the counter immediately
 * - Lockout auto-expires — no manual admin action needed for timed unlocks
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoginAttemptService {

    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Value("${security.max-login-attempts:5}")
    private int maxAttempts;

    @Value("${security.lockout-minutes:1}")
    private int lockoutMinutes;

    @Transactional
    public void recordFailure(User user, String ip) {
        user.incrementFailedAttempts();

        if (user.getFailedLoginAttempts() >= maxAttempts) {
            user.lockAccount(lockoutMinutes);
            userRepository.save(user);
            log.warn("Account LOCKED — username={}, ip={}, minutes={}",
                    user.getUsername(), ip, lockoutMinutes);
            auditLogService.accountLocked(user.getUsername(), ip, lockoutMinutes);
        } else {
            userRepository.save(user);
            log.info("Failed login attempt {}/{} — username={}, ip={}",
                    user.getFailedLoginAttempts(), maxAttempts, user.getUsername(), ip);
        }
    }

    @Transactional
    public void recordSuccess(User user) {
        if (user.getFailedLoginAttempts() > 0) {
            user.resetFailedAttempts();
            userRepository.save(user);
        }
    }

    public boolean isLocked(User user) {
        return user.isAccountLocked();
    }
}