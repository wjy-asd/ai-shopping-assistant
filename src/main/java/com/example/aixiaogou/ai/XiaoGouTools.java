package com.example.aixiaogou.ai;

import java.util.List;
import java.util.Map;

import com.example.aixiaogou.entity.LogisticsEntity;
import com.example.aixiaogou.entity.OrderItemEntity;
import com.example.aixiaogou.entity.ProductEntity;
import com.example.aixiaogou.service.CartService;
import com.example.aixiaogou.service.LogisticsService;
import com.example.aixiaogou.service.OrderService;
import com.example.aixiaogou.service.ProductService;

import org.springframework.stereotype.Component;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;

/**
 * AI小购工具集：把商品/购物车/物流查询/知识检索注册为工具，供大模型按意图调用。
 */
@Component
public class XiaoGouTools {

    private final ProductService productService;
    private final CartService cartService;
    private final LogisticsService logisticsService;
    private final ContentRetriever productRetriever;
    private final OrderService orderService;

    public XiaoGouTools(ProductService productService, CartService cartService,
                        LogisticsService logisticsService, ContentRetriever productRetriever,
                        OrderService orderService) {
        this.productService = productService;
        this.cartService = cartService;
        this.logisticsService = logisticsService;
        this.productRetriever = productRetriever;
        this.orderService = orderService;
    }

    /** 搜索商品：按名称或分类关键字 */
    @Tool("搜索商品，按商品名称或分类关键字返回符合条件的商品列表")
    public String searchProduct(@P("商品名称或分类关键字") String keyword) {
        List<ProductEntity> products = productService.search(keyword);
        if (products.isEmpty()) {
            return "没有找到相关商品。";
        }
        StringBuilder sb = new StringBuilder("为您找到以下商品：\n");
        for (ProductEntity p : products) {
            sb.append(String.format("【商品ID：%d】【%s】%s，价格 %.2f 元，库存 %d 件。%s%n",
                    p.getId(), p.getCategory(), p.getName(), p.getPrice(), p.getStock(), p.getDescription()));
        }
        return sb.toString();
    }

    /** 加入购物车 */
    @Tool("把指定商品加入用户购物车")
    public String addToCart(@P("用户ID") Long userId,
                            @P("商品ID") Long productId,
                            @P("购买数量，默认1") Integer quantity) {
        cartService.add(userId, productId, quantity == null ? 1 : quantity);
        return "已将商品ID " + productId + " 加入购物车成功。";
    }

    /** 查询物流 */
    @Tool("按订单号查询物流信息")
    public String getLogistics(@P("订单号") String orderNo) {
        LogisticsEntity logistics = logisticsService.getByOrderNo(orderNo);
        if (logistics == null) {
            return "未找到订单号 " + orderNo + " 的物流信息。";
        }
        return String.format("订单 %s 当前物流状态：%s，所在地：%s。",
                orderNo, logistics.getStatus(), logistics.getLocation());
    }

    /**
     * 商品知识检索（RAG）：把用户问题向量化后从商品知识库检索最相关的商品资料。
     * 与 searchProduct 的分工：searchProduct 按名称/分类精确搜商品列表（用户明确要买什么）；
     * 本工具用于推荐、介绍、对比、评价、优缺点、怎么样等需要依据商品知识回答的问题。
     */
    @Tool("商品知识检索，当用户询问商品推荐、介绍、对比、评价、优缺点、怎么样等需要依据商品知识库回答的问题时调用，返回最相关的商品资料")
    public String searchKnowledgeBase(@P("用户关于商品的问题") String query) {
        List<Content> contents = productRetriever.retrieve(Query.from(query));
        if (contents.isEmpty()) {
            return "知识库中没有找到相关资料。";
        }
        StringBuilder sb = new StringBuilder("为您检索到以下商品资料：\n");
        for (Content c : contents) {
            sb.append("- ").append(c.textSegment().text()).append("\n");
        }
        return sb.toString();
    }

