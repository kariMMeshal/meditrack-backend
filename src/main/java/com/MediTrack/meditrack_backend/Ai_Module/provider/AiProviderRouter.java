package com.MediTrack.meditrack_backend.Ai_Module.provider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Routes LLM requests through providers in priority order:
 *   1. Groq  (primary  — fast, free, OpenAI-compatible)
 *   2. OpenRouter (fallback —  free tier)
 *
 * If Groq fails for any reason (timeout, quota, error),
 * the router transparently retries on OpenRouter.
 * If both fail, a safe error message is returned — never a stack trace.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AiProviderRouter {

    private final GroqClient groqClient;
    private final OpenRouterClient openRouterClient;

    /**
     * @param systemPrompt  Role + safety instructions for the model
     * @param userMessage   Actual user input or aggregated prompt
     * @return              AI response text + which provider served it
     */
    public AiProviderResult complete(String systemPrompt, String userMessage) {

        // ── Try Groq first ───────────────────────────────────────────
        try {
            String response = groqClient.complete(systemPrompt, userMessage);
            log.info("AI response served by Groq");
            return new AiProviderResult(response, "groq");
        } catch (Exception groqEx) {
            log.warn("Groq failed: {} — falling back to OpenRouter", groqEx.getMessage());
        }

        // ── Fallback to OpenRouter ───────────────────────────────────────
        try {
            String response = openRouterClient.complete(systemPrompt, userMessage);
            log.info("AI response served by OpenRouter (fallback)");
            return new AiProviderResult(response, "OpenAI: gpt-oss-120b");
        } catch (Exception openAiEx) {
            log.error("OpenRouter also failed: {}", openAiEx.getMessage());
        }

        // ── Both failed — safe fallback message ──────────────────────
        log.error("All AI providers failed — returning safe fallback response");
        return new AiProviderResult(
                "I'm sorry, I'm currently unable to process your request. " +
                        "Please try again in a few moments. " +
                        "If you have a medical concern, please consult a licensed healthcare professional.",
                "fallback"
        );
    }

    public record AiProviderResult(String content, String provider) {}
}