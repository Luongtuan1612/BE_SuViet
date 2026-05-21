package com.suviet.suviet_api.repository;

import com.suviet.suviet_api.entity.HistoricalFigure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoricalFigureRepository extends JpaRepository<HistoricalFigure, Long> {

    // Hàm ma thuật của Spring Data JPA: Tự động tìm các nhân vật dựa vào ID của Sự kiện
    // (Vì 1 sự kiện có thể có nhiều nhân vật tham gia)
    List<HistoricalFigure> findByArticlesId(Long articleId);
}