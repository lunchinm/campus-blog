package com.example.demo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.entity.ArticleComment;
import com.example.demo.mapper.ArticleCommentMapper;
import com.example.demo.service.ArticleCommentService;
import org.springframework.stereotype.Service;

@Service
public class ArticleCommentServiceImpl extends ServiceImpl<ArticleCommentMapper, ArticleComment> implements ArticleCommentService {
}
