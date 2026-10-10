package com.example.aixiaogou.controller;

import java.util.List;
import java.util.Map;

import com.example.aixiaogou.common.Result;
import com.example.aixiaogou.service.OrderService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单接口（前端「我的订单」页）。
 */
@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /** 购物车结算：生成订单并清空购物车 */
    @PostMapping("/checkout")
    public Result<Map<String, Object>> checkout(@RequestParam(value = "userId") Long userId) {
        return Result.ok(orderService.checkout(userId));
    }

    /** 我的订单列表 */
    @GetMapping
    public Result<List<Map<String, Object>>> list(@RequestParam(value = "userId") Long userId) {
        return Result.ok(orderService.listByUser(userId));
    }

    /** 订单详情 */
    @GetMapping("/{orderNo}")
    public Result<Map<String, Object>> detail(@PathVariable("orderNo") String orderNo) {
        return Result.ok(orderService.getByOrderNo(orderNo));
    }

    /** 取消订单（仅待发货可取消，库存回滚） */
    @PostMapping("/{orderNo}/cancel")
    public Result<Map<String, Object>> cancel(@PathVariable("orderNo") String orderNo) {
        return Result.ok(orderService.cancel(orderNo));
    }
}
