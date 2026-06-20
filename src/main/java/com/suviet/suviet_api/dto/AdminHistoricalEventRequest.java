package com.suviet.suviet_api.dto;

import lombok.Data;

@Data
public class AdminHistoricalEventRequest {
    private String title;
    private String year;
    private String shortSummary;
    private String content;
    private String image;
    private String category;
    private Long periodId;
}