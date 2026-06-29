package com.suviet.suviet_api.dto;

import lombok.Data;

@Data
public class QuizSubmitRequest {

    private Long topicId;

    private String difficulty;

    private int score;

    private int totalQuestions;
}