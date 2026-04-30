package com.MediTrack.meditrack_backend.Ai_Module.repository;

import com.MediTrack.meditrack_backend.Ai_Module.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // All messages in a session — used by report generator to fetch history
    List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(String sessionId);

    // Paginated history for a user
    Page<ChatMessage> findByUserIdOrderByCreatedAtDesc(Integer userId, Pageable pageable);

    // Latest N messages in a session for context window
    List<ChatMessage> findTop10BySessionIdOrderByCreatedAtDesc(String sessionId);
}