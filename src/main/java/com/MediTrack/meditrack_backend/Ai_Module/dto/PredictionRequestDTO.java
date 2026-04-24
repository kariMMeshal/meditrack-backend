package com.MediTrack.meditrack_backend.Ai_Module.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class PredictionRequestDTO {

    @NotNull(message = "deviceId is required")
    private Integer deviceId;

    @NotNull(message = "sequence is required")
    @Size(min = 30, max = 30, message = "Sequence must contain exactly 30 timesteps")
    private List<List<Double>> sequence;
}