    /** 查询当前登录用户的我的订单列表 */
    @Tool("查询当前登录用户的我的订单列表，包括订单号、订单状态、总金额和购买的商品明细")
    public String listMyOrders() {
        List<Map<String, Object>> orders = orderService.listByUser(1L);
        if (orders.isEmpty()) {
            return "您当前还没有任何订单，可以先去购物车选购商品并下单哦。";
        }
        StringBuilder sb = new StringBuilder("为您查询到以下订单：\n");
        for (Map<String, Object> order : orders) {
            Object total = order.get("totalAmount");
            double totalAmount = (total instanceof Number) ? ((Number) total).doubleValue() : 0d;
            sb.append(String.format("订单号：%s，状态：%s，总金额：%.2f 元，下单时间：%s。%n",
                    order.get("orderNo"), order.get("status"), totalAmount, order.get("createdAt")));
            @SuppressWarnings("unchecked")
            List<OrderItemEntity> items = (List<OrderItemEntity>) order.get("items");
            if (items != null) {
                for (OrderItemEntity item : items) {
                    sb.append(String.format("  - 商品：%s，数量：%d%n",
                            item.getProductName(), item.getQuantity()));
                }
            }
        }
        return sb.toString();
    }

    /** 取消当前用户的指定订单（仅待发货可取消，库存回滚） */
    @Tool("取消当前登录用户的指定订单，传入订单号。只有待发货状态的订单可以取消，取消后库存退回")
    public String cancelMyOrder(@P("要取消的订单号") String orderNo) {
        try {
            orderService.cancel(orderNo);
            return String.format("订单 %s 已成功取消，对应商品库存已退回。", orderNo);
        } catch (IllegalArgumentException e) {
            return "取消失败：" + e.getMessage();
        }
    }

    /** 查询当前登录用户的购物车内容（含商品ID、名称、数量） */
    @Tool("查询当前登录用户的购物车内容，返回每件商品的商品ID、名称、单价、数量和小计")
    @SuppressWarnings("unchecked")
    public String listMyCart() {
        Map<String, Object> cart = cartService.listByUser(1L);
        List<Map<String, Object>> items = (List<Map<String, Object>>) cart.get("items");
        if (items == null || items.isEmpty()) {
            return "您当前的购物车是空的，可以先去挑选商品哦。";
        }
        StringBuilder sb = new StringBuilder("您的购物车中有以下商品：\n");
        double total = 0d;
        for (Map<String, Object> item : items) {
            Number subtotal = (Number) item.get("subtotal");
            double sub = subtotal == null ? 0d : subtotal.doubleValue();
            total += sub;
            sb.append(String.format("【商品ID：%d】%s，单价 %.2f 元，数量 %d 件，小计 %.2f 元。%n",
                    ((Number) item.get("productId")).longValue(),
                    item.get("productName"),
                    ((Number) item.get("price")).doubleValue(),
                    ((Number) item.get("quantity")).intValue(),
                    sub));
        }
        sb.append(String.format("购物车合计：%.2f 元。", total));
        return sb.toString();
    }

    /** 修改当前用户购物车中指定商品的数量 */
    @Tool("修改当前登录用户购物车中指定商品的购买数量，传入商品ID和新的数量。用于用户说'买多了/少买几件/数量不对/改成X件'等调整购物车数量的场景")
    @SuppressWarnings("unchecked")
    public String updateCartQuantity(@P("要调整的商品ID") Long productId,
                                     @P("新的购买数量，必须大于0") Integer newQuantity) {
        if (newQuantity == null || newQuantity <= 0) {
            return "数量必须大于0。";
        }
        Map<String, Object> cart = cartService.listByUser(1L);
        List<Map<String, Object>> items = (List<Map<String, Object>>) cart.get("items");
        if (items != null) {
            for (Map<String, Object> item : items) {
                long pid = ((Number) item.get("productId")).longValue();
                if (pid == productId.longValue()) {
                    long cartId = ((Number) item.get("cartId")).longValue();
                    cartService.updateQuantity(cartId, newQuantity);
                    return String.format("已将购物车中「%s」的数量调整为 %d 件，小计 %.2f 元。",
                            item.get("productName"), newQuantity,
                            ((Number) item.get("price")).doubleValue() * newQuantity);
                }
            }
        }
        return "购物车中未找到商品ID " + productId + "，请先确认商品是否在购物车中。";
    }
}
