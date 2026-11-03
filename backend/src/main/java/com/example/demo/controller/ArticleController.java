package com.example.demo.controller;

import com.example.demo.entity.Article;
import com.example.demo.entity.ArticleComment;
import com.example.demo.entity.ArticleLike;
import com.example.demo.entity.ArticleTag;
import com.example.demo.entity.Tag;
import com.example.demo.entity.User;
import com.example.demo.entity.UserFollow;
import com.example.demo.mapper.ArticleCommentMapper;
import com.example.demo.mapper.ArticleLikeMapper;
import com.example.demo.mapper.ArticleTagMapper;
import com.example.demo.mapper.TagMapper;
import com.example.demo.mapper.UserFollowMapper;
import com.example.demo.service.ArticleService;
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
public class ArticleController {

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
    private UserFollowMapper userFollowMapper;

    @GetMapping("/list")
    public List<Map<String, Object>> list() {
        List<Article> articles = articleService.list(new QueryWrapper<Article>().orderByDesc("create_time"));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Article article : articles) {
            result.add(buildArticleItem(article));
        }
        return result;
    }

    @GetMapping("/my")
    public List<Map<String, Object>> myArticles(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return new ArrayList<>();
        }
        List<Article> articles = articleService.list(
                new QueryWrapper<Article>().eq("user_id", userId).orderByDesc("create_time"));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Article article : articles) {
            result.add(buildArticleItem(article));
        }
        return result;
    }

    @GetMapping("/following")
    public List<Map<String, Object>> followingArticles(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return new ArrayList<>();
        }
        List<UserFollow> follows = userFollowMapper.selectList(
                new QueryWrapper<UserFollow>().eq("follower_id", userId));
        List<Integer> followingIds = follows.stream()
                .map(UserFollow::getFollowedId)
                .toList();
        if (followingIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<Article> articles = articleService.list(
                new QueryWrapper<Article>()
                        .in("user_id", followingIds)
                        .orderByDesc("create_time"));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Article article : articles) {
            result.add(buildArticleItem(article));
        }
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> detail(@PathVariable Integer id) {
        Article article = articleService.getById(id);
        if (article == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "文章不存在");
            return error;
        }
        articleService.update()
                .set("view_count", article.getViewCount() + 1)
                .eq("id", id)
                .update();

        article.setViewCount(article.getViewCount() + 1);

        Map<String, Object> result = new HashMap<>();
        result.put("id", article.getId());
        result.put("title", article.getTitle());
        result.put("content", article.getContent());
        result.put("summary", article.getSummary());
        result.put("userId", article.getUserId());
        result.put("viewCount", article.getViewCount());
        result.put("shareCount", article.getShareCount() != null ? article.getShareCount() : 0);
        result.put("createTime", article.getCreateTime());
        result.put("updateTime", article.getUpdateTime());

        User author = userService.getById(article.getUserId());
        String authorName = (author != null)
                ? (author.getNickname() != null ? author.getNickname() : author.getUsername())
                : "未知";
        result.put("authorName", authorName);
        result.put("authorAvatar", author != null ? author.getAvatar() : "");

        long likeCount = articleLikeMapper.selectCount(
                new QueryWrapper<ArticleLike>().eq("article_id", id));
        long commentCount = articleCommentMapper.selectCount(
                new QueryWrapper<ArticleComment>().eq("article_id", id));
        result.put("likeCount", likeCount);
        result.put("commentCount", commentCount);
        result.put("shareCount", article.getShareCount() != null ? article.getShareCount() : 0);

        // 获取文章标签
        List<ArticleTag> articleTags = articleTagMapper.selectList(
                new QueryWrapper<ArticleTag>().eq("article_id", id));
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
        result.put("tags", tags);
        return result;
    }

    @PostMapping
    public Map<String, Object> add(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return result;
        }
        Article article = new Article();
        article.setTitle((String) body.get("title"));
        article.setContent((String) body.get("content"));
        article.setSummary((String) body.get("summary"));
        article.setUserId(userId);
        article.setViewCount(0);
        articleService.save(article);

        // 保存标签
        saveArticleTags(article.getId(), body.get("tags"));

        result.put("success", true);
        result.put("message", "发布成功");
        result.put("id", article.getId());
        return result;
    }

    @PutMapping
    public Map<String, Object> update(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return result;
        }
        Integer id = (Integer) body.get("id");
        Article existing = articleService.getById(id);
        if (existing == null) {
            result.put("success", false);
            result.put("message", "文章不存在");
            return result;
        }
        if (!existing.getUserId().equals(userId)) {
            result.put("success", false);
            result.put("message", "只能修改自己的文章");
            return result;
        }
        Article article = new Article();
        article.setId(id);
        article.setTitle((String) body.get("title"));
        article.setContent((String) body.get("content"));
        article.setSummary((String) body.get("summary"));
        articleService.updateById(article);

        // 更新标签
        articleTagMapper.delete(new QueryWrapper<ArticleTag>().eq("article_id", id));
        saveArticleTags(id, body.get("tags"));

        result.put("success", true);
        result.put("message", "更新成功");
        return result;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Integer id, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return result;
        }
        Article existing = articleService.getById(id);
        if (existing == null) {
            result.put("success", false);
            result.put("message", "文章不存在");
            return result;
        }
        if (!existing.getUserId().equals(userId)) {
            result.put("success", false);
            result.put("message", "只能删除自己的文章");
            return result;
        }
        articleService.removeById(id);
        result.put("success", true);
        result.put("message", "删除成功");
        return result;
    }

    @DeleteMapping("/batch")
    public Map<String, Object> batchDelete(@RequestBody Map<String, List<Integer>> body, HttpServletRequest request) {
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
            result.put("message", "请选择要删除的文章");
            return result;
        }
        List<Article> articles = articleService.listByIds(ids);
        for (Article article : articles) {
            if (!article.getUserId().equals(userId)) {
                result.put("success", false);
                result.put("message", "只能删除自己的文章");
                return result;
            }
        }
        articleService.removeByIds(ids);
        result.put("success", true);
        result.put("message", "批量删除成功");
        return result;
    }

    private Map<String, Object> buildArticleItem(Article article) {
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
        item.put("authorAvatar", author != null ? author.getAvatar() : "");
        item.put("userId", article.getUserId());
        long likeCount = articleLikeMapper.selectCount(
                new QueryWrapper<ArticleLike>().eq("article_id", article.getId()));
        long commentCount = articleCommentMapper.selectCount(
                new QueryWrapper<ArticleComment>().eq("article_id", article.getId()));
        item.put("likeCount", likeCount);
        item.put("commentCount", commentCount);
        item.put("shareCount", article.getShareCount() != null ? article.getShareCount() : 0);

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
        return item;
    }

    @SuppressWarnings("unchecked")
    private void saveArticleTags(Integer articleId, Object tagsObj) {
        if (tagsObj == null) return;
        List<Map<String, Object>> tags = (List<Map<String, Object>>) tagsObj;
        for (Map<String, Object> tagMap : tags) {
            String tagName = (String) tagMap.get("name");
            Integer tagType = (Integer) tagMap.get("type");

            // 查找或创建标签
            Tag existingTag = tagMapper.selectOne(
                    new QueryWrapper<Tag>().eq("name", tagName));
            Integer tagId;
            if (existingTag != null) {
                tagId = existingTag.getId();
            } else {
                Tag newTag = new Tag();
                newTag.setName(tagName);
                newTag.setType(tagType != null ? tagType : 1);
                newTag.setColor(TAG_COLORS.getOrDefault(tagName, "#909399"));
                tagMapper.insert(newTag);
                tagId = newTag.getId();
            }

            // 关联文章和标签
            ArticleTag articleTag = new ArticleTag();
            articleTag.setArticleId(articleId);
            articleTag.setTagId(tagId);
            articleTagMapper.insert(articleTag);
        }
    }

    @GetMapping("/search")
    public List<Map<String, Object>> search(@RequestParam String keyword) {
        List<Article> articles = articleService.list(
                new QueryWrapper<Article>().like("title", keyword).orderByDesc("create_time"));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Article article : articles) {
            result.add(buildArticleItem(article));
        }
        return result;
    }

    // 预定义标签关键词映射 + 颜色
    private static final Map<String, List<String>> TAG_KEYWORDS = Map.of(
        "技术", List.of("java","spring","python","vue","react","前端","后端","代码","编程",
                "api","数据库","算法","开发","系统","架构","框架","linux","docker",
                "git","接口","服务器","部署","debug","函数","类库","组件","node",
                "javascript","typescript","css","html","sql","mysql","缓存","并发"),
        "生活", List.of("日常","生活","记录","心情","分享","感悟","日记","周末",
                "假期","运动","健身","电影","音乐","读书","推荐","购物"),
        "学习", List.of("学习","教程","笔记","入门","课程","考试","复习","总结",
                "知识","原理","指南","快速入门","基础","进阶","实战","经验"),
        "随笔", List.of("随笔","想法","思考","杂谈","闲聊","吐槽","观点","随想",
                "碎碎念","杂感","漫谈","絮叨"),
        "旅游", List.of("旅游","旅行","出游","景点","攻略","打卡","民宿","酒店",
                "自驾","风景","城市","游记","路线"),
        "美食", List.of("美食","做饭","食谱","吃","料理","烘焙","甜品","奶茶",
                "咖啡","餐厅","探店","食材")
    );

    private static final Map<String, String> TAG_COLORS = Map.of(
        "技术", "#409eff",
        "生活", "#67c23a",
        "学习", "#e6a23c",
        "随笔", "#9b59b6",
        "旅游", "#00bcd4",
        "美食", "#e91e63",
        "其他", "#909399"
    );

    @GetMapping("/auto-tag")
    public Map<String, Object> autoTag() {
        Map<String, Object> result = new HashMap<>();
        int totalTagged = 0;

        // 获取所有文章
        List<Article> allArticles = articleService.list(
                new QueryWrapper<Article>().orderByAsc("id"));

        for (Article article : allArticles) {
            // 检查是否已有标签
            Long existingCount = articleTagMapper.selectCount(
                    new QueryWrapper<ArticleTag>().eq("article_id", article.getId()));
            if (existingCount > 0) continue;

            // 合并标题和内容用于匹配
            String text = (article.getTitle() + " " + article.getContent()).toLowerCase();

            // 匹配标签
            List<Map.Entry<String, List<String>>> matched = new ArrayList<>();
            for (Map.Entry<String, List<String>> entry : TAG_KEYWORDS.entrySet()) {
                long hitCount = entry.getValue().stream()
                        .filter(kw -> text.contains(kw.toLowerCase()))
                        .count();
                if (hitCount > 0) {
                    matched.add(entry);
                }
            }

            // 按命中数量降序，取前2个
            matched.sort((a, b) ->
                    Long.compare(
                            b.getValue().stream().filter(kw -> text.contains(kw.toLowerCase())).count(),
                            a.getValue().stream().filter(kw -> text.contains(kw.toLowerCase())).count()));

            int limit = Math.min(matched.size(), 2);
            for (int i = 0; i < limit; i++) {
                String tagName = matched.get(i).getKey();
                saveSingleTag(article.getId(), tagName, 0); // type=0 预定义
            }

            // 如果没有匹配到任何标签，打上"其他"
            if (matched.isEmpty()) {
                saveSingleTag(article.getId(), "其他", 0);
            }

            totalTagged++;
        }

        result.put("success", true);
        result.put("message", "自动标签完成，共处理 " + totalTagged + " 篇文章");
        return result;
    }

    private void saveSingleTag(Integer articleId, String tagName, Integer tagType) {
        Tag existingTag = tagMapper.selectOne(
                new QueryWrapper<Tag>().eq("name", tagName));
        Integer tagId;
        if (existingTag != null) {
            tagId = existingTag.getId();
        } else {
            Tag newTag = new Tag();
            newTag.setName(tagName);
            newTag.setType(tagType);
            newTag.setColor(TAG_COLORS.getOrDefault(tagName, "#909399"));
            tagMapper.insert(newTag);
            tagId = newTag.getId();
        }
        ArticleTag articleTag = new ArticleTag();
        articleTag.setArticleId(articleId);
        articleTag.setTagId(tagId);
        articleTagMapper.insert(articleTag);
    }
}
