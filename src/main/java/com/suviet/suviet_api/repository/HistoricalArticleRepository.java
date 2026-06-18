package com.suviet.suviet_api.repository;

import com.suviet.suviet_api.entity.HistoricalArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoricalArticleRepository extends JpaRepository<HistoricalArticle, Long> {

    List<HistoricalArticle> findByPeriodId(Long periodId);

    List<HistoricalArticle> findAllByOrderByIdDesc();

    long countByArticleTypeIgnoreCase(String articleType);
}