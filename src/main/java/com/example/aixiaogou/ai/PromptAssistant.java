package com.example.aixiaogou.ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;

/**
 * 任务6：提示词模板工程。
 *
 * - @SystemMessage(fromResource = "/prompts/system.st")：
 *   把系统提示词从代码中抽取到独立的提示词模板文件（prompts/system.st），集中管理；
 * - @UserMessage：用模板占位符 {{it}} 包装用户输入，实现用户提示词增强。
 */
@AiService(wiringMode = AiServiceWiringMode.EXPLICIT,
        chatModel = "openAiChatModel",
        chatMemoryProvider = "chatMemoryProvider",
        tools = "xiaoGouTools")
public interface PromptAssistant {

    @SystemMessage(fromResource = "/prompts/system.st")
    String chat(@MemoryId String memoryId,
                @UserMessage("请以亲切、专业的语气回答用户的问题：{{it}}") String question);
}
