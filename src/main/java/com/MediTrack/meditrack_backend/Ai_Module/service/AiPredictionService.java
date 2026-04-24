package com.MediTrack.meditrack_backend.Ai_Module.service;


import com.MediTrack.meditrack_backend.Ai_Module.AiServiceClient;
import com.MediTrack.meditrack_backend.Ai_Module.dto.AiPredictionDTO;
import com.MediTrack.meditrack_backend.Ai_Module.dto.PredictionRequestDTO;
import com.MediTrack.meditrack_backend.Ai_Module.entity.AiPrediction;
import com.MediTrack.meditrack_backend.Ai_Module.entity.PredictRequest;
import com.MediTrack.meditrack_backend.Ai_Module.entity.PredictResponse;
import com.MediTrack.meditrack_backend.Ai_Module.repository.AiPredictionRepository;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class AiPredictionService {

    private final AiServiceClient aiServiceClient;
    private final AiPredictionRepository predictionRepository;
    private final MedicalDeviceRepository deviceRepository;
    private final UserRepository userRepository;

    @Transactional
    public AiPredictionDTO predict(@Valid PredictionRequestDTO request) {
        MedicalDevice device = deviceRepository.findById(request.getDeviceId())
                .orElseThrow(() -> new RuntimeException(
                        "Device not found with ID: " + request.getDeviceId()));

        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User requestedBy = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        PredictRequest mlRequest = PredictRequest.builder()
                .sequence(request.getSequence())
                .build();

        PredictResponse mlResponse = aiServiceClient.predict(mlRequest);

        AiPrediction prediction;

        if (mlResponse == null) {
            // circuit breaker fired — log failure
            prediction = AiPrediction.builder()
                    .device(device)
                    .probability(null)
                    .prediction(null)
                    .modelVersion("lstm_v1")
                    .thresholdUsed(0.9897)
                    .status("FALLBACK")
                    .errorMessage("ML service unavailable")
                    .requestedBy(requestedBy)
                    .build();
        } else {
            prediction = AiPrediction.builder()
                    .device(device)
                    .probability(mlResponse.getProbability())
                    .prediction(mlResponse.getPrediction())
                    .modelVersion("lstm_v1")
                    .thresholdUsed(0.9897)
                    .status("SUCCESS")
                    .requestedBy(requestedBy)
                    .build();
        }

        AiPrediction saved = predictionRepository.save(prediction);
        log.info("Prediction saved: id={}, deviceId={}, prediction={}",
                saved.getId(), device.getId(), saved.getPrediction());

        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public Page<AiPredictionDTO> getByDevice(Integer deviceId, Pageable pageable) {
        return predictionRepository.findByDeviceId(deviceId, pageable)
                .map(this::toDTO);
    }

    private AiPredictionDTO toDTO(AiPrediction p) {
        return AiPredictionDTO.builder()
                .id(p.getId())
                .deviceId(p.getDevice().getId())
                .deviceName(p.getDevice().getName())
                .probability(p.getProbability())
                .prediction(p.getPrediction())
                .failurePredicted(Integer.valueOf(1).equals(p.getPrediction()))
                .modelVersion(p.getModelVersion())
                .status(p.getStatus())
                .createdAt(p.getCreatedAt())
                .build();
    }
}