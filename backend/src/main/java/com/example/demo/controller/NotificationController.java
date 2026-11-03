package com.example.demo.controller;

import com.example.demo.entity.Notification;
import com.example.demo.entity.User;
import com.example.demo.mapper.NotificationMapper;
import com.example.demo.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/notification")
public class NotificationController {

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/list")
    public List<Map<String, Object>> list(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) return new ArrayList<>();

        List<Notification> notifications = notificationMapper.selectList(
                new QueryWrapper<Notification>()
                        .eq("user_id", userId)
                        .orderByDesc("create_time"));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Notification n : notifications) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", n.getId());
            map.put("type", n.getType());
            map.put("articleId", n.getArticleId());
            map.put("commentId", n.getCommentId());
            map.put("content", n.getContent());
            map.put("isRead", n.getIsRead());
            map.put("createTime", n.getCreateTime());

            User actor = userMapper.selectById(n.getActorId());
            if (actor != null) {
                Map<String, Object> actorMap = new HashMap<>();
                actorMap.put("id", actor.getId());
                actorMap.put("nickname", actor.getNickname());
                actorMap.put("avatar", actor.getAvatar());
                map.put("actor", actorMap);
            }
            result.add(map);
        }
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
        long count = notificationMapper.selectCount(
                new QueryWrapper<Notification>()
                        .eq("user_id", userId)
                        .eq("is_read", false));
        result.put("count", count);
        return result;
    }

    @PutMapping("/read/{id}")
    public Map<String, Object> markAsRead(@PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();
        Notification notification = new Notification();
        notification.setId(id);
        notification.setIsRead(true);
        notificationMapper.updateById(notification);
        result.put("success", true);
        return result;
    }

    @PutMapping("/read-all")
    public Map<String, Object> markAllAsRead(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            result.put("success", false);
            return result;
        }
        Notification update = new Notification();
        update.setIsRead(true);
        notificationMapper.update(update,
                new QueryWrapper<Notification>()
                        .eq("user_id", userId)
                        .eq("is_read", false));
        result.put("success", true);
        return result;
    }
}
