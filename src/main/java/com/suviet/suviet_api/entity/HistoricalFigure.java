package com.suviet.suviet_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "historical_figures")
@Data
public class HistoricalFigure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name; // Tên nhân vật (VD: Ngô Quyền, Trần Hưng Đạo)

    @Column(name = "born_died", length = 50)
    private String bornDied; // Năm sinh - năm mất (VD: 898 – 944)

    @Column(columnDefinition = "TEXT")
    private String description; // Tóm tắt ngắn về nhân vật

    @Column(columnDefinition = "LONGTEXT")
    private String story; // Tiểu sử chi tiết cuộc đời và sự nghiệp

    @Column(length = 255)
    private String image; // Đường dẫn ảnh của nhân vật

    // Kết nối Nhiều-Nhiều với bảng Sự kiện (HistoricalArticle)
    @ManyToMany
    @JoinTable(
            name = "article_figure", // Tên bảng trung gian tự động sinh ra
            joinColumns = @JoinColumn(name = "figure_id"),
            inverseJoinColumns = @JoinColumn(name = "article_id")
    )
    @JsonIgnoreProperties("figures") // Chống lặp vô hạn khi xuất JSON
    private List<HistoricalArticle> articles;
}