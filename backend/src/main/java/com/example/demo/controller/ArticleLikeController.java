package com.example.demo.controller;

import com.example.demo.entity.Article;
import com.example.demo.entity.ArticleLike;
import com.example.demo.entity.ArticleTag;
import com.example.demo.entity.Tag;
import com.example.demo.entity.User;
import com.example.demo.mapper.ArticleLikeMapper;
import com.example.demo.mapper.ArticleMapper;
import com.example.demo.mapper.ArticleTagMapper;
import com.example.demo.mapper.TagMapper;
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
public class ArticleLikeController {

    @Autowired
    private ArticleLikeMapper articleLikeMapper;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private UserService userService;

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private ArticleTagMapper articleTagMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/{articleId}/like")
    public Map<String, Object> toggleLike(@PathVariable Integer articleId, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return result;
        }
        ArticleLike existing = articleLikeMapper.selectOne(
                new QueryWrapper<ArticleLike>().eq("article_id", articleId).eq("user_id", userId));
        if (existing != null) {
            articleLikeMapper.deleteById(existing.getId());
            result.put("liked", false);
        } else {
            ArticleLike like = new ArticleLike();
            like.setArticleId(articleId);
            like.setUserId(userId);
            articleLikeMapper.insert(like);
            result.put("liked", true);

            Article article = articleMapper.selectById(articleId);
            if (article != null && !article.getUserId().equals(userId)) {
                notificationService.createNotification(
                        article.getUserId(), userId, "like", articleId, null,
                        "赞了你的文章《" + article.getTitle() + "》");
            }
        }
        long count = articleLikeMapper.selectCount(
                new QueryWrapper<ArticleLike>().eq("article_id", articleId));
        result.put("success", true);
        result.put("count", count);
        return result;
    }

    @GetMapping("/{articleId}/like")
    public Map<String, Object> getLikeStatus(@PathVariable Integer articleId, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        long count = articleLikeMapper.selectCount(
                new QueryWrapper<ArticleLike>().eq("article_id", articleId));
        result.put("count", count);

        Integer userId = (Integer) request.getAttribute("userId");
        if (userId != null) {
            ArticleLike existing = articleLikeMapper.selectOne(
                    new QueryWrapper<ArticleLike>().eq("article_id", articleId).eq("user_id", userId));
            result.put("liked", existing != null);
        } else {
            result.put("liked", false);
        }
        return result;
    }

    @GetMapping("/likes/my")
    public List<Map<String, Object>> myLikes(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return new ArrayList<>();
        }
        List<ArticleLike> likes = articleLikeMapper.selectList(
                new QueryWrapper<ArticleLike>().eq("user_id", userId).orderByDesc("create_time"));
        List<Map<String, Object>> result = new ArrayList<>();
        for (ArticleLike like : likes) {
            Article article = articleService.getById(like.getArticleId());
            if (article == null) continue;
            Map<String, Object> item = new HashMap<>();
            item.put("id", article.getId());
            item.put("title", article.getTitle());
            item.put("summary", article.getSummary());
            item.put("viewCount", article.getViewCount());
            item.put("createTime", article.getCreateTime());
            User author = userService.getById(article.getUserId());
            String authorName = (author != null)
                    ? (author.getNickname() != null ? author.getNickname() : author.getUsername())
                    : "未知";
            item.put("authorName", authorName);
            long count = articleLikeMapper.selectCount(
                    new QueryWrapper<ArticleLike>().eq("article_id", article.getId()));
            item.put("likeCount", count);

            // 获取文章标签
            List<ArticleTag> articleTags = articleTagMapper.selectList(
                    new QueryWrapper<ArticleTag>().eq("article_id", article.getId()));
            List<Map<String, Object>> tags = new ArrayList<>();
            for (ArticleTag at : articleTags) {
                Tag tag = tagMapper.selectById(at.getTagId());
                if (tag != null) {
                    Map<String, Object> tagMap = new HashMap<>();
                    tagMap.put("name", tag.getName());
                    tagMap.put("type", tag.getType());
                    tagMap.put("color", tag.getColor());
                    tags.add(tagMap);
                }
            }
            item.put("tags", tags);
            result.add(item);
        }
        return result;
    }
}
