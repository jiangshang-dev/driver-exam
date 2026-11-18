package com.heima.ai.dto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class LlmChatCallDto {
    private String message;
    private String userId;
    private String modelId;
    private Boolean enableThinking;
    private String sessionId;
}
