package com.MediTrack.meditrack_backend.controller;

import com.MediTrack.meditrack_backend.model.dto.MedicalDeviceDTO;
import com.MediTrack.meditrack_backend.service.MedicalDeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medical-devices")
@RequiredArgsConstructor
public class MedicalDeviceController {

    private final MedicalDeviceService medicalDeviceService;

    @PostMapping
    public ResponseEntity<MedicalDeviceDTO> createDevice(@RequestBody MedicalDeviceDTO dto) {
        MedicalDeviceDTO createdDevice = medicalDeviceService.createDevice(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDevice);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicalDeviceDTO> getDeviceById(@PathVariable Integer id) {
        MedicalDeviceDTO device = medicalDeviceService.getDeviceById(id);
        return ResponseEntity.ok(device);
    }

    @GetMapping
    public ResponseEntity<List<MedicalDeviceDTO>> getAllDevices() {
        List<MedicalDeviceDTO> devices = medicalDeviceService.getAllDevices();
        return ResponseEntity.ok(devices);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicalDeviceDTO> updateDevice(@PathVariable Integer id, @RequestBody MedicalDeviceDTO dto) {
        MedicalDeviceDTO updatedDevice = medicalDeviceService.updateDevice(id, dto);
        return ResponseEntity.ok(updatedDevice);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Integer id) {
        medicalDeviceService.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }
}
