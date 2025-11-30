package com.zzh.aichat.service;

import com.volcengine.ark.runtime.model.bot.completion.chat.BotChatCompletionRequest;
import com.volcengine.ark.runtime.model.bot.completion.chat.BotChatCompletionResult;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class AiManager {

    @Resource
    private ArkService service;

    // 使用示例中的有效 Bot ID
    private final String botId = "bot-20251128180338-q5cvn";

    /**
     * 传入系统prompt和用户prompt进行聊天
     */
    public String doChat(String systemPrompt, String userPrompt) {
        System.out.println("=== AiManager.doChat 被调用 ===");
        System.out.println("Bot ID: " + botId);

        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = ChatMessage.builder()
                .role(ChatMessageRole.SYSTEM)
                .content(systemPrompt)
                .build();
        final ChatMessage userMessage = ChatMessage.builder()
                .role(ChatMessageRole.USER)
                .content(userPrompt)
                .build();
        messages.add(systemMessage);
        messages.add(userMessage);

        return doChat(messages);
    }

    /**
     * 允许用户传入任意条消息进行聊天
     */
    public String doChat(List<ChatMessage> messages) {
        System.out.println("=== 调用火山引擎 API ===");

        try {
            BotChatCompletionRequest chatCompletionRequest = BotChatCompletionRequest.builder()
                    .botId(botId)
                    .messages(messages)
                    .build();

            BotChatCompletionResult chatCompletionResult = service.createBotChatCompletion(chatCompletionRequest);

            if (chatCompletionResult.getChoices() == null || chatCompletionResult.getChoices().isEmpty()) {
                throw new RuntimeException("AI没有返回结果");
            }

            String result = (String) chatCompletionResult.getChoices().get(0).getMessage().getContent();
            System.out.println("AI 返回结果: " + result);
            return result;

        } catch (Exception e) {
            System.out.println("=== 调用火山引擎 API 失败 ===");
            e.printStackTrace();
            throw new RuntimeException("调用AI服务失败: " + e.getMessage(), e);
        }
    }
}