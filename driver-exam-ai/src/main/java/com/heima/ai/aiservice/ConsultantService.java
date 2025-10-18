package com.heima.ai.aiservice;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import reactor.core.publisher.Flux;

@AiService(
        wiringMode = AiServiceWiringMode.EXPLICIT,
        streamingChatModel = "openAiStreamingChatModel", // 配置流式聊天模型对象
        chatMemory = "chatMemory", // 配置会话记忆对象
        chatMemoryProvider = "chatMemoryProvider", // 配置会话记忆对象提供者对象
        contentRetriever = "contentRetriever" // 配置向量数据库检索对象
        // 移除了tools配置，避免可能的初始化问题
    )
public interface ConsultantService {
    // 系统提示词
    @SystemMessage(fromResource = "system.txt")
    Flux<String> chat(@UserMessage String message, @MemoryId String memoryId);

    // 移除了不需要的重载方法，避免混淆
}