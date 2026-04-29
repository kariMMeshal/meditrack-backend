package com.MediTrack.meditrack_backend.Ai_Module.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RfFeaturesRequest {

    @NotNull(message = "deviceId is required")
    private Integer deviceId;

    // ── 15 RF model features ──────────────────────────────────────────

    @NotBlank(message = "Device_Type is required")
    private String Device_Type;

    @NotBlank(message = "Manufacturer is required")
    private String Manufacturer;

    @NotBlank(message = "Model is required")
    private String Model;

    @NotBlank(message = "Country is required")
    private String Country;

    @NotNull(message = "Age is required")
    @Min(value = 0, message = "Age must be zero or greater")
    private Double Age;

    @NotNull(message = "Maintenance_Cost is required")
    @Min(value = 0, message = "Maintenance_Cost must be zero or greater")
    private Double Maintenance_Cost;

    @NotNull(message = "Downtime is required")
    @Min(value = 0, message = "Downtime must be zero or greater")
    private Double Downtime;

    @NotNull(message = "Maintenance_Frequency is required")
    @Min(value = 0, message = "Maintenance_Frequency must be zero or greater")
    private Double Maintenance_Frequency;

    @NotNull(message = "Failure_Event_Count is required")
    @Min(value = 0, message = "Failure_Event_Count must be zero or greater")
    private Integer Failure_Event_Count;

    @NotBlank(message = "Maintenance_Class is required")
    private String Maintenance_Class;

    @NotNull(message = "Operational_Hours_Est is required")
    @Min(value = 0, message = "Operational_Hours_Est must be zero or greater")
    private Double Operational_Hours_Est;

    @NotNull(message = "Expected_Lifespan_Est is required")
    @Min(value = 0, message = "Expected_Lifespan_Est must be zero or greater")
    private Double Expected_Lifespan_Est;

    @NotNull(message = "MTBF is required")
    @Min(value = 0, message = "MTBF must be zero or greater")
    private Double MTBF;

    @NotNull(message = "Cost_Per_Hour is required")
    @Min(value = 0, message = "Cost_Per_Hour must be zero or greater")
    private Double Cost_Per_Hour;

    @NotNull(message = "Lifespan_Usage_Ratio is required")
    @DecimalMin(value = "0.0", message = "Lifespan_Usage_Ratio must be between 0 and 1")
    @DecimalMax(value = "1.0", message = "Lifespan_Usage_Ratio must be between 0 and 1")
    private Double Lifespan_Usage_Ratio;
}