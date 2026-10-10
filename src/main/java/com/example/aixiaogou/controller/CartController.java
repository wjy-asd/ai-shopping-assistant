package com.example.aixiaogou.controller;

import java.util.List;
import java.util.Map;

import com.example.aixiaogou.common.Result;
import com.example.aixiaogou.entity.CartEntity;
import com.example.aixiaogou.service.CartService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 购物车接口（前端购物车页）。
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /** 用户购物车列表 */
    @GetMapping
    public Result<Map<String, Object>> list(@RequestParam(value = "userId") Long userId) {
        return Result.ok(cartService.listByUser(userId));
    }

    /** 加入购物车 */
    @PostMapping
    public Result<CartEntity> add(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        Long productId = Long.valueOf(body.get("productId").toString());
        Integer quantity = Integer.valueOf(body.getOrDefault("quantity", 1).toString());
        return Result.ok(cartService.add(userId, productId, quantity));
    }

    /** 修改数量 */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable("id") Long id,
                               @RequestBody Map<String, Object> body) {
        cartService.updateQuantity(id, Integer.valueOf(body.get("quantity").toString()));
        return Result.ok();
    }

    /** 删除单条 */
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable("id") Long id) {
        cartService.remove(id);
        return Result.ok();
    }

    /** 清空购物车 */
    @DeleteMapping
    public Result<Void> clear(@RequestParam(value = "userId") Long userId) {
        cartService.clear(userId);
        return Result.ok();
    }

    /** 结算 */
    @PostMapping("/checkout")
    public Result<Map<String, Object>> checkout(@RequestParam(value = "userId") Long userId) {
        int count = cartService.checkout(userId);
        return Result.ok(Map.of("cleared", count));
    }
}
