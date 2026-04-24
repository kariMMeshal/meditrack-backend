package com.MediTrack.meditrack_backend.Ai_Module.entity;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PredictRequest {

    // exactly 30 timesteps, each with 3 features
    // [[temp_variance, motor_vibration_hz, voltage_drop], ...]
    @NotNull
    @Size(min = 30, max = 30, message = "Sequence must contain exactly 30 timesteps")
    private List<List<Double>> sequence;
}