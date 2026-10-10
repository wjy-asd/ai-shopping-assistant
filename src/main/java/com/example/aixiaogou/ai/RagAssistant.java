package com.example.aixiaogou.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;

/**
 * RAG 增强对话：通过 contentRetriever 注入商品知识检索器，
 * 回答前先从商品知识库检索相关内容，再交给模型生成（检索增强生成）。
 */
@AiService(wiringMode = AiServiceWiringMode.EXPLICIT,
        chatModel = "openAiChatModel",
        contentRetriever = "productRetriever")
public interface RagAssistant {

    @SystemMessage("""
            你是“AI小购”电商平台的商品推荐助手。
            请依据检索到的商品资料回答用户问题，若资料中没有相关内容请直接说明。
            请用中文回答。
            """)
    String chat(@UserMessage String message);
}
