package com.MediTrack.meditrack_backend.Ai_Module.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Detects and blocks prompt injection and jailbreak attempts before
 * they reach the LLM provider.
 *
 * Defense strategy: pattern matching on known attack signatures.
 * This is a first-line defense — models should also have strict system prompts.
 *
 * Patterns checked:
 * - Role override attempts ("ignore previous instructions", "you are now")
 * - System prompt extraction ("reveal your prompt", "what are your instructions")
 * - Jailbreak triggers ("DAN", "do anything now", "act as")
 * - Medical boundary bypass ("ignore medical rules", "give me a diagnosis")
 */
@Component
@Slf4j
public class PromptSanitizer {

    private static final List<Pattern> INJECTION_PATTERNS = List.of(
            Pattern.compile("ignore\\s+(all\\s+)?(previous|prior|above)\\s+instructions?", Pattern.CASE_INSENSITIVE),
            Pattern.compile("you\\s+are\\s+now\\s+(a|an|the)?\\s*\\w+", Pattern.CASE_INSENSITIVE),
            Pattern.compile("act\\s+as\\s+(a|an|the)?\\s*\\w+", Pattern.CASE_INSENSITIVE),
            Pattern.compile("pretend\\s+(you\\s+are|to\\s+be)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(reveal|show|print|display|output|tell me)\\s+(your\\s+)?(system\\s+)?(prompt|instructions|rules)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("jailbreak", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bDAN\\b"),                               // "Do Anything Now" jailbreak
            Pattern.compile("do\\s+anything\\s+now", Pattern.CASE_INSENSITIVE),
            Pattern.compile("without\\s+(any\\s+)?(restrictions?|limits?|rules?|guidelines?)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("ignore\\s+(medical|safety|ethical)\\s+(rules?|guidelines?|restrictions?)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("give\\s+me\\s+(a\\s+)?(diagnosis|prescription|treatment)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("override\\s+(your\\s+)?(instructions?|programming|rules?)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(forget|disregard|discard)\\s+(everything|all)", Pattern.CASE_INSENSITIVE)
    );

    /**
     * Validates user input before sending to LLM.
     *
     * @param input raw user message
     * @throws SecurityException if injection attempt detected
     */
    public void validate(String input) {
        if (input == null || input.isBlank()) {
            return;
        }

        // Guard against oversized inputs that could overwhelm the context window
        if (input.length() > 2000) {
            throw new SecurityException("Message exceeds maximum allowed length");
        }

        for (Pattern pattern : INJECTION_PATTERNS) {
            if (pattern.matcher(input).find()) {
                log.warn("Prompt injection attempt detected — pattern={}", pattern.pattern());
                throw new SecurityException("Your message contains content that cannot be processed");
            }
        }
    }

    /**
     * Returns true if input is safe, false if injection detected.
     * Non-throwing variant for use in conditional logic.
     */
    public boolean isSafe(String input) {
        try {
            validate(input);
            return true;
        } catch (SecurityException ex) {
            return false;
        }
    }
}