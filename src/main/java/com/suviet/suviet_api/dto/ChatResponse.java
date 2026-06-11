package com.suviet.suviet_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponse {
    private Long sessionId;
    private Long aiMessageId;
    private String answer;
    private List<SourceDto> sources;
}