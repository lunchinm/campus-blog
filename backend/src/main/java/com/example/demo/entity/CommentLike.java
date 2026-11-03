package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("comment_like")
public class CommentLike {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer commentId;
    private Integer userId;
    private LocalDateTime createdAt;
}
