package com.suviet.suviet_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiFetchUrlRequest {

    private String title;
    private String url;
    private String period;
    private String category;
}