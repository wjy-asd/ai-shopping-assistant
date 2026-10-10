package com.example.aixiaogou.service;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aixiaogou.entity.LogisticsEntity;
import com.example.aixiaogou.mapper.LogisticsMapper;

import org.springframework.stereotype.Service;

/**
 * 物流服务。
 */
@Service
public class LogisticsService {

    private final LogisticsMapper logisticsMapper;

    public LogisticsService(LogisticsMapper logisticsMapper) {
        this.logisticsMapper = logisticsMapper;
    }

    /** 按订单号查询物流 */
    public LogisticsEntity getByOrderNo(String orderNo) {
        return logisticsMapper.selectOne(
                new LambdaQueryWrapper<LogisticsEntity>()
                        .eq(LogisticsEntity::getOrderNo, orderNo));
    }

    /** 查询用户全部物流记录 */
    public List<LogisticsEntity> listByUser(Long userId) {
        return logisticsMapper.selectList(
                new LambdaQueryWrapper<LogisticsEntity>()
                        .eq(LogisticsEntity::getUserId, userId));
    }
}
