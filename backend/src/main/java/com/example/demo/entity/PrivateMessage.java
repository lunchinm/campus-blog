package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("private_message")
public class PrivateMessage {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer conversationId;
    private Integer senderId;
    private Integer receiverId;
    private String content;
    private String type;
    private String extraData;
    private Boolean isRead;
    private LocalDateTime createTime;
}
