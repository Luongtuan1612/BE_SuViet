package com.suviet.suviet_api.controller;

import com.suviet.suviet_api.entity.HistoricalFigure;
import com.suviet.suviet_api.repository.HistoricalFigureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/figures")
@CrossOrigin(origins = "*") // Mở cửa CORS cho ReactJS
public class HistoricalFigureController {

    @Autowired
    private HistoricalFigureRepository figureRepository;

    // 1. Lấy danh sách TOÀN BỘ nhân vật lịch sử (Dùng cho trang Danh sách nhân vật nếu có)
    @GetMapping
    public ResponseEntity<List<HistoricalFigure>> getAllFigures() {
        List<HistoricalFigure> figures = figureRepository.findAll();
        return ResponseEntity.ok(figures);
    }

    // 2. Lấy danh sách nhân vật theo ID sự kiện (Dùng để hiển thị trong trang Chi tiết Sự kiện)
    @GetMapping("/article/{articleId}")
    public ResponseEntity<List<HistoricalFigure>> getFiguresByArticle(@PathVariable Long articleId) {
        List<HistoricalFigure> figures = figureRepository.findByArticlesId(articleId);
        return ResponseEntity.ok(figures);
    }
}