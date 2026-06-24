package com.suviet.suviet_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "historical_articles")
@Getter
@Setter
public class HistoricalArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "period_id")
    @JsonIgnoreProperties({
            "summary",
            "articles",
            "hibernateLazyInitializer",
            "handler"
    })
    private HistoricalPeriod period;

    @Column(length = 50)
    private String year;

    @Column(name = "short_summary", columnDefinition = "TEXT")
    private String shortSummary;

    // Tên sự kiện lịch sử, ví dụ: Khởi nghĩa Lam Sơn
    @Column(nullable = false, length = 255)
    private String title;

    // Phân loại bài viết: EVENT, FIGURE...
    @Column(name = "article_type", length = 50)
    private String articleType;

    // Nội dung chi tiết của sự kiện
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String content;

    // Link ảnh minh họa sự kiện
    @Column(name = "image", length = 500)
    private String image;

    // Thể loại: trieu-dai, chong-ngoai-xam, khoi-nghia...
    @Column(length = 50)
    private String category;

    /*
     * Quan hệ nhiều - nhiều với nhân vật lịch sử.
     *
     * HistoricalFigure là bên sở hữu quan hệ vì bên đó có @JoinTable.
     * Bên HistoricalArticle chỉ cần mappedBy = "articles".
     */
    @ManyToMany(mappedBy = "articles")
    @JsonIgnoreProperties({
            "articles",
            "hibernateLazyInitializer",
            "handler"
    })
    private List<HistoricalFigure> figures = new ArrayList<>();
}