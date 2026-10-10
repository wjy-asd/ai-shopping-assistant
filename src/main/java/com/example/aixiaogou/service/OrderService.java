package com.example.aixiaogou.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aixiaogou.entity.LogisticsEntity;
import com.example.aixiaogou.entity.OrderEntity;
import com.example.aixiaogou.entity.OrderItemEntity;
import com.example.aixiaogou.entity.ProductEntity;
import com.example.aixiaogou.mapper.LogisticsMapper;
import com.example.aixiaogou.mapper.OrderItemMapper;
import com.example.aixiaogou.mapper.OrderMapper;
import com.example.aixiaogou.mapper.ProductMapper;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 订单服务：购物车结算生成订单、订单列表、订单详情、取消订单。
 */
@Service
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartService cartService;
    private final LogisticsMapper logisticsMapper;
    private final ProductMapper productMapper;

    public OrderService(OrderMapper orderMapper, OrderItemMapper orderItemMapper,
                        CartService cartService, LogisticsMapper logisticsMapper,
                        ProductMapper productMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.cartService = cartService;
        this.logisticsMapper = logisticsMapper;
        this.productMapper = productMapper;
    }

    /** 购物车结算：生成订单 + 明细快照 + 物流记录（已下单），扣减库存，并清空购物车 */
    @Transactional
    public Map<String, Object> checkout(Long userId) {
        Map<String, Object> cart = cartService.listByUser(userId);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) cart.get("items");
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("购物车为空，无法下单");
        }

        String orderNo = "ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")) + userId;
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Map<String, Object> item : items) {
            totalAmount = totalAmount.add(toBigDecimal(item.get("subtotal")));
        }

        OrderEntity order = new OrderEntity();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus("待发货");
        order.setCreatedAt(LocalDateTime.now());
        orderMapper.insert(order);

        for (Map<String, Object> item : items) {
            OrderItemEntity detail = new OrderItemEntity();
            detail.setOrderId(order.getId());
            detail.setProductId(toLong(item.get("productId")));
            detail.setProductName((String) item.get("productName"));
            detail.setPrice(toBigDecimal(item.get("price")));
            detail.setQuantity(toInteger(item.get("quantity")));
            detail.setSubtotal(toBigDecimal(item.get("subtotal")));
            orderItemMapper.insert(detail);
        }

        // 扣减库存（不足则整单回滚）
        for (Map<String, Object> item : items) {
            Long pid = toLong(item.get("productId"));
            int qty = toInteger(item.get("quantity"));
            ProductEntity product = productMapper.selectById(pid);
            if (product == null || product.getStock() < qty) {
                throw new IllegalArgumentException("商品【" + item.get("productName") + "】库存不足，无法下单");
            }
            product.setStock(product.getStock() - qty);
            productMapper.updateById(product);
        }

        // 生成物流记录（已下单），订单页可跳转物流查询跟踪
        LogisticsEntity logistics = new LogisticsEntity();
        logistics.setOrderNo(orderNo);
        logistics.setUserId(userId);
        logistics.setStatus("已下单");
        logistics.setLocation("订单已生成，待出库");
        logistics.setUpdatedAt(LocalDateTime.now());
        logisticsMapper.insert(logistics);

        // 清空购物车
        cartService.clear(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("orderNo", orderNo);
        result.put("totalAmount", totalAmount);
        return result;
    }

    /** 我的订单列表（按下单时间倒序，含每单明细） */
    public List<Map<String, Object>> listByUser(Long userId) {
        List<OrderEntity> orders = orderMapper.selectList(
                new LambdaQueryWrapper<OrderEntity>()
                        .eq(OrderEntity::getUserId, userId)
                        .orderByDesc(OrderEntity::getCreatedAt));
        List<Map<String, Object>> result = new ArrayList<>();
        for (OrderEntity order : orders) {
            result.add(toOrderMap(order));
        }
        return result;
    }

    /** 订单详情（按订单号） */
    public Map<String, Object> getByOrderNo(String orderNo) {
        OrderEntity order = orderMapper.selectOne(
                new LambdaQueryWrapper<OrderEntity>().eq(OrderEntity::getOrderNo, orderNo));
        if (order == null) {
            throw new IllegalArgumentException("订单不存在：" + orderNo);
        }
        return toOrderMap(order);
    }

    /** 取消订单：仅待发货订单可取消，状态置为已取消，库存按明细回滚，物流记录同步取消 */
    @Transactional
    public Map<String, Object> cancel(String orderNo) {
        OrderEntity order = orderMapper.selectOne(
                new LambdaQueryWrapper<OrderEntity>().eq(OrderEntity::getOrderNo, orderNo));
        if (order == null) {
            throw new IllegalArgumentException("订单不存在：" + orderNo);
        }
        if (!"待发货".equals(order.getStatus())) {
            throw new IllegalArgumentException("订单当前状态为【" + order.getStatus() + "】，只有待发货订单可以取消。");
        }

        // 1. 订单状态置为已取消
        order.setStatus("已取消");
        orderMapper.updateById(order);

        // 2. 按明细把购买数量回滚到商品库存
        List<OrderItemEntity> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItemEntity>()
                        .eq(OrderItemEntity::getOrderId, order.getId()));
        for (OrderItemEntity item : items) {
            ProductEntity product = productMapper.selectById(item.getProductId());
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                productMapper.updateById(product);
            }
        }

        // 3. 物流记录同步取消
        LogisticsEntity logistics = logisticsMapper.selectOne(
                new LambdaQueryWrapper<LogisticsEntity>().eq(LogisticsEntity::getOrderNo, orderNo));
        if (logistics != null) {
            logistics.setStatus("已取消");
            logistics.setLocation("订单已取消");
            logistics.setUpdatedAt(LocalDateTime.now());
            logisticsMapper.updateById(logistics);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("orderNo", orderNo);
        result.put("status", "已取消");
        return result;
    }

    /** 组装订单返回结构（含明细列表） */
    private Map<String, Object> toOrderMap(OrderEntity order) {
        List<OrderItemEntity> details = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItemEntity>()
                        .eq(OrderItemEntity::getOrderId, order.getId()));
        Map<String, Object> map = new HashMap<>();
        map.put("id", order.getId());
        map.put("orderNo", order.getOrderNo());
        map.put("userId", order.getUserId());
        map.put("totalAmount", order.getTotalAmount());
        map.put("status", order.getStatus());
        map.put("createdAt", order.getCreatedAt());
        map.put("items", details);
        return map;
    }

    /** 兼容 Number 与 BigDecimal 的金额转换 */
    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        return BigDecimal.valueOf(((Number) value).doubleValue());
    }

    private Long toLong(Object value) {
        return value == null ? null : ((Number) value).longValue();
    }

    private Integer toInteger(Object value) {
        return value == null ? null : ((Number) value).intValue();
    }
}
