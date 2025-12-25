package com.example.demo.repository;

import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findByUserId(Long userId);
    Optional<Conversation> findTopByUserIdAndTitle(Long userId, String title);
    /**
     * 根据 Conversation 的精确标题查找记录。
     * 由于业务约束：一个文档最多一个对话，所以返回 Optional。
     * @param title 完整的对话标题，例如 "Doc-123 对话"
     * @return 匹配该标题的 Conversation，如果不存在则为 Optional.empty()
     */
    Optional<Conversation> findByTitle(String title);

}





