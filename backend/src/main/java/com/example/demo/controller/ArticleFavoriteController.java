package com.example.demo.controller;

import com.example.demo.entity.Article;
import com.example.demo.entity.ArticleComment;
import com.example.demo.entity.ArticleFavorite;
import com.example.demo.entity.ArticleLike;
import com.example.demo.entity.ArticleTag;
import com.example.demo.entity.Tag;
import com.example.demo.entity.User;
import com.example.demo.mapper.ArticleFavoriteMapper;
import com.example.demo.mapper.ArticleLikeMapper;
import com.example.demo.mapper.ArticleCommentMapper;
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
public class ArticleFavoriteController {

    @Autowired
    private ArticleFavoriteMapper favoriteMapper;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private UserService userService;

    @Autowired
    private ArticleLikeMapper articleLikeMapper;

    @Autowired
    private ArticleCommentMapper articleCommentMapper;

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private ArticleTagMapper articleTagMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/{articleId}/favorite")
    public Map<String, Object> toggleFavorite(@PathVariable Integer articleId, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return result;
        }
        ArticleFavorite existing = favoriteMapper.selectOne(
                new QueryWrapper<ArticleFavorite>().eq("article_id", articleId).eq("user_id", userId));
        if (existing != null) {
            favoriteMapper.deleteById(existing.getId());
            result.put("favorited", false);
        } else {
            ArticleFavorite fav = new ArticleFavorite();
            fav.setArticleId(articleId);
            fav.setUserId(userId);
            favoriteMapper.insert(fav);
            result.put("favorited", true);

            Article article = articleMapper.selectById(articleId);
            if (article != null && !article.getUserId().equals(userId)) {
                notificationService.createNotification(
                        article.getUserId(), userId, "favorite", articleId, null,
                        "收藏了你的文章《" + article.getTitle() + "》");
            }
        }
        long count = favoriteMapper.selectCount(
                new QueryWrapper<ArticleFavorite>().eq("article_id", articleId));
        result.put("success", true);
        result.put("count", count);
        return result;
    }

    @GetMapping("/{articleId}/favorite")
    public Map<String, Object> getFavoriteStatus(@PathVariable Integer articleId, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        long count = favoriteMapper.selectCount(
                new QueryWrapper<ArticleFavorite>().eq("article_id", articleId));
        result.put("count", count);

        Integer userId = (Integer) request.getAttribute("userId");
        if (userId != null) {
            ArticleFavorite existing = favoriteMapper.selectOne(
                    new QueryWrapper<ArticleFavorite>().eq("article_id", articleId).eq("user_id", userId));
            result.put("favorited", existing != null);
        } else {
            result.put("favorited", false);
        }
        return result;
    }

    @DeleteMapping("/favorites/batch")
    public Map<String, Object> batchRemoveFavorites(@RequestBody Map<String, List<Integer>> body, HttpServletRequest request) {
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
            result.put("message", "请选择要取消收藏的文章");
            return result;
        }
        for (Integer articleId : ids) {
            favoriteMapper.delete(
                    new QueryWrapper<ArticleFavorite>().eq("article_id", articleId).eq("user_id", userId));
        }
        result.put("success", true);
        result.put("message", "取消收藏成功");
        return result;
    }

    @GetMapping("/favorites/my")
    public List<Map<String, Object>> myFavorites(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return new ArrayList<>();
        }
        List<ArticleFavorite> favorites = favoriteMapper.selectList(
                new QueryWrapper<ArticleFavorite>().eq("user_id", userId).orderByDesc("create_time"));
        List<Map<String, Object>> result = new ArrayList<>();
        for (ArticleFavorite fav : favorites) {
            Article article = articleService.getById(fav.getArticleId());
            if (article == null) continue;
            Map<String, Object> item = new HashMap<>();
            item.put("id", article.getId());
            item.put("title", article.getTitle());
            item.put("summary", article.getSummary());
            item.put("viewCount", article.getViewCount());
            item.put("createTime", article.getCreateTime());
            item.put("favoriteTime", fav.getCreateTime());
            User author = userService.getById(article.getUserId());
            String authorName = (author != null)
                    ? (author.getNickname() != null ? author.getNickname() : author.getUsername())
                    : "未知";
            item.put("authorName", authorName);
            long likeCount = articleLikeMapper.selectCount(
                    new QueryWrapper<ArticleLike>().eq("article_id", article.getId()));
            long commentCount = articleCommentMapper.selectCount(
                    new QueryWrapper<ArticleComment>().eq("article_id", article.getId()));
            item.put("likeCount", likeCount);
            item.put("commentCount", commentCount);

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
