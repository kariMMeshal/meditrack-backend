package com.MediTrack.meditrack_backend.Asset_Management_Module.controller;

import com.MediTrack.meditrack_backend.Asset_Management_Module.dto.MaintenanceLogDTO;
import com.MediTrack.meditrack_backend.Asset_Management_Module.service.MaintenanceLogService;
import com.MediTrack.meditrack_backend.Asset_Management_Module.enums.MaintenancePriority;
import com.MediTrack.meditrack_backend.Asset_Management_Module.enums.MaintenanceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/maintenance-logs")
@RequiredArgsConstructor
public class MaintenanceLogController {

    private final MaintenanceLogService maintenanceLogService;


    @PostMapping
    public ResponseEntity<MaintenanceLogDTO> createLog(@RequestBody MaintenanceLogDTO dto) {
        MaintenanceLogDTO created = maintenanceLogService.createLog(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<Page<MaintenanceLogDTO>> getAllLogs(
            @RequestParam(required = false) MaintenanceStatus status,
            @RequestParam(required = false) MaintenancePriority priority,
            @RequestParam(required = false) Integer deviceId,
            @PageableDefault(page = 0, size = 10, sort = "id") Pageable pageable
    ) {
        Page<MaintenanceLogDTO> logs = maintenanceLogService.getAllLogs(status, priority, deviceId, pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaintenanceLogDTO> getLogById(@PathVariable Integer id) {
        MaintenanceLogDTO log = maintenanceLogService.getLogById(id);
        return ResponseEntity.ok(log);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MaintenanceLogDTO> updateLog(
            @PathVariable Integer id,
            @RequestBody MaintenanceLogDTO dto
    ) {
        MaintenanceLogDTO updated = maintenanceLogService.updateLog(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLog(@PathVariable Integer id) {
        maintenanceLogService.deleteLog(id);
        return ResponseEntity.noContent().build();
    }
}
