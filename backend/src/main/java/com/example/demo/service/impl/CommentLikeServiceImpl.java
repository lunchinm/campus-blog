package com.example.demo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.entity.CommentLike;
import com.example.demo.mapper.CommentLikeMapper;
import com.example.demo.service.CommentLikeService;
import org.springframework.stereotype.Service;

@Service
public class CommentLikeServiceImpl extends ServiceImpl<CommentLikeMapper, CommentLike> implements CommentLikeService {
}
