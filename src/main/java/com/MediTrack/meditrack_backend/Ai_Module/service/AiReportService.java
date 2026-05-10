package com.MediTrack.meditrack_backend.Ai_Module.service;

import com.MediTrack.meditrack_backend.Ai_Module.dto.ReportRequest;
import com.MediTrack.meditrack_backend.Ai_Module.dto.ReportResponse;
import com.MediTrack.meditrack_backend.Ai_Module.entity.AiReport;
import com.MediTrack.meditrack_backend.Ai_Module.entity.ChatMessage;
import com.MediTrack.meditrack_backend.Ai_Module.provider.AiProviderRouter;
import com.MediTrack.meditrack_backend.Ai_Module.provider.MedicalPrompts;
import com.MediTrack.meditrack_backend.Ai_Module.repository.AiReportRepository;
import com.MediTrack.meditrack_backend.Ai_Module.repository.ChatMessageRepository;
import com.MediTrack.meditrack_backend.Auth_Module.entity.User;
import com.MediTrack.meditrack_backend.Auth_Module.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiReportService {

    private final AiProviderRouter providerRouter;
    private final AiReportRepository reportRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final PromptSanitizer promptSanitizer;

    @Transactional
    public ReportResponse generateReport(ReportRequest request) {
        validateRequest(request);
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        log.info("Generating report — user={}, session={}, type={}",
                username, request.getSessionId(), request.getReportType());

        // Fetch chat history for this session
        List<ChatMessage> history = chatMessageRepository
                .findBySessionIdOrderByCreatedAtAsc(request.getSessionId());

        // Build aggregated prompt from all available inputs
        String aggregatedPrompt = buildReportPrompt(request, history);

        // Route through Groq → OpenRouter → fallback
        AiProviderRouter.AiProviderResult result = providerRouter.complete(
                MedicalPrompts.REPORT_SYSTEM,
                aggregatedPrompt
        );

        // Persist the generated report
        AiReport saved = reportRepository.save(
                AiReport.builder()
                        .user(user)
                        .sessionId(request.getSessionId())
                        .reportContent(result.content())
                        .provider(result.provider())
                        .reportType(request.getReportType())
                        .build()
        );

        log.info("Report saved — id={}, provider={}", saved.getId(), result.provider());

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<ReportResponse> getMyReports(Pageable pageable) {
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return reportRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ReportResponse getReportById(Long reportId) {
        AiReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found with ID: " + reportId));
        return toResponse(report);
    }

    // ── Prompt Builder ────────────────────────────────────────────────

    /**
     * Assembles all available context into a structured prompt.
     * The AI receives exactly what data is present — nothing is fabricated.
     */
    private String buildReportPrompt(ReportRequest request, List<ChatMessage> history) {
        StringBuilder sb = new StringBuilder();

        sb.append("=== REPORT GENERATION REQUEST ===\n\n");
        sb.append("Report Type: ").append(request.getReportType()).append("\n\n");

        // Chat history section
        if (!history.isEmpty()) {
            sb.append("=== CHAT HISTORY (").append(history.size()).append(" messages) ===\n");
            for (ChatMessage msg : history) {
                String safeUserMsg = sanitize(msg.getUserMessage());

                sb.append("[").append(msg.getCreatedAt()).append("]\n");
                sb.append("User: ").append(safeUserMsg).append("\n");
                sb.append("Assistant: ").append(msg.getAiResponse()).append("\n\n");
            }
        } else {
            sb.append("=== CHAT HISTORY ===\nNo chat history found for this session.\n\n");
        }

        // Device logs section
        if (request.getDeviceLogs() != null && !request.getDeviceLogs().isBlank()) {
            sb.append("=== DEVICE LOGS / TELEMETRY ===\n");
            sb.append(request.getDeviceLogs()).append("\n\n");
        }

        // Reported events section
        if (request.getReportedEvents() != null && !request.getReportedEvents().isBlank()) {
            sb.append("=== REPORTED EVENTS / SYMPTOMS ===\n");
            sb.append(request.getReportedEvents()).append("\n\n");
        }

        // Additional context
        if (request.getAdditionalContext() != null && !request.getAdditionalContext().isBlank()) {
            sb.append("=== ADDITIONAL CONTEXT ===\n");
            sb.append(request.getAdditionalContext()).append("\n\n");
        }

        sb.append("=== END OF INPUT ===\n");
        sb.append("Please generate the structured summary report now.");

        return sb.toString();
    }

    // Validation and sanitization .
    private void validateRequest(ReportRequest request) {
        promptSanitizer.validate(request.getReportedEvents());
        promptSanitizer.validate(request.getAdditionalContext());
        promptSanitizer.validate(request.getDeviceLogs());
        promptSanitizer.validate(request.getReportType());
        promptSanitizer.validate(request.getSessionId());
    }
    private String sanitize(String input) {
        promptSanitizer.validate(input);
        return input;
    }


    private ReportResponse toResponse(AiReport r) {
        return ReportResponse.builder()
                .reportId(r.getId())
                .sessionId(r.getSessionId())
                .reportType(r.getReportType())
                .reportContent(r.getReportContent())
                .provider(r.getProvider())
                .createdAt(r.getCreatedAt())
                .build();
    }
}