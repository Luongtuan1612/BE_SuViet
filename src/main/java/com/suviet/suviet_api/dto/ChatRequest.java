package com.suviet.suviet_api.dto;

import lombok.Data;

@Data
public class ChatRequest {
    private Long sessionId;
    private String question;
}