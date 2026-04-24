package com.MediTrack.meditrack_backend.Ai_Module.entity;

import lombok.Data;

@Data
public class PredictResponse {
    private Double probability;
    private Integer prediction; // 0 = no failure, 1 = failure predicted
}
