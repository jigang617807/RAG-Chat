package com.example.demo.service;

import ai.z.openapi.service.embedding.Embedding;
import com.example.demo.entity.DocumentChunk;
import com.example.demo.repository.DocumentChunkRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

import ai.z.openapi.ZhipuAiClient;
//官方文档有误
import ai.z.openapi.service.embedding.EmbeddingCreateParams;
import ai.z.openapi.service.embedding.EmbeddingResponse;

import java.util.List;


@Service
@RequiredArgsConstructor
public class RagService {

    private final DocumentChunkRepository chunkRepo;


//    @Value("${zhipu.api-key}")
//    private final String apiKey = "69d9b4f79f154b6fb9afa7a70fdbb202.ag70xz362XpCuEE1";
    @Value("${ZHIPU_API_KEY}")
    private String apiKey;

    @Value("${zhipu.embedding-model}")
    private String embeddingModel;

//    private final ZhipuAiClient zhipuClient = ZhipuAiClient.builder()
//            .apiKey(apiKey)
//            .baseUrl("https://open.bigmodel.cn/api/paas/v4/")
//            .build();

    private ZhipuAiClient zhipuClient;
    @PostConstruct
    public void init() {
        this.zhipuClient = ZhipuAiClient.builder()
                .apiKey(apiKey)
                .baseUrl("https://open.bigmodel.cn/api/paas/v4/")
                .build();
    }

    public List<Double> embedding(String text) {

        // 1. 创建 Embedding 请求（参考了 Embedding3Example 的构建方式）
        EmbeddingCreateParams request = EmbeddingCreateParams.builder()
                .model(embeddingModel) // 使用配置文件中的模型名
                .input(Collections.singletonList(text)) // 将单个文本封装成 List<String>
                // .dimensions(768) // 维度是可选的，这里省略，使用模型默认值，如果需要自定义，则取消注释
                .build();

        // 2. 发送请求（参考client.embeddings().createEmbeddings(request) 的调用方式）
        // SDK 调用是同步的，可以直接获取响应
        EmbeddingResponse response = zhipuClient.embeddings().createEmbeddings(request);
        Embedding embeddingObject = response.getData().getData().get(0);
        return embeddingObject.getEmbedding();
    }




    public List<DocumentChunk> searchRelevant(Long documentId, String question) {
        List<Double> qvec = embedding(question);
        List<DocumentChunk> chunks = chunkRepo.findByDocumentId(documentId);

        if (chunks == null || chunks.isEmpty()) {
            return new ArrayList<>(); // 防止空列表报错
        }

        chunks.sort(Comparator.comparingDouble(
                (DocumentChunk c)->cosineSimilarity(qvec,c.getEmbeddingVector())
        ).reversed());

        return chunks.subList(0, Math.min(5,chunks.size()));
    }

    public double cosineSimilarity(List<Double> a, List<Double> b){
        double dot=0, na=0, nb=0;
        for(int i=0;i<a.size();i++){
            dot += a.get(i)*b.get(i);
            na += a.get(i)*a.get(i);
            nb += b.get(i)*b.get(i);
        }
        return dot/(Math.sqrt(na)*Math.sqrt(nb));
    }
}
