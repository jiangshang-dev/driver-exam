package com.heima.ai.aiservice;

import cn.hutool.core.util.StrUtil;
import com.heima.ai.dto.AiAgentOptionsDto;
import com.heima.ai.enums.MessageTypeEnum;
import com.heima.ai.factory.AgentModelFactory;
import com.heima.ai.factory.AgentScopeFactory;
import io.agentscope.core.event.AgentEventType;
import io.agentscope.core.event.AgentResultEvent;
import io.agentscope.core.event.TextBlockDeltaEvent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.model.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * App 端 AI 顾问服务：按 memoryId 维持会话记忆，流式返回纯文本
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultantService {

    private final AgentModelFactory modelFactory;
    private final AgentScopeFactory agentScopeFactory;

    public Flux<String> chat(String message, String memoryId) {
        if (StrUtil.isBlank(message)) {
            return Flux.just("请输入内容");
        }
        String sessionId = StrUtil.isBlank(memoryId) ? String.valueOf(System.currentTimeMillis()) : memoryId;

        OpenAIChatModel chatModel = modelFactory.createChatModel(null, false);
        AiAgentOptionsDto options = AiAgentOptionsDto.builder()
                .userId("app")
                .sessionId(sessionId)
                .chatMode(chatModel)
                .enableWebSearch(false)
                .routeType(Set.of("drive"))
                .build();

        HarnessAgent agent = agentScopeFactory.createAgent(options);
        Msg userMessage = Msg.builderForRole(MsgRole.USER)
                .name("user")
                .textContent(message)
                .build();

        AtomicBoolean emitted = new AtomicBoolean(false);
        return agent.streamEvents(userMessage)
                .<String>handle((event, sink) -> {
                    if (event.getType() == AgentEventType.TEXT_BLOCK_DELTA && event instanceof TextBlockDeltaEvent delta) {
                        String text = delta.getDelta();
                        if (StrUtil.isNotEmpty(text)) {
                            emitted.set(true);
                            sink.next(text);
                        }
                    } else if (event.getType() == AgentEventType.AGENT_RESULT && event instanceof AgentResultEvent result) {
                        // 部分模型不支持增量输出，兜底返回完整结果
                        if (!emitted.get()) {
                            String text = result.getResult() == null ? null : result.getResult().getTextContent();
                            if (StrUtil.isNotBlank(text)) {
                                emitted.set(true);
                                sink.next(text);
                            }
                        }
                    }
                })
                .doOnError(e -> log.error("AI 顾问对话失败, sessionId={}", sessionId, e))
                .onErrorResume(e -> Flux.just("抱歉，AI服务暂时不可用，请稍后再试"))
                .switchIfEmpty(Flux.just("抱歉，我暂时没有想好怎么回答，请换个问法试试"))
                .doFinally(signal -> log.info("AI 顾问对话结束, sessionId={}, type={}, messageType={}",
                        sessionId, signal, MessageTypeEnum.TEXT.getCode()));
    }
}
