package com.heima.ai.dto;

import com.heima.ai.enums.LanguageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * AI 回复消息落库参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssistantMessageSaveDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private String sessionId;
    private String msgId;
    private String content;
    private List<String> thinking;
    private String toolLink;
    private List<FileInfo> files;
    private LanguageType targetLanguageType;
    private LanguageType sourceLanguageType;
    private String translateDocId;
}
