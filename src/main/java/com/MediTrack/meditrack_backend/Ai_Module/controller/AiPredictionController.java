package com.MediTrack.meditrack_backend.Ai_Module.controller;


import com.MediTrack.meditrack_backend.Ai_Module.dto.AiHealthDTO;
import com.MediTrack.meditrack_backend.Ai_Module.dto.AiPredictionDTO;
import com.MediTrack.meditrack_backend.Ai_Module.dto.PredictionRequestDTO;
import com.MediTrack.meditrack_backend.Ai_Module.service.AiPredictionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiPredictionController {

    private final AiPredictionService aiPredictionService;

    @PostMapping("/predict")
    public ResponseEntity<AiPredictionDTO> predict(
            @Valid @RequestBody PredictionRequestDTO request) {
        return ResponseEntity.ok(aiPredictionService.predict(request));
    }

    @GetMapping("/device-predictions/{deviceId}")
    public ResponseEntity<Page<AiPredictionDTO>> getByDevice(
            @PathVariable Integer deviceId,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(aiPredictionService.getByDevice(deviceId, pageable));
    }

    @GetMapping("/predictions")
    public Page<AiPredictionDTO> getAll(Pageable pageable) {
        return aiPredictionService.getAllPredictions(pageable);
    }

    // AI-4: Health Check
    @GetMapping("/health")
    public AiHealthDTO health() {
        return aiPredictionService.checkHealth();
    }
}
