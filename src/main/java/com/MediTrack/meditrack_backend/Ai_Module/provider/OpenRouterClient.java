package com.MediTrack.meditrack_backend.Ai_Module.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Calls Google OpenRouter generateContent endpoint.
 * Model: OpenAI: gpt-oss-120b(free tier, fast).
 * Used as fallback when Groq is unavailable.
 */
@Component
@Slf4j
public class OpenRouterClient implements AiProvider {

    private final RestClient restClient;
    private final String model;
    private final String apiKey;
    private final boolean enabled;

    public OpenRouterClient(
            @Value("${ai.openrouter.api-key:}") String apiKey,
            @Value("${ai.openrouter.model:openai/gpt-oss-120b:free}") String model,
            @Value("${ai.openrouter.enabled:false}") boolean enabled
    ) {
        this.apiKey = apiKey;
        this.model = model;
        this.enabled = enabled && !apiKey.isBlank();

        this.restClient = RestClient.builder()
                .baseUrl("https://openrouter.ai/api/v1")
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    @Override
    public String complete(String systemPrompt, String userMessage) {
        if (!enabled) {
            throw new RuntimeException("OpenRouter provider disabled or API key missing");
        }

        log.info("Sending request to OpenRouter — model={}", model);

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userMessage)
                ),
                "temperature", 0.3
        );

        Map response = restClient.post()
                .uri("/chat/completions")
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        return extract(response);
    }

    private String extract(Map response) {
        try {
            List<Map> choices = (List<Map>) response.get("choices");
            Map message = (Map) choices.get(0).get("message");
            return (String) message.get("content");
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse OpenRouter response: " + e.getMessage());
        }
    }

    @Override
    public String providerName() {
        return "openrouter";
    }
}