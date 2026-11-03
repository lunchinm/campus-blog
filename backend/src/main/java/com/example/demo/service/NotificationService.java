package com.example.demo.service;

import com.example.demo.entity.Notification;
import com.example.demo.mapper.NotificationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    public void createNotification(Integer userId, Integer actorId, String type,
                                   Integer articleId, Integer commentId, String content) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setActorId(actorId);
        notification.setType(type);
        notification.setArticleId(articleId);
        notification.setCommentId(commentId);
        notification.setContent(content);
        notification.setIsRead(false);
        notificationMapper.insert(notification);
    }
}
