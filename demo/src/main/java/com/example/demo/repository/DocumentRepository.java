package com.example.demo.repository;

import com.example.demo.entity.Document;
import com.example.demo.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByUserId(Long userId);

}

