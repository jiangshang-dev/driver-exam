package com.heima.ai.dto;

import com.heima.ai.enums.LanguageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 用户消息落库参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMessageDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private String sessionId;
    private Integer userId;
    private String msgId;
    private String content;
    private List<FileInfo> files;
    private LanguageType targetLanguageType;
    private LanguageType sourceLanguageType;
}
