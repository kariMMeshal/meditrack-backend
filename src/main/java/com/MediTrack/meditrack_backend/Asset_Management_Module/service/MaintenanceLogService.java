package com.MediTrack.meditrack_backend.Asset_Management_Module.service;

import com.MediTrack.meditrack_backend.Asset_Management_Module.dto.MaintenanceLogDTO;
import com.MediTrack.meditrack_backend.Asset_Management_Module.enums.MaintenancePriority;
import com.MediTrack.meditrack_backend.Asset_Management_Module.enums.MaintenanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MaintenanceLogService {

    MaintenanceLogDTO createLog(MaintenanceLogDTO dto);

    MaintenanceLogDTO updateLog(Integer id, MaintenanceLogDTO dto);

    void deleteLog(Integer id);

    MaintenanceLogDTO getLogById(Integer id);

    Page<MaintenanceLogDTO> getAllLogs(
            MaintenanceStatus status,
            MaintenancePriority priority,
            Integer deviceId,
            Pageable pageable
    );
}
