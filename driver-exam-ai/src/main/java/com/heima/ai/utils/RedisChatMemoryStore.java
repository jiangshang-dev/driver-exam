package com.heima.ai.utils;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 会话记忆对象存储
 */
@Repository
public class RedisChatMemoryStore implements ChatMemoryStore {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 获取会话消息
     * @param memoryId
     * @return
     */
    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        // 1.从redis中根据会话id获取会话消息
        String json = stringRedisTemplate.opsForValue().get(memoryId.toString());
        // 2.把json消息转换为list
        return ChatMessageDeserializer.messagesFromJson(json);
    }

    /**
     * 新增会话消息
     * @param memoryId 会话id
     * @param list 会话消息列表
     */
    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> list) {
        // 更新会话消息
        // 1.把list转换为json消息
        String json = ChatMessageSerializer.messagesToJson(list);
        // 2.把json数据存储到redis中
        stringRedisTemplate.opsForValue().set(memoryId.toString(), json, 1, TimeUnit.DAYS);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        stringRedisTemplate.delete(memoryId.toString());
    }
}
