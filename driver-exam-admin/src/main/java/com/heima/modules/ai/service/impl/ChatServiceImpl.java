package com.heima.modules.ai.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.heima.ai.constant.RedisConstant;
import com.heima.ai.dto.AiAgentOptionsDto;
import com.heima.ai.enums.MessageTypeEnum;
import com.heima.ai.factory.AgentModelFactory;
import com.heima.ai.factory.AgentScopeFactory;
import com.heima.ai.params.ChatParam;
import com.heima.ai.response.SseResponse;
import com.heima.ai.store.AgentStateDbSyncContext;
import com.heima.ai.store.MyRedisAgentStateStore;
import com.heima.ai.vo.MessageDataVO;
import com.heima.common.utils.CacheService;
import com.heima.common.utils.IdGeneratorUtil;
import com.heima.modules.ai.service.ChatService;
import com.heima.modules.security.user.SecurityUser;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.AgentEventType;
import io.agentscope.core.event.AgentResultEvent;
import io.agentscope.core.event.TextBlockDeltaEvent;
import io.agentscope.core.event.ThinkingBlockDeltaEvent;
import io.agentscope.core.event.ToolCallStartEvent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.model.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@AllArgsConstructor
public class ChatServiceImpl implements ChatService {
    private static final ConcurrentHashMap<String, AtomicLong> SESSION_TURN_EPOCH = new ConcurrentHashMap<>();
    private final CacheService cacheService;
    private final MyRedisAgentStateStore agentStateStore;
    private final AgentScopeFactory agentScopeFactory;
    private final AgentModelFactory modelFactory;

    @Override
    public void stop(String sessionId) {
        Assert.notNull("会话id不能为空！", sessionId);
        try {
            Long userId = SecurityUser.getUserId();
            log.info("用户 [{}] 发送终止会话信号，会话ID: {}", userId, sessionId);
            String redisStopKey = RedisConstant.getChatStopKey(String.valueOf(userId), sessionId);
            cacheService.setEx(redisStopKey, "stopped", 1, TimeUnit.MINUTES);
            // 阻止 AgentState 后续完整回复落库，避免多次停止/重发产生脏消息与 sort 乱序
            AgentStateDbSyncContext.markAborted(sessionId);
            SESSION_TURN_EPOCH.computeIfAbsent(sessionId, k -> new AtomicLong(0)).incrementAndGet();
            // 立即清理ASKING和未完成工具轮次，避免停止后下一轮无法继续或无正文
            agentStateStore.clearIncompleteAgentTurn(String.valueOf(userId), sessionId);
        } catch (Exception e) {
            log.error("终止会话失败，ID: {}", sessionId, e);
        }
    }

    @Override
    public Flux<SseResponse<MessageDataVO<MessageTypeEnum>>> sendWithSseFlux(ChatParam chatSendParams) {
        Long userId = SecurityUser.getUserId();
        if (StrUtil.isBlank(chatSendParams.getSessionId())) {
            return Flux.just(SseResponse.errorFlux("会话ID不能为空"));
        }
        String sessionId = chatSendParams.getSessionId();
        String userMsgId = IdGeneratorUtil.getSnowflakeNextIdStr();
        String assistantMsgId = IdGeneratorUtil.getSnowflakeNextIdStr();
        String message = chatSendParams.getMessage();

        Set<String> intents = new HashSet<>();
        OpenAIChatModel chatModel = modelFactory.createChatModel(chatSendParams.getModelId(),
                Boolean.TRUE.equals(chatSendParams.getEnableThinking()));
        AiAgentOptionsDto agentOptionsDto = AiAgentOptionsDto.builder()
                .userId(String.valueOf(userId))
                .sessionId(sessionId)
                .routeType(intents)
                .enableWebSearch(Boolean.TRUE.equals(chatSendParams.getEnableWebSearch()))
                .chatMode(chatModel)
                .build();
        HarnessAgent agent = agentScopeFactory.createAgent(agentOptionsDto);

        // 记录本轮元数据，供 AgentState 落库
        AgentStateDbSyncContext.register(AgentStateDbSyncContext.TurnSyncMeta.builder()
                .sessionId(sessionId)
                .userId(userId == null ? null : userId.intValue())
                .userMsgId(userMsgId)
                .assistantMsgId(assistantMsgId)
                .userContent(message)
                .build());

        Msg userMessage = Msg.builderForRole(MsgRole.USER)
                .name("user")
                .textContent(message)
                .build();

        StringBuilder answerBuffer = new StringBuilder();
        List<String> thinkingBuffer = new ArrayList<>();

        return agent.streamEvents(userMessage)
                .filter(this::isSseEvent)
                .concatMap(event -> Flux.just(toSseResponse(sessionId, event, answerBuffer, thinkingBuffer)))
                .concatWith(Flux.defer(() -> Flux.just(finishSse(sessionId, userMsgId, assistantMsgId,
                        answerBuffer.toString(), thinkingBuffer))))
                .onErrorResume(e -> {
                    log.error("AI 对话失败, sessionId={}", sessionId, e);
                    AgentStateDbSyncContext.markAborted(sessionId);
                    return Flux.just(SseResponse.<MessageDataVO<MessageTypeEnum>>errorFlux(
                            StrUtil.blankToDefault(e.getMessage(), "AI服务异常")));
                })
                .doFinally(signal -> AgentStateDbSyncContext.clearIfMatch(sessionId, assistantMsgId));
    }

