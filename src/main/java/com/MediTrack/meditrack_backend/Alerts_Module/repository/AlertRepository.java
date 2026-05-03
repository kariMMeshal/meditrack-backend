package com.MediTrack.meditrack_backend.Alerts_Module.repository;



import com.MediTrack.meditrack_backend.Alerts_Module.entity.Alert;
import com.MediTrack.meditrack_backend.Alerts_Module.enums.AlertSeverity;
import com.MediTrack.meditrack_backend.Alerts_Module.enums.AlertStatus;
import com.MediTrack.meditrack_backend.Alerts_Module.enums.AlertType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    // ── Role-based queries ────────────────────────────────────────────

    // ADMIN — all alerts
    Page<Alert> findAll(Pageable pageable);

    // TECHNICIAN — device + cleaning alerts
    Page<Alert> findByTypeIn(List<AlertType> types, Pageable pageable);

    // USER — only their own alerts
    Page<Alert> findByUserId(Integer userId, Pageable pageable);

    // Device-specific
    Page<Alert> findByDeviceId(Integer deviceId, Pageable pageable);

    // Severity filter
    Page<Alert> findBySeverity(AlertSeverity severity, Pageable pageable);

    // Status filter
    Page<Alert> findByStatus(AlertStatus status, Pageable pageable);

    // Combined type + status
    Page<Alert> findByTypeInAndStatus(List<AlertType> types, AlertStatus status, Pageable pageable);

    // Unread count for a user — used by SSE to push badge count
    @Query("SELECT COUNT(a) FROM Alert a WHERE a.userId = :userId AND a.status = 'NEW'")
    long countNewByUserId(Integer userId);

    // Scheduler uses this to check for unresolved device alerts
    List<Alert> findByDeviceIdAndTypeAndStatus(
            Integer deviceId, AlertType type, AlertStatus status);

    // For deduplication — prevent duplicate active alerts for same device+type
    boolean existsByDeviceIdAndTypeAndStatusIn(
            Integer deviceId, AlertType type, List<AlertStatus> statuses);
}