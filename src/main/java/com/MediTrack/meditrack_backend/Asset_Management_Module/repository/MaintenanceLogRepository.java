package com.MediTrack.meditrack_backend.Asset_Management_Module.repository;

import com.MediTrack.meditrack_backend.Asset_Management_Module.entity.MaintenanceLog;
import com.MediTrack.meditrack_backend.Asset_Management_Module.enums.MaintenancePriority;
import com.MediTrack.meditrack_backend.Asset_Management_Module.enums.MaintenanceStatus;
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
