package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

@Entity
@Data
public class DocumentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long documentId;
    private int chunkIndex;

    @Column(columnDefinition = "TEXT")
    private String text;

    @Column(columnDefinition = "JSON")
    private String embedding;

    /**
     * JSON → List<Double>
     */

    public List<Double> getEmbeddingVector() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(embedding, List.class);
        } catch (Exception e) {
            throw new RuntimeException("解析 embedding JSON 失败: " + embedding);
        }
    }


    /**
     * List<Double> → JSON
     */
    public void setEmbeddingVector(List<Double> vector) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.embedding = mapper.writeValueAsString(vector);
        } catch (Exception e) {
            throw new RuntimeException("embedding 转 JSON 失败");
        }
    }
}
