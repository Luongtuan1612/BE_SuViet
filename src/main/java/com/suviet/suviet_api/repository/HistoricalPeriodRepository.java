package com.suviet.suviet_api.repository;

import com.suviet.suviet_api.entity.HistoricalPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoricalPeriodRepository extends JpaRepository<HistoricalPeriod, Long> {
    // Kế thừa JpaRepository đã cung cấp sẵn mọi hàm cần thiết (findAll, findById, save, delete...)
    // Hiện tại bạn chưa cần viết thêm code gì ở đây.
}