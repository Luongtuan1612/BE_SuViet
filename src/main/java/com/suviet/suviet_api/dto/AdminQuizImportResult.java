package com.suviet.suviet_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminQuizImportResult {
    private int totalRows;
    private int successRows;
    private int failedRows;
    private List<String> errors = new ArrayList<>();
}
