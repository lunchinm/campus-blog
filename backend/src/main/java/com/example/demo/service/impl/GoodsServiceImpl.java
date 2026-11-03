package com.example.demo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.entity.Goods;
import com.example.demo.service.GoodsService;
import com.example.demo.mapper.GoodsMapper;
import org.springframework.stereotype.Service;

/**
* @author MIC lunchinm
* @description 针对表【goods】的数据库操作Service实现
* @createDate 2026-04-25 17:03:20
*/
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods>
    implements GoodsService{

}