    private boolean isSseEvent(AgentEvent event) {
        AgentEventType type = event.getType();
        return type == AgentEventType.THINKING_BLOCK_DELTA
                || type == AgentEventType.TEXT_BLOCK_DELTA
                || type == AgentEventType.TOOL_CALL_START
                || type == AgentEventType.AGENT_RESULT;
    }

    private SseResponse<MessageDataVO<MessageTypeEnum>> toSseResponse(String sessionId, AgentEvent event,
                                                                     StringBuilder answerBuffer,
                                                                     List<String> thinkingBuffer) {
        if (event.getType() == AgentEventType.THINKING_BLOCK_DELTA && event instanceof ThinkingBlockDeltaEvent delta) {
            String text = StrUtil.blankToDefault(delta.getDelta(), "");
            if (StrUtil.isNotEmpty(text)) {
                thinkingBuffer.add(text);
            }
            return SseResponse.chunkThinking(sessionId, buildMessageData(text, "thinking_delta", null, null));
        }
        if (event.getType() == AgentEventType.TEXT_BLOCK_DELTA && event instanceof TextBlockDeltaEvent delta) {
            String text = StrUtil.blankToDefault(delta.getDelta(), "");
            answerBuffer.append(text);
            return SseResponse.chunk(sessionId, buildMessageData(text, "answer_delta", null, null));
        }
        if (event.getType() == AgentEventType.TOOL_CALL_START && event instanceof ToolCallStartEvent start) {
            return SseResponse.chunkTool(sessionId, buildMessageData(
                    "正在调用工具：" + start.getToolCallName(), "tool_call_start",
                    start.getToolCallId(), start.getToolCallName()));
        }
        if (event.getType() == AgentEventType.AGENT_RESULT && event instanceof AgentResultEvent result) {
            String text = result.getResult() == null ? "" : StrUtil.trimToEmpty(result.getResult().getTextContent());
            if (answerBuffer.length() == 0 && StrUtil.isNotBlank(text)) {
                answerBuffer.append(text);
                return SseResponse.chunk(sessionId, buildMessageData(text, "answer_delta", null, null));
            }
            return SseResponse.chunk(sessionId, buildMessageData("", "answer_end", null, null));
        }
        return SseResponse.heartbeat(buildMessageData("", event.getType().name().toLowerCase(), null, null));
    }

    private SseResponse<MessageDataVO<MessageTypeEnum>> finishSse(String sessionId, String userMsgId,
                                                                  String assistantMsgId, String answer,
                                                                  List<String> thinking) {
        try {
            if (!AgentStateDbSyncContext.isAborted(sessionId)) {
                agentStateStore.completeAssistantMeta(sessionId, assistantMsgId, null, answer, thinking);
            }
        } catch (Exception e) {
            log.warn("AI 回复落库失败, sessionId={}", sessionId, e);
        }
        MessageDataVO<MessageTypeEnum> data = MessageDataVO.<MessageTypeEnum>builder()
                .message(answer)
                .timestamp(System.currentTimeMillis())
                .type(MessageTypeEnum.TEXT)
                .userMsgId(userMsgId)
                .assistantMsgId(assistantMsgId)
                .build();
        SseResponse<MessageDataVO<MessageTypeEnum>> response = SseResponse.endFlux(userMsgId, assistantMsgId);
        response.setConversationId(sessionId);
        response.setData(data);
        return response;
    }

    private MessageDataVO<MessageTypeEnum> buildMessageData(String message, String streamEvent,
                                                            String toolCallId, String toolCallName) {
        return MessageDataVO.<MessageTypeEnum>builder()
                .message(message)
                .timestamp(System.currentTimeMillis())
                .type(MessageTypeEnum.TEXT)
                .streamEvent(streamEvent)
                .toolCallId(toolCallId)
                .toolCallName(toolCallName)
                .build();
    }
}
