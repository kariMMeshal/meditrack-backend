package com.MediTrack.meditrack_backend.service;



import com.MediTrack.meditrack_backend.model.dto.alerts.CreateAlertRequest;
import com.MediTrack.meditrack_backend.util.enums.AlertSeverity;
import com.MediTrack.meditrack_backend.util.enums.AlertType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Central factory for alert creation.
 * All alert generation paths go through here — keeps message formatting
 * and severity logic in one place and easy to audit.
 *
 * Sources that call this:
 * - AiPredictionService  (LSTM failure prediction)
 * - RiskAssessmentService (RF risk classifier)
 * - AlertScheduler       (periodic device checks)
 * - AuthServiceImpl      (login events)
 * - UserServiceImpl      (user management events)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AlertGenerator {

    private final AlertService alertService;

    // ── AI / ML Alerts ────────────────────────────────────────────────

    /**
     * Called by AiPredictionService after LSTM inference.
     * prediction=1 means imminent failure within 72 hours.
     */
    public void fromLstmPrediction(Integer deviceId, int prediction,
                                   double probability, String deviceName) {
        if (prediction != 1) return; // only alert on positive prediction

        AlertSeverity severity = probability >= 0.99 ? AlertSeverity.CRITICAL : AlertSeverity.WARNING;
        String message = String.format(
                "LSTM model predicts imminent failure for device '%s' within 72 hours. " +
                        "Confidence: %.1f%%", deviceName, probability * 100);

        String metadata = String.format(
                "{\"model\":\"lstm_v1\",\"probability\":%.4f,\"prediction\":%d,\"deviceId\":%d}",
                probability, prediction, deviceId);

        log.info("Generating AI_ALERT from LSTM prediction — deviceId={}, probability={}", deviceId, probability);
        alertService.createAlert(CreateAlertRequest.builder()
                .type(AlertType.AI_ALERT)
                .severity(severity)
                .message(message)
                .deviceId(deviceId)
                .metadata(metadata)
                .build());
    }

    /**
     * Called by RiskAssessmentService after RF inference.
     * riskClass: 0=Low, 1=Medium, 2=High
     */
    public void fromRfRiskAssessment(Integer deviceId, int riskClass,
                                     String riskLabel, double confidence, String deviceName) {
        if (riskClass == 0) return; // Low Risk — no alert needed

        AlertSeverity severity = riskClass == 2 ? AlertSeverity.CRITICAL : AlertSeverity.WARNING;
        String message = String.format(
                "RF risk model classified device '%s' as '%s'. " +
                        "Confidence: %.1f%%", deviceName, riskLabel, confidence * 100);

        String metadata = String.format(
                "{\"model\":\"rf_v1\",\"riskClass\":%d,\"riskLabel\":\"%s\",\"confidence\":%.4f,\"deviceId\":%d}",
                riskClass, riskLabel, confidence, deviceId);

        log.info("Generating AI_ALERT from RF assessment — deviceId={}, riskClass={}", deviceId, riskClass);
        alertService.createAlert(CreateAlertRequest.builder()
                .type(AlertType.AI_ALERT)
                .severity(severity)
                .message(message)
                .deviceId(deviceId)
                .metadata(metadata)
                .build());
    }

    // ── Device Alerts ─────────────────────────────────────────────────

    public void sterilizationOverdue(Integer deviceId, String deviceName, long hoursOverdue) {
        String message = String.format(
                "Device '%s' is overdue for sterilization by %d hours. Immediate action required.",
                deviceName, hoursOverdue);
        String metadata = String.format(
                "{\"deviceId\":%d,\"hoursOverdue\":%d,\"checkType\":\"STERILIZATION\"}",
                deviceId, hoursOverdue);

        log.info("Generating DEVICE_ALERT (sterilization overdue) — deviceId={}", deviceId);
        alertService.createAlert(CreateAlertRequest.builder()
                .type(AlertType.DEVICE_ALERT)
                .severity(AlertSeverity.CRITICAL)
                .message(message)
                .deviceId(deviceId)
                .metadata(metadata)
                .build());
    }

    public void usageHoursExceeded(Integer deviceId, String deviceName,
                                   double usageHours, double maxHours) {
        String message = String.format(
                "Device '%s' has exceeded maximum usage hours. " +
                        "Current: %.1f hours, Limit: %.1f hours.",
                deviceName, usageHours, maxHours);
        String metadata = String.format(
                "{\"deviceId\":%d,\"usageHours\":%.1f,\"maxHours\":%.1f,\"checkType\":\"USAGE\"}",
                deviceId, usageHours, maxHours);

        log.info("Generating DEVICE_ALERT (usage exceeded) — deviceId={}", deviceId);
        alertService.createAlert(CreateAlertRequest.builder()
                .type(AlertType.DEVICE_ALERT)
                .severity(AlertSeverity.WARNING)
                .message(message)
                .deviceId(deviceId)
                .metadata(metadata)
                .build());
    }

    public void cleaningOverdue(Integer deviceId, String deviceName, long hoursOverdue) {
        String message = String.format(
                "Device '%s' has not been cleaned for %d hours. Cleaning compliance check required.",
                deviceName, hoursOverdue);
        String metadata = String.format(
                "{\"deviceId\":%d,\"hoursOverdue\":%d,\"checkType\":\"CLEANING\"}",
                deviceId, hoursOverdue);

        log.info("Generating DEVICE_ALERT (cleaning overdue) — deviceId={}", deviceId);
        alertService.createAlert(CreateAlertRequest.builder()
                .type(AlertType.DEVICE_ALERT)
                .severity(AlertSeverity.WARNING)
                .message(message)
                .deviceId(deviceId)
                .metadata(metadata)
                .build());
    }

    // ── Security Alerts ───────────────────────────────────────────────

    public void failedLoginAttempt(String username, String ipAddress) {
        String message = String.format(
                "Multiple failed login attempts detected for user '%s' from IP: %s.",
                username, ipAddress);
        String metadata = String.format(
                "{\"username\":\"%s\",\"ip\":\"%s\",\"event\":\"FAILED_LOGIN\"}",
                username, ipAddress);

        log.info("Generating SECURITY_ALERT (failed login) — username={}", username);
        alertService.createAlert(CreateAlertRequest.builder()
                .type(AlertType.SECURITY_ALERT)
                .severity(AlertSeverity.WARNING)
                .message(message)
                .metadata(metadata)
                .build());
    }

    // ── User Activity Alerts ──────────────────────────────────────────

    public void userCreated(Integer newUserId, String newUsername, String performedBy) {
        String message = String.format(
                "New user '%s' was created by '%s'.", newUsername, performedBy);
        String metadata = String.format(
                "{\"newUserId\":%d,\"newUsername\":\"%s\",\"performedBy\":\"%s\",\"event\":\"USER_CREATED\"}",
                newUserId, newUsername, performedBy);

        alertService.createAlert(CreateAlertRequest.builder()
                .type(AlertType.USER_ACTIVITY_ALERT)
                .severity(AlertSeverity.INFO)
                .message(message)
                .userId(newUserId)
                .metadata(metadata)
                .build());
    }

    public void roleChanged(Integer userId, String username, String newRole, String performedBy) {
        String message = String.format(
                "Role '%s' was assigned to user '%s' by '%s'.", newRole, username, performedBy);
        String metadata = String.format(
                "{\"userId\":%d,\"username\":\"%s\",\"newRole\":\"%s\",\"performedBy\":\"%s\",\"event\":\"ROLE_CHANGED\"}",
                userId, username, newRole, performedBy);

        alertService.createAlert(CreateAlertRequest.builder()
                .type(AlertType.USER_ACTIVITY_ALERT)
                .severity(AlertSeverity.INFO)
                .message(message)
                .userId(userId)
                .metadata(metadata)
                .build());
    }

    // ── System Alerts ─────────────────────────────────────────────────

    public void systemEvent(String eventDescription, AlertSeverity severity) {
        alertService.createAlert(CreateAlertRequest.builder()
                .type(AlertType.SYSTEM_ALERT)
                .severity(severity)
                .message(eventDescription)
                .metadata("{\"source\":\"system\"}")
                .build());
    }
}
