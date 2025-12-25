package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
//具体聊天消息
@Entity
@Data
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long conversationId;
    private String role; // user / assistant

    @Column(columnDefinition = "TEXT")
    private String message;

    private LocalDateTime createdAt = LocalDateTime.now();
}
