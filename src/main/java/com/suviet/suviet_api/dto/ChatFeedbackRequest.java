package com.suviet.suviet_api.dto;

import lombok.Data;

@Data
public class ChatFeedbackRequest {
    private Long messageId;
    private String rating;
    private String comment;
}