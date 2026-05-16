package com.suviet.suviet_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "quiz_questions")
@Data
public class QuizQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nhiều câu hỏi thuộc về 1 Chủ đề
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "topic_id", nullable = false)
    @JsonIgnoreProperties("questions")
    private QuizTopic topic;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionText; // Nội dung câu hỏi

    // 4 phương án lựa chọn
    @Column(nullable = false)
    private String optionA;

    @Column(nullable = false)
    private String optionB;

    @Column(nullable = false)
    private String optionC;

    @Column(nullable = false)
    private String optionD;

    // Lưu đáp án đúng dạng số: 0 = A, 1 = B, 2 = C, 3 = D (Để khớp với logic mảng trên React của bạn)
    @Column(nullable = false)
    private Integer correctAnswer;

    @Column(columnDefinition = "TEXT")
    private String explanation; // Lời giải thích chi tiết sau khi học sinh chọn đáp án
}