package com.MediTrack.meditrack_backend.model.enitity;

import com.MediTrack.meditrack_backend.util.enums.DeviceStatus;
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
}