package com.example.aixiaogou.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;

/**
 * 任务2：基础 AIService 对话。
 *
 * @AiService 注解声明接口为 AIService，Spring 为接口生成动态代理：
 * 接口方法调用被转换为一次模型请求，@UserMessage 声明用户消息模板。
 * 采用 EXPLICIT 手动装配（见 AiConfig.assistant()），装配细节可控。
 */
@AiService(wiringMode = AiServiceWiringMode.EXPLICIT, chatModel = "openAiChatModel")
public interface Assistant {

    /** 基础对话：直接调用大模型 */
    @SystemMessage("你是“AI小购”电商平台的智能助手，请用中文简洁回答用户问题。")
    String chat(@UserMessage String message);
}
