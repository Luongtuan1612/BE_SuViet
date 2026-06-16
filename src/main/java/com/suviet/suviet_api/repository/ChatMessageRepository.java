package com.suviet.suviet_api.repository;

import com.suviet.suviet_api.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId);

    List<ChatMessage> findBySessionIdOrderByIdAsc(Long sessionId);

    List<ChatMessage> findBySessionIdAndIdGreaterThanOrderByIdAsc(Long sessionId, Long id);

    void deleteBySessionId(Long sessionId);

    void deleteBySessionIdAndIdGreaterThan(Long sessionId, Long id);
}