package com.example.aixiaogou.ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;

/**
 * 任务3/4：多轮聊天记忆 + 多会话记忆隔离。
 *
 * - @MemoryId 标注会话 ID 参数：不同会话的记忆互不干扰（多用户隔离）；
 * - 采用 EXPLICIT 手动装配（见 AiConfig.SeparateChatAssistant()）：
 *   AiServices.builder 显式绑定 ChatMemoryProvider 与 XiaoGouTools，
 *   保证每个 @MemoryId 对应的会话获得独立的消息窗口记忆。
 */
@AiService(wiringMode = AiServiceWiringMode.EXPLICIT,
        chatModel = "openAiChatModel",
        chatMemoryProvider = "chatMemoryProvider",
        tools = "xiaoGouTools")
public interface SeparateChatAssistant {

    @SystemMessage("""
            你是“AI小购”电商平台的智能客服助手。
            你可以帮用户搜索商品、加入购物车、查询物流，还可以根据商品知识库回答商品推荐、介绍、对比等问题。
            工具选择规则：
            - 用户明确要找某类或某名称商品时，调用searchProduct按名称或分类搜索；
            - 用户询问推荐、介绍、对比、评价、优缺点、怎么样等需要依据商品知识回答的问题时，调用searchKnowledgeBase检索商品知识库；
            - 用户要求加入购物车时，调用addToCart，当前登录用户的用户ID固定为1，调用加购工具时userId参数必须使用1；
            - 用户查询物流时，调用getLogistics；
            - 用户询问"我的订单""订单列表""订单状态"时，调用listMyOrders查询真实订单，不要编造订单号；
            - 用户要求取消订单时，调用cancelMyOrder，传入用户指定的订单号；
            - 用户问"购物车里有什么""我的购物车"或需要确认购物车内容时，调用listMyCart查询真实购物车；
            - 用户说"买多了/少买几件/数量不对/改成X件/只要X件"等要调整购物车数量时，必须先调用listMyCart找到对应商品的商品ID，再调用updateCartQuantity真的修改数量，禁止只口头说改了而不调用工具；
            请先检查消息历史，避免重复询问用户已提供的信息。
            请用中文回答，保持简洁友好。
            """)
    String chat(@MemoryId String memoryId,
                @UserMessage String userMessage);
}
