package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.entity.Goods;
import com.example.demo.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// 标记该类为 RESTful 风格的控制器，所有方法返回值会直接写入 HTTP 响应体（相当于 @Controller + @ResponseBody）
@RestController
// 声明一个控制器类，处理与商品相关的请求
public class TestController {

    // 自动注入 GoodsService 的 Bean（由 Spring 容器管理，无需手动 new）
    @Autowired
    // 创建一个service层对象
            GoodsService goodsService;

    // 映射 HTTP GET 请求路径 "/test" 到此方法
    @GetMapping("/test")
    // 方法返回一个 Goods 对象，Spring 会自动将其转为 JSON 格式响应
    public Goods test(){
        // 创建 QueryWrapper 对象，用于构建查询条件，泛型指定操作的数据表实体类为 Goods
        QueryWrapper<Goods> userQueryWrapper = new QueryWrapper<>();
        // 添加等值条件：数据库字段 "id" 等于 1（注意字段名是数据库列名，不是实体类属性名）
        userQueryWrapper.eq("id",1);
        // 调用 Service 的 getOne 方法，根据条件查询一条记录（若有多条会抛出异常）
        Goods good = goodsService.getOne(userQueryWrapper);
        // 返回查询到的 Goods 对象
        return good;
    }
}