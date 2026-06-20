package com.suviet.suviet_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SourceDto {

    private String title;

    private String source;

    private String period;

    private String url;

    @JsonProperty("file_name")
    private String fileName;

    @JsonProperty("chunk_index")
    private Integer chunkIndex;
}