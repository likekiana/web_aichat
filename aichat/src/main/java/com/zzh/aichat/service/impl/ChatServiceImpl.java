package com.zzh.aichat.service.impl;

import com.zzh.aichat.model.ChatRoom;
import com.zzh.aichat.service.AiManager;
import com.zzh.aichat.service.ChatService;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatServiceImpl implements ChatService {

    @Resource
    private AiManager aiManager;

    // 存储历史对话 - 使用ConcurrentHashMap保证线程安全
    private final Map<Long, List<ChatMessage>> chatHistories = new ConcurrentHashMap<>();
    private final Map<Long, Long> roomCreateTime = new ConcurrentHashMap<>();

    @Override
    public String doChat(long roomId, String userPrompt) {
        String systemPrompt = "你是一位脑筋急转弯游戏主持人，我们将进行一个\"是非问答\"推理游戏。\n" +
                "\n游戏规则如下：\n" +
                "当我说\"开始\"时，你要随机出一道脑筋急转弯题目（题干简短、有趣、但需要逻辑推理或反向思考）。\n" +
                "出题后，你只负责回答我的提问，每次只能回答以下三种之一：是、否、与此无关\n" +
                "在合适的时候，你可以适当引导我，比如说\"你离真相更近了\"或\"你可能忽略了某个细节\"。\n" +
                "\n游戏结束条件（满足任一即可）：\n" +
                "1. 我说出\"不想玩了\"、\"告诉我答案\"、\"揭晓答案\"等类似表达；\n" +
                "2. 我已经基本推理出真相、还原了故事，或所有关键问题都被询问到；\n" +
                "3. 我输入\"退出\"；\n" +
                "4. 已经问了10个问题，但我仍然没有接近真相或关键线索。\n" +
                "\n结束时你的任务：\n" +
                "输出\"游戏结束\"，并给出本题的正确答案或\"汤底\"（即故事的完整解释）。\n" +
                "如果我表现得不错，可以适当给一句点评或鼓励。\n" +
                "\n准备好后，当我输入\"开始\"，游戏正式开始。";

        // 准备消息列表
        List<ChatMessage> messages = new ArrayList<>();

        // 检查是否是新房间
        if (!chatHistories.containsKey(roomId)) {
            System.out.println("🆕 创建新房间: " + roomId);

            // 新房间：添加系统消息并记录创建时间
            roomCreateTime.put(roomId, System.currentTimeMillis());

            final ChatMessage systemMessage = ChatMessage.builder()
                    .role(ChatMessageRole.SYSTEM)
                    .content(systemPrompt)
                    .build();
            messages.add(systemMessage);

            // 初始化历史记录（只包含系统消息）
            chatHistories.put(roomId, new ArrayList<>(messages));
        } else {
            // 已有房间：直接使用历史消息
            System.out.println("📂 加载房间历史: " + roomId + ", 消息数: " + chatHistories.get(roomId).size());
            messages = new ArrayList<>(chatHistories.get(roomId));
        }

        // 添加当前用户消息
        final ChatMessage userMessage = ChatMessage.builder()
                .role(ChatMessageRole.USER)
                .content(userPrompt)
                .build();
        messages.add(userMessage);

        System.out.println("📤 发送给AI的消息数: " + messages.size());
        for (ChatMessage msg : messages) {
            String content = msg.getContent().toString(); // 转换为String
            System.out.println("  " + msg.getRole() + ": " +
                    (content.length() > 100 ? content.substring(0, 100) + "..." : content));
        }

        // 调用AI
        String answer = aiManager.doChat(messages);

        // 添加AI回复到历史
        final ChatMessage answerMessage = ChatMessage.builder()
                .role(ChatMessageRole.ASSISTANT)
                .content(answer)
                .build();

        // 更新历史记录：包含之前的所有消息 + 当前用户消息 + AI回复
        List<ChatMessage> updatedHistory = new ArrayList<>(chatHistories.get(roomId));
        updatedHistory.add(userMessage);
        updatedHistory.add(answerMessage);
        chatHistories.put(roomId, updatedHistory);

        System.out.println("💾 更新后房间 " + roomId + " 的消息数: " + updatedHistory.size());

        // 如果游戏结束，清理历史记录
        if (answer.contains("游戏结束")) {
            System.out.println("🎯 游戏结束，清理房间: " + roomId);
            chatHistories.remove(roomId);
            roomCreateTime.remove(roomId);
        }

        return answer;
    }

    @Override
    public List<ChatRoom> getChatRoomList() {
        List<ChatRoom> chatRoomList = new ArrayList<>();

        System.out.println("📋 获取房间列表，当前房间数: " + chatHistories.size());

        for (Map.Entry<Long, List<ChatMessage>> entry : chatHistories.entrySet()) {
            Long roomId = entry.getKey();
            List<ChatMessage> messages = entry.getValue();

            System.out.println("  房间 " + roomId + " 有 " + messages.size() + " 条消息");

            ChatRoom chatRoom = new ChatRoom();
            chatRoom.setRoomId(roomId);
            chatRoom.setChatMessageList(new ArrayList<>(messages));
            chatRoom.setCreateTime(roomCreateTime.get(roomId));

            chatRoomList.add(chatRoom);
        }

        // 按创建时间倒序排列
        chatRoomList.sort((r1, r2) -> {
            Long time1 = r1.getCreateTime() != null ? r1.getCreateTime() : 0L;
            Long time2 = r2.getCreateTime() != null ? r2.getCreateTime() : 0L;
            return Long.compare(time2, time1);
        });

        return chatRoomList;
    }
}