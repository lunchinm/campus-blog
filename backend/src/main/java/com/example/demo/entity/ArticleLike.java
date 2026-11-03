package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("article_like")
public class ArticleLike {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer articleId;
    private Integer userId;
    private LocalDateTime createTime;
}
