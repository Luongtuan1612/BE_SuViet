package com.suviet.suviet_api.controller;

import com.suviet.suviet_api.dto.AdminHistoricalEventRequest;
import com.suviet.suviet_api.entity.HistoricalArticle;
import com.suviet.suviet_api.entity.HistoricalFigure;
import com.suviet.suviet_api.entity.HistoricalPeriod;
import com.suviet.suviet_api.repository.HistoricalArticleRepository;
import com.suviet.suviet_api.repository.HistoricalFigureRepository;
import com.suviet.suviet_api.repository.HistoricalPeriodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/events")
@CrossOrigin(origins = "*")
public class AdminHistoricalEventController {

    @Autowired
    private HistoricalArticleRepository articleRepository;

    @Autowired
    private HistoricalPeriodRepository periodRepository;

    @Autowired
    private HistoricalFigureRepository figureRepository;

    @GetMapping
    public List<HistoricalArticle> getAllEvents() {
        return articleRepository.findAllByOrderByIdDesc();
    }

    @GetMapping("/stats")
    public Map<String, Object> getEventStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        stats.put("totalEvents", articleRepository.count());
        stats.put("totalEventType", articleRepository.countByArticleTypeIgnoreCase("EVENT"));
        stats.put("totalPeriods", periodRepository.count());

        return stats;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HistoricalArticle createEvent(@RequestBody AdminHistoricalEventRequest request) {
        validateRequest(request);

        HistoricalArticle event = new HistoricalArticle();
        applyRequestToEntity(event, request);

        return articleRepository.save(event);
    }

    @PutMapping("/{id}")
    public HistoricalArticle updateEvent(
            @PathVariable Long id,
            @RequestBody AdminHistoricalEventRequest request
    ) {
        validateRequest(request);

        HistoricalArticle event = articleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy sự kiện có ID: " + id
                ));

        applyRequestToEntity(event, request);

        return articleRepository.save(event);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteEvent(@PathVariable Long id) {
        HistoricalArticle event = articleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy sự kiện có ID: " + id
                ));

        /*
         * Nếu sự kiện đang liên kết với nhân vật lịch sử qua bảng article_figure,
         * cần gỡ liên kết trước để tránh lỗi khóa ngoại khi xóa.
         */
        List<HistoricalFigure> linkedFigures = figureRepository.findByArticlesId(id);

        for (HistoricalFigure figure : linkedFigures) {
            if (figure.getArticles() != null) {
                figure.getArticles().removeIf(article -> article.getId().equals(id));
            }
        }

        figureRepository.saveAll(linkedFigures);

        articleRepository.delete(event);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("message", "Đã xóa sự kiện thành công.");
        result.put("deletedId", id);

        return result;
    }

    private void applyRequestToEntity(
            HistoricalArticle event,
            AdminHistoricalEventRequest request
    ) {
        event.setTitle(cleanRequired(request.getTitle()));
        event.setYear(clean(request.getYear()));
        event.setShortSummary(clean(request.getShortSummary()));
        event.setContent(cleanRequired(request.getContent()));
        event.setImage(clean(request.getImage()));
        event.setCategory(clean(request.getCategory()));

        /*
         * Dữ liệu trang sự kiện vẫn lưu trong bảng historical_articles.
         * articleType = EVENT giúp phân biệt đây là sự kiện lịch sử.
         */
        event.setArticleType("EVENT");

        if (request.getPeriodId() != null) {
            HistoricalPeriod period = periodRepository.findById(request.getPeriodId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Không tìm thấy thời kỳ lịch sử có ID: " + request.getPeriodId()
                    ));

            event.setPeriod(period);
        } else {
            event.setPeriod(null);
        }
    }

    private void validateRequest(AdminHistoricalEventRequest request) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Dữ liệu sự kiện không được để trống."
            );
        }

        if (isBlank(request.getTitle())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tiêu đề sự kiện không được để trống."
            );
        }

        if (isBlank(request.getContent())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nội dung sự kiện không được để trống."
            );
        }
    }

    private String clean(String value) {
        if (value == null) return null;

        String trimmed = value.trim();

        return trimmed.isEmpty() ? null : trimmed;
    }

    private String cleanRequired(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}