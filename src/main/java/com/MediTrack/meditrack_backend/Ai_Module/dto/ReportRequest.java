package com.MediTrack.meditrack_backend.Ai_Module.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class ReportRequest {

    // Session whose chat history is included in the report
    @NotBlank(message = "sessionId is required")
    private String sessionId;

    // Optional: additional device telemetry or maintenance notes
    private String deviceLogs;

    // Optional: user-reported symptoms or events in free text
    private String reportedEvents;

    // Optional: additional context (device name, department, etc.)
    private String additionalContext;

    // CHAT_SUMMARY | DEVICE_REVIEW | FULL_OVERVIEW
    private String reportType = "FULL_OVERVIEW";
}