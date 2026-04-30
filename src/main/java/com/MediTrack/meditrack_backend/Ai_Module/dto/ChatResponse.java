package com.MediTrack.meditrack_backend.Ai_Module.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatResponse {
    private Long messageId;
    private String sessionId;
    private String userMessage;
    private String aiResponse;
    private String provider;          // groq / OpenRouter / fallback
    private LocalDateTime createdAt;
}