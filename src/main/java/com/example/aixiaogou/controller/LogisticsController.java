package com.example.aixiaogou.controller;

import java.util.List;

import com.example.aixiaogou.common.Result;
import com.example.aixiaogou.entity.LogisticsEntity;
import com.example.aixiaogou.service.LogisticsService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 物流接口（前端物流页）。
 */
@RestController
@RequestMapping("/api/logistics")
public class LogisticsController {

    private final LogisticsService logisticsService;

    public LogisticsController(LogisticsService logisticsService) {
        this.logisticsService = logisticsService;
    }

    /** 按订单号查询物流 */
    @GetMapping("/{orderNo}")
    public Result<LogisticsEntity> query(@PathVariable("orderNo") String orderNo) {
        return Result.ok(logisticsService.getByOrderNo(orderNo));
    }

    /** 查询用户全部物流 */
    @GetMapping("/user/{userId}")
    public Result<List<LogisticsEntity>> listByUser(@PathVariable("userId") Long userId) {
        return Result.ok(logisticsService.listByUser(userId));
    }
}
