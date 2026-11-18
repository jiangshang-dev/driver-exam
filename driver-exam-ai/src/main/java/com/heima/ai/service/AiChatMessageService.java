package com.heima.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.heima.ai.dao.AiChatMessageDao;
import com.heima.ai.entity.AiChatMessage;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * AI 会话消息服务
 */
@Service
public class AiChatMessageService {

    @Resource
    private AiChatMessageDao chatMessageDao;

    public AiChatMessage getById(String id) {
        return id == null ? null : chatMessageDao.selectById(id);
    }

    public void save(AiChatMessage message) {
        chatMessageDao.insert(message);
    }

    public void updateById(AiChatMessage message) {
        chatMessageDao.updateById(message);
    }

    /**
     * 获取会话下最大的消息序号
     */
    public long getMaxSequenceBySessionId(String sessionId) {
        QueryWrapper<AiChatMessage> wrapper = new QueryWrapper<>();
        wrapper.select("IFNULL(MAX(sort), 0) AS maxSort").eq("session_id", sessionId);
        List<Map<String, Object>> maps = chatMessageDao.selectMaps(wrapper);
        if (maps == null || maps.isEmpty()) {
            return 0L;
        }
        Object value = maps.get(0).get("maxSort");
        if (value == null) {
            for (Object item : maps.get(0).values()) {
                if (item instanceof Number number) {
                    return number.longValue();
                }
            }
            return 0L;
        }
        return value instanceof Number number ? number.longValue() : Long.parseLong(value.toString());
    }
}
