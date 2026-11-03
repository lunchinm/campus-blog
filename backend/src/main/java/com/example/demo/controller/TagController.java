package com.example.demo.controller;

import com.example.demo.entity.Tag;
import com.example.demo.service.TagService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tags")
public class TagController {

    @Autowired
    private TagService tagService;

    @GetMapping
    public List<Tag> list() {
        return tagService.list(new QueryWrapper<Tag>().orderByAsc("type", "id"));
    }
}
