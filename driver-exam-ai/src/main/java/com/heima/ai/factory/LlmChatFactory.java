package com.heima.ai.factory;

import cn.hutool.core.lang.Assert;
import com.heima.ai.dto.AiAgentOptionsDto;
import com.heima.ai.dto.LlmChatCallDto;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.model.OpenAIChatModel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 大模型聊天工具类
 */
@Slf4j
@Component
public class LlmChatFactory {
    @Resource
    private AgentScopeFactory agentScopeFactory;
    @Resource
    private AgentModelFactory modelFactory;

    /**
     * 聊天（阻塞），每次调用都会查询数据库获取模型配置
     */
    public String call(LlmChatCallDto dto) {
        OpenAIChatModel chatModel = modelFactory.createChatModel(dto.getModelId(), Boolean.FALSE);
        return call(dto, chatModel);
    }

    /**
     * 聊天（阻塞），复用已加载的模型，避免并行翻译时重复查库占连接
     */
    public String call(LlmChatCallDto dto, OpenAIChatModel chatModel) {
        Assert.notNull(dto.getMessage(), dto.getUserId(), dto.getSessionId(), "大语言模型参数为空");
        Assert.notNull(chatModel, "大语言模型为空");
        String block = null;
        try {
            AiAgentOptionsDto agentOptionsDto = AiAgentOptionsDto.builder()
                    .userId(dto.getUserId())
                    .sessionId(dto.getSessionId())
                    .chatMode(chatModel)
                    .build();
            ReActAgent agent = agentScopeFactory.createLiteAgent(agentOptionsDto);
            long startTime = System.currentTimeMillis();
            log.info("[LLM] 开始阻塞调用 sessionId={} userId={}", dto.getSessionId(), dto.getUserId());
            block = Objects.requireNonNull(agent.call(dto.getMessage()).block()).getTextContent();
            log.info("[LLM] 阻塞调用结束 sessionId={} 耗时={}ms", dto.getSessionId(), System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error("大模型调用异常", e);
        }
        return block;
    }

}
