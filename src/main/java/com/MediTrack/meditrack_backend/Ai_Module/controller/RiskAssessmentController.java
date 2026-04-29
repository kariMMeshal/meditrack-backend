package com.MediTrack.meditrack_backend.Ai_Module.controller;

import com.MediTrack.meditrack_backend.Ai_Module.dto.RfFeaturesRequest;
import com.MediTrack.meditrack_backend.Ai_Module.dto.RiskAssessmentResponse;
import com.MediTrack.meditrack_backend.Ai_Module.service.RiskAssessmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/risk-assessment")
@RequiredArgsConstructor
public class RiskAssessmentController {

    private final RiskAssessmentService riskAssessmentService;

    @PostMapping("/general")
    public ResponseEntity<RiskAssessmentResponse> assess(
            @Valid @RequestBody RfFeaturesRequest request) {
        return ResponseEntity.ok(riskAssessmentService.assess(request));
    }

    @GetMapping("/device/{deviceId}")
    public ResponseEntity<Page<RiskAssessmentResponse>> getByDevice(
            @PathVariable Integer deviceId,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(riskAssessmentService.getByDevice(deviceId, pageable));
    }

    @GetMapping
    public ResponseEntity<Page<RiskAssessmentResponse>> getAll(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(riskAssessmentService.getAll(pageable));
    }
}