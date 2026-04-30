package com.MediTrack.meditrack_backend.Ai_Module.controller;

import com.MediTrack.meditrack_backend.Ai_Module.dto.ReportRequest;
import com.MediTrack.meditrack_backend.Ai_Module.dto.ReportResponse;
import com.MediTrack.meditrack_backend.Ai_Module.service.AiReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/report")
@RequiredArgsConstructor
public class AiReportController {

    private final AiReportService aiReportService;

    @PostMapping("/generate")
    public ResponseEntity<ReportResponse> generate(
            @Valid @RequestBody ReportRequest request) {
        return ResponseEntity.ok(aiReportService.generateReport(request));
    }

    @GetMapping("/my")
    public ResponseEntity<Page<ReportResponse>> getMyReports(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(aiReportService.getMyReports(pageable));
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<ReportResponse> getById(@PathVariable Long reportId) {
        return ResponseEntity.ok(aiReportService.getReportById(reportId));
    }
}