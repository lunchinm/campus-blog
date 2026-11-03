package com.example.demo.controller;

import com.example.demo.entity.ArticleDraft;
import com.example.demo.service.ArticleDraftService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/drafts")
public class ArticleDraftController {

    @Autowired
    private ArticleDraftService articleDraftService;

    @GetMapping
    public List<Map<String, Object>> list(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return new ArrayList<>();
        }
        List<ArticleDraft> drafts = articleDraftService.list(
                new QueryWrapper<ArticleDraft>().eq("user_id", userId).orderByDesc("updated_at"));
        List<Map<String, Object>> result = new ArrayList<>();
        for (ArticleDraft draft : drafts) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", draft.getId());
            item.put("title", draft.getTitle());
            item.put("summary", draft.getSummary());
            item.put("category", draft.getCategory());
            item.put("content", draft.getContent());
            item.put("tags", draft.getTags());
            item.put("created_at", draft.getCreatedAt());
            item.put("updated_at", draft.getUpdatedAt());
            result.add(item);
        }
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> detail(@PathVariable Integer id, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return result;
        }
        ArticleDraft draft = articleDraftService.getById(id);
        if (draft == null || !draft.getUserId().equals(userId)) {
            result.put("success", false);
            result.put("message", "草稿不存在");
            return result;
        }
        result.put("id", draft.getId());
        result.put("title", draft.getTitle());
        result.put("summary", draft.getSummary());
        result.put("category", draft.getCategory());
        result.put("content", draft.getContent());
        result.put("tags", draft.getTags());
        result.put("success", true);
        return result;
    }

    @PostMapping
    public Map<String, Object> add(@RequestBody ArticleDraft draft, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return result;
        }
        draft.setUserId(userId);
        articleDraftService.save(draft);
        result.put("success", true);
        result.put("id", draft.getId());
        result.put("message", "保存成功");
        return result;
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Integer id, @RequestBody ArticleDraft draft, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            result.put("success", false);
            result.put("message", "未登录");
            return result;
        }
        ArticleDraft existing = articleDraftService.getById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            result.put("success", false);
            result.put("message", "草稿不存在");
            return result;
        }
        draft.setId(id);
        draft.setUserId(null);
        articleDraftService.updateById(draft);
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
        ArticleDraft existing = articleDraftService.getById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            result.put("success", false);
            result.put("message", "草稿不存在");
            return result;
        }
        articleDraftService.removeById(id);
        result.put("success", true);
        result.put("message", "删除成功");
        return result;
    }
}
