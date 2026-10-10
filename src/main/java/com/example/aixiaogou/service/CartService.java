package com.example.aixiaogou.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aixiaogou.entity.CartEntity;
import com.example.aixiaogou.entity.ProductEntity;
import com.example.aixiaogou.mapper.CartMapper;

import org.springframework.stereotype.Service;

/**
 * 购物车服务。
 */
@Service
public class CartService {

    private final CartMapper cartMapper;
    private final ProductService productService;

    public CartService(CartMapper cartMapper, ProductService productService) {
        this.cartMapper = cartMapper;
        this.productService = productService;
    }

    /** 用户购物车列表（关联商品信息，含汇总） */
    public Map<String, Object> listByUser(Long userId) {
        List<CartEntity> carts = cartMapper.selectList(
                new LambdaQueryWrapper<CartEntity>()
                        .eq(CartEntity::getUserId, userId)
                        .orderByDesc(CartEntity::getCreatedAt));

        List<Map<String, Object>> items = new ArrayList<>();
        int totalQuantity = 0;
        double totalAmount = 0;

        for (CartEntity cart : carts) {
            ProductEntity product = productService.getById(cart.getProductId());
            if (product == null) continue;

            double subtotal = product.getPrice().doubleValue() * cart.getQuantity();
            Map<String, Object> item = new HashMap<>();
            item.put("cartId", cart.getId());
            item.put("productId", product.getId());
            item.put("productName", product.getName());
            item.put("category", product.getCategory());
            item.put("price", product.getPrice());
            item.put("quantity", cart.getQuantity());
            item.put("stock", product.getStock());
            item.put("subtotal", subtotal);
            items.add(item);

            totalQuantity += cart.getQuantity();
            totalAmount += subtotal;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("items", items);
        result.put("totalQuantity", totalQuantity);
        result.put("totalAmount", totalAmount);
        return result;
    }

    /** 加入购物车：已有同商品则累加数量 */
    public CartEntity add(Long userId, Long productId, Integer quantity) {
        CartEntity existing = cartMapper.selectOne(
                new LambdaQueryWrapper<CartEntity>()
                        .eq(CartEntity::getUserId, userId)
                        .eq(CartEntity::getProductId, productId));
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
            cartMapper.updateById(existing);
            return existing;
        }
        CartEntity cart = new CartEntity();
        cart.setUserId(userId);
        cart.setProductId(productId);
        cart.setQuantity(quantity);
        cart.setCreatedAt(LocalDateTime.now());
        cartMapper.insert(cart);
        return cart;
    }

    /** 修改数量 */
    public void updateQuantity(Long id, Integer quantity) {
        CartEntity cart = cartMapper.selectById(id);
        if (cart == null) {
            throw new IllegalArgumentException("购物车记录不存在：" + id);
        }
        cart.setQuantity(quantity);
        cartMapper.updateById(cart);
    }

    /** 删除单条 */
    public void remove(Long id) {
        cartMapper.deleteById(id);
    }

    /** 清空用户购物车 */
    public void clear(Long userId) {
        cartMapper.delete(
                new LambdaQueryWrapper<CartEntity>().eq(CartEntity::getUserId, userId));
    }

    /** 结算：清空购物车（演示，不做订单生成） */
    public int checkout(Long userId) {
        List<CartEntity> items = cartMapper.selectList(
                new LambdaQueryWrapper<CartEntity>().eq(CartEntity::getUserId, userId));
        clear(userId);
        return items.size();
    }
}
