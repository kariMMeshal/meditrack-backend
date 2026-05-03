package com.MediTrack.meditrack_backend.Ai_Module.entity;

import com.MediTrack.meditrack_backend.Auth_Module.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Optional — if report is tied to a specific session
    @Column(name = "session_id")
    private String sessionId;

    // The full markdown report returned by the AI
    @Column(name = "report_content", columnDefinition = "TEXT", nullable = false)
    private String reportContent;

    // Which provider generated this report: groq, OpenRouter, fallback
    @Column(name = "provider", nullable = false)
    private String provider;

    // Report type: CHAT_SUMMARY, DEVICE_REVIEW, FULL_OVERVIEW
    @Column(name = "report_type", nullable = false)
    private String reportType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}