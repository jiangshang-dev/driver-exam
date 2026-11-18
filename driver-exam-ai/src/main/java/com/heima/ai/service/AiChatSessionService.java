package com.heima.ai.service;

import com.heima.ai.dao.AiChatSessionDao;
import com.heima.ai.entity.AiChatSession;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * AI 会话服务
 */
@Service
public class AiChatSessionService {

    @Resource
    private AiChatSessionDao chatSessionDao;

    public AiChatSession getById(String id) {
        return id == null ? null : chatSessionDao.selectById(id);
    }

    public void save(AiChatSession session) {
        chatSessionDao.insert(session);
    }

    public void updateById(AiChatSession session) {
        chatSessionDao.updateById(session);
    }
}
