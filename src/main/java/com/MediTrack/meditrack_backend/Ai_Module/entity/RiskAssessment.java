package com.MediTrack.meditrack_backend.Ai_Module.entity;

import com.MediTrack.meditrack_backend.Asset_Management_Module.entity.MedicalDevice;
import com.MediTrack.meditrack_backend.Auth_Module.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "risk_assessments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private MedicalDevice device;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by", nullable = false)
    private User requestedBy;

    // ── RF Output ────────────────────────────────────────────────────
    // All four fields below are NULLABLE because a FALLBACK record
    // (RF service unavailable) legitimately has no prediction at all.

    @Column(name = "predicted_class")
    private Integer predictedClass;             // 0, 1, 2 — null if FALLBACK

    @Column(name = "predicted_label")
    private String predictedLabel;              // Low Risk / Medium Risk / High Risk — null if FALLBACK

    @Column(name = "confidence_score")
    private Double confidenceScore;             // null if FALLBACK

    @Column(name = "recommendation", length = 500)
    private String recommendation;              // always set — fallback message for FALLBACK status

    // ── Audit ─────────────────────────────────────────────────────────

    @Column(name = "model_version", nullable = false)
    private String modelVersion;

    @Column(name = "status", nullable = false)
    private String status;                      // SUCCESS, FALLBACK

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}