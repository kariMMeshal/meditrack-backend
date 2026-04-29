package com.MediTrack.meditrack_backend.Ai_Module.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Exactly matches the Flask /predict/rf request body:
 * { "features": { "Device_Type": "...", "Age": 5, ... } }
 */
@Data
@Builder
public class RfFlaskRequest {
    private Map<String, Object> features;
}