package com.suviet.suviet_api.dto;

import lombok.Data;

@Data
public class AiDeleteFileResponse {

    private Boolean success;
    private String message;
    private String filePath;
    private String documentId;
    private Boolean deletedFromChroma;
    private Boolean deletedFile;
    private Integer totalChunks;
}