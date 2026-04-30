package com.MediTrack.meditrack_backend.Ai_Module.repository;

import com.MediTrack.meditrack_backend.Ai_Module.entity.AiReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiReportRepository extends JpaRepository<AiReport, Long> {

    Page<AiReport> findByUserIdOrderByCreatedAtDesc(Integer userId, Pageable pageable);

    Page<AiReport> findBySessionIdOrderByCreatedAtDesc(String sessionId, Pageable pageable);
}