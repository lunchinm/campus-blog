package com.example.demo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.entity.ArticleDraft;
import com.example.demo.mapper.ArticleDraftMapper;
import com.example.demo.service.ArticleDraftService;
import org.springframework.stereotype.Service;

@Service
public class ArticleDraftServiceImpl extends ServiceImpl<ArticleDraftMapper, ArticleDraft> implements ArticleDraftService {
}
