package com.MediTrack.meditrack_backend.Alerts_Module.service;


import com.MediTrack.meditrack_backend.Ai_Module.provider.AlertAiProvider;
import com.MediTrack.meditrack_backend.Alerts_Module.dto.AlertDTO;
import com.MediTrack.meditrack_backend.Alerts_Module.dto.AlertReportDTO;
import com.MediTrack.meditrack_backend.Alerts_Module.dto.CreateAlertRequest;
import com.MediTrack.meditrack_backend.Alerts_Module.entity.Alert;
import com.MediTrack.meditrack_backend.Auth_Module.entity.User;
import com.MediTrack.meditrack_backend.Alerts_Module.repository.AlertRepository;
import com.MediTrack.meditrack_backend.Auth_Module.repository.UserRepository;
import com.MediTrack.meditrack_backend.Alerts_Module.enums.AlertStatus;
import com.MediTrack.meditrack_backend.Alerts_Module.enums.AlertType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private final AlertRepository alertRepository;
    private final AlertAiProvider alertAiProvider;
    private final UserRepository userRepository;

    // ── Create ────────────────────────────────────────────────────────

    @Transactional
    public AlertDTO createAlert(CreateAlertRequest request) {
        // Deduplication — skip if an identical active alert already exists
        if (request.getDeviceId() != null &&
                alertRepository.existsByDeviceIdAndTypeAndStatusIn(
                        request.getDeviceId(),
                        request.getType(),
                        List.of(AlertStatus.NEW, AlertStatus.ACKNOWLEDGED))) {

            log.info("Duplicate alert skipped — deviceId={}, type={}",
                    request.getDeviceId(), request.getType());

            throw new IllegalStateException("Duplicate alert suppressed");
        }

        Alert alert = Alert.builder()
                .type(request.getType())
                .severity(request.getSeverity())
                .status(AlertStatus.NEW)
                .message(request.getMessage())
                .deviceId(request.getDeviceId())
                .userId(request.getUserId())
                .metadata(request.getMetadata())
                .build();

        Alert saved = alertRepository.save(alert);
        log.info("Alert created — id={}, type={}, severity={}", saved.getId(), saved.getType(), saved.getSeverity());

        // Generate AI explanation asynchronously — never blocks alert creation
        enrichWithAiExplanation(saved);

        return toDTO(saved);
    }

    // ── Status transitions ────────────────────────────────────────────

    @Transactional
    public AlertDTO markAsRead(Long alertId) {
        Alert alert = findById(alertId);
        alert.setStatus(AlertStatus.READ);
        log.info("Alert marked as READ — id={}", alertId);
        return toDTO(alertRepository.save(alert));
    }

    @Transactional
    public AlertDTO acknowledgeAlert(Long alertId) {
        Alert alert = findById(alertId);
        alert.setStatus(AlertStatus.ACKNOWLEDGED);
        log.info("Alert ACKNOWLEDGED — id={}", alertId);
        return toDTO(alertRepository.save(alert));
    }

    @Transactional
    public AlertDTO resolveAlert(Long alertId) {
        Alert alert = findById(alertId);
        alert.setStatus(AlertStatus.RESOLVED);
        alert.setResolvedAt(LocalDateTime.now());
        log.info("Alert RESOLVED — id={}", alertId);
        return toDTO(alertRepository.save(alert));
    }

    // ── Queries ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AlertDTO getById(Long alertId) {
        return toDTO(findById(alertId));
    }

    @Transactional(readOnly = true)
    public Page<AlertDTO> getByDevice(Integer deviceId, Pageable pageable) {
        return alertRepository.findByDeviceId(deviceId, pageable).map(this::toDTO);
    }

    /**
     * Role-based alert access:
     * - ROLE_ADMIN       → all alerts
     * - ROLE_BIOMED      → device + AI alerts
     * - ROLE_USER        → only own personal alerts
     */
    @Transactional(readOnly = true)
    public Page<AlertDTO> getAlertsByCurrentUser(Pageable pageable) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(r -> r.getRole().equals("ROLE_ADMIN"));
        boolean isBiomed = user.getRoles().stream()
                .anyMatch(r -> r.getRole().equals("ROLE_BIOMED"));

        if (isAdmin) {
            log.debug("ADMIN alert access — user={}", username);
            return alertRepository.findAll(pageable).map(this::toDTO);
        }

        if (isBiomed) {
            log.debug("BIOMED alert access — user={}", username);
            return alertRepository.findByTypeIn(
                            List.of(AlertType.DEVICE_ALERT, AlertType.AI_ALERT), pageable)
                    .map(this::toDTO);
        }

        log.debug("USER alert access — userId={}", user.getId());
        return alertRepository.findByUserId(user.getId(), pageable).map(this::toDTO);
    }

    // ── AI Report ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AlertReportDTO generateReport(Integer deviceId) {
        List<Alert> alerts = deviceId != null
                ? alertRepository.findByDeviceIdAndTypeAndStatus(
                deviceId, AlertType.DEVICE_ALERT, AlertStatus.NEW)
                : alertRepository.findAll();

        String context = buildReportContext(alerts, deviceId);
        AlertAiProvider.AiResult result = alertAiProvider.generateReport(context);

        String raw = result.content();
        return AlertReportDTO.builder()
                .summary(extractSection(raw, "Summary"))
                .keyFindings(extractSection(raw, "Key Findings"))
                .riskLevel(extractSection(raw, "Risk Level"))
                .recommendedActions(extractSection(raw, "Recommended Actions"))
                .disclaimer(extractSection(raw, "Disclaimer"))
                .provider(result.provider())
                .generatedAt(LocalDateTime.now())
                .build();
    }

    // ── Private Helpers ───────────────────────────────────────────────

    private Alert findById(Long id) {
        return alertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alert not found with ID: " + id));
    }

    /**
     * Adds AI explanation to an already-saved alert.
     * Called after save — does not block the original createAlert response.
     */
    private void enrichWithAiExplanation(Alert alert) {
        try {
            AlertAiProvider.AiResult result = alertAiProvider.explain(
                    alert.getMessage(), alert.getMetadata());
            alert.setAiExplanation(result.content());
            alert.setAiProvider(result.provider());
            alertRepository.save(alert);
            log.info("AI explanation added — alertId={}, provider={}", alert.getId(), result.provider());
        } catch (Exception ex) {
            log.error("Failed to enrich alert with AI explanation — alertId={}: {}", alert.getId(), ex.getMessage());
        }
    }

    private String buildReportContext(List<Alert> alerts, Integer deviceId) {
        StringBuilder sb = new StringBuilder();
        if (deviceId != null) sb.append("Device ID: ").append(deviceId).append("\n\n");
        sb.append("Total alerts: ").append(alerts.size()).append("\n\n");
        alerts.forEach(a -> sb.append("[")
                .append(a.getSeverity()).append("] ")
                .append(a.getType()).append(": ")
                .append(a.getMessage())
                .append(" (").append(a.getStatus()).append(")")
                .append("\n"));
        return sb.toString();
    }

    private String extractSection(String raw, String section) {
        try {
            int start = raw.indexOf("## " + section);
            if (start == -1) return raw;
            int end = raw.indexOf("## ", start + 3);
            String block = end == -1 ? raw.substring(start) : raw.substring(start, end);
            return block.replace("## " + section, "").trim();
        } catch (Exception ex) {
            return raw;
        }
    }

    private AlertDTO toDTO(Alert a) {
        return AlertDTO.builder()
                .id(a.getId())
                .type(a.getType())
                .severity(a.getSeverity())
                .status(a.getStatus())
                .message(a.getMessage())
                .deviceId(a.getDeviceId())
                .userId(a.getUserId())
                .metadata(a.getMetadata())
                .aiExplanation(a.getAiExplanation())
                .aiProvider(a.getAiProvider())
                .createdAt(a.getCreatedAt())
                .resolvedAt(a.getResolvedAt())
                .build();
    }
}