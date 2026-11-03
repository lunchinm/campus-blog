package com.example.demo.controller;

import com.example.demo.service.PrivateMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/message")
public class PrivateMessageController {

    @Autowired
    private PrivateMessageService privateMessageService;

    @PostMapping("/send")
    public Map<String, Object> sendMessage(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        Map<String, Object> result = new HashMap<>();
        Integer senderId = (Integer) request.getAttribute("userId");
        if (senderId == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return result;
        }

        Integer receiverId = (Integer) body.get("receiverId");
        String content = (String) body.get("content");
        String type = body.get("type") != null ? String.valueOf(body.get("type")) : "text";
        String extraData = body.get("extraData") != null ? String.valueOf(body.get("extraData")) : null;
        if (receiverId == null || content == null || content.isEmpty()) {
            result.put("success", false);
            result.put("message", "参数错误");
            return result;
        }

        privateMessageService.sendMessage(senderId, receiverId, content, type, extraData);
        result.put("success", true);
        return result;
    }

    @GetMapping("/conversations")
    public List<Map<String, Object>> getConversations(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return new java.util.ArrayList<>();
        }
        return privateMessageService.getConversations(userId);
    }

    @GetMapping("/conversation/{conversationId}")
    public List<Map<String, Object>> getMessages(HttpServletRequest request,
                                                  @PathVariable Integer conversationId) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return new java.util.ArrayList<>();
        }
        return privateMessageService.getMessages(conversationId, userId);
    }

    @PutMapping("/conversation/{conversationId}/read")
    public Map<String, Object> markConversationAsRead(HttpServletRequest request,
                                                       @PathVariable Integer conversationId) {
        Map<String, Object> result = new HashMap<>();
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return result;
        }

        privateMessageService.markConversationAsRead(conversationId, userId);
        result.put("success", true);
        return result;
    }

    @GetMapping("/unread-count")
    public Map<String, Object> unreadCount(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            result.put("count", 0);
            return result;
        }
        int count = privateMessageService.getUnreadCount(userId);
        result.put("count", count);
        return result;
    }
}
