package com.suviet.suviet_api.repository;

import com.suviet.suviet_api.entity.QuizTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuizTopicRepository extends JpaRepository<QuizTopic, Long> {
    Optional<QuizTopic> findByTitleIgnoreCase(String title);
}