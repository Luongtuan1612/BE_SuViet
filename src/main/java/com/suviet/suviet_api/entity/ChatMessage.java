package com.suviet.suviet_api.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Data
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Mỗi tin nhắn thuộc một phiên chat
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatSession session;

    @Column(length = 20, nullable = false)
    private String sender; // USER hoặc AI

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String message;

    // Lưu sources dạng JSON string
    @Column(columnDefinition = "LONGTEXT")
    private String sources;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}