package com.MediTrack.meditrack_backend.Alerts_Module.entity;

import com.MediTrack.meditrack_backend.Alerts_Module.enums.AlertSeverity;
import com.MediTrack.meditrack_backend.Alerts_Module.enums.AlertStatus;
import com.MediTrack.meditrack_backend.Alerts_Module.enums.AlertType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "alerts", indexes = {
        @Index(name = "idx_alert_type",      columnList = "type"),
        @Index(name = "idx_alert_status",    columnList = "status"),
        @Index(name = "idx_alert_device",    columnList = "device_id"),
        @Index(name = "idx_alert_user",      columnList = "user_id"),
        @Index(name = "idx_alert_created",   columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AlertStatus status = AlertStatus.NEW;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    // Optional — links alert to a specific device
    @Column(name = "device_id")
    private Integer deviceId;

    // Optional — links alert to a specific user
    @Column(name = "user_id")
    private Integer userId;

    // Flexible JSON field — stores context like probabilities, scores, event details
    @Column(columnDefinition = "TEXT")
    private String metadata;

    // AI-generated explanation — populated asynchronously after alert creation
    @Column(name = "ai_explanation", columnDefinition = "TEXT")
    private String aiExplanation;

    // Which provider generated the explanation: groq, openrouter, fallback
    @Column(name = "ai_provider")
    private String aiProvider;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}