package com.MediTrack.meditrack_backend.Ai_Module.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Exactly matches the Flask /predict/rf response body:
 * { "confidence_score": 0.94, "predicted_class": 2, "predicted_label": "High Risk" }
 */
@Data
public class RfFlaskResponse {

    @JsonProperty("confidence_score")
    private Double confidenceScore;

    @JsonProperty("predicted_class")
    private Integer predictedClass;

    @JsonProperty("predicted_label")
    private String predictedLabel;
}