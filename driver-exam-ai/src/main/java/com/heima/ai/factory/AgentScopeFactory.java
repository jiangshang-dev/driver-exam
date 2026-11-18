package com.heima.ai.factory;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.heima.ai.config.SystemPromptConfig;
import com.heima.ai.constant.RedisConstant;
import com.heima.ai.dto.AiAgentOptionsDto;
import com.heima.ai.handler.CompactionHintModel;
import com.heima.ai.handler.FullObservabilityMiddleware;
import com.heima.ai.properties.AgentScopeProperties;
import com.heima.ai.store.MyRedisAgentStateStore;
import com.heima.ai.tools.KnowledgeBaseTool;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.model.Model;
import io.agentscope.core.model.OpenAIChatModel;
import io.agentscope.core.permission.PermissionContextState;
import io.agentscope.core.permission.PermissionMode;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.extensions.redis.store.RedisStore;
import io.agentscope.harness.agent.DistributedStore;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.IsolationScope;
import io.agentscope.harness.agent.filesystem.spec.RemoteFilesystemSpec;
import io.agentscope.harness.agent.memory.MemoryConfig;
import io.agentscope.harness.agent.memory.compaction.CompactionConfig;
import io.agentscope.harness.agent.memory.compaction.ToolResultEvictionConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPooled;

import java.time.Duration;
import java.util.*;

/**
 * AgentScope 工厂类
 * 负责按 userId + sessionId 隔离创建和管理 AgentScope 的 Agent 实例
 * 解决不同用户、不同会话之间记忆隔离的问题
 */
@Slf4j
@Component
public class AgentScopeFactory {
    
    @Resource
    private JedisPooled jedisPooled;
    @Resource
    private MyRedisAgentStateStore agentStateStore;
    @Resource
    private AgentScopeProperties properties;
    @Resource
    private SystemPromptConfig systemPromptConfig;
    @Resource
    private KnowledgeBaseTool knowledgeBaseTool;

    public Toolkit buildToolkit(Set<String> intents) {
        Toolkit toolkit = new Toolkit();
        Set<String> effectiveIntents = intents == null ? Collections.emptySet() : intents;

        // 直接调tool工具
        return toolkit;
    }

    /**
     * 注册本地工具
     */
    public Toolkit registToolKit() {
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(knowledgeBaseTool);
        return toolkit;
    }

    /**
     * 创建轻量Agent，不连接MCP，无记忆/文件系统/分布式存储。
     * 适用于文档翻译、模型测试等纯LLM调用场景
     */
    public ReActAgent createLiteAgent(AiAgentOptionsDto agentOptionsDto) {
        return ReActAgent.builder()
                .name("ryd-ai-lite")
                .model(agentOptionsDto.getChatMode())
                .permissionContext(PermissionContextState.builder().mode(PermissionMode.BYPASS).build())
                .build();
    }

