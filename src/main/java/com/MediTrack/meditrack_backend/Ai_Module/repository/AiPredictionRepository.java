package com.MediTrack.meditrack_backend.Ai_Module.repository;

import com.MediTrack.meditrack_backend.Ai_Module.entity.AiPrediction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiPredictionRepository extends JpaRepository<AiPrediction, Long> {

    Page<AiPrediction> findByDeviceId(Integer deviceId, Pageable pageable);

    Page<AiPrediction> findByDeviceIdAndPrediction(Integer deviceId, Integer prediction, Pageable pageable);
}
