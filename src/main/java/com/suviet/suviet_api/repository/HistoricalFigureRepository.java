package com.suviet.suviet_api.repository;

import com.suviet.suviet_api.entity.HistoricalFigure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoricalFigureRepository extends JpaRepository<HistoricalFigure, Long> {

    List<HistoricalFigure> findAllByOrderByIdDesc();

    List<HistoricalFigure> findByArticlesId(Long articleId);
}