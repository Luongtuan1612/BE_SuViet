package com.suviet.suviet_api.repository;

import com.suviet.suviet_api.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    // Hàm mở rộng: Sau này dùng để lấy lịch sử thi của riêng 1 người dùng
    List<QuizAttempt> findByUserIdOrderByCompletedAtDesc(Long userId);
}