package com.MediTrack.meditrack_backend.Asset_Management_Module.controller;

import com.MediTrack.meditrack_backend.Asset_Management_Module.dto.MedicalDeviceDTO;
import com.MediTrack.meditrack_backend.Asset_Management_Module.enums.DeviceStatus;
import jakarta.validation.Valid;
import com.MediTrack.meditrack_backend.Asset_Management_Module.service.MedicalDeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/medical-devices")
@RequiredArgsConstructor
public class MedicalDeviceController {

    private final MedicalDeviceService medicalDeviceService;

    @PostMapping
    public ResponseEntity<MedicalDeviceDTO> createDevice(@Valid @RequestBody MedicalDeviceDTO dto) {
        MedicalDeviceDTO createdDevice = medicalDeviceService.createDevice(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDevice);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicalDeviceDTO> getDeviceById(@PathVariable Integer id) {
        MedicalDeviceDTO device = medicalDeviceService.getDeviceById(id);
        return ResponseEntity.ok(device);
    }

    @GetMapping
    public Page<MedicalDeviceDTO> getAllDevices(
            @RequestParam(required = false) DeviceStatus status,
            @RequestParam(required = false) String name,
            @PageableDefault(page = 0, size = 5, sort = "id") Pageable pageable
    ) {
        return medicalDeviceService.getAllDevices(status, name, pageable);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicalDeviceDTO> updateDevice(@PathVariable Integer id, @Valid @RequestBody MedicalDeviceDTO dto) {
        MedicalDeviceDTO updatedDevice = medicalDeviceService.updateDevice(id, dto);
        return ResponseEntity.ok(updatedDevice);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Integer id) {
        medicalDeviceService.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }
}
