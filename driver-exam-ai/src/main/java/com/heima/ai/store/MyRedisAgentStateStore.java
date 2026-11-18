package com.heima.ai.store;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.heima.ai.constant.RedisConstant;
import com.heima.ai.dto.AssistantMessageSaveDto;
import com.heima.ai.dto.UserMessageDto;
import com.heima.ai.utils.AgentThinkingExtractor;
import com.heima.ai.utils.ThinkingProcessCodec;
import com.heima.ai.vo.ThinkingStepVO;
import com.heima.common.exception.RenException;
import io.agentscope.core.message.*;
import io.agentscope.core.state.AgentState;
import io.agentscope.core.state.AgentStateStore;
import io.agentscope.core.state.ListHashUtil;
import io.agentscope.core.state.State;
import io.agentscope.core.util.JsonUtils;
import io.agentscope.extensions.redis.state.RedisClientAdapter;
import io.agentscope.extensions.redis.state.jedis.JedisClientAdapter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import redis.clients.jedis.JedisPooled;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class MyRedisAgentStateStore implements AgentStateStore {
    private static final String KEYS_SUFFIX = ":_keys";
    private static final String LIST_SUFFIX = ":list";
    private static final String ANON_SUFFIX = ":__anon__";
    public static final String AGENT_STATE_KEY = "agent_state";
    private static final String CONTEXT_KEY = "context";
    private static final String INTERRUPT_RECOVERY_SNIPPET = "I noticed that you have interrupted me";

    /** SSE 轮次异步 save 的 in-flight Future，读 state 前需等待，避免读到旧快照 */
    private final ConcurrentHashMap<String, CompletableFuture<Void>> pendingSseSaves = new ConcurrentHashMap<>();

    private RedisClientAdapter client;
    private String keyPrefix;
    private TransactionTemplate transactionTemplate;

    @Resource
    private JedisPooled redis;
    @Resource
    private AgentChatDbStore agentChatDbStore;
    @Resource
    private PlatformTransactionManager transactionManager;

    @PostConstruct
    public void init() {
        client = JedisClientAdapter.of(redis);
        keyPrefix = RedisConstant.REDIS_KEY_PREFIX;
        transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public void save(String userId, String sessionId, String key, State value) {
        String slotId = slotId(userId, sessionId);
        String redisKey = this.getStateKey(slotId, key);
        String keysKey = this.getKeysKey(slotId);

        // SSE 聊天轮次：ReAct 在最后一条正文后会同步 saveState，大 JSON 序列化会卡住
        // AgentResult/endFlux。此处立即返回，序列化+写 Redis 异步做；DB 仍由 completeAssistantMeta 负责。
        if (AGENT_STATE_KEY.equals(key) && value instanceof AgentState && AgentStateDbSyncContext.get(sessionId) != null) {
            State captured = value;
            CompletableFuture<Void> task = CompletableFuture.runAsync(() -> {
                long t0 = System.currentTimeMillis();
                try {
                    String json = JsonUtils.getJsonCodec().toJson(captured);
                    this.client.set(redisKey, json);
                    this.client.addToSet(keysKey, key);
                    log.info("SSE 异步保存 agent_state 完成, sessionId={}, cost={}ms, jsonBytes≈{}",
                            sessionId, System.currentTimeMillis() - t0, json.length());
                } catch (Exception e) {
                    log.error("SSE 异步保存 agent_state 失败, sessionId={}", sessionId, e);
                }
            });
            pendingSseSaves.compute(sessionId, (k, prev) -> prev == null ? task : prev.thenCompose(v -> task));
            return;
        }

        String json = JsonUtils.getJsonCodec().toJson(value);

        AgentContextSyncDto syncDto = null;
        if (AGENT_STATE_KEY.equals(key) && value instanceof AgentState agentState) {
            AgentState oldState = get(userId, sessionId, AGENT_STATE_KEY, AgentState.class).orElse(null);
            syncDto = this.buildContextSyncDto(sessionId, getContext(oldState), getContext(agentState));
        }

        AgentContextSyncDto finalSyncDto = syncDto;
        persistDbThenRedis(finalSyncDto, () -> {
            this.client.set(redisKey, json);
            this.client.addToSet(keysKey, key);
        });
    }

    /**
     * 保存数据库和redis
     */
    public void save(String userId, String sessionId, String key, List<? extends State> values) {
        String slotId = slotId(userId, sessionId);
        String listKey = this.getListKey(slotId, key);
        String hashKey = listKey + ":_hash";
        String keysKey = this.getKeysKey(slotId);

        String currentHash = ListHashUtil.computeHash(values);
        String storedHash = this.client.get(hashKey);
        long existingCount = this.client.getListLength(listKey);
        boolean needsFullRewrite = ListHashUtil.needsFullRewrite(values, storedHash, (int) existingCount);

        AgentContextSyncDto syncDto = null;
        if (needsFullRewrite || (long) values.size() > existingCount) {
            List<Msg> oldContext = loadExistingList(userId, sessionId, key);
            syncDto = buildContextSyncDtoForList(sessionId, key, oldContext, values);
        }

        AgentContextSyncDto finalSyncDto = syncDto;
        persistDbThenRedis(finalSyncDto, () -> {
            if (needsFullRewrite) {
                this.client.deleteKeys(new String[]{listKey});
                for (State item : values) {
                    String json = JsonUtils.getJsonCodec().toJson(item);
                    this.client.rightPushList(listKey, json);
                }
            } else if ((long) values.size() > existingCount) {
                for (State item : values.subList((int) existingCount, values.size())) {
                    String json = JsonUtils.getJsonCodec().toJson(item);
                    this.client.rightPushList(listKey, json);
                }
            }
            this.client.set(hashKey, currentHash);
            this.client.addToSet(keysKey, key + ":list"); // set 里记 :list，delete 时好认
        });
    }

    public void completeAssistantMeta(String sessionId, String assistantMsgId, String toolLink, String content, List<String> thinking) {
        if (StrUtil.isBlank(sessionId) || StrUtil.isBlank(assistantMsgId)) {
            return;
        }
        if (StrUtil.isNotBlank(toolLink)) {
            // 仅本轮 meta 仍有效时更新，避免写到下一轮
            if (AgentStateDbSyncContext.isCurrentTurn(sessionId, assistantMsgId)) {
                AgentStateDbSyncContext.updateToolLink(sessionId, toolLink);
            }
        }
        boolean hasThinking = thinking != null && !thinking.isEmpty();
        // 判断是否有 toolLink / content / thinking
        if (StrUtil.isBlank(toolLink) && StrUtil.isBlank(content) && !hasThinking) {
            return;
        }

        AgentStateDbSyncContext.TurnSyncMeta meta = AgentStateDbSyncContext.get(sessionId);
        UserMessageDto userMessage = null;
        // 关键当 meta 仍属于本轮 assistant 时才带上 user，防止被下一轮 register 覆盖后串写/重复插用户消息
        if (meta != null && assistantMsgId.equals(meta.getAssistantMsgId()) && StrUtil.isNotBlank(meta.getUserMsgId())
                && !agentChatDbStore.existsById(meta.getUserMsgId())) {
            userMessage = UserMessageDto.builder().sessionId(sessionId).userId(meta.getUserId()).msgId(meta.getUserMsgId())
                    .content(meta.getUserContent())
                    .files(meta.getFiles())
                    .targetLanguageType(meta.getTargetLanguageType())
                    .sourceLanguageType(meta.getSourceLanguageType())
                    .build();
        }

        long startTime = System.currentTimeMillis();
        agentChatDbStore.syncAgentContext(AgentContextSyncDto.builder().sessionId(sessionId)
                .persistUser(userMessage != null)
                .userMessage(userMessage)
                .persistAssistant(true)
                .assistantMessage(AssistantMessageSaveDto.builder()
                        .sessionId(sessionId)
                        .msgId(assistantMsgId)
                        .content(StrUtil.blankToDefault(content, ""))
                        .thinking(thinking)
                        .toolLink(toolLink)
                        .build())
                .build());
        long endTime = System.currentTimeMillis();
        log.info("完成AgentState耗时: {}", endTime - startTime);
    }

    public <T extends State> Optional<T> get(String userId, String sessionId, String key, Class<T> type) {
        waitPendingSseSave(sessionId);
        String slotId = slotId(userId, sessionId);
        String redisKey = this.getStateKey(slotId, key);

        try {
            String json = this.client.get(redisKey);
            return json == null ? Optional.empty() : Optional.of((T) JsonUtils.getJsonCodec().fromJson(json, type));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RenException("Failed to get state: " + key, e);
        }
    }

    public <T extends State> List<T> getList(String userId, String sessionId, String key, Class<T> itemType) {
        waitPendingSseSave(sessionId);
        String slotId = slotId(userId, sessionId);
        String redisKey = this.getListKey(slotId, key);

        try {
            List<String> jsonList = this.client.rangeList(redisKey, 0L, -1L);
            if (jsonList != null && !jsonList.isEmpty()) {
                List<T> result = new ArrayList();

                for (String json : jsonList) {
                    T item = (T) (JsonUtils.getJsonCodec().fromJson(json, itemType));
                    result.add(item);
                }

                return result;
            } else {
                return List.of();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RenException("Failed to get list: " + key, e);
        }
    }

    public boolean exists(String userId, String sessionId) {
        String slotId = slotId(userId, sessionId);
        String keysKey = this.getKeysKey(slotId);

        try {
            return this.client.keyExists(keysKey) && this.client.getSetSize(keysKey) > 0L;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RenException("Failed to check session existence: " + slotId, e);
        }
    }

    public void delete(String userId, String sessionId) {
        String slotId = slotId(userId, sessionId);
        String keysKey = this.getKeysKey(slotId);

        try {
            Set<String> trackedKeys = this.client.getSetMembers(keysKey);
            if (trackedKeys != null && !trackedKeys.isEmpty()) {
                Set<String> keysToDelete = new HashSet();
                keysToDelete.add(keysKey);

                for (String trackedKey : trackedKeys) {
                    if (trackedKey.endsWith(":list")) {
                        String baseKey = trackedKey.substring(0, trackedKey.length() - ":list".length());
                        keysToDelete.add(this.getListKey(slotId, baseKey));
                        String var10001 = this.getListKey(slotId, baseKey);
                        keysToDelete.add(var10001 + ":_hash");
                    } else {
                        keysToDelete.add(this.getStateKey(slotId, trackedKey));
                    }
                }

                this.client.deleteKeys(keysToDelete.toArray(new String[0]));
            }

            // 兜底：直接删 agent_state，避免 _keys 丢失时残留会话记忆
            this.client.deleteKeys(new String[]{this.getStateKey(slotId, AGENT_STATE_KEY)});

        } catch (Exception e) {
            e.printStackTrace();
            throw new RenException("Failed to delete session: " + slotId, e);
        }
    }

    /**
     * 回滚最近一轮用户提问及相关 ASSISTANT/TOOL 上下文（用于编辑重发）。
     */
    public void rollbackLastUserTurn(String userId, String sessionId) {
        try {
            Optional<AgentState> opt = get(userId, sessionId, AGENT_STATE_KEY, AgentState.class);
            if (opt.isEmpty() || opt.get().getContext() == null || opt.get().getContext().isEmpty()) {
                return;
            }
            AgentState state = opt.get();
            List<Msg> context = state.contextMutable();
            int lastUserIdx = -1;
            for (int i = context.size() - 1; i >= 0; i--) {
                Msg msg = context.get(i);
                if (msg != null && msg.getRole() == MsgRole.USER) {
                    lastUserIdx = i;
                    break;
                }
            }
            if (lastUserIdx < 0) {
                return;
            }
            context.subList(lastUserIdx, context.size()).clear();
            save(userId, sessionId, AGENT_STATE_KEY, state);
            log.info("已回滚会话最近一轮 AgentState, userId={}, sessionId={}, remain={}", userId, sessionId, context.size());
        } catch (Exception e) {
            log.warn("回滚 AgentState 最近一轮失败, userId={}, sessionId={}", userId, sessionId, e);
            e.printStackTrace();
        }
    }

    /**
     * 清理未完成的HITL/工具轮次。
     * 用户停止时工具可能停在ASKING，若不清理下一轮会出现异常或只出思考无正文。
     *
     * @return true 表示已回滚未完成轮次
     */
    public boolean clearIncompleteAgentTurn(String userId, String sessionId) {
        try {
            Optional<AgentState> opt = get(userId, sessionId, AGENT_STATE_KEY, AgentState.class);
            if (opt.isEmpty() || opt.get().getContext() == null || opt.get().getContext().isEmpty()) {
                return false;
            }
            AgentState state = opt.get();
            List<Msg> context = state.getContext();
            if (!hasUnresolvedToolCalls(context)) {
                return false;
            }
            rollbackLastUserTurn(userId, sessionId);
            log.info("已清理未完成 HITL/工具轮次, userId={}, sessionId={}", userId, sessionId);
            return true;
        } catch (Exception e) {
            log.warn("清理未完成 Agent 轮次失败, userId={}, sessionId={}", userId, sessionId, e);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 最近一轮是否存在未解决的工具调用（ASKING，或有ToolUse但无对应ToolResult）
     */
    private boolean hasUnresolvedToolCalls(List<Msg> context) {
        if (context == null || context.isEmpty()) {
            return false;
        }
        int lastUserIdx = -1;
        for (int i = context.size() - 1; i >= 0; i--) {
            Msg msg = context.get(i);
            if (msg != null && msg.getRole() == MsgRole.USER) {
                lastUserIdx = i;
                break;
            }
        }
        if (lastUserIdx < 0) {
            return false;
        }
        Set<String> toolUseIds = new HashSet<>();
        Set<String> toolResultIds = new HashSet<>();
        boolean hasAsking = false;
        for (int i = lastUserIdx + 1; i < context.size(); i++) {
            Msg msg = context.get(i);
            if (msg == null || CollUtil.isEmpty(msg.getContent())) {
                continue;
            }
            for (ContentBlock block : msg.getContent()) {
                if (block instanceof ToolUseBlock toolUse) {
                    if (toolUse.getState() == ToolCallState.ASKING
                            || toolUse.getState() == ToolCallState.PENDING) {
                        hasAsking = true;
                    }
                    if (StrUtil.isNotBlank(toolUse.getId())) {
                        toolUseIds.add(toolUse.getId());
                    }
                } else if (block instanceof ToolResultBlock toolResult) {
                    if (StrUtil.isNotBlank(toolResult.getId())) {
                        toolResultIds.add(toolResult.getId());
                    }
                }
            }
        }
        if (hasAsking) {
            return true;
        }
        for (String id : toolUseIds) {
            if (!toolResultIds.contains(id)) {
                return true;
            }
        }
        return false;
    }

    public Set<String> listSessionIds(String userId) {
        String userSegment = normalizeUser(userId);

        try {
            String pattern = this.keyPrefix + userSegment + "/*:_keys";
            Set<String> keysKeys = this.client.findKeysByPattern(pattern);
            Set<String> sessionIds = new HashSet();
            String userPrefix = this.keyPrefix + userSegment + "/";

            for (String keysKey : keysKeys) {
                String withoutPrefix = keysKey.substring(userPrefix.length());
                String sessionId = withoutPrefix.substring(0, withoutPrefix.length() - ":_keys".length());
                sessionIds.add(sessionId);
            }

            return sessionIds;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RenException("Failed to list sessions", e);
        }
    }

    public void close() {
        // jedisPooled 是 Spring 管理的共享连接池，此处不能关闭
    }

    /**
     * 先写数据库，再写Redis，用同一个事务。
     */
    private void persistDbThenRedis(AgentContextSyncDto syncDto, Runnable redisWrite) {
        transactionTemplate.executeWithoutResult(status -> {
            if (syncDto != null && syncDto.hasWork()) {
                try {
                    agentChatDbStore.syncAgentContextInTx(syncDto);
                } catch (Exception e) {
                    status.setRollbackOnly();
                    log.error("AgentState 同步数据库失败, sessionId={}", syncDto.getSessionId(), e);
                    throw new RenException("Failed to sync agent context to database", e);
                }
            }
            try {
                redisWrite.run();
            } catch (RenException e) {
                status.setRollbackOnly();
                throw e;
            } catch (Exception e) {
                status.setRollbackOnly();
                e.printStackTrace();
                throw new RenException("Failed to save state to redis", e);
            }
        });
    }

    /** 等待同会话 SSE 异步 save 完成，避免下一轮读到旧 agent_state */
    private void waitPendingSseSave(String sessionId) {
        if (StrUtil.isBlank(sessionId)) {
            return;
        }
        CompletableFuture<Void> pending = pendingSseSaves.remove(sessionId);
        if (pending == null) {
            return;
        }
        try {
            pending.get(15, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("等待 SSE 异步 save 超时/失败, sessionId={}", sessionId, e);
        }
    }

    private List<Msg> loadExistingList(String userId, String sessionId, String key) {
        if (!CONTEXT_KEY.equals(key)) {
            return List.of();
        }
        List<Msg> existing = getList(userId, sessionId, key, Msg.class);
        return existing == null ? List.of() : existing;
    }

    private AgentContextSyncDto buildContextSyncDtoForList(String sessionId, String key, List<Msg> oldContext, List<? extends State> newValues) {
        if (!CONTEXT_KEY.equals(key)) {
            return null;
        }
        List<Msg> newContext = newValues.stream()
                .filter(Msg.class::isInstance)
                .map(Msg.class::cast)
                .toList();
        return buildContextSyncDto(sessionId, oldContext, newContext);
    }

    /**
     * 构建context同步dto
     * @param sessionId 会话id
     * @param oldContext 旧context列表
     * @param newContext 新context列表
     * @return
     */
    private AgentContextSyncDto buildContextSyncDto(String sessionId, List<Msg> oldContext, List<Msg> newContext) {
        if (AgentStateDbSyncContext.isAborted(sessionId)) {
            log.info("会话已中止，跳过 AgentState 完整回复落库, sessionId={}", sessionId);
            return null;
        }
        AgentStateDbSyncContext.TurnSyncMeta meta = AgentStateDbSyncContext.get(sessionId);
        if (meta == null) {
            return null;
        }
        // 中止标记可能与meta清理有时序差
        if (AgentStateDbSyncContext.isAborted(sessionId)) {
            return null;
        }

        Msg newLastAssistant = findLastAssistant(newContext);
        AssistantMessageSaveDto assistantMessage = null;
        boolean persistAssistant = false;

        // 判断是否需要有assistant
        if (newLastAssistant != null && !isInterruptRecoveryMessage(newLastAssistant) && StrUtil.isNotBlank(meta.getAssistantMsgId())) {
            Msg oldLastAssistant = findLastAssistant(oldContext);
            String answer = StrUtil.trimToEmpty(newLastAssistant.getTextContent());
            String toolLink = meta.getToolLink();
            // 判断内容和工具是否为空
            if (StrUtil.isNotBlank(answer) || StrUtil.isNotBlank(toolLink)) {
                if (shouldPersistAssistant(oldContext, newContext, oldLastAssistant, newLastAssistant, answer)) {
                    persistAssistant = true;
                    ThinkingProcessCodec.PersistPayload persist = buildAssistantPersistContent(newContext, answer);
                    // 构建assistant消息
                    assistantMessage = AssistantMessageSaveDto.builder()
                            .sessionId(sessionId)
                            .msgId(meta.getAssistantMsgId())
                            .content(persist.content())
                            .thinking(persist.thinking())
                            .toolLink(toolLink)
                            .build();
                }
            }
        }

        if (!persistAssistant) {
            return null;
        }

        // 落库前再确认本轮未被中止/替换，避免stop后或下一轮已开始仍插入脏数据
        if (AgentStateDbSyncContext.isAborted(sessionId) || !AgentStateDbSyncContext.isCurrentTurn(sessionId, meta.getAssistantMsgId())) {
            log.info("会话轮次已失效，跳过 AgentState 落库, sessionId={}, assistantMsgId={}",
                    sessionId, meta.getAssistantMsgId());
            return null;
        }

        UserMessageDto userMessage = null;
        // 同一事务里带上user，防重复插入数据
        if (StrUtil.isNotBlank(meta.getUserMsgId()) && !agentChatDbStore.existsById(meta.getUserMsgId())) {
            userMessage = UserMessageDto.builder().sessionId(sessionId)
                    .userId(meta.getUserId())
                    .msgId(meta.getUserMsgId())
                    .content(meta.getUserContent())
                    .files(meta.getFiles())
                    .targetLanguageType(meta.getTargetLanguageType())
                    .sourceLanguageType(meta.getSourceLanguageType())
                    .build();
        }

        return AgentContextSyncDto.builder().sessionId(sessionId).persistUser(userMessage != null)
                .userMessage(userMessage)
                .persistAssistant(true)
                .assistantMessage(assistantMessage)
                .build();
    }

    /**
     * 从当前轮次 context 提取思考/工具步骤：答案进 content，步骤进 thinking 字段。
     */
    private ThinkingProcessCodec.PersistPayload buildAssistantPersistContent(List<Msg> newContext, String answer) {
        String safeAnswer = ThinkingProcessCodec.stripThinkTags(StrUtil.blankToDefault(answer, ""));
        List<ThinkingStepVO> steps = List.of();
        List<List<ThinkingStepVO>> turns = AgentThinkingExtractor.extractPerAssistantTurn(newContext);
        if (!turns.isEmpty()) {
            steps = turns.get(turns.size() - 1);
        }
        if ((steps == null || steps.isEmpty()) && (answer.contains("<think>") || answer.contains("</think>"))) {
            steps = ThinkingProcessCodec.fromThinkTags(answer);
        }
        return ThinkingProcessCodec.encode(steps, safeAnswer);
    }

    /**
     * 判断这轮assistant要不要更新数据
     */
    private boolean shouldPersistAssistant(List<Msg> oldContext, List<Msg> newContext, Msg oldLastAssistant, Msg newLastAssistant, String newContent) {
        // 第一次出assistant
        if (oldLastAssistant == null) {
            return true;
        }
        if (newContext.size() > oldContext.size()) {
            return newLastAssistant == findLastAssistant(newContext);
        }
        String oldContent = StrUtil.trimToEmpty(oldLastAssistant.getTextContent());
        return newLastAssistant == findLastAssistant(newContext) && (!StrUtil.equals(oldContent, newContent) || isInterruptRecoveryMessage(oldLastAssistant));
    }

    private List<Msg> getContext(AgentState state) {
        if (state == null || state.getContext() == null) {
            return List.of();
        }
        return state.getContext();
    }

    private Msg findLastAssistant(List<Msg> context) {
        for (int i = context.size() - 1; i >= 0; i--) {
            Msg msg = context.get(i);
            if (msg != null && msg.getRole() == MsgRole.ASSISTANT) {
                return msg;
            }
        }
        return null;
    }

    private boolean isInterruptRecoveryMessage(Msg msg) {
        if (msg == null || msg.getRole() != MsgRole.ASSISTANT) {
            return false;
        }
        String text = msg.getTextContent();
        return text != null && text.contains(INTERRUPT_RECOVERY_SNIPPET);
    }

    private static String normalizeUser(String userId) {
        //判断是否已登录，未登录则返回匿名用户
        return StrUtil.isNotBlank(userId) ? userId : ANON_SUFFIX;
    }

    private static String slotId(String userId, String sessionId) {
        if (StrUtil.isNotBlank(sessionId)) {
            String newUserId = normalizeUser(userId);
            return newUserId + "/" + sessionId;
        } else {
            throw new RenException("sessionId不允许为空！");
        }
    }

    private String getStateKey(String sessionId, String key) {
        return this.keyPrefix + sessionId + ":" + key;
    }

    private String getListKey(String sessionId, String key) {
        return this.keyPrefix + sessionId + ":" + key + LIST_SUFFIX;
    }

    private String getKeysKey(String sessionId) {
        return this.keyPrefix + sessionId + KEYS_SUFFIX;
    }
}
