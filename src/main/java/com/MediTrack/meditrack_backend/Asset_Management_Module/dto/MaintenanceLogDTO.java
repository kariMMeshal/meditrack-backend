package com.MediTrack.meditrack_backend.Asset_Management_Module.dto;

import com.MediTrack.meditrack_backend.Asset_Management_Module.enums.MaintenancePriority;
import com.MediTrack.meditrack_backend.Asset_Management_Module.enums.MaintenanceStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceLogDTO {

    private Integer id;

    private Integer deviceId;
    private String deviceName;

    private Integer performedById;
    private String performedByUsername;

    private String issueDescription;
    private String actionTaken;

    private MaintenanceStatus maintenanceStatus;
    private MaintenancePriority priority;

    private LocalDate maintenanceDate;
    private LocalDate nextMaintenanceDate;

    private Double cost;
    private String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}