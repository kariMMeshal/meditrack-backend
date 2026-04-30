package com.MediTrack.meditrack_backend.Ai_Module.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChatRequest {

    @NotBlank(message = "message is required")
    @Size(max = 2000, message = "message must not exceed 2000 characters")
    private String message;

    // Frontend generates UUID per conversation — groups messages into sessions
    @NotBlank(message = "sessionId is required")
    private String sessionId;
}