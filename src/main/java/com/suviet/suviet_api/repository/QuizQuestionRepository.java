package com.suviet.suviet_api.repository;

import com.suviet.suviet_api.entity.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    // Hàm tự động tìm tất cả câu hỏi thuộc về một chủ đề cụ thể
    List<QuizQuestion> findByTopicId(Long topicId);
}