package com.heima.ai.store;

import com.heima.ai.dto.AssistantMessageSaveDto;
import com.heima.ai.dto.UserMessageDto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AgentContextSyncDto {
    private String sessionId;
    private boolean persistUser;
    private boolean persistAssistant;
    private UserMessageDto userMessage;
    private AssistantMessageSaveDto assistantMessage;

    public boolean hasWork() {
        return persistUser || persistAssistant;
    }
}
