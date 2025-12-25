package com.example.demo.repository;

import com.example.demo.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByConversationIdOrderByCreatedAtAsc(Long convId);

    List<ChatMessage> findByConversationId(Long id);

    void deleteByConversationId(Long conversationId);
}
