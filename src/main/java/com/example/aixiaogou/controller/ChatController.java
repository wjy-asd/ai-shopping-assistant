package com.example.aixiaogou.controller;

import com.example.aixiaogou.ai.Assistant;
import com.example.aixiaogou.ai.SeparateChatAssistant;
import com.example.aixiaogou.ai.PromptAssistant;
import com.example.aixiaogou.ai.RagAssistant;
import com.example.aixiaogou.common.Result;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 对话接口（前端 Chat 页 / 任务书各任务验收入口）。
 */
@RestController
@RequestMapping("/xiaogou")
public class ChatController {

    private final Assistant assistant;
    private final SeparateChatAssistant SeparateChatAssistant;
    private final PromptAssistant promptAssistant;
    private final RagAssistant ragAssistant;

    public ChatController(Assistant assistant,
                          SeparateChatAssistant SeparateChatAssistant,
                          PromptAssistant promptAssistant,
                          RagAssistant ragAssistant) {
        this.assistant = assistant;
        this.SeparateChatAssistant = SeparateChatAssistant;
        this.promptAssistant = promptAssistant;
        this.ragAssistant = ragAssistant;
    }

    public record ChatRequest(String memoryId, String message) {
    }

    /** 任务2：基础 AIService 对话（无记忆） */
    @PostMapping("/basic/chat")
    public Result<String> basicChat(@RequestBody ChatRequest req) {
        return Result.ok(assistant.chat(req.message()));
    }

    /** 任务3/4：带多轮记忆与会话隔离的客服对话（前端主接口） */
    @PostMapping("/chat")
    public Result<String> chat(@RequestBody ChatRequest req) {
        String memoryId = req.memoryId() == null || req.memoryId().isBlank()
                ? "default" : req.memoryId();
        return Result.ok(SeparateChatAssistant.chat(memoryId, req.message()));
    }

    /** 任务6：提示词模板工程对话 */
    @PostMapping("/prompt/chat")
    public Result<String> promptChat(@RequestBody ChatRequest req) {
        String memoryId = req.memoryId() == null || req.memoryId().isBlank()
                ? "prompt" : req.memoryId();
        return Result.ok(promptAssistant.chat(memoryId, req.message()));
    }

    /** RAG 增强对话（基于商品知识库检索） */
    @PostMapping("/rag")
    public Result<String> rag(@RequestBody ChatRequest req) {
        return Result.ok(ragAssistant.chat(req.message()));
    }
}
