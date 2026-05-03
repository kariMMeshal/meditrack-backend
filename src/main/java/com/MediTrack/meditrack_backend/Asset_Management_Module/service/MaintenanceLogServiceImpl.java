package com.MediTrack.meditrack_backend.Asset_Management_Module.service;

import com.MediTrack.meditrack_backend.Asset_Management_Module.dto.MaintenanceLogDTO;
import com.MediTrack.meditrack_backend.Asset_Management_Module.entity.MaintenanceLog;
import com.MediTrack.meditrack_backend.Asset_Management_Module.entity.MedicalDevice;
import com.MediTrack.meditrack_backend.Auth_Module.entity.User;
import com.MediTrack.meditrack_backend.Asset_Management_Module.repository.MaintenanceLogRepository;
import com.MediTrack.meditrack_backend.Asset_Management_Module.repository.MedicalDeviceRepository;
import com.MediTrack.meditrack_backend.Auth_Module.repository.UserRepository;
import com.MediTrack.meditrack_backend.Asset_Management_Module.enums.MaintenancePriority;
import com.MediTrack.meditrack_backend.Asset_Management_Module.enums.MaintenanceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MaintenanceLogServiceImpl implements MaintenanceLogService {

    private final MaintenanceLogRepository maintenanceLogRepository;
    private final MedicalDeviceRepository medicalDeviceRepository;
    private final UserRepository userRepository;



    @Override
    @Transactional
    public MaintenanceLogDTO createLog(MaintenanceLogDTO dto) {
        MedicalDevice device = medicalDeviceRepository.findById(dto.getDeviceId())
                .orElseThrow(() -> new RuntimeException("Device not found with ID: " + dto.getDeviceId()));

        User performedBy = userRepository.findById(dto.getPerformedById())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + dto.getPerformedById()));

        MaintenanceLog log = toEntity(dto, device, performedBy);
        MaintenanceLog saved = maintenanceLogRepository.save(log);
        return toDTO(saved);
    }

    @Override
    @Transactional
    public MaintenanceLogDTO updateLog(Integer id, MaintenanceLogDTO dto) {
        MaintenanceLog existing = maintenanceLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Maintenance log not found with ID: " + id));

        MedicalDevice device = medicalDeviceRepository.findById(dto.getDeviceId())
                .orElseThrow(() -> new RuntimeException("Device not found with ID: " + dto.getDeviceId()));

        User performedBy = userRepository.findById(dto.getPerformedById())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + dto.getPerformedById()));

        existing.setDevice(device);
        existing.setPerformedBy(performedBy);
        existing.setIssueDescription(dto.getIssueDescription());
        existing.setActionTaken(dto.getActionTaken());
        existing.setStatus(dto.getMaintenanceStatus());
        existing.setPriority(dto.getPriority());
        existing.setMaintenanceDate(dto.getMaintenanceDate());
        existing.setNextMaintenanceDate(dto.getNextMaintenanceDate());
        existing.setCost(dto.getCost());
        existing.setNotes(dto.getNotes());

        MaintenanceLog updated = maintenanceLogRepository.save(existing);
        return toDTO(updated);
    }

    @Override
    @Transactional
    public void deleteLog(Integer id) {
        if (!maintenanceLogRepository.existsById(id)) {
            throw new RuntimeException("Maintenance log not found with ID: " + id);
        }
        maintenanceLogRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public MaintenanceLogDTO getLogById(Integer id) {
        MaintenanceLog log = maintenanceLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Maintenance log not found with ID: " + id));
        return toDTO(log);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MaintenanceLogDTO> getAllLogs(
            MaintenanceStatus status,
            MaintenancePriority priority,
            Integer deviceId,
            Pageable pageable
    ) {
        Page<MaintenanceLog> page;

        if (deviceId != null && status != null) {
            page = maintenanceLogRepository.findByDeviceIdAndStatus(deviceId, status, pageable);
        } else if (deviceId != null) {
            page = maintenanceLogRepository.findByDeviceId(deviceId, pageable);
        } else if (status != null) {
            page = maintenanceLogRepository.findByStatus(status, pageable);
        } else if (priority != null) {
            page = maintenanceLogRepository.findByPriority(priority, pageable);
        } else {
            page = maintenanceLogRepository.findAll(pageable);
        }

        return page.map(this::toDTO);
    }

    // ─── Mapping Helpers ────────────────────────────────────────────────────────

    private MaintenanceLog toEntity(MaintenanceLogDTO dto, MedicalDevice device, User performedBy) {
        return MaintenanceLog.builder()
                .device(device)
                .performedBy(performedBy)
                .issueDescription(dto.getIssueDescription())
                .actionTaken(dto.getActionTaken())
                .status(dto.getMaintenanceStatus() != null ? dto.getMaintenanceStatus() : MaintenanceStatus.PENDING)
                .priority(dto.getPriority() != null ? dto.getPriority() : MaintenancePriority.MEDIUM)
                .maintenanceDate(dto.getMaintenanceDate())
                .nextMaintenanceDate(dto.getNextMaintenanceDate())
                .cost(dto.getCost())
                .notes(dto.getNotes())
                .build();
    }

    private MaintenanceLogDTO toDTO(MaintenanceLog log) {
        return MaintenanceLogDTO.builder()
                .id(log.getId())
                .deviceId(log.getDevice().getId())
                .deviceName(log.getDevice().getName())
                .performedById(log.getPerformedBy().getId())
                .performedByUsername(log.getPerformedBy().getUsername())
                .issueDescription(log.getIssueDescription())
                .actionTaken(log.getActionTaken())
                .maintenanceStatus(log.getStatus())
                .priority(log.getPriority())
                .maintenanceDate(log.getMaintenanceDate())
                .nextMaintenanceDate(log.getNextMaintenanceDate())
                .cost(log.getCost())
                .notes(log.getNotes())
                .createdAt(log.getCreatedAt())
                .updatedAt(log.getUpdatedAt())
                .build();
    }
}