package com.suviet.suviet_api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AdminHistoricalFigureRequest {
    private String name;

    @JsonAlias("born_died")
    private String bornDied;

    private String description;
    private String story;
    private String image;

    private List<Long> articleIds = new ArrayList<>();
}