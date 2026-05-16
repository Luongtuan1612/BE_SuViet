package com.suviet.suviet_api.controller;

import com.suviet.suviet_api.entity.HistoricalPeriod;
import com.suviet.suviet_api.repository.HistoricalPeriodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/history/periods") // Đây là đường dẫn API (Endpoint)
public class HistoricalPeriodController {

    @Autowired
    private HistoricalPeriodRepository periodRepository;

    // API Lấy toàn bộ danh sách các thời kỳ lịch sử
    @GetMapping
    public List<HistoricalPeriod> getAllPeriods() {
        return periodRepository.findAll();
    }
}