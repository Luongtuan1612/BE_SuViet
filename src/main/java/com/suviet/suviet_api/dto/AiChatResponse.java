package com.suviet.suviet_api.dto;

import lombok.Data;

import java.util.List;

@Data
public class AiChatResponse {
    private String answer;
    private List<SourceDto> sources;
}