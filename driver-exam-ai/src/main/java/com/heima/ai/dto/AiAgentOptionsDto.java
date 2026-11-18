package com.heima.ai.dto;

import io.agentscope.core.model.ChatModelBase;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Getter
@SuperBuilder
public class AiAgentOptionsDto {
    private String userId;
    private ChatModelBase chatMode;
    private Boolean enableWebSearch;
    private Set<String> routeType;
    private String sessionId;
}
