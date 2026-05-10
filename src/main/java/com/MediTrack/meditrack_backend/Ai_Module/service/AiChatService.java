package com.MediTrack.meditrack_backend.Ai_Module.service;

import com.MediTrack.meditrack_backend.Ai_Module.dto.ChatRequest;
import com.MediTrack.meditrack_backend.Ai_Module.dto.ChatResponse;
import com.MediTrack.meditrack_backend.Ai_Module.entity.ChatMessage;
import com.MediTrack.meditrack_backend.Ai_Module.provider.AiProviderRouter;
import com.MediTrack.meditrack_backend.Ai_Module.provider.MedicalPrompts;
import com.MediTrack.meditrack_backend.Ai_Module.repository.ChatMessageRepository;
import com.MediTrack.meditrack_backend.Auth_Module.entity.User;
import com.MediTrack.meditrack_backend.Auth_Module.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatService {

    private final AiProviderRouter providerRouter;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final PromptSanitizer promptSanitizer;

    @Transactional
    public ChatResponse chat(ChatRequest request) {
        promptSanitizer.validate(request.getMessage());
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        log.info("Chat request — user={}, session={}", username, request.getSessionId());

        // Route through Groq → OpenRouter → fallback
        AiProviderRouter.AiProviderResult result = providerRouter.complete(
                MedicalPrompts.CHAT_SYSTEM,
                request.getMessage()
        );

        // Persist the full exchange
        ChatMessage saved = chatMessageRepository.save(
                ChatMessage.builder()
                        .user(user)
                        .sessionId(request.getSessionId())
                        .userMessage(request.getMessage())
                        .aiResponse(result.content())
                        .provider(result.provider())
                        .build()
        );

        log.info("Chat message saved — id={}, provider={}", saved.getId(), result.provider());

        return ChatResponse.builder()
                .messageId(saved.getId())
                .sessionId(saved.getSessionId())
                .userMessage(saved.getUserMessage())
                .aiResponse(saved.getAiResponse())
                .provider(saved.getProvider())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public Page<ChatResponse> getHistory(Pageable pageable) {
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return chatMessageRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(this::toResponse);
    }

    private ChatResponse toResponse(ChatMessage m) {
        return ChatResponse.builder()
                .messageId(m.getId())
                .sessionId(m.getSessionId())
                .userMessage(m.getUserMessage())
                .aiResponse(m.getAiResponse())
                .provider(m.getProvider())
                .createdAt(m.getCreatedAt())
                .build();
    }
}