/*package com.example.demo.controller;

import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.Conversation;
import com.example.demo.entity.DocumentChunk;
import com.example.demo.repository.ChatMessageRepository;
import com.example.demo.repository.ConversationRepository;
import com.example.demo.service.RagService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor

public class ChatController {

    private final ConversationRepository convRepo;
    private final ChatMessageRepository msgRepo;
    private final RagService ragService;

    @Value("${zhipu.api-key}")
    private String apiKey;

    @Value("${zhipu.chat-model}")
    private String chatModel;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://open.bigmodel.cn/api/paas/v4/chat/completions")
            .build();

    @GetMapping("/start")
    public String start(@RequestParam Long docId,
                        HttpSession session,
                        Model model) {

        Long uid=(Long)session.getAttribute("uid");

        Conversation conv=new Conversation();
        conv.setUserId(uid);
        conv.setTitle("Doc-"+docId+" 对话");
        convRepo.save(conv);

        model.addAttribute("conversationId", conv.getId());
        model.addAttribute("documentId", docId);
        model.addAttribute("messages",
                msgRepo.findByConversationId(conv.getId()));

        return "chat";
    }

    @PostMapping("/ask")
    @ResponseBody

    public String ask(@RequestParam Long conversationId,
                      @RequestParam Long documentId,
                      @RequestParam String question){

        // 保存用户问题
        ChatMessage m1=new ChatMessage();
        m1.setConversationId(conversationId);
        m1.setRole("user");
        m1.setMessage(question);
        msgRepo.save(m1);

        // RAG 检索
        List<DocumentChunk> chunks = ragService.searchRelevant(documentId, question);
        StringBuilder context=new StringBuilder();
        for(DocumentChunk c:chunks) context.append(c.getText()).append("\n");

        String prompt="相关内容：\n"+context+
                "\n请根据以上内容回答问题："+question;

        // 调智谱 GLM
        String body = String.format("""
                {
                  "model": "%s",
                  "messages": [
                    {"role":"user", "content":"%s"}
                  ]
                }
                """, chatModel, prompt.replace("\"","'"));

        ZhipuChatResponse resp = webClient.post()
                .header("Authorization","Bearer "+apiKey)
                .header("Content-Type","application/json")
                .body(Mono.just(body), String.class)
                .retrieve()
                .bodyToMono(ZhipuChatResponse.class)
                .block();

        String answer = resp.output();

        // 保存 AI 回复
        ChatMessage m2=new ChatMessage();
        m2.setConversationId(conversationId);
        m2.setRole("assistant");
        m2.setMessage(answer);
        msgRepo.save(m2);

        return answer;
    }





    record ZhipuChatResponse(List<Choice> choices){
        public String output(){
            return choices.get(0).message().content();
        }
    }
    record Choice(Message message){}
    record Message(String role, String content){}
}
*/

package com.example.demo.controller;
import ai.z.openapi.ZhipuAiClient;
import ai.z.openapi.service.model.ChatCompletionCreateParams;
import ai.z.openapi.service.model.ChatCompletionResponse;
import ai.z.openapi.service.model.ChatMessage;
import ai.z.openapi.service.model.ChatMessageRole;
import ai.z.openapi.service.model.Delta;
import com.example.demo.entity.Conversation;
import com.example.demo.entity.DocumentChunk;
import com.example.demo.repository.ChatMessageRepository;
import com.example.demo.repository.ConversationRepository;
import com.example.demo.service.RagService;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ConversationRepository convRepo;
    private final ChatMessageRepository msgRepo;
    private final RagService ragService;

    @Value("${ZHIPU_API_KEY}")
    private String apiKey;

    @Value("${zhipu.chat-model}")
    private String chatModel;

    /**
     * 进入聊天页面（加载历史记录）
     */
    @GetMapping("/start")
    public String start(@RequestParam Long docId,
                        HttpSession session,
                        Model model) {

        Long uid = (Long) session.getAttribute("uid");
        String title = "Doc-" + docId + " 对话";

        // 查找是否存在旧对话
        Conversation conv = convRepo.findTopByUserIdAndTitle(uid, title)
                .orElseGet(() -> {
                    Conversation c = new Conversation();
                    c.setUserId(uid);
                    c.setTitle(title);
                    return convRepo.save(c);
                });

        model.addAttribute("conversationId", conv.getId());
        model.addAttribute("documentId", docId);
        model.addAttribute("messages",
                msgRepo.findByConversationId(conv.getId()));

        return "chat";
    }

    /**
     * 清空聊天记录
     */
    @PostMapping("/clear")
    @ResponseBody
    @Transactional
    public ResponseEntity<String> clearConversation(@RequestParam("documentId") Long documentId,
                                                    HttpSession session) {

        Long uid = (Long) session.getAttribute("uid");
        if (uid == null) {
            return ResponseEntity.status(401).body("用户未登录或会话已过期。");
        }

        if (documentId == null) {
            return ResponseEntity.badRequest().body("文档ID不能为空。");
        }

        try {
            // 1. 根据 docId 和 userId 重新定位 Conversation
            String title = "Doc-" + documentId + " 对话";

            // 使用 findTopByUserIdAndTitle 查找对应的唯一对话
            convRepo.findTopByUserIdAndTitle(uid, title).ifPresent(conversation -> {
                Long conversationId = conversation.getId();

                // 2. 删除所有聊天记录 (子表)
                // 确保 ChatMessageRepository 已经实现了 @Modifying 的 deleteByConversationId
                msgRepo.deleteByConversationId(conversationId);

                // 3. 删除对话本身 (父表)
                convRepo.deleteById(conversationId);
            });

            // 如果对话不存在 (Optional.isEmpty())，也视为成功，因为目标是清空记录。
            return ResponseEntity.ok("聊天记录清除成功");

        } catch (Exception e) {
            // 实际项目中应使用 Logger 记录 e
            return ResponseEntity.internalServerError().body("服务器处理清除请求失败：" + e.getMessage());
        }
    }


    /**
     * 流式回答（SSE） + 写入数据库
     */
    @GetMapping(value = "/ask", produces = "text/event-stream")
    @ResponseBody
    public Flux<String> ask(@RequestParam Long conversationId,
                            @RequestParam Long documentId,
                            @RequestParam String question) {

        Long uid = conversationId; // 已在外部写入

        // 1. 保存用户消息到数据库
        {
            com.example.demo.entity.ChatMessage m = new com.example.demo.entity.ChatMessage();
            m.setConversationId(conversationId);
            m.setRole("user");
            m.setMessage(question);
            msgRepo.save(m);
        }

        // 2. 检索 RAG chunk
        List<DocumentChunk> chunks = ragService.searchRelevant(documentId, question);

        StringBuilder context = new StringBuilder();
        for (DocumentChunk c : chunks) {
            context.append(c.getText()).append("\n");
        }

        String ragPrompt = "相关内容：\n" + context +
                "\n请根据以上内容回答问题：" + question;

        // ---- 3. 拼历史对话（写入 SDK 格式 ChatMessage） ----
        List<ChatMessage> his = new ArrayList<>();

        // 加入之前的历史对话（数据库里的）
        msgRepo.findByConversationId(conversationId).forEach(m -> {
            his.add(ChatMessage.builder()
                    .role(m.getRole())
                    .content(m.getMessage())
                    .build());
        });

        // 当前用户问题追加
        his.add(ChatMessage.builder()
                .role(ChatMessageRole.USER.value())
                .content(ragPrompt)
                .build());

        // ---- 4. 调用 GLM 流式接口 ----
        ZhipuAiClient client = ZhipuAiClient.builder()
                .apiKey(apiKey)
                .baseUrl("https://open.bigmodel.cn/api/paas/v4/")
                .build();

        ChatCompletionCreateParams req = ChatCompletionCreateParams.builder()
                .model(chatModel)
                .messages(his)
                .stream(true)
                .build();

        ChatCompletionResponse resp = client.chat().createChatCompletion(req);

        StringBuilder finalReply = new StringBuilder();

        // ---- 5. SSE + 保存数据库 ----
        return Flux.create(sink ->
                resp.getFlowable().subscribe(
                        delta -> {
                            if (delta.getChoices() != null &&
                                    !delta.getChoices().isEmpty()) {

                                Delta d = delta.getChoices().get(0).getDelta();
                                if (d != null && d.getContent() != null) {

                                    String chunk = d.getContent().toString();
                                    finalReply.append(chunk);

                                    sink.next(  chunk );
                                }
                            }
                        },
                        err -> {
                            sink.next("data:【错误】" + err.getMessage());
                            sink.complete();
                        },
                        () -> {
                            // SSE 结束：保存最终 AI 回复
                            com.example.demo.entity.ChatMessage ai =
                                    new com.example.demo.entity.ChatMessage();

                            ai.setConversationId(conversationId);
                            ai.setRole("assistant");
                            ai.setMessage(finalReply.toString());
                            msgRepo.save(ai);

                            sink.next("[DONE]");
                            sink.complete();
                        }
                )
        );
    }
}
