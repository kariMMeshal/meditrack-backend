package com.MediTrack.meditrack_backend.Auth_Module.service;

import com.MediTrack.meditrack_backend.Auth_Module.entity.AuditLog;
import com.MediTrack.meditrack_backend.Auth_Module.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Records security-relevant events to the audit_logs table.
 * All writes are async — they never block the authentication response.
 *
 * Event types:
 *   LOGIN_SUCCESS, LOGIN_FAILURE, LOGOUT,
 *   PASSWORD_CHANGE, PASSWORD_RESET,
 *   TOKEN_REFRESH, ACCOUNT_LOCKED,
 *   USER_CREATED, ROLE_CHANGED
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Async
    public void log(String eventType, String username,
                    String ipAddress, String userAgent,
                    String details, boolean success) {
        try {
            auditLogRepository.save(AuditLog.builder()
                    .eventType(eventType)
                    .username(username)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .details(details)
                    .success(success)
                    .build());
        } catch (Exception ex) {
            // Audit logging must never crash the application
            log.error("Audit log write failed for event={}, user={}: {}", eventType, username, ex.getMessage());
        }
    }

    // ── Convenience methods ───────────────────────────────────────────

    @Async
    public void loginSuccess(String username, String ip, String userAgent) {
        log("LOGIN_SUCCESS", username, ip, userAgent, null, true);
    }

    @Async
    public void loginFailure(String username, String ip, String userAgent, String reason) {
        log("LOGIN_FAILURE", username, ip, userAgent, reason, false);
    }

    @Async
    public void logout(String username, String ip, String userAgent) {
        log("LOGOUT", username, ip, userAgent, null, true);
    }

    @Async
    public void accountLocked(String username, String ip, int lockoutMinutes) {
        log("ACCOUNT_LOCKED", username, ip, null,
                "Account locked for " + lockoutMinutes + " minutes after repeated failed attempts", false);
    }

    @Async
    public void passwordChanged(String username, String ip) {
        log("PASSWORD_CHANGE", username, ip, null, null, true);
    }

    @Async
    public void tokenRefreshed(String username, String ip) {
        log("TOKEN_REFRESH", username, ip, null, null, true);
    }
}