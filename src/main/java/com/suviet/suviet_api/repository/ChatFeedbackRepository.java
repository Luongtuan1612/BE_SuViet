package com.suviet.suviet_api.repository;

import com.suviet.suviet_api.entity.ChatFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatFeedbackRepository extends JpaRepository<ChatFeedback, Long> {

    Optional<ChatFeedback> findByUserIdAndMessageId(Long userId, Long messageId);

    @Modifying
    @Query("DELETE FROM ChatFeedback f WHERE f.message.session.id = :sessionId")
    void deleteBySessionId(@Param("sessionId") Long sessionId);
}