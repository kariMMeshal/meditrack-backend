package com.MediTrack.meditrack_backend.Asset_Management_Module.dto;

import com.MediTrack.meditrack_backend.Asset_Management_Module.enums.DeviceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalDeviceDTO {
    private Integer id;
    private String assetTag;

    @NotBlank(message = "name is required")
    private String name;

    private String model;
    private String manufacturer;

    @NotBlank(message = "serialNumber is required")
    private String serialNumber;

    @NotNull(message = "status is required")
    private DeviceStatus status;
    private String conditionDescription;

    @NotNull(message = "departmentId is required")
    private Integer departmentId;  //request

    private String departmentName; //response
    private String location;
    private String supplier;

    @PositiveOrZero(message = "purchasePrice must be zero or positive")
    private Double purchasePrice;
    private LocalDate purchaseDate;
    private LocalDate warrantyExpiryDate;
    private LocalDate lastMaintenanceDate;
    private LocalDate nextMaintenanceDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
