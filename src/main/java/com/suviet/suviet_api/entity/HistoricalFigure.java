package com.suviet.suviet_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "historical_figures")
@Getter
@Setter
public class HistoricalFigure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "born_died", length = 50)
    private String bornDied;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "LONGTEXT")
    private String story;

    @Column(length = 500)
    private String image;

    /*
     * Quan hệ nhiều - nhiều giữa nhân vật lịch sử và sự kiện lịch sử.
     *
     * Một nhân vật có thể xuất hiện trong nhiều sự kiện.
     * Một sự kiện có thể liên quan đến nhiều nhân vật.
     *
     * Bảng trung gian: article_figure
     */
    @ManyToMany
    @JoinTable(
            name = "article_figure",
            joinColumns = @JoinColumn(name = "figure_id"),
            inverseJoinColumns = @JoinColumn(name = "article_id")
    )
    @JsonIgnoreProperties({
            "figures",
            "hibernateLazyInitializer",
            "handler"
    })
    private List<HistoricalArticle> articles = new ArrayList<>();
}