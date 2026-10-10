package com.example.aixiaogou.controller;

import java.util.List;

import com.example.aixiaogou.common.Result;
import com.example.aixiaogou.entity.ProductEntity;
import com.example.aixiaogou.service.ProductService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品接口（前端商品列表页）。
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /** 商品列表（支持 keyword 模糊搜索） */
    @GetMapping
    public Result<List<ProductEntity>> list(
            @RequestParam(value = "keyword", required = false) String keyword) {
        List<ProductEntity> data = (keyword == null || keyword.isBlank())
                ? productService.listProducts()
                : productService.search(keyword);
        return Result.ok(data);
    }

    /** 商品详情 */
    @GetMapping("/{id}")
    public Result<ProductEntity> detail(@PathVariable("id") Long id) {
        return Result.ok(productService.getById(id));
    }
}
