package com.suviet.suviet_api.dto;

import lombok.Data;

@Data
public class AdminAiSourceRequest {

    private String title;
    private String url;
    private String period;
    private Long periodId;
    private String category;
}
