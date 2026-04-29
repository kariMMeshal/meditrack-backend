package com.MediTrack.meditrack_backend.Ai_Module.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RiskAssessmentResponse {
    private Long assessmentId;
    private Integer deviceId;
    private String deviceName;
    private Integer riskClass;
    private String riskLabel;
    private Double confidence;
    private String recommendation;
    private String modelVersion;
    private String status;
    private LocalDateTime createdAt;
}