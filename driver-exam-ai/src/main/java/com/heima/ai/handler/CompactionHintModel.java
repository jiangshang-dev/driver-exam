package com.heima.ai.handler;

import io.agentscope.core.event.AgentEventEmitter;
import io.agentscope.core.event.ThinkingBlockDeltaEvent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.ChatResponse;
import io.agentscope.core.model.GenerateOptions;
import io.agentscope.core.model.Model;
import io.agentscope.core.model.ToolSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@RequiredArgsConstructor
public class CompactionHintModel implements Model {

    public static final String TIP_START = "\n对话上下文较长，正在压缩整理历史消息，请稍候…\n";

    private final Model delegate;
    private final String sessionId;

    @Override
    public Flux<ChatResponse> stream(List<Msg> messages, List<ToolSchema> tools, GenerateOptions options) {

        return Flux.deferContextual(contextView -> {
            AtomicBoolean started = new AtomicBoolean(false);
            long startAt = System.currentTimeMillis();

            AgentEventEmitter.fromContext(contextView).ifPresent(emitter -> {
                started.set(true);
                log.info("[Compaction] 小模型开始真实压缩, sessionId={}", sessionId);
                emitter.emit(new ThinkingBlockDeltaEvent("compaction-hint", "compaction-hint-start", TIP_START + "\n"));
            });

            return delegate.stream(messages, tools, options)
                    .doOnComplete(() -> {
                        long elapsed = System.currentTimeMillis() - startAt;
                        log.info("[Compaction] 小模型压缩完成, sessionId={}, elapsed={}ms", sessionId, elapsed);

                        AgentEventEmitter.fromContext(contextView).ifPresent(emitter ->
                                emitter.emit(new ThinkingBlockDeltaEvent("compaction-hint", "compaction-hint-done", "\n【压缩耗时：" + elapsed/1000 + "s】\n 上下文整理完成，继续分析中…\n"
                                ))
                        );
                    })
                    .doOnError(error -> {
                        log.warn("[Compaction] 小模型压缩失败, sessionId={}", sessionId, error);

                        AgentEventEmitter.fromContext(contextView).ifPresent(emitter ->
                                emitter.emit(new ThinkingBlockDeltaEvent("compaction-hint", "compaction-hint-fail", "上下文压缩失败，正在继续处理请求。\n"))
                        );
                    });
        });
    }

    @Override
    public String getModelName() {
        return delegate.getModelName();
    }

    @Override
    public boolean supportsNativeStructuredOutput() {
        return delegate.supportsNativeStructuredOutput();
    }

    @Override
    public int getContextWindowSize() {
        return delegate.getContextWindowSize();
    }
}