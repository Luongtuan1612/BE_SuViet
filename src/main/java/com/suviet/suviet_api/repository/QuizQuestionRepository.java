package com.suviet.suviet_api.repository;

import com.suviet.suviet_api.entity.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {

    List<QuizQuestion> findByTopicIdOrderByIdAsc(Long topicId);

    List<QuizQuestion> findByTopicIdAndDifficultyOrderByIdAsc(Long topicId, String difficulty);

    long countByTopicId(Long topicId);
}