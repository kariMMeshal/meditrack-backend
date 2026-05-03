package com.MediTrack.meditrack_backend.Alerts_Module.dto;

import com.MediTrack.meditrack_backend.Alerts_Module.enums.AlertSeverity;
import com.MediTrack.meditrack_backend.Alerts_Module.enums.AlertStatus;
import com.MediTrack.meditrack_backend.Alerts_Module.enums.AlertType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AlertDTO {
    private Long id;
    private AlertType type;
    private AlertSeverity severity;
    private AlertStatus status;
    private String message;
    private Integer deviceId;
    private Integer userId;
    private String metadata;
    private String aiExplanation;
    private String aiProvider;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}
