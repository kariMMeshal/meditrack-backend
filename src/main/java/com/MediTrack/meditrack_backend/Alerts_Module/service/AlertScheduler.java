package com.MediTrack.meditrack_backend.Alerts_Module.service;


import com.MediTrack.meditrack_backend.Asset_Management_Module.entity.MedicalDevice;
import com.MediTrack.meditrack_backend.Asset_Management_Module.repository.MedicalDeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Runs periodic compliance checks on all active devices.
 * Checks are idempotent — AlertService deduplicates before saving.
 *
 * Schedule:
 *   - Device checks: every hour (fixedRate = 3_600_000 ms)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AlertScheduler {

    private static final int CLEANING_INTERVAL_HOURS = 24;   // alert if not cleaned in 24h
    private static final int DEFAULT_STERILIZATION_HOURS = 48; // default if not set on device

    private final MedicalDeviceRepository deviceRepository;
    private final AlertGenerator alertGenerator;

    @Scheduled(fixedRate = 3_600_000) // every 1 hour
    public void runDeviceComplianceChecks() {
        log.info("AlertScheduler — starting device compliance checks");

        List<MedicalDevice> devices = deviceRepository.findAll();
        int sterilizationAlerts = 0;
        int usageAlerts = 0;
        int cleaningAlerts = 0;

        for (MedicalDevice device : devices) {
            sterilizationAlerts += checkSterilization(device) ? 1 : 0;
            usageAlerts        += checkUsageHours(device) ? 1 : 0;
            cleaningAlerts     += checkCleaning(device) ? 1 : 0;
        }

        log.info("AlertScheduler — done. sterilization={}, usage={}, cleaning={}",
                sterilizationAlerts, usageAlerts, cleaningAlerts);
    }

    // ── Check Methods ─────────────────────────────────────────────────

    private boolean checkSterilization(MedicalDevice device) {
        if (device.getLastSterilizationDate() == null) return false;

        int intervalHours = device.getSterilizationIntervalHours() != null
                ? device.getSterilizationIntervalHours()
                : DEFAULT_STERILIZATION_HOURS;

        LocalDateTime dueAt = device.getLastSterilizationDate().plusHours(intervalHours);

        if (LocalDateTime.now().isAfter(dueAt)) {
            long hoursOverdue = ChronoUnit.HOURS.between(dueAt, LocalDateTime.now());
            log.warn("Sterilization overdue — deviceId={}, hoursOverdue={}", device.getId(), hoursOverdue);
            alertGenerator.sterilizationOverdue(device.getId(), device.getName(), hoursOverdue);
            return true;
        }
        return false;
    }

    private boolean checkUsageHours(MedicalDevice device) {
        if (device.getUsageHours() == null || device.getMaxUsageHours() == null) return false;

        if (device.getUsageHours() > device.getMaxUsageHours()) {
            log.warn("Usage hours exceeded — deviceId={}, usage={}, max={}",
                    device.getId(), device.getUsageHours(), device.getMaxUsageHours());
            alertGenerator.usageHoursExceeded(
                    device.getId(), device.getName(),
                    device.getUsageHours(), device.getMaxUsageHours());
            return true;
        }
        return false;
    }

    private boolean checkCleaning(MedicalDevice device) {
        if (device.getLastCleanedDate() == null) return false;

        long hoursSinceClean = ChronoUnit.HOURS.between(
                device.getLastCleanedDate(), LocalDateTime.now());

        if (hoursSinceClean > CLEANING_INTERVAL_HOURS) {
            log.warn("Cleaning overdue — deviceId={}, hoursSince={}", device.getId(), hoursSinceClean);
            alertGenerator.cleaningOverdue(device.getId(), device.getName(), hoursSinceClean);
            return true;
        }
        return false;
    }
}