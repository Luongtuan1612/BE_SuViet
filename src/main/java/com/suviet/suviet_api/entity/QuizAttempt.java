package com.suviet.suviet_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_attempts")
@Data
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Liên kết với bảng Users: Một người dùng có thể làm nhiều bài kiểm tra
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Liên kết với bảng QuizTopic: Bài kiểm tra này thuộc chủ đề nào
    @ManyToOne
    @JoinColumn(name = "topic_id", nullable = false)
    private QuizTopic topic;

    @Column(name = "difficulty", length = 20)
    private String difficulty; // EASY, MEDIUM, HARD

    @Column(nullable = false)
    private int score; // Số câu trả lời đúng (Ví dụ: 8)

    @Column(name = "total_questions", nullable = false)
    private int totalQuestions; // Tổng số câu hỏi của đề (Ví dụ: 10)

    @Column(name = "completed_at")
    private LocalDateTime completedAt = LocalDateTime.now(); // Thời gian nộp bài
}