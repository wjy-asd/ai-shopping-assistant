package com.example.aixiaogou.config;

import com.example.aixiaogou.ai.MongoChatMemoryStore;
import com.example.aixiaogou.ai.ProductRetriever;
import com.example.aixiaogou.service.ProductService;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;

/**
 * AI 装配中心。
 *
 * 1. ChatMemoryStore：聊天记忆的底层存储。
 *    默认 InMemoryChatMemoryStore（不依赖外部服务）；
 *    启动参数 --ai.memory.store=mongodb 时切换为 MongoChatMemoryStore（任务5）。
 * 2. ChatMemoryProvider：根据 memoryId 为每个会话创建独立的消息窗口记忆（最多保留 20 条）。
 * 3. 四个 @AiService 接口由 langchain4j spring starter 扫描注册，
 *    注解以 EXPLICIT 模式按 bean 名显式注入模型、记忆提供者、工具与检索器。
 * 4. productRetriever：商品知识库检索器（RAG），供 RagAssistant 使用。
 */
@Configuration
public class AiConfig {

    /** 默认内存聊天记忆存储（不依赖外部服务） */
    @Bean
    @ConditionalOnProperty(name = "ai.memory.store", havingValue = "in-memory", matchIfMissing = true)
    ChatMemoryStore inMemoryChatMemoryStore() {
        return new InMemoryChatMemoryStore();
    }

    /** 任务5：MongoDB 聊天记忆存储（需启动 MongoDB） */
    @Bean
    @ConditionalOnProperty(name = "ai.memory.store", havingValue = "mongodb")
    ChatMemoryStore mongoChatMemoryStore(MongoTemplate mongoTemplate) {
        return new MongoChatMemoryStore(mongoTemplate);
    }

    /**
     * 任务3/4：ChatMemoryProvider。
     * 每个 memoryId（会话 ID）动态创建独立的 MessageWindowChatMemory：
     *  - id(memoryId)：记忆窗口按会话 ID 隔离（关键，否则所有会话共用默认 ID 串记忆）；
     *  - chatMemoryStore：窗口内容读写底层存储，实现持久化；
     *  - maxMessages(20)：滑动窗口最多保留最近 20 条消息。
     */
    @Bean
    ChatMemoryProvider chatMemoryProvider(ChatMemoryStore chatMemoryStore) {
        return (Object memoryId) -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .chatMemoryStore(chatMemoryStore)
                .maxMessages(20)
                .build();
    }

    /** RAG 商品知识检索器：回答问题前先从商品库检索相关内容 */
    @Bean
    ContentRetriever productRetriever(EmbeddingModel embeddingModel,
                                      ProductService productService) {
        return new ProductRetriever(embeddingModel, productService);
    }
}
