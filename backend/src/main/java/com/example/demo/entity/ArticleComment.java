package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("article_comment")
public class ArticleComment {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer articleId;
    private Integer userId;
    private String content;
    private Integer parentId;
    private LocalDateTime createTime;
}
