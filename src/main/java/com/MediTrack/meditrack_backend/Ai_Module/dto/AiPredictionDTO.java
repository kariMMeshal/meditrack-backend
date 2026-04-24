package com.MediTrack.meditrack_backend.Ai_Module.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AiPredictionDTO {
    private Long id;
    private Integer deviceId;
    private String deviceName;
    private Double probability;
    private Integer prediction;
    private Boolean failurePredicted;
    private String modelVersion;
    private String status;
    private LocalDateTime createdAt;
    private Double avgTemperatureVariance;
    private Double avgMotorVibration;
    private Double avgVoltageDrop;
}