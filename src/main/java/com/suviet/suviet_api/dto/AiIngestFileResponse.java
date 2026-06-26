package com.suviet.suviet_api.dto;

import lombok.Data;

@Data
public class AiIngestFileResponse {

    private Boolean success;
    private Boolean skipped;
    private String message;
    private String filePath;
    private String documentId;
    private String sourceUrl;
    private Integer chunksAdded;
    private Integer totalChunks;
}
