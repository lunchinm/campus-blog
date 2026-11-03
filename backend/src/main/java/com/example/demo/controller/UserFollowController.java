package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.entity.UserFollow;
import com.example.demo.mapper.UserFollowMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.NotificationService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/follow")
public class UserFollowController {

    @Autowired
    private UserFollowMapper userFollowMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/{userId}")
    public Map<String, Object> follow(@PathVariable Integer userId, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        Integer currentUserId = (Integer) request.getAttribute("userId");
        if (currentUserId == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return result;
        }
        if (currentUserId.equals(userId)) {
            result.put("success", false);
            result.put("message", "不能关注自己");
            return result;
        }

        long count = userFollowMapper.selectCount(
                new QueryWrapper<UserFollow>()
                        .eq("follower_id", currentUserId)
                        .eq("followed_id", userId));
        if (count > 0) {
            result.put("success", false);
            result.put("message", "已关注该用户");
            return result;
        }

        UserFollow follow = new UserFollow();
        follow.setFollowerId(currentUserId);
        follow.setFollowedId(userId);
        follow.setCreateTime(LocalDateTime.now());
        userFollowMapper.insert(follow);

        notificationService.createNotification(
                userId, currentUserId, "follow", null, null,
                "关注了你");

        result.put("success", true);
        result.put("message", "关注成功");
        return result;
    }

    @DeleteMapping("/{userId}")
    public Map<String, Object> unfollow(@PathVariable Integer userId, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        Integer currentUserId = (Integer) request.getAttribute("userId");
        if (currentUserId == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return result;
        }

        userFollowMapper.delete(
                new QueryWrapper<UserFollow>()
                        .eq("follower_id", currentUserId)
                        .eq("followed_id", userId));

        result.put("success", true);
        result.put("message", "取消关注成功");
        return result;
    }

    @GetMapping("/followers/{userId}")
    public List<Map<String, Object>> getFollowers(@PathVariable Integer userId) {
        List<UserFollow> follows = userFollowMapper.selectList(
                new QueryWrapper<UserFollow>().eq("followed_id", userId));
        return buildUserList(follows, true);
    }

    @GetMapping("/following/{userId}")
    public List<Map<String, Object>> getFollowing(@PathVariable Integer userId) {
        List<UserFollow> follows = userFollowMapper.selectList(
                new QueryWrapper<UserFollow>().eq("follower_id", userId));
        return buildUserList(follows, false);
    }

    private List<Map<String, Object>> buildUserList(List<UserFollow> follows, boolean isFollower) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (UserFollow follow : follows) {
            Integer targetId = isFollower ? follow.getFollowerId() : follow.getFollowedId();
            User user = userMapper.selectById(targetId);
            if (user != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", user.getId());
                map.put("username", user.getUsername());
                map.put("nickname", user.getNickname());
                map.put("avatar", user.getAvatar());
                map.put("followTime", follow.getCreateTime());
                result.add(map);
            }
        }
        return result;
    }
}
