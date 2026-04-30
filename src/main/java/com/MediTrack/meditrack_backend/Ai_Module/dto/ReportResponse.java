package com.MediTrack.meditrack_backend.Ai_Module.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReportResponse {
    private Long reportId;
    private String sessionId;
    private String reportType;
    private String reportContent;     // Full markdown report
    private String provider;          // groq / OpenRouter / fallback
    private LocalDateTime createdAt;
}