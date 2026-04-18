package com.MediTrack.meditrack_backend.service;

import com.MediTrack.meditrack_backend.model.dto.MaintenanceLogDTO;
import com.MediTrack.meditrack_backend.util.enums.MaintenancePriority;
import com.MediTrack.meditrack_backend.util.enums.MaintenanceStatus;
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
