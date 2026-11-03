package com.example.demo.controller;

import com.example.demo.entity.CommentLike;
import com.example.demo.mapper.CommentLikeMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/comment")
public class CommentLikeController {

    @Autowired
    private CommentLikeMapper commentLikeMapper;

    @PostMapping("/{commentId}/like")
    public Map<String, Object> toggleLike(@PathVariable Integer commentId, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return result;
        }
        CommentLike existing = commentLikeMapper.selectOne(
                new QueryWrapper<CommentLike>().eq("comment_id", commentId).eq("user_id", userId));
        if (existing != null) {
            commentLikeMapper.deleteById(existing.getId());
            result.put("liked", false);
        } else {
            CommentLike like = new CommentLike();
            like.setCommentId(commentId);
            like.setUserId(userId);
            commentLikeMapper.insert(like);
            result.put("liked", true);
        }
        long count = commentLikeMapper.selectCount(
                new QueryWrapper<CommentLike>().eq("comment_id", commentId));
        result.put("success", true);
        result.put("count", count);
        return result;
    }

    @GetMapping("/{commentId}/like")
    public Map<String, Object> getLikeStatus(@PathVariable Integer commentId, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        long count = commentLikeMapper.selectCount(
                new QueryWrapper<CommentLike>().eq("comment_id", commentId));
        result.put("count", count);

        Integer userId = (Integer) request.getAttribute("userId");
        if (userId != null) {
            CommentLike existing = commentLikeMapper.selectOne(
                    new QueryWrapper<CommentLike>().eq("comment_id", commentId).eq("user_id", userId));
            result.put("liked", existing != null);
        } else {
            result.put("liked", false);
        }
        return result;
    }
}
