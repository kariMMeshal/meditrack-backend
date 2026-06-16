package com.MediTrack.meditrack_backend.Ai_Module.service;

import com.MediTrack.meditrack_backend.Ai_Module.RfAiClient;
import com.MediTrack.meditrack_backend.Ai_Module.dto.RfFeaturesRequest;
import com.MediTrack.meditrack_backend.Ai_Module.dto.RfFlaskRequest;
import com.MediTrack.meditrack_backend.Ai_Module.dto.RfFlaskResponse;
import com.MediTrack.meditrack_backend.Ai_Module.dto.RiskAssessmentResponse;
import com.MediTrack.meditrack_backend.Ai_Module.entity.RiskAssessment;
import com.MediTrack.meditrack_backend.Ai_Module.repository.RiskAssessmentRepository;
import com.MediTrack.meditrack_backend.Alerts_Module.enums.AlertSeverity;
import com.MediTrack.meditrack_backend.Alerts_Module.service.AlertGenerator;
import com.MediTrack.meditrack_backend.Asset_Management_Module.entity.MedicalDevice;
import com.MediTrack.meditrack_backend.Auth_Module.entity.User;
import com.MediTrack.meditrack_backend.Asset_Management_Module.repository.MedicalDeviceRepository;
import com.MediTrack.meditrack_backend.Auth_Module.repository.UserRepository;
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
    private static final String FALLBACK_RECOMMENDATION =
            "AI inference service unavailable. Manual assessment required.";

    private final RfAiClient rfAiClient;
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final MedicalDeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final AlertGenerator alertGenerator;

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

            log.warn("RF prediction returned null (service unavailable or circuit breaker open) — deviceId={}",
                    device.getId());

            alertGenerator.systemEvent(
                    "RF risk model unavailable for device " + device.getName(),
                    AlertSeverity.WARNING
            );

            assessment = RiskAssessment.builder()
                    .device(device)
                    .requestedBy(requestedBy)
                    .predictedClass(0)
                    .predictedLabel("Unknown")
                    .confidenceScore(0.0)
                    .recommendation(FALLBACK_RECOMMENDATION)
                    .modelVersion(MODEL_VERSION)
                    .status("FALLBACK")
                    .errorMessage("RF ML service unavailable")
                    .build();

            RiskAssessment saved = riskAssessmentRepository.save(assessment);

            return toResponse(saved, device);
        }

        Double confidence = flaskResponse.getConfidenceScore() != null
                ? flaskResponse.getConfidenceScore()
                : 0.0;

        String recommendation = RiskRecommendationMapper.resolve(flaskResponse.getPredictedClass());

        assessment = RiskAssessment.builder()
                .device(device)
                .requestedBy(requestedBy)
                .predictedClass(flaskResponse.getPredictedClass())
                .predictedLabel(flaskResponse.getPredictedLabel())
                .confidenceScore(confidence)
                .recommendation(recommendation)
                .modelVersion(MODEL_VERSION)
                .status("SUCCESS")
                .build();


        RiskAssessment saved = riskAssessmentRepository.save(assessment);

        alertGenerator.fromRfRiskAssessment(
                device.getId(),
                flaskResponse.getPredictedClass(),
                flaskResponse.getPredictedLabel(),
                confidence,
                device.getName()
        );

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

    private Map<String, Object> buildFeaturesMap(RfFeaturesRequest r) {

        if (r == null) {
            throw new IllegalArgumentException("RF request is null");
        }

        Map<String, Object> features = new LinkedHashMap<>();

        features.put("Device_Type", r.getDeviceType());
        features.put("Manufacturer", r.getManufacturer());
        features.put("Model", r.getModel());
        features.put("Country", r.getCountry());
        features.put("Age", r.getAge());
        features.put("Maintenance_Cost", r.getMaintenanceCost());
        features.put("Downtime", r.getDowntime());
        features.put("Maintenance_Frequency", r.getMaintenanceFrequency());
        features.put("Failure_Event_Count", r.getFailureEventCount());
        features.put("Maintenance_Class", encodeMaintenanceClass(r.getMaintenanceClass()));
        features.put("Operational_Hours_Est", r.getOperationalHoursEst());
        features.put("Expected_Lifespan_Est", r.getExpectedLifespanEst());
        features.put("MTBF", r.getMtbf());
        features.put("Cost_Per_Hour", r.getCostPerHour());
        features.put("Lifespan_Usage_Ratio", r.getLifespanUsageRatio());

        return features;
    }
    private int encodeMaintenanceClass(String value) {
        return switch (value.toLowerCase()) {
            case "low" -> 0;
            case "medium" -> 1;
            case "high" -> 2;
            default -> 0;
        };
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