package com.MediTrack.meditrack_backend.Auth_Module.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_username",  columnList = "username"),
        @Index(name = "idx_audit_event",     columnList = "event_type"),
        @Index(name = "idx_audit_created",   columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Type of event — e.g. LOGIN_SUCCESS, LOGIN_FAILURE, LOGOUT, PASSWORD_CHANGE */
    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(length = 100)
    private String username;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    /** Additional context — e.g. "Account locked after 5 attempts" */
    @Column(columnDefinition = "TEXT")
    private String details;

    /** true = action succeeded, false = action failed */
    private boolean success;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}