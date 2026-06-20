package com.suviet.suviet_api.dto;

import lombok.Data;

@Data
public class AdminQuizQuestionRequest {
    private Long topicId;
    private String difficulty;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private Integer correctAnswer;
    private String explanation;
}
