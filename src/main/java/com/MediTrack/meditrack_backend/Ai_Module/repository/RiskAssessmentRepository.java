package com.MediTrack.meditrack_backend.Ai_Module.repository;

import com.MediTrack.meditrack_backend.Ai_Module.entity.RiskAssessment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, Long> {

    Page<RiskAssessment> findByDeviceId(Integer deviceId, Pageable pageable);

    Page<RiskAssessment> findByDeviceIdAndPredictedClass(Integer deviceId,
                                                         Integer predictedClass,
                                                         Pageable pageable);
}