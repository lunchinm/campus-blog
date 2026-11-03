package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("private_message_conversation")
public class PrivateMessageConversation {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer user1Id;
    private Integer user2Id;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Integer user1UnreadCount;
    private Integer user2UnreadCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
