package com.MediTrack.meditrack_backend.repository;

import com.MediTrack.meditrack_backend.model.enitity.MaintenanceLog;
import com.MediTrack.meditrack_backend.util.enums.MaintenancePriority;
import com.MediTrack.meditrack_backend.util.enums.MaintenanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaintenanceLogRepository extends JpaRepository<MaintenanceLog, Integer> {

    Page<MaintenanceLog> findByDeviceId(Integer deviceId, Pageable pageable);

    Page<MaintenanceLog> findByStatus(MaintenanceStatus status, Pageable pageable);

    Page<MaintenanceLog> findByPriority(MaintenancePriority priority, Pageable pageable);

    Page<MaintenanceLog> findByDeviceIdAndStatus(Integer deviceId, MaintenanceStatus status, Pageable pageable);
}
