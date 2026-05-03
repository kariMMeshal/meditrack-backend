package com.MediTrack.meditrack_backend.Alerts_Module.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AlertReportDTO {
    private String summary;
    private String keyFindings;
    private String riskLevel;           // LOW / MEDIUM / HIGH / CRITICAL
    private String recommendedActions;  // non-medical general advice only
    private String disclaimer;
    private String provider;            // groq / openrouter / fallback
    private LocalDateTime generatedAt;
}