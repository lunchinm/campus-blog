package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.demo.entity.Article;
import com.example.demo.entity.PrivateMessage;
import com.example.demo.entity.PrivateMessageConversation;
import com.example.demo.entity.User;
import com.example.demo.mapper.ArticleMapper;
import com.example.demo.mapper.PrivateMessageConversationMapper;
import com.example.demo.mapper.PrivateMessageMapper;
import com.example.demo.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PrivateMessageService {

    @Autowired
    private PrivateMessageMapper privateMessageMapper;

    @Autowired
    private PrivateMessageConversationMapper conversationMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ArticleMapper articleMapper;

    public PrivateMessage sendMessage(Integer senderId, Integer receiverId, String content, String type, String extraData) {
        if (type == null || type.isEmpty()) {
            type = "text";
        }

        PrivateMessageConversation conversation = getOrCreateConversation(senderId, receiverId);

        PrivateMessage message = new PrivateMessage();
        message.setConversationId(conversation.getId());
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setType(type);
        message.setExtraData(extraData);
        message.setIsRead(false);
        message.setCreateTime(LocalDateTime.now());
        privateMessageMapper.insert(message);

        // 如果分享的是文章，增加文章分享次数
        if ("article".equals(type) && extraData != null && !extraData.isEmpty()) {
            try {
                Integer articleId = Integer.parseInt(extraData);
                Article article = articleMapper.selectById(articleId);
                if (article != null) {
                    article.setShareCount((article.getShareCount() != null ? article.getShareCount() : 0) + 1);
                    articleMapper.updateById(article);
                }
            } catch (Exception e) {
                // 忽略分享计数异常，不影响消息发送
            }
        }

        conversation.setLastMessage(content);
        conversation.setLastMessageTime(LocalDateTime.now());

        if (conversation.getUser1Id().equals(senderId)) {
            conversation.setUser2UnreadCount(conversation.getUser2UnreadCount() + 1);
        } else {
            conversation.setUser1UnreadCount(conversation.getUser1UnreadCount() + 1);
        }
        conversation.setUpdateTime(LocalDateTime.now());
        conversationMapper.updateById(conversation);

        return message;
    }

    public List<Map<String, Object>> getConversations(Integer userId) {
        List<PrivateMessageConversation> conversations = conversationMapper.selectList(
                new QueryWrapper<PrivateMessageConversation>()
                        .eq("user1_id", userId)
                        .or()
                        .eq("user2_id", userId)
                        .orderByDesc("last_message_time"));

        List<Map<String, Object>> result = new ArrayList<>();
        for (PrivateMessageConversation c : conversations) {
            Map<String, Object> map = new HashMap<>();
            map.put("conversationId", c.getId());
            map.put("lastMessage", c.getLastMessage());
            map.put("lastMessageTime", c.getLastMessageTime());
            map.put("createTime", c.getCreateTime());
            map.put("updateTime", c.getUpdateTime());

            Integer otherUserId = c.getUser1Id().equals(userId) ? c.getUser2Id() : c.getUser1Id();
            Integer unreadCount = c.getUser1Id().equals(userId) ? c.getUser1UnreadCount() : c.getUser2UnreadCount();
            map.put("unreadCount", unreadCount);

            User otherUser = userMapper.selectById(otherUserId);
            if (otherUser != null) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", otherUser.getId());
                userMap.put("nickname", otherUser.getNickname());
                userMap.put("avatar", otherUser.getAvatar());
                map.put("otherUser", userMap);
            }

            result.add(map);
        }
        return result;
    }

    public List<Map<String, Object>> getMessages(Integer conversationId, Integer userId) {
        List<PrivateMessage> messages = privateMessageMapper.selectList(
                new QueryWrapper<PrivateMessage>()
                        .eq("conversation_id", conversationId)
                        .orderByAsc("create_time"));

        List<Map<String, Object>> result = new ArrayList<>();
        for (PrivateMessage m : messages) {
            if (m.getReceiverId().equals(userId) && !m.getIsRead()) {
                PrivateMessage update = new PrivateMessage();
                update.setId(m.getId());
                update.setIsRead(true);
                privateMessageMapper.updateById(update);
            }

            Map<String, Object> map = new HashMap<>();
            map.put("id", m.getId());
            map.put("conversationId", m.getConversationId());
            map.put("senderId", m.getSenderId());
            map.put("receiverId", m.getReceiverId());
            map.put("content", m.getContent());
            map.put("type", m.getType());
            map.put("extraData", m.getExtraData());
            map.put("isRead", m.getIsRead());
            map.put("createTime", m.getCreateTime());
            result.add(map);
        }

        PrivateMessageConversation conversation = conversationMapper.selectById(conversationId);
        if (conversation != null) {
            if (conversation.getUser1Id().equals(userId)) {
                conversation.setUser1UnreadCount(0);
            } else if (conversation.getUser2Id().equals(userId)) {
                conversation.setUser2UnreadCount(0);
            }
            conversation.setUpdateTime(LocalDateTime.now());
            conversationMapper.updateById(conversation);
        }

        return result;
    }

    public void markConversationAsRead(Integer conversationId, Integer userId) {
        PrivateMessageConversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) return;

        PrivateMessage update = new PrivateMessage();
        update.setIsRead(true);
        privateMessageMapper.update(update,
                new UpdateWrapper<PrivateMessage>()
                        .eq("conversation_id", conversationId)
                        .eq("receiver_id", userId)
                        .eq("is_read", false));

        if (conversation.getUser1Id().equals(userId)) {
            conversation.setUser1UnreadCount(0);
        } else if (conversation.getUser2Id().equals(userId)) {
            conversation.setUser2UnreadCount(0);
        }
        conversation.setUpdateTime(LocalDateTime.now());
        conversationMapper.updateById(conversation);
    }

    public int getUnreadCount(Integer userId) {
        List<PrivateMessageConversation> conversations = conversationMapper.selectList(
                new QueryWrapper<PrivateMessageConversation>()
                        .eq("user1_id", userId)
                        .or()
                        .eq("user2_id", userId));

        int count = 0;
        for (PrivateMessageConversation c : conversations) {
            if (c.getUser1Id().equals(userId)) {
                count += c.getUser1UnreadCount();
            } else {
                count += c.getUser2UnreadCount();
            }
        }
        return count;
    }

    public PrivateMessageConversation getOrCreateConversation(Integer userId1, Integer userId2) {
        Integer smallerId = Math.min(userId1, userId2);
        Integer largerId = Math.max(userId1, userId2);

        PrivateMessageConversation conversation = conversationMapper.selectOne(
                new QueryWrapper<PrivateMessageConversation>()
                        .eq("user1_id", smallerId)
                        .eq("user2_id", largerId));

        if (conversation == null) {
            conversation = new PrivateMessageConversation();
            conversation.setUser1Id(smallerId);
            conversation.setUser2Id(largerId);
            conversation.setLastMessage("");
            conversation.setUser1UnreadCount(0);
            conversation.setUser2UnreadCount(0);
            conversation.setCreateTime(LocalDateTime.now());
            conversation.setUpdateTime(LocalDateTime.now());
            conversationMapper.insert(conversation);
        }

        return conversation;
    }
}
