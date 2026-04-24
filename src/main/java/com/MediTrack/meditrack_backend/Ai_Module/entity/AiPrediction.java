package com.MediTrack.meditrack_backend.Ai_Module.entity;

import com.MediTrack.meditrack_backend.model.enitity.MaintenanceLog;
import com.MediTrack.meditrack_backend.model.enitity.MedicalDevice;
import com.MediTrack.meditrack_backend.model.enitity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_predictions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiPrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private MedicalDevice device;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maintenance_log_id")
    private MaintenanceLog maintenanceLog;

    @Column(nullable = false)
    private Double probability;

    @Column(nullable = false)
    private Integer prediction;

    @Column(nullable = false)
    private String modelVersion;

    @Column(nullable = false)
    private Double thresholdUsed;

    @Column(nullable = false)
    private String status; // SUCCESS, FAILED, FALLBACK

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by", nullable = false)
    private User requestedBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
