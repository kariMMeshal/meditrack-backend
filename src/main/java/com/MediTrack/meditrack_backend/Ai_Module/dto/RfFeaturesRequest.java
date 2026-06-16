package com.MediTrack.meditrack_backend.Ai_Module.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RfFeaturesRequest {

    @NotNull(message = "deviceId is required")
    private Integer deviceId;

    // ── 15 RF model features ──────────────────────────────────────────

    @JsonProperty("Device_Type")
    @NotBlank(message = "Device_Type is required")
    private String deviceType;

    @JsonProperty("Manufacturer")
    @NotBlank(message = "Manufacturer is required")
    private String manufacturer;

    @JsonProperty("Model")
    @NotBlank(message = "Model is required")
    private String model;

    @JsonProperty("Country")
    @NotBlank(message = "Country is required")
    private String country;

    @JsonProperty("Age")
    @NotNull
    private Double age;

    @JsonProperty("Maintenance_Cost")
    @NotNull
    private Double maintenanceCost;

    @JsonProperty("Downtime")
    @NotNull
    private Double downtime;

    @JsonProperty("Maintenance_Frequency")
    @NotNull
    private Double maintenanceFrequency;

    @JsonProperty("Failure_Event_Count")
    @NotNull
    private Integer failureEventCount;

    @JsonProperty("Maintenance_Class")
    @NotBlank
    private String maintenanceClass;

    @JsonProperty("Operational_Hours_Est")
    @NotNull
    private Double operationalHoursEst;

    @JsonProperty("Expected_Lifespan_Est")
    @NotNull
    private Double expectedLifespanEst;

    @JsonProperty("MTBF")
    @NotNull
    private Double mtbf;

    @JsonProperty("Cost_Per_Hour")
    @NotNull
    private Double costPerHour;

    @JsonProperty("Lifespan_Usage_Ratio")
    @NotNull
    private Double lifespanUsageRatio;
}