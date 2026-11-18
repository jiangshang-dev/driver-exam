package com.heima.ai.store;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.heima.ai.dto.AssistantMessageSaveDto;
import com.heima.ai.dto.FileInfo;
import com.heima.ai.dto.UserMessageDto;
import com.heima.ai.entity.AiChatMessage;
import com.heima.ai.entity.AiChatSession;
import com.heima.ai.enums.LanguageType;
import com.heima.ai.service.AiChatMessageService;
import com.heima.ai.service.AiChatSessionService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class AgentChatDbStore {

    /** 同一会话内串行落库，避免连发时 getMax+1 乱序，以及 user/assistant 交错不成对 */
    private final ConcurrentHashMap<String, Object> sessionPersistLocks = new ConcurrentHashMap<>();

    @Resource
    private AiChatMessageService chatMessageService;

    @Resource
    private AiChatSessionService chatSessionService;

    @Transactional(rollbackFor = Exception.class)
    public void saveUserMessage(UserMessageDto messageDto) {
        if (messageDto == null) {
            return;
        }
//        String sessionId = messageDto.getSessionId();
//        Object lock = sortLocks.computeIfAbsent(StrUtil.blankToDefault(sessionId, "_"), k -> new Object());
        this.doSaveUserMessage(messageDto);
    }

    @Transactional(rollbackFor = Exception.class)
    public void syncAgentContext(AgentContextSyncDto dto) {
        long startTime = System.currentTimeMillis();
        this.doSyncAgentContext(dto);
        long endTime = System.currentTimeMillis();
        log.info("完成AgentContext耗时: {}", endTime - startTime);
    }

    public void syncAgentContextInTx(AgentContextSyncDto dto) {
        this.doSyncAgentContext(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveAssistantMessage(AssistantMessageSaveDto dto) {
        if (dto == null) {
            return;
        }
//        String sessionId = dto.getSessionId();
//        Object lock = sortLocks.computeIfAbsent(StrUtil.blankToDefault(sessionId, "_"), k -> new Object());
        this.doSaveAssistantMessage(dto);
    }

    public boolean existsById(String msgId) {
        AiChatMessage chatMessage = chatMessageService.getById(msgId);
        return StrUtil.isNotBlank(msgId) && chatMessage != null;
    }

    private void doSyncAgentContext(AgentContextSyncDto dto) {
        if (dto == null || !dto.hasWork()) {
            return;
        }
        String sessionId = dto.getSessionId();
        if (StrUtil.isBlank(sessionId)) {
            if (dto.getUserMessage() != null) {
                sessionId = dto.getUserMessage().getSessionId();
            } else if (dto.getAssistantMessage() != null) {
                sessionId = dto.getAssistantMessage().getSessionId();
            }
        }
        Object lock = sessionPersistLocks.computeIfAbsent(StrUtil.blankToDefault(sessionId, "_"), k -> new Object());
        long startTime = System.currentTimeMillis();
        synchronized (lock) {
            if (dto.isPersistUser() && dto.getUserMessage() != null) {
                this.doSaveUserMessage(dto.getUserMessage());
            }
            if (dto.isPersistAssistant() && dto.getAssistantMessage() != null) {
                this.doSaveAssistantMessage(dto.getAssistantMessage());
            }
        }
        long endTime = System.currentTimeMillis();
        log.info("完成保存数据库耗时: {}", endTime - startTime);
    }

    private long nextSort(String sessionId) {
        return chatMessageService.getMaxSequenceBySessionId(sessionId) + 1;
    }

    private void doSaveUserMessage(UserMessageDto messageDto) {
        String content = messageDto.getContent();
        Integer userId = messageDto.getUserId();
        String msgId = messageDto.getMsgId();
        String sessionId = messageDto.getSessionId();
        List<FileInfo> files = messageDto.getFiles();
        LanguageType sourceLang = messageDto.getSourceLanguageType();
        LanguageType targetLang = messageDto.getTargetLanguageType();
        // 判断内容是否为空
        if (StrUtil.isBlank(content)) {
            return;
        }
        this.ensureSession(userId, sessionId, content);

        // 判断msgId是否存在，存在则直接返回
        AiChatMessage existing = chatMessageService.getById(msgId);
        if (existing != null) {
            return;
        }

        AiChatMessage message = new AiChatMessage();
        message.setId(msgId);
        message.setSessionId(sessionId);
        message.setRole("user");
        message.setContent(content);
        message.setSort(nextSort(sessionId));
        message.setCreateTime(System.currentTimeMillis());
        fillFiles(message, files);
        if (targetLang != null) {
            message.setTranslateDocTargetLanguage(targetLang.name());
        }
        if (sourceLang != null) {
            message.setTranslateDocSourceLanguage(sourceLang.name());
        }
        chatMessageService.save(message);
    }

    private void doSaveAssistantMessage(AssistantMessageSaveDto dto) {
        String sessionId = dto.getSessionId();
        String msgId = dto.getMsgId();
        String content = dto.getContent();
        String toolLink = dto.getToolLink();
        List<String> thinking = dto.getThinking();
        List<FileInfo> files = dto.getFiles();

        String targetTranslateLanguage = "";
        String sourceTranslateLanguage = "";
        String translateDocId = "";
        if (dto.getTargetLanguageType() != null) {
            targetTranslateLanguage = dto.getTargetLanguageType().name();
            sourceTranslateLanguage = dto.getSourceLanguageType().name();
            translateDocId = dto.getTranslateDocId();
        }

        boolean hasContent = StrUtil.isNotBlank(content);
        boolean hasToolLink = StrUtil.isNotBlank(toolLink);
        boolean hasFiles = CollUtil.isNotEmpty(files);
        boolean hasThinking = CollUtil.isNotEmpty(thinking);
        if (!hasContent && !hasToolLink && !hasFiles && !hasThinking) {
            return;
        }
        AiChatMessage existing = chatMessageService.getById(msgId);
        if (existing != null) {
            AiChatMessage update = new AiChatMessage();
            update.setId(msgId);
            if (hasContent) {
                update.setContent(content);
            }
            if (hasThinking) {
                update.setThinking(JSONUtil.toJsonStr(thinking));
            }
            if (hasToolLink) {
                update.setToolLink(toolLink);
            }
            if (hasFiles) {
                fillFiles(update, files);
            }
            fillTranslateFields(update, targetTranslateLanguage, sourceTranslateLanguage, translateDocId);
            chatMessageService.updateById(update);
            return;
        }

        AiChatMessage message = new AiChatMessage();
        message.setId(msgId);
        message.setSessionId(sessionId);
        message.setRole("assistant");
        message.setContent(StrUtil.blankToDefault(content, ""));
        message.setThinking(hasThinking ? JSONUtil.toJsonStr(thinking) : null);
        message.setToolLink(toolLink);
        message.setSort(nextSort(sessionId));
        message.setCreateTime(System.currentTimeMillis());
        fillFiles(message, files);
        fillTranslateFields(message, targetTranslateLanguage, sourceTranslateLanguage, translateDocId);
        chatMessageService.save(message);

        AiChatSession session = chatSessionService.getById(sessionId);
        if (session != null) {
            AiChatSession updateSession = new AiChatSession();
            updateSession.setId(sessionId);
            updateSession.setUpdateTime(System.currentTimeMillis());
            chatSessionService.updateById(updateSession);
        }
    }

    /**
     * 翻译相关
     * @param message 消息实体
     * @param targetLanguage 目标语言
     * @param sourceLanguage 源语言
     * @param translateDocId 翻译文档id
     */
    private void fillTranslateFields(AiChatMessage message, String targetLanguage,
                                     String sourceLanguage, String translateDocId) {
        if (StrUtil.isNotBlank(targetLanguage)) {
            message.setTranslateDocTargetLanguage(targetLanguage);
        }
        if (StrUtil.isNotBlank(sourceLanguage)) {
            message.setTranslateDocSourceLanguage(sourceLanguage);
        }
        if (StrUtil.isNotBlank(translateDocId)) {
            message.setTranslateDocid(translateDocId);
        }
    }

    private void ensureSession(Integer userId, String sessionId, String firstUserContent) {
        AiChatSession existing = chatSessionService.getById(sessionId);
        if (existing != null) {
            AiChatSession update = new AiChatSession();
            update.setId(sessionId);
            update.setUpdateTime(System.currentTimeMillis());
            chatSessionService.updateById(update);
            return;
        }

        AiChatSession session = new AiChatSession();
        session.setId(sessionId);
        session.setUserId(userId);
        session.setMessageCount(0);
        session.setCreateTime(System.currentTimeMillis());
        session.setUpdateTime(System.currentTimeMillis());
        String title = firstUserContent.length() > 20 ? firstUserContent.substring(0, 20) : firstUserContent;
        session.setTitle(StrUtil.isBlank(title) ? "新对话" : title);
        chatSessionService.save(session);
    }

    private void fillFiles(AiChatMessage message, List<FileInfo> files) {
        if (CollUtil.isEmpty(files)) {
            return;
        }
        List<JSONObject> fileList = new ArrayList<>();
        for (FileInfo file : files) {
            JSONObject fileJson = new JSONObject();
            fileJson.set("conversionFileUrl", file.getConversionFileUrl());
            fileJson.set("fileName", file.getFileName());
            fileJson.set("documentId", file.getDocumentId());
            fileJson.set("originalFileUrl", file.getOriginalFileUrl());
            fileList.add(fileJson);
        }
        message.setFiles(new JSONArray(fileList).toString());
        if (StrUtil.isNotBlank(files.getFirst().getDocumentId())) {
            message.setTranslateDocid(files.getFirst().getDocumentId());
        }
    }
}
