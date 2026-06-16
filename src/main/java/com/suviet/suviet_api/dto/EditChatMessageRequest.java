package com.suviet.suviet_api.dto;

import lombok.Data;

@Data
public class EditChatMessageRequest {

    private String content;

    private String question;

    private String message;
}