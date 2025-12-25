package com.example.demo.repository;

import com.example.demo.entity.Conversation;
import com.example.demo.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {
    List<DocumentChunk> findByDocumentId(Long docId);
    void deleteByDocumentId(Long documentId);
}

