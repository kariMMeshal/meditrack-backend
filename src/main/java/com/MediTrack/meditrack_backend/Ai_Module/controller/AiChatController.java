package com.MediTrack.meditrack_backend.Ai_Module.controller;

import com.MediTrack.meditrack_backend.Ai_Module.dto.ChatRequest;
import com.MediTrack.meditrack_backend.Ai_Module.dto.ChatResponse;
import com.MediTrack.meditrack_backend.Ai_Module.service.AiChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/chat")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    @PostMapping
    public ResponseEntity<ChatResponse> chat(
            @Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(aiChatService.chat(request));
    }

    @GetMapping("/history")
    public ResponseEntity<Page<ChatResponse>> getHistory(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(aiChatService.getHistory(pageable));
    }
}