package com.example.demo.controller;

import com.example.demo.entity.Article;
import com.example.demo.entity.ArticleComment;
import com.example.demo.entity.User;
import com.example.demo.service.ArticleCommentService;
import com.example.demo.service.ArticleService;
import com.example.demo.service.NotificationService;
import com.example.demo.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/article")
public class ArticleCommentController {

    @Autowired
    private ArticleCommentService commentService;

    @Autowired
    private UserService userService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/{articleId}/comments")
    public List<Map<String, Object>> listComments(@PathVariable Integer articleId) {
        List<ArticleComment> comments = commentService.list(
                new QueryWrapper<ArticleComment>().eq("article_id", articleId).orderByAsc("create_time"));
        Map<Integer, Map<String, Object>> map = new HashMap<>();
        List<Map<String, Object>> topLevel = new ArrayList<>();
        for (ArticleComment comment : comments) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", comment.getId());
            item.put("content", comment.getContent());
            item.put("createTime", comment.getCreateTime());
            item.put("userId", comment.getUserId());
            item.put("parentId", comment.getParentId());
            User user = userService.getById(comment.getUserId());
            String nickname = (user != null)
                    ? (user.getNickname() != null ? user.getNickname() : user.getUsername())
                    : "未知";
            item.put("nickname", nickname);
            item.put("children", new ArrayList<>());
            map.put(comment.getId(), item);
            if (comment.getParentId() == null) {
                topLevel.add(item);
            }
        }
        for (ArticleComment comment : comments) {
            if (comment.getParentId() != null) {
                Map<String, Object> parent = map.get(comment.getParentId());
                if (parent != null) {
                    ((List<Map<String, Object>>) parent.get("children")).add(map.get(comment.getId()));
                } else {
                    topLevel.add(map.get(comment.getId()));
                }
            }
        }
        return topLevel;
    }

    @PostMapping("/{articleId}/comments")
    public Map<String, Object> addComment(@PathVariable Integer articleId,
                                          @RequestBody Map<String, String> body,
                                          HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return result;
        }
        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "评论内容不能为空");
            return result;
        }
        ArticleComment comment = new ArticleComment();
        comment.setArticleId(articleId);
        comment.setUserId(userId);
        comment.setContent(content.trim());
        String parentIdStr = body.get("parentId");
        if (parentIdStr != null && !parentIdStr.isEmpty()) {
            comment.setParentId(Integer.valueOf(parentIdStr));
        }
        commentService.save(comment);

        Article article = articleService.getById(articleId);
        if (article != null && !article.getUserId().equals(userId)) {
            if (comment.getParentId() != null) {
                ArticleComment parentComment = commentService.getById(comment.getParentId());
                if (parentComment != null && !parentComment.getUserId().equals(userId)) {
                    notificationService.createNotification(
                            parentComment.getUserId(), userId, "reply", articleId, comment.getParentId(),
                            "回复了你在文章《" + article.getTitle() + "》下的评论");
                }
            } else {
                notificationService.createNotification(
                        article.getUserId(), userId, "comment", articleId, comment.getId(),
                        "评论了你的文章《" + article.getTitle() + "》");
            }
        }

        result.put("success", true);
        result.put("message", "评论成功");
        result.put("id", comment.getId());
        return result;
    }

    @DeleteMapping("/comments/{commentId}")
    public Map<String, Object> deleteComment(@PathVariable Integer commentId, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return result;
        }
        ArticleComment comment = commentService.getById(commentId);
        if (comment == null) {
            result.put("success", false);
            result.put("message", "评论不存在");
            return result;
        }
        if (!comment.getUserId().equals(userId)) {
            result.put("success", false);
            result.put("message", "只能删除自己的评论");
            return result;
        }
        commentService.removeById(commentId);
        result.put("success", true);
        result.put("message", "删除成功");
        return result;
    }

    @GetMapping("/comments/my")
    public List<Map<String, Object>> myComments(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return new ArrayList<>();
        }
        List<ArticleComment> comments = commentService.list(
                new QueryWrapper<ArticleComment>().eq("user_id", userId).orderByDesc("create_time"));
        List<Map<String, Object>> result = new ArrayList<>();
        for (ArticleComment comment : comments) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", comment.getId());
            item.put("content", comment.getContent());
            item.put("createTime", comment.getCreateTime());
            item.put("articleId", comment.getArticleId());
            Article article = articleService.getById(comment.getArticleId());
            item.put("articleTitle", article != null ? article.getTitle() : "文章已删除");
            result.add(item);
        }
        return result;
    }

    @DeleteMapping("/comments/batch")
    public Map<String, Object> batchDeleteComments(@RequestBody Map<String, List<Integer>> body, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return result;
        }
        List<Integer> ids = body.get("ids");
        if (ids == null || ids.isEmpty()) {
            result.put("success", false);
            result.put("message", "请选择要删除的评论");
            return result;
        }
        List<ArticleComment> comments = commentService.listByIds(ids);
        for (ArticleComment comment : comments) {
            if (!comment.getUserId().equals(userId)) {
                result.put("success", false);
                result.put("message", "只能删除自己的评论");
                return result;
            }
        }
        commentService.removeByIds(ids);
        result.put("success", true);
        result.put("message", "批量删除成功");
        return result;
    }
}
