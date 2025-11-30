package com.zzh.aichat.controller;

import com.zzh.aichat.model.ChatRoom;
import com.zzh.aichat.service.ChatService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@Api(tags = "脑筋急转弯聊天API")
public class ChatController {

    @Resource
    private ChatService chatService;

    @PostMapping("/{roomId}")
    @ApiOperation("与AI进行脑筋急转弯对话")
    public String doChat(@PathVariable long roomId, @RequestParam String userPrompt) {
        return chatService.doChat(roomId, userPrompt);
    }

    @GetMapping("/rooms")
    @ApiOperation("获取所有聊天室列表")
    public List<ChatRoom> getChatRoomList() {
        return chatService.getChatRoomList();
    }

    @PostMapping("/start")
    @ApiOperation("开始新的脑筋急转弯游戏")
    public String startGame(@RequestParam long roomId) {
        return chatService.doChat(roomId, "开始");
    }
}