    public HarnessAgent createAgent(AiAgentOptionsDto agentOptionsDto) {
        Set<String> effectiveIntents = agentOptionsDto.getRouteType() == null
                ? Collections.emptySet() : agentOptionsDto.getRouteType();
        // 文件系统隔离 共享存储
        RemoteFilesystemSpec remoteFilesystemSpec = new RemoteFilesystemSpec();
        remoteFilesystemSpec.isolationScope(IsolationScope.USER);
        remoteFilesystemSpec.addSharedPrefix("configs");

        String systemText = systemPromptConfig.getMaterialQueryAgentSystemMessage().get();
        log.info("%系统提示词：{}%", systemText);
        String sysPrompt = "我是一个专业得私人助理，可以帮你查询材料数据、翻译文档、对文件内容提问题等！忽略前面所有记忆，重新开始，必须联网搜索后准确为用户回答问题！联网搜索功能已开启，开始联网搜索相关资料，必须进行联网搜索查找资料准确回答！";
        HarnessAgent.Builder builder = HarnessAgent.builder()
                .name("ryd-ai")
                .model(agentOptionsDto.getChatMode())
                .defaultSessionId(agentOptionsDto.getSessionId())
//                .sysPrompt("我是一个专业得私人助理，可以帮你查询材料数据、翻译文档、对文件内容提问题等！")
                .sysPrompt(Boolean.TRUE.equals(agentOptionsDto.getEnableWebSearch()) ? sysPrompt : systemText)
//                .sysPrompt("你是全能助手小可，你可以教全天下的人做菜、讲解历史、天下万物无所不能的知识")
                .permissionContext(PermissionContextState.builder().mode(PermissionMode.BYPASS).build())
//                .toolkit(registToolKit())
                .toolkit(Boolean.TRUE.equals(agentOptionsDto.getEnableWebSearch()) ? buildToolkit(effectiveIntents) : registToolKit())
                // CompactionHint 尽量靠外：在静默压缩前先把提示推到思考区
//                .middlewares(List.of(new FullObservabilityMiddleware()))
                .middlewares(List.of(
                        // todo 上下文给前端压缩提示
//                        new CompactionHintMiddleware(
//                                properties.getMemory().getCompactionTriggerMessages(),
//                                properties.getMemory().isCompactionEnabled()),
                        new FullObservabilityMiddleware()
                ))
                .enablePendingToolRecovery(true) // 极端压缩模式
                // 不传则用 ${user.dir}/.agentscope/workspace
                .workspace(".rydAgent")
                // 异步工具调用超时时间
                .asyncToolTimeout(Duration.ofMinutes(10))
                .maxIters(10)
//                .maxContextTokens(8000)
                .distributedStore(
                        DistributedStore.builder()
                                .agentStateStore(agentStateStore)
                                .baseStore(new RedisStore(jedisPooled, RedisConstant.REDIS_AGENT_STORE_PREFIX))
                                .build()
                )
                .filesystem(remoteFilesystemSpec)
                .toolResultEviction(
                        ToolResultEvictionConfig.builder()
                                .maxResultChars(4_000) // 超过约4k字卸载
                                .previewChars(800) // context里只留头尾各约800
                                .build()
                )
                .disableSubagents();

        // 压缩开关
        AgentScopeProperties.MemoryProperties memory = properties.getMemory();
        log.info("是否开启了上下文压缩{}", memory.isCompactionEnabled());
        if (memory.isCompactionEnabled()) {
            // todo 压缩用小模型 能不能用vllm部署的小模型，不用在线api模型
            // 在配置文件配置数值
            CompactionConfig.Builder keptMessages = CompactionConfig.builder()
                    // 按估算 token 触发/保留；triggerMessages=0 即关闭按条数
                    // https://java.agentscope.io/v2/zh/docs/harness/memory.html
                    .triggerMessages(memory.getCompactionTriggerMessages())
                    .triggerTokens(memory.getCompactionTriggerTokens())
                    .keepMessages(memory.getCompactionKeepMessages())
                    .keepTokens(memory.getCompactionKeepTokens())
//                    .reserved(10000)
                    // 参考：https://java.agentscope.io/v2/zh/docs/harness/compaction.html
                    // 在 LLM 摘要前,先做一遍不走 LLM 的字符串截断——write_file、edit_file 这类工具的入参体量大但事后没人再看:
                    .truncateArgs(CompactionConfig.TruncateArgsConfig.builder()
                            .maxArgLength(2000) // 预压缩参数截断 (可选)
                            .build())
                    ;

            // 开启通过使用小模型压缩
            log.info("开启通过使用小模型压缩: {}", memory.isCompactionModelEnabled());
            if (memory.isCompactionModelEnabled()) {
                String compactionModelName = memory.getCompactionModelName();
                String compactionModelBaseUrl = memory.getCompactionModelBaseUrl();
                String compactionModelApiKey = memory.getCompactionModelApiKey();
                log.info("小模型压缩名称:{}", compactionModelName);
                log.info("小模型压缩地址:{}", compactionModelBaseUrl);
                log.info("小模型压缩key:{}", compactionModelApiKey);

                Model rawCompactionModel = OpenAIChatModel.builder()
                        .modelName(compactionModelName)
                        .baseUrl(compactionModelBaseUrl)
                        .apiKey(compactionModelApiKey)
                        .build();

                // todo
                Model compactionModel = new CompactionHintModel(
                        rawCompactionModel,
                        agentOptionsDto.getSessionId()
                );

                log.info("使用小模型压缩 model={}", compactionModelName);
                keptMessages.model(compactionModel);
            }

            // 上下文压缩
            CompactionConfig compactionConfig = keptMessages.build();
            builder.compaction(compactionConfig);
            log.info("LLM 对话压缩已启用 triggerMessages={} triggerTokens={} keepMessages={} keepTokens={}",
                    memory.getCompactionTriggerMessages(), memory.getCompactionTriggerTokens(),
                    memory.getCompactionKeepMessages(), memory.getCompactionKeepTokens());
        } else {
            // 不压缩
            builder.disableCompaction();
        }

        // 长期记忆节流flush
        MemoryConfig.FlushTrigger flushTrigger = MemoryConfig.FlushTrigger.throttled(Duration.ofMinutes(10));
        // 长期记忆
        MemoryConfig memoryConfig = MemoryConfig.builder()
                .flushTrigger(flushTrigger)
                .consolidationMinGap(Duration.ofMinutes(50))
                // todo 调整后台维护节奏
                .consolidationMinGap(Duration.ofHours(2))   // 后台合并最少 2 小时一次
                .dailyFileRetentionDays(30)                 // 30 天就归档
                .sessionRetentionDays(60)                   // 60 天后删 session JSONL
                .consolidationMaxTokens(8_000)              // MEMORY.md 上限放宽到 8K tokens
                .build();
        builder.memory(memoryConfig);

        // todo 完全关掉 如果你想自己接管记忆 / 自己写工具
        // 关掉 flush + 后台维护（并去掉「对话结束自动抽取」引导）
        builder.disableMemoryHooks();
        // 不注册 memory_search / memory_get / memory_save / session_search
        builder.disableMemoryTools();
        return builder.build();
    }

}

