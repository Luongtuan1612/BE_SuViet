package com.suviet.suviet_api.dto;

import lombok.Data;

@Data
public class QuizSubmitRequest {
    private Long topicId;       // ID của chủ đề vừa làm
    private int score;          // Số câu đúng
    private int totalQuestions; // Tổng số câu hỏi
}