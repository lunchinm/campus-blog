package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.entity.Goods;
import com.example.demo.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    /**
     * 1. 新增商品
     * POST /goods
     * 请求体 JSON：{"name":"香皂", "price":5.5, "num":30}
     */
    @PostMapping("/add")
    public String addGoods(@RequestBody Goods goods) {
        boolean success = goodsService.save(goods);
        return success ? "添加成功，生成ID：" + goods.getId() : "添加失败";
    }

    /**
     * 2. 根据 id 删除商品
     * DELETE /goods/{id}
     */
    @DeleteMapping("/delete/{id}")
    public String deleteGoods(@PathVariable Integer id) {
        boolean success = goodsService.removeById(id);
        return success ? "删除成功" : "删除失败（id不存在）";
    }

    /**
     * 3. 条件删除（删除价格大于指定值的商品）
     * DELETE /goods/condition?minPrice=3
     */
    @DeleteMapping("/condition")
    public String deleteByPrice(@RequestParam Double minPrice) {
        QueryWrapper<Goods> wrapper = new QueryWrapper<>();
        wrapper.gt("price", minPrice);
        boolean success = goodsService.remove(wrapper);  // 改为 boolean
        return success ? "删除成功" : "删除失败";
    }

    /**
     * 4. 修改商品（根据 id 更新非空字段）
     * PUT /goods
     * 请求体 JSON：{"id":2, "num":100, "price":2.5}
     */
    @PutMapping("/update")
    public String updateGoods(@RequestBody Goods goods) {
        boolean success = goodsService.updateById(goods);
        return success ? "更新成功" : "更新失败（id不存在）";
    }

    /**
     * 5. 条件修改（将数量小于10的商品价格打8折）
     * PUT /goods/update/condition
     */
    @PutMapping("/update/condition")
    public String updateByCondition() {
        UpdateWrapper<Goods> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lt("num", 10);
        updateWrapper.setSql("price = price * 0.8");
        boolean success = goodsService.update(updateWrapper);  // 改为 boolean
        return success ? "更新成功" : "更新失败";
    }

    /**
     * 6. 根据 id 查询单个商品
     * GET /goods/{id}
     */
    @GetMapping("{id}")
    public Goods getGoodsById(@PathVariable Integer id) {
        return goodsService.getById(id);
    }

    /**
     * 7. 根据名称精确查询单条（若存在多条则只取第一条，不抛异常）
     * GET /goods/one?name=牙刷
     */
    @GetMapping("/one")
    public Goods getGoodsByName(@RequestParam String name) {
        QueryWrapper<Goods> wrapper = new QueryWrapper<>();
        wrapper.eq("name", name);
        return goodsService.getOne(wrapper, false);
    }

    /**
     * 8. 查询所有商品列表
     * GET /goods/list
     */
    @GetMapping("/list")
    public List<Goods> listAllGoods() {
        return goodsService.list();
    }

    /**
     * 9. 条件查询列表（价格在指定范围内，按价格降序）
     * GET /goods/search?minPrice=1&maxPrice=3
     */
    @GetMapping("/search")
    public List<Goods> searchByPrice(@RequestParam Double minPrice,
                                     @RequestParam Double maxPrice) {
        QueryWrapper<Goods> wrapper = new QueryWrapper<>();
        wrapper.between("price", minPrice, maxPrice);
        wrapper.orderByDesc("price");
        return goodsService.list(wrapper);
    }

    /**
     * 10. 分页查询（支持按名称模糊查询）
     * GET /goods/page?current=1&size=2&name=刷
     */
    @GetMapping("/page")
    public Page<Goods> getGoodsPage(@RequestParam(defaultValue = "1") Integer current,
                                    @RequestParam(defaultValue = "5") Integer size,
                                    @RequestParam(required = false) String name) {
        Page<Goods> page = new Page<>(current, size);
        QueryWrapper<Goods> wrapper = new QueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like("name", name);   // name like '%刷%'
        }
        wrapper.orderByDesc("id");  // 按id降序，方便观察
        return goodsService.page(page, wrapper);
    }
}