package com.suviet.suviet_api.controller;

import com.suviet.suviet_api.dto.AdminHistoricalFigureRequest;
import com.suviet.suviet_api.entity.HistoricalArticle;
import com.suviet.suviet_api.entity.HistoricalFigure;
import com.suviet.suviet_api.repository.HistoricalArticleRepository;
import com.suviet.suviet_api.repository.HistoricalFigureRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/api/v1/admin/figures")
@CrossOrigin(origins = "*")
public class AdminHistoricalFigureController {

    private final HistoricalFigureRepository figureRepository;
    private final HistoricalArticleRepository articleRepository;

    public AdminHistoricalFigureController(
            HistoricalFigureRepository figureRepository,
            HistoricalArticleRepository articleRepository
    ) {
        this.figureRepository = figureRepository;
        this.articleRepository = articleRepository;
    }

    @GetMapping
    public List<HistoricalFigure> getAllFigures() {
        return figureRepository.findAllByOrderByIdDesc();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public HistoricalFigure createFigure(@RequestBody AdminHistoricalFigureRequest request) {
        validateRequest(request);

        HistoricalFigure figure = new HistoricalFigure();
        applyRequestToEntity(figure, request);

        return figureRepository.save(figure);
    }

    @PutMapping("/{id}")
    @Transactional
    public HistoricalFigure updateFigure(
            @PathVariable Long id,
            @RequestBody AdminHistoricalFigureRequest request
    ) {
        validateRequest(request);

        HistoricalFigure figure = figureRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy nhân vật lịch sử có ID: " + id
                ));

        applyRequestToEntity(figure, request);

        return figureRepository.save(figure);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public Map<String, Object> deleteFigure(@PathVariable Long id) {
        HistoricalFigure figure = figureRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy nhân vật lịch sử có ID: " + id
                ));

        if (figure.getArticles() != null) {
            figure.getArticles().clear();
            figureRepository.save(figure);
        }

        figureRepository.delete(figure);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("message", "Đã xóa nhân vật lịch sử thành công.");
        result.put("deletedId", id);

        return result;
    }

    private void applyRequestToEntity(
            HistoricalFigure figure,
            AdminHistoricalFigureRequest request
    ) {
        figure.setName(cleanRequired(request.getName()));
        figure.setBornDied(clean(request.getBornDied()));
        figure.setDescription(clean(request.getDescription()));
        figure.setStory(clean(request.getStory()));
        figure.setImage(clean(request.getImage()));
        figure.setArticles(resolveArticles(request.getArticleIds()));
    }

    private List<HistoricalArticle> resolveArticles(List<Long> articleIds) {
        if (articleIds == null || articleIds.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Long> uniqueIds = new LinkedHashSet<>(articleIds);
        List<HistoricalArticle> articles = articleRepository.findAllById(uniqueIds);

        if (articles.size() != uniqueIds.size()) {
            Set<Long> foundIds = new LinkedHashSet<>();

            for (HistoricalArticle article : articles) {
                foundIds.add(article.getId());
            }

            List<Long> missingIds = uniqueIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Không tìm thấy sự kiện lịch sử có ID: " + missingIds
            );
        }

        return articles;
    }

    private void validateRequest(AdminHistoricalFigureRequest request) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Dữ liệu nhân vật lịch sử không được để trống."
            );
        }

        if (isBlank(request.getName())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tên nhân vật lịch sử không được để trống."
            );
        }
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }

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