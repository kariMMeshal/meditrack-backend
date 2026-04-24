package com.MediTrack.meditrack_backend.Ai_Module.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AiHealthDTO {
    private String status;
    private Boolean modelLoaded;
    private String modelVersion;
    private LocalDateTime timestamp;
    private String error;
}