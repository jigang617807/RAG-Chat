package com.example.demo.service;
import com.example.demo.entity.Conversation;
import com.example.demo.entity.Document;
import com.example.demo.entity.DocumentChunk;

import com.example.demo.repository.ChatMessageRepository;
import com.example.demo.repository.ConversationRepository;
import com.example.demo.repository.DocumentChunkRepository;
import com.example.demo.repository.DocumentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Optional;



@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository docRepo;
    private final DocumentChunkRepository chunkRepo;
    private final RagService ragService;  // ✅ 注入 RagService


    @Autowired
    private ConversationRepository conversationRepository;
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    @Autowired
    private DocumentRepository documentRepository;


    // 上传 PDF
    public Document saveDocument(Long userId, String title, String filePath, String content) {
        Document doc = new Document();
        doc.setUserId(userId);
        doc.setTitle(title);
        doc.setFilePath(filePath);
        doc.setContent(content);
        return docRepo.save(doc);
    }


    // 保存chunk并生成 chunk embedding
    public void saveChunks(Long docId, List<String> chunks) {

        int index = 0;
        for (String text : chunks) {

            if (text.strip().length() == 0) continue; // 跳过空文本
            // 1) 创建 chunk
            DocumentChunk c = new DocumentChunk();
            c.setDocumentId(docId);
            c.setChunkIndex(index++);
            c.setText(text);
            chunkRepo.save(c);
            // 2) 生成 chunk 的 embedding
            List<Double> vec = ragService.embedding(text);
            // 3) 写入 embedding JSON
            c.setEmbeddingVector(vec);
            // 4) 数据库存储
            chunkRepo.save(c);
        }
    }


    //显示所有文档
    public List<Document> listDocs(Long userId) {
        return docRepo.findByUserId(userId);
    }


    /*
     * 事务性删除文档、相关的对话和聊天记录，以及本地文件。
     * 先删除chat_massage、再删除conservative、再删除document，这样才不会报错！
     */
    @Transactional
    public void deleteDocumentWithRelatedData(Long docId) {


        // --- 1. 查找 Document 记录 ---
        Document document = documentRepository.findById(docId)
                .orElseThrow(() -> new RuntimeException("文档不存在或已被删除！ID: " + docId));

        // 获取文件路径用于本地删除
        String filePath = document.getFilePath();

        // --- 2. 查找相关的 Conversation (0或1个) ---
        String expectedTitle = "Doc-" + docId + " 对话";
        Optional<Conversation> conversationOpt = conversationRepository.findByTitle(expectedTitle);

        if (conversationOpt.isPresent()) {
            Conversation conversation = conversationOpt.get();
            //获取对话的id
            Long conversationId = conversation.getId();

            // --- 3. 删除相关的 Chat_Message ---
            // 批量删除该对话下的所有聊天记录
            chatMessageRepository.deleteByConversationId(conversationId);

            // --- 4. 删除 Conversation ---
            conversationRepository.delete(conversation);
        }
        // 如果 conversationOpt.isEmpty()，则跳过步骤 3 和 4，因为没有关联的对话。



        // --- 5. 删除关联的 DocumentChunk 记录
        // ----------------------------------------------------
        chunkRepo.deleteByDocumentId(docId);
        // ----------------------------------------------------

        // --- 6. 删除 Document 记录 ---
        documentRepository.delete(document);

        // --- 7. 删除本地文件 ---
        deleteLocalDocumentFile(filePath);
    }

    /*
     * 删除服务器上的本地文件。
     */
    private void deleteLocalDocumentFile(String filePath) {
        File file = new File(filePath);

        if (file.exists()) {
            if (!file.delete()) {
                // 建议记录日志，但通常不抛出异常中断事务（除非文件删除是核心业务）
                System.err.println("本地文件删除失败！路径: " + file.getAbsolutePath());
            }
        }
    }



}
