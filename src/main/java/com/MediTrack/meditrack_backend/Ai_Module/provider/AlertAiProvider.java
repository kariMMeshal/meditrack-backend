package com.MediTrack.meditrack_backend.Ai_Module.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Provides AI-generated explanations for alerts.
 * Routing: Groq (primary) → OpenRouter (fallback) → safe static message
 *
 * All outputs follow medical safety rules:
 * - No diagnosis
 * - No treatment instructions
 * - Observations and recommendations only
 */
@Component
@Slf4j
public class AlertAiProvider {

    private static final String ALERT_SYSTEM_PROMPT = """
            You are a medical device monitoring assistant for MediTrack.
            
            Your role is to generate clear, structured alert explanations and reports.
            
            STRICT RULES:
            - Do NOT provide any medical diagnosis.
            - Do NOT suggest any medical treatments or medications.
            - Only explain what the alert means from a device monitoring perspective.
            - Provide general safety recommendations (non-clinical).
            - Always recommend consulting a licensed healthcare professional for clinical concerns.
            - Be concise, factual, and professional.
            """;

    private final RestClient groqClient;
    private final RestClient openRouterClient;
    private final String groqModel;
    private final String openRouterModel;
    private final boolean groqEnabled;
    private final boolean openRouterEnabled;

    public AlertAiProvider(
            @Value("${ai.groq.api-key:}") String groqKey,
            @Value("${ai.groq.model:llama3-8b-8192}") String groqModel,
            @Value("${ai.openrouter.api-key:}") String openRouterKey,
            @Value("${ai.openrouter.model:mistralai/mistral-7b-instruct}") String openRouterModel
    ) {
        this.groqModel = groqModel;
        this.openRouterModel = openRouterModel;
        this.groqEnabled = !groqKey.isBlank();
        this.openRouterEnabled = !openRouterKey.isBlank();

        this.groqClient = RestClient.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .defaultHeader("Authorization", "Bearer " + groqKey)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();

        this.openRouterClient = RestClient.builder()
                .baseUrl("https://openrouter.ai/api/v1")
                .defaultHeader("Authorization", "Bearer " + openRouterKey)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public record AiResult(String content, String provider) {}

    /**
     * Generates an AI explanation for an alert message.
     * Tries Groq first, falls back to OpenRouter, then returns safe static message.
     */
    public AiResult explain(String alertMessage, String metadata) {
        String userPrompt = buildExplanationPrompt(alertMessage, metadata);

        // ── Try Groq ──────────────────────────────────────────────────
        if (groqEnabled) {
            try {
                String result = callOpenAiCompatible(groqClient, groqModel, userPrompt);
                log.info("Alert explanation served by Groq");
                return new AiResult(result, "groq");
            } catch (Exception ex) {
                log.warn("Groq failed for alert explanation: {} — trying OpenRouter", ex.getMessage());
            }
        }

        // ── Try OpenRouter ────────────────────────────────────────────
        if (openRouterEnabled) {
            try {
                String result = callOpenAiCompatible(openRouterClient, openRouterModel, userPrompt);
                log.info("Alert explanation served by OpenRouter");
                return new AiResult(result, "openrouter");
            } catch (Exception ex) {
                log.error("OpenRouter also failed for alert explanation: {}", ex.getMessage());
            }
        }

        // ── Safe fallback ─────────────────────────────────────────────
        log.error("All AI providers failed — returning safe fallback explanation");
        return new AiResult(
                "This alert has been recorded and requires attention. " +
                        "Please review the alert details and consult the relevant team. " +
                        "For clinical concerns, contact a licensed healthcare professional.",
                "fallback"
        );
    }

    /**
     * Generates a structured summary report for a set of alerts.
     */
    public AiResult generateReport(String reportContext) {
        String reportPrompt = """
                Based on the following alert data, generate a structured summary report.
                
                Output format (use exactly these sections):
                ## Summary
                ## Key Findings
                ## Risk Level (LOW / MEDIUM / HIGH / CRITICAL)
                ## Recommended Actions (non-medical, device and process focused only)
                ## Disclaimer
                
                Alert context:
                """ + reportContext;

        // ── Try Groq ──────────────────────────────────────────────────
        if (groqEnabled) {
            try {
                String result = callOpenAiCompatible(groqClient, groqModel, reportPrompt);
                log.info("Alert report served by Groq");
                return new AiResult(result, "groq");
            } catch (Exception ex) {
                log.warn("Groq failed for report: {} — trying OpenRouter", ex.getMessage());
            }
        }

        // ── Try OpenRouter ────────────────────────────────────────────
        if (openRouterEnabled) {
            try {
                String result = callOpenAiCompatible(openRouterClient, openRouterModel, reportPrompt);
                log.info("Alert report served by OpenRouter");
                return new AiResult(result, "openrouter");
            } catch (Exception ex) {
                log.error("OpenRouter also failed for report: {}", ex.getMessage());
            }
        }

        // ── Safe fallback ─────────────────────────────────────────────
        return new AiResult(buildFallbackReport(), "fallback");
    }

    // ── Private Helpers ───────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private String callOpenAiCompatible(RestClient client, String model, String userMessage) {
        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", ALERT_SYSTEM_PROMPT),
                        Map.of("role", "user", "content", userMessage)
                ),
                "max_tokens", 512,
                "temperature", 0.2
        );

        Map response = client.post()
                .uri("/chat/completions")
                .body(body)
                .retrieve()
                .body(Map.class);

        List<Map> choices = (List<Map>) response.get("choices");
        Map message = (Map) choices.get(0).get("message");
        return (String) message.get("content");
    }

    private String buildExplanationPrompt(String alertMessage, String metadata) {
        StringBuilder sb = new StringBuilder();
        sb.append("Explain this medical device alert in 2-3 sentences:\n\n");
        sb.append("Alert: ").append(alertMessage).append("\n");
        if (metadata != null && !metadata.isBlank()) {
            sb.append("Context: ").append(metadata).append("\n");
        }
        sb.append("\nProvide a brief, factual explanation and one general safety recommendation.");
        return sb.toString();
    }

    private String buildFallbackReport() {
        return """
                ## Summary
                AI report generation is currently unavailable. Alert data has been recorded.
                
                ## Key Findings
                Unable to generate AI analysis at this time.
                
                ## Risk Level
                UNKNOWN — Manual review required.
                
                ## Recommended Actions
                Review all active alerts manually. Contact the biomedical engineering team for device alerts.
                
                ## Disclaimer
                This is an automated fallback response. No AI analysis was performed. \
                Consult licensed professionals for all clinical and engineering decisions.
                """;
    }
}