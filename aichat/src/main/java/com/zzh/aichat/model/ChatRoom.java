package com.zzh.aichat.model;

import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import lombok.Data;
import java.util.List;

@Data
public class ChatRoom {
    private Long roomId;
    private List<ChatMessage> chatMessageList;
    private Long createTime; // 新增：房间创建时间戳

    // 由于使用了 @Data 注解，Lombok 会自动生成 getter/setter
    // 包括 getCreateTime() 和 setCreateTime()
}