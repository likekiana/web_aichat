package com.zzh.aichat.service;

import com.zzh.aichat.model.ChatRoom;
import java.util.List;

public interface ChatService {
    // 实现用户与AI聊天
    String doChat(long roomId, String userPrompt);
    // 返回对话列表
    List<ChatRoom> getChatRoomList();
}