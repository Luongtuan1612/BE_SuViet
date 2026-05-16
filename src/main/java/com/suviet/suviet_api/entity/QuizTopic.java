package com.suviet.suviet_api.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "quiz_topics")
@Data
public class QuizTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title; // Tên chủ đề (VD: Trận chiến trên sông Bạch Đằng)

    @Column(length = 255)
    private String description; // Mô tả ngắn của bài test (VD: Kiểm tra kiến thức trận 938)

    @Column(length = 10)
    private String emoji; // Biểu tượng vui nhộn hiển thị trên giao diện (VD: ⚔️, 👑)
}