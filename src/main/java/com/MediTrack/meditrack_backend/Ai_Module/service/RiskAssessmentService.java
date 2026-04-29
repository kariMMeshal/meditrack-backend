package com.MediTrack.meditrack_backend.Ai_Module.service;

import com.MediTrack.meditrack_backend.Ai_Module.RfAiClient;
import com.MediTrack.meditrack_backend.Ai_Module.dto.RfFeaturesRequest;
import com.MediTrack.meditrack_backend.Ai_Module.dto.RfFlaskRequest;
import com.MediTrack.meditrack_backend.Ai_Module.dto.RfFlaskResponse;
import com.MediTrack.meditrack_backend.Ai_Module.dto.RiskAssessmentResponse;
import com.MediTrack.meditrack_backend.Ai_Module.entity.RiskAssessment;
import com.MediTrack.meditrack_backend.Ai_Module.repository.RiskAssessmentRepository;
import com.MediTrack.meditrack_backend.model.enitity.MedicalDevice;
import com.MediTrack.meditrack_backend.model.enitity.User;
import com.MediTrack.meditrack_backend.repository.MedicalDeviceRepository;
import com.MediTrack.meditrack_backend.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiskAssessmentService {

    private static final String MODEL_VERSION = "rf_v1";

    private final RfAiClient rfAiClient;
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final MedicalDeviceRepository deviceRepository;
    private final UserRepository userRepository;

    @Transactional
    public RiskAssessmentResponse assess(@Valid RfFeaturesRequest request) {

        MedicalDevice device = deviceRepository.findById(request.getDeviceId())
                .orElseThrow(() -> new RuntimeException(
                        "Device not found with ID: " + request.getDeviceId()));

        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User requestedBy = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        RfFlaskRequest flaskRequest = RfFlaskRequest.builder()
                .features(buildFeaturesMap(request))
                .build();

        RfFlaskResponse flaskResponse = rfAiClient.predict(flaskRequest);

        RiskAssessment assessment;

        if (flaskResponse == null) {
            // circuit breaker fired — persist fallback record
            assessment = RiskAssessment.builder()
                    .device(device)
                    .requestedBy(requestedBy)
                    .predictedClass(null)
                    .predictedLabel(null)
                    .confidenceScore(null)
                    .recommendation(null)
                    .modelVersion(MODEL_VERSION)
                    .status("FALLBACK")
                    .errorMessage("RF ML service unavailable")
                    .build();

            RiskAssessment saved = riskAssessmentRepository.save(assessment);
            log.warn("RF assessment fallback saved: id={}, deviceId={}", saved.getId(), device.getId());

            return RiskAssessmentResponse.builder()
                    .assessmentId(saved.getId())
                    .deviceId(device.getId())
                    .deviceName(device.getName())
                    .status("FALLBACK")
                    .recommendation("AI inference service unavailable. Manual assessment required.")
                    .modelVersion(MODEL_VERSION)
                    .createdAt(saved.getCreatedAt())
                    .build();
        }

        String recommendation = RiskRecommendationMapper.resolve(flaskResponse.getPredictedClass());

        assessment = RiskAssessment.builder()
                .device(device)
                .requestedBy(requestedBy)
                .predictedClass(flaskResponse.getPredictedClass())
                .predictedLabel(flaskResponse.getPredictedLabel())
                .confidenceScore(flaskResponse.getConfidenceScore())
                .recommendation(recommendation)
                .modelVersion(MODEL_VERSION)
                .status("SUCCESS")
                .build();

        RiskAssessment saved = riskAssessmentRepository.save(assessment);
        log.info("RF assessment saved: id={}, deviceId={}, riskClass={}, label={}",
                saved.getId(), device.getId(),
                saved.getPredictedClass(), saved.getPredictedLabel());

        return toResponse(saved, device);
    }

    @Transactional(readOnly = true)
    public Page<RiskAssessmentResponse> getByDevice(Integer deviceId, Pageable pageable) {
        MedicalDevice device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException(
                        "Device not found with ID: " + deviceId));

        return riskAssessmentRepository.findByDeviceId(deviceId, pageable)
                .map(a -> toResponse(a, device));
    }

    @Transactional(readOnly = true)
    public Page<RiskAssessmentResponse> getAll(Pageable pageable) {
        log.info("Fetching all risk assessments — page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());
        return riskAssessmentRepository.findAll(pageable)
                .map(a -> toResponse(a, a.getDevice()));
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    /**
     * Builds the features map in exact field order the RF model expects.
     * Key names must match Python training column names exactly.
     */
    private Map<String, Object> buildFeaturesMap(RfFeaturesRequest r) {
        Map<String, Object> features = new LinkedHashMap<>();
        features.put("Device_Type",          r.getDevice_Type());
        features.put("Manufacturer",         r.getManufacturer());
        features.put("Model",                r.getModel());
        features.put("Country",              r.getCountry());
        features.put("Age",                  r.getAge());
        features.put("Maintenance_Cost",     r.getMaintenance_Cost());
        features.put("Downtime",             r.getDowntime());
        features.put("Maintenance_Frequency",r.getMaintenance_Frequency());
        features.put("Failure_Event_Count",  r.getFailure_Event_Count());
        features.put("Maintenance_Class",    r.getMaintenance_Class());
        features.put("Operational_Hours_Est",r.getOperational_Hours_Est());
        features.put("Expected_Lifespan_Est",r.getExpected_Lifespan_Est());
        features.put("MTBF",                 r.getMTBF());
        features.put("Cost_Per_Hour",        r.getCost_Per_Hour());
        features.put("Lifespan_Usage_Ratio", r.getLifespan_Usage_Ratio());
        return features;
    }

    private RiskAssessmentResponse toResponse(RiskAssessment a, MedicalDevice device) {
        return RiskAssessmentResponse.builder()
                .assessmentId(a.getId())
                .deviceId(device.getId())
                .deviceName(device.getName())
                .riskClass(a.getPredictedClass())
                .riskLabel(a.getPredictedLabel())
                .confidence(a.getConfidenceScore())
                .recommendation(a.getRecommendation())
                .modelVersion(a.getModelVersion())
                .status(a.getStatus())
                .createdAt(a.getCreatedAt())
                .build();
    }
}