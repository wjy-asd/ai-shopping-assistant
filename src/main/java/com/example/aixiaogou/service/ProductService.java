package com.example.aixiaogou.service;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aixiaogou.entity.ProductEntity;
import com.example.aixiaogou.mapper.ProductMapper;

import org.springframework.stereotype.Service;

/**
 * 商品服务：商品列表与关键字搜索（供 REST 接口与大模型工具共用）。
 */
@Service
public class ProductService {

    private final ProductMapper productMapper;

    public ProductService(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    /** 全部商品 */
    public List<ProductEntity> listProducts() {
        return productMapper.selectList(null);
    }

    /** 按 ID 查询 */
    public ProductEntity getById(Long id) {
        return productMapper.selectById(id);
    }

    /** 按名称/分类关键字模糊搜索 */
    public List<ProductEntity> search(String keyword) {
        return productMapper.selectList(
                new LambdaQueryWrapper<ProductEntity>()
                        .like(ProductEntity::getName, keyword)
                        .or()
                        .like(ProductEntity::getCategory, keyword)
        );
    }
}
