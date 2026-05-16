package com.suviet.suviet_api.repository;

import com.suviet.suviet_api.entity.HistoricalArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoricalArticleRepository extends JpaRepository<HistoricalArticle, Long> {

    // Spring Boot siêu thông minh, bạn chỉ cần gõ đúng tên tiếng Anh, nó tự dịch ra câu lệnh SQL SELECT ... WHERE period_id = ?
    List<HistoricalArticle> findByPeriodId(Long periodId);
}