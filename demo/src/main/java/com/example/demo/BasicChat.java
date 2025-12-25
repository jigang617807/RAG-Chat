package com.example.demo;
import ai.z.openapi.ZhipuAiClient;
import ai.z.openapi.service.model.ChatCompletionCreateParams;
import ai.z.openapi.service.model.ChatCompletionResponse;
import ai.z.openapi.service.model.ChatMessage;
import ai.z.openapi.service.model.ChatMessageRole;
import java.util.*;


public class BasicChat {
    public static void main(String[] args) {
        ZhipuAiClient client = ZhipuAiClient.builder()
                .apiKey("69d9b4f79f154b6fb9afa7a70fdbb202.ag70xz362XpCuEE1")
                .ofZHIPU()
                .build();

        // 存储整个对话历史
        List<ChatMessage> history = new ArrayList<>();

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("你：");
            String userInput = scanner.nextLine();

            if (userInput.trim().isEmpty()) return;

            // 加入一条用户消息
            history.add(ChatMessage.builder()
                    .role(ChatMessageRole.USER.value())
                    .content(userInput)
                    .build()
            );

            // 构建请求
            ChatCompletionCreateParams request = ChatCompletionCreateParams.builder()
                    .model("glm-4-plus")
                    .messages(history)
                    .build();

            // 调用 API
            ChatCompletionResponse response = client.chat().createChatCompletion(request);

            if (response.isSuccess()) {
                String aiReply = response.getData().getChoices().get(0).getMessage().getContent().toString();
                System.out.println("AI：" + aiReply);

                // 把 AI 回复加入历史
                history.add(ChatMessage.builder()
                        .role(ChatMessageRole.ASSISTANT.value())
                        .content(aiReply)
                        .build()
                );

            } else {
                System.err.println("错误：" + response.getMsg());
            }
        }
    }
}

/*
import ai.z.openapi.ZhipuAiClient;
import ai.z.openapi.service.model.ChatCompletionCreateParams;
import ai.z.openapi.service.model.ChatCompletionResponse;
import ai.z.openapi.service.model.ChatMessage;
import ai.z.openapi.service.model.ChatMessageRole;
import ai.z.openapi.service.model.Delta;

import java.util.*;

public class BasicChat {
    public static void main(String[] args) {
        ZhipuAiClient client = ZhipuAiClient.builder()
                .apiKey("69d9b4f79f154b6fb9afa7a70fdbb202.ag70xz362XpCuEE1")
                .build();
        // 存储全对话历史
        List<ChatMessage> history = new ArrayList<>();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String userInput = scanner.nextLine();
            if (userInput.trim().isEmpty()) {
                System.out.println("对话结束。");

                //这里可以加逻辑，退出对话前，存储当前历史会话！下次启动的时候，读取历史会话，作为参考依据。

                client.close();
                System.exit(0);
            }
            // 记录用户消息
            history.add(ChatMessage.builder()
                    .role(ChatMessageRole.USER.value())
                    .content(userInput)
                    .build()
            );
            // 构建请求（流式）
            ChatCompletionCreateParams request = ChatCompletionCreateParams.builder()
                    .model("glm-4-plus")
                    .messages(history)
                    .stream(true) // 必须开启流式！
                    .build();
            // 调用流式接口
            ChatCompletionResponse response = client.chat().createChatCompletion(request);
            if (!response.isSuccess()) {
                System.err.println("错误：" + response.getMsg());
                continue;
            }
//            System.out.print("AI：");
            // 用于把流式结果拼接成完整回复（加入历史使用）
            StringBuilder finalReply = new StringBuilder();
            // 订阅流
            response.getFlowable().subscribe(
                    // 每次收到一个 delta 就触发
                    data -> {
                        if (data.getChoices() != null && !data.getChoices().isEmpty()) {
                            Delta delta = data.getChoices().get(0).getDelta();
                            if (delta != null && delta.getContent() != null) {
                                // 流式内容（分片）
                                String chunk = delta.getContent().toString();
                                // 实时打印
                                System.out.print(chunk);
                                // 拼进最终回复
                                finalReply.append(chunk);
                            }
                        }
                    },
                    // 出错
                    error -> System.err.println("\nStream Error: " + error.getMessage()),
                    // 流结束
                    () -> {
                        System.out.println();
                        // 将最终回复写入历史
                        history.add(ChatMessage.builder()
                                .role(ChatMessageRole.ASSISTANT.value())
                                .content(finalReply.toString())
                                .build()
                        );
                    }
            );
        }
    }
}
*/

