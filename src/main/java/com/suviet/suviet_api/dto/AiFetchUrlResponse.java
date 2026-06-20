package com.suviet.suviet_api.dto;

import lombok.Data;

@Data
public class AiFetchUrlResponse {

    private Boolean success;
    private String message;
    private String title;
    private String url;
    private String filePath;
    private String contentPreview;
    private Integer contentLength;
}