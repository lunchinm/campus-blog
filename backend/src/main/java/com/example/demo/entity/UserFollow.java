package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_follow")
public class UserFollow {
    private Integer followerId;
    private Integer followedId;
    private LocalDateTime createTime;
}
