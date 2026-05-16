package com.suviet.suviet_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "historical_articles")
@Data
public class HistoricalArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Khóa ngoại liên kết với bảng HistoricalPeriod
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "period_id")
    @JsonIgnoreProperties("summary") // Ẩn bớt trường summary của triều đại khi hiển thị JSON cho gọn
    private HistoricalPeriod period;

    @Column(nullable = false, length = 255)
    private String title; // Tên sự kiện hoặc nhân vật (VD: Khởi nghĩa Lam Sơn)

    @Column(name = "article_type", length = 50)
    private String articleType; // Phân loại: EVENT (Sự kiện), FIGURE (Nhân vật)

    // Dùng LONGTEXT vì bài viết lịch sử thường rất dài (để AI RAG còn đọc)
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String content;

    @Column(name = "image_url")
    private String imageUrl; // Link ảnh minh họa
}