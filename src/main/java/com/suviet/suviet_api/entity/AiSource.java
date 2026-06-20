package com.suviet.suviet_api.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_sources")
@Data
public class AiSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 1000)
    private String url;

    @Column(length = 255)
    private String period;

    @Column(length = 255)
    private String category;

    @Column(nullable = false, length = 30)
    private String status = "PENDING";

    @Column(name = "local_file_path", length = 1000)
    private String localFilePath;

    @Column(name = "content_preview", columnDefinition = "TEXT")
    private String contentPreview;

    @Column(name = "content_length")
    private Integer contentLength = 0;

    @Column(name = "chunks_added")
    private Integer chunksAdded = 0;

    @Column(name = "total_chunks")
    private Integer totalChunks = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}