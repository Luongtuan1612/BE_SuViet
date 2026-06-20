package com.suviet.suviet_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiDeleteFileRequest {

    private String filePath;
    private Boolean deleteFile;
}