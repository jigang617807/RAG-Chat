package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
//PDF 文档
@Entity
@Data
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String title;
    private String filePath;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime uploadedAt = LocalDateTime.now();
}
