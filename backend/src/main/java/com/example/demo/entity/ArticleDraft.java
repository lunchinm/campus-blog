package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("article_draft")
public class ArticleDraft {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private String title;
    private String summary;
    private String category;
    private String content;
    private String tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
