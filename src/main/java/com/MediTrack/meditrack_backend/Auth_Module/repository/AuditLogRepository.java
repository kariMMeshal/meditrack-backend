package com.MediTrack.meditrack_backend.Auth_Module.repository;

import com.MediTrack.meditrack_backend.Auth_Module.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByUsernameOrderByCreatedAtDesc(String username, Pageable pageable);

    Page<AuditLog> findByEventTypeOrderByCreatedAtDesc(String eventType, Pageable pageable);
}