package com.MediTrack.meditrack_backend.model.dto;

import com.MediTrack.meditrack_backend.util.enums.DeviceStatus;
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
    private String name;
    private String model;
    private String manufacturer;
    private String serialNumber;
    private DeviceStatus status;
    private String conditionDescription;
    private Integer departmentId;  //request
    private String departmentName; //response
    private String location;
    private String supplier;
    private Double purchasePrice;
    private LocalDate purchaseDate;
    private LocalDate warrantyExpiryDate;
    private LocalDate lastMaintenanceDate;
    private LocalDate nextMaintenanceDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
