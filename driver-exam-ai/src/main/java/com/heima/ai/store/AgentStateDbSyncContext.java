package com.heima.ai.store;

import com.heima.ai.dto.FileInfo;
import com.heima.ai.enums.LanguageType;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本轮对话的元数据暂存
 */
public final class AgentStateDbSyncContext {

    private static final Map<String, TurnSyncMeta> META_BY_SESSION = new ConcurrentHashMap<>();
    /** 用户主动中止后标记，阻止 Agent 后续完整回复再次落库覆盖截断内容 */
    private static final Set<String> ABORTED_SESSIONS = ConcurrentHashMap.newKeySet();

    private AgentStateDbSyncContext() {}

    public static void register(TurnSyncMeta meta) {
        if (meta == null || meta.getSessionId() == null) {
            return;
        }
        ABORTED_SESSIONS.remove(meta.getSessionId());
        META_BY_SESSION.put(meta.getSessionId(), meta);
    }

    /**
     * 流结束后把mcp工具调用结果重新赋值
     */
    public static void updateToolLink(String sessionId, String toolLink) {
        TurnSyncMeta meta = get(sessionId);
        if (meta == null) {
            return;
        }
        meta.setToolLink(toolLink);
    }

    public static TurnSyncMeta get(String sessionId) {
        return sessionId == null ? null : META_BY_SESSION.get(sessionId);
    }

    /**
     * 结尾清理会话数据，防止下一轮串数据
     * @param sessionId 会话id
     */
    public static void clear(String sessionId) {
        if (sessionId != null) {
            META_BY_SESSION.remove(sessionId);
        }
    }

    /**
     * 仅当仍是本轮 meta 时清理，避免上一轮 doFinally 清掉下一轮刚 register 的数据。
     */
    public static void clearIfMatch(String sessionId, String assistantMsgId) {
        if (sessionId == null) {
            return;
        }
        TurnSyncMeta meta = META_BY_SESSION.get(sessionId);
        if (meta != null && assistantMsgId != null && assistantMsgId.equals(meta.getAssistantMsgId())) {
            META_BY_SESSION.remove(sessionId);
        }
    }

    public static void markAborted(String sessionId) {
        if (sessionId != null) {
            ABORTED_SESSIONS.add(sessionId);
        }
    }

    public static boolean isAborted(String sessionId) {
        return sessionId != null && ABORTED_SESSIONS.contains(sessionId);
    }

    /**
     * meta 是否仍属于指定 assistant 轮次（未被下一轮 register 覆盖）。
     */
    public static boolean isCurrentTurn(String sessionId, String assistantMsgId) {
        if (sessionId == null || assistantMsgId == null) {
            return false;
        }
        TurnSyncMeta meta = META_BY_SESSION.get(sessionId);
        return meta != null && assistantMsgId.equals(meta.getAssistantMsgId());
    }

    @Data
    @Builder
    public static class TurnSyncMeta {
        private String sessionId;
        private Integer userId;
        private String userMsgId;
        private String assistantMsgId;
        private String userContent;
        private List<FileInfo> files;
        private LanguageType targetLanguageType;
        private LanguageType sourceLanguageType;
        /**
         * mcp工具调用结果，JSON
         */
        private String toolLink;
    }
}
