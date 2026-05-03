package com.MediTrack.meditrack_backend.Asset_Management_Module.entity;

import com.MediTrack.meditrack_backend.Asset_Management_Module.enums.DeviceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "medical_devices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    private String model;

    private String manufacturer;

    @Column(unique = true, nullable = false)
    private String serialNumber;

    @Column(unique = true)
    private String assetTag;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeviceStatus status;

    private String conditionDescription;

    private String location;

    private String supplier;

    private Double purchasePrice;

    private LocalDate purchaseDate;

    private LocalDate warrantyExpiryDate;

    private LocalDate lastMaintenanceDate;

    private LocalDate nextMaintenanceDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    // ── Alert & Monitoring Fields  ──────────────────────────────

    /** Date of last sterilization cycle */
    @Column(name = "last_sterilization_date")
    private LocalDateTime lastSterilizationDate;

    /**
     * How often sterilization must occur in hours.
     * e.g. 24 = must be sterilized every 24 hours
     */
    @Column(name = "sterilization_interval_hours")
    private Integer sterilizationIntervalHours;

    /** Date of last cleaning */
    @Column(name = "last_cleaned_date")
    private LocalDateTime lastCleanedDate;

    /**
     * Total accumulated usage hours since last maintenance or installation.
     * Scheduler checks this against usage thresholds.
     */
    @Column(name = "usage_hours")
    private Double usageHours;

    /**
     * Maximum allowed usage hours before an alert is raised.
     * e.g. 500.0 = alert when usageHours > 500
     */
    @Column(name = "max_usage_hours")
    private Double maxUsageHours;
}