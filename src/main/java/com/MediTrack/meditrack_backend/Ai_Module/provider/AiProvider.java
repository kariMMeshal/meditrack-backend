package com.MediTrack.meditrack_backend.Ai_Module.provider;

/**
 * Abstraction over external LLM providers (Groq, OpenRouter).
 * Each implementation sends a system prompt + user message and returns a text response.
 */
public interface AiProvider {

    /**
     * @param systemPrompt  Instructs the model how to behave (role, safety rules)
     * @param userMessage   The actual user input or aggregated report prompt
     * @return              The model's text response
     */
    String complete(String systemPrompt, String userMessage);

    /**
     * Identifier used in audit logs and response metadata.
     */
    String providerName();
}