package com.MediTrack.meditrack_backend.Ai_Module.service;

/**
 * Maps RF predicted_class (0, 1, 2) to clinical recommendation messages.
 * Isolated here so recommendation logic is easy to update without touching
 * service or controller.
 */
public final class RiskRecommendationMapper {

    private RiskRecommendationMapper() {}

    public static String resolve(int riskClass) {
        return switch (riskClass) {
            case 0 -> "Device is reliable and cost-effective. Continue standard scheduled maintenance.";
            case 1 -> "Device shows normal wear and tear. Schedule a detailed inspection within 30 days.";
            case 2 -> "Device is a financial or operational liability. Immediate maintenance review recommended.";
            default -> "Risk class unknown. Manual assessment required.";
        };
    }
}