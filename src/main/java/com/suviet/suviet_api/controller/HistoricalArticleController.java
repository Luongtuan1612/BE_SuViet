package com.suviet.suviet_api.controller;

import com.suviet.suviet_api.entity.HistoricalArticle;
import com.suviet.suviet_api.repository.HistoricalArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/history/articles")
public class HistoricalArticleController {

    @Autowired
    private HistoricalArticleRepository articleRepository;

    // API 1: Lấy danh sách TẤT CẢ bài viết có trong DB
    @GetMapping
    public List<HistoricalArticle> getAllArticles() {
        return articleRepository.findAll();
    }

    // API 2: Lấy danh sách bài viết theo ID của một triều đại cụ thể
    // Ví dụ: /api/v1/history/articles/period/1 (Lấy bài viết của triều đại có ID = 1)
    @GetMapping("/period/{periodId}")
    public List<HistoricalArticle> getArticlesByPeriod(@PathVariable Long periodId) {
        return articleRepository.findByPeriodId(periodId);
    }
    // API 3: Lấy chi tiết 1 bài viết theo ID (Dành cho trang EventDetail)
    @GetMapping("/{id}")
    public HistoricalArticle getArticleById(@PathVariable Long id) {
        // Tìm bài viết theo ID, nếu không thấy thì trả về null
        return articleRepository.findById(id).orElse(null);
    }
}