package com.MediTrack.meditrack_backend.Ai_Module.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Calls Groq's OpenAI-compatible /chat/completions endpoint.
 * Model: llama3-8b-8192 (fast, free, production-grade).
 */
@Component
@Slf4j
public class GroqClient implements AiProvider {

    private final RestClient restClient;
    private final String model;
    private final boolean enabled;

    public GroqClient(
            @Value("${ai.groq.api-key:}") String apiKey,
            @Value("${ai.groq.model:llama-3.1-8b-instant}") String model,
            @Value("${ai.groq.enabled:true}") boolean enabled
    ) {
        this.model = model;
        this.enabled = enabled && !apiKey.isBlank();

        this.restClient = RestClient.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public String complete(String systemPrompt, String userMessage) {
        if (!enabled) {
            throw new RuntimeException("Groq provider is disabled or API key not configured");
        }

        log.info("Sending request to Groq — model={}", model);

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userMessage)
                ),
                "max_tokens", 1024,
                "temperature", 0.3   // lower = more consistent, safer for medical context
        );

        Map response = restClient.post()
                .uri("/chat/completions")
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        return extractContent(response);
    }

    @Override
    public String providerName() {
        return "groq";
    }

    @SuppressWarnings("unchecked")
    private String extractContent(Map response) {
        try {
            List<Map> choices = (List<Map>) response.get("choices");
            Map message = (Map) choices.get(0).get("message");
            return (String) message.get("content");
        } catch (Exception ex) {
            throw new RuntimeException("Failed to parse Groq response: " + ex.getMessage());
        }
    }
}