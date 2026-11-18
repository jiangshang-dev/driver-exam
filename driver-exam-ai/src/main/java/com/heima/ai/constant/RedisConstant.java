package com.heima.ai.constant;

public class RedisConstant {
    public static final String REDIS_KEY_PREFIX = "ai-server:agent-chat:";
    public static final String REDIS_AGENT_STORE_PREFIX = "ai-server:agent-store:";
    public static final String REDIS_CHATSTOP_PREFIX = "ai-server:agent-chat-stop:";
    /** 上传文档 Tika 全文缓存 */
    public static final String REDIS_DOC_TEXT_PREFIX = "ai-server:doc-text:";
    /** 上传文档总览摘要缓存 */
    public static final String REDIS_DOC_SUMMARY_PREFIX = "ai-server:doc-summary:";

    public static String getChatPrefixKey(String userId, String session) {
        return REDIS_KEY_PREFIX + userId + ":" + session;
    }

    public static String getChatStopKey(String userId, String session) {
        return REDIS_CHATSTOP_PREFIX + userId + ":" + session;
    }

    public static String getChatStopKey(Integer userId, String session) {
        return REDIS_CHATSTOP_PREFIX + userId + ":" + session;
    }

    public static String getDocTextKey(String documentId) {
        return REDIS_DOC_TEXT_PREFIX + documentId;
    }

    public static String getDocSummaryKey(String documentId) {
        return REDIS_DOC_SUMMARY_PREFIX + documentId;
    }
}
