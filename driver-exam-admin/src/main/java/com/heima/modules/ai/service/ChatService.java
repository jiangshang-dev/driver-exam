package com.heima.modules.ai.service;

import com.heima.ai.enums.MessageTypeEnum;
import com.heima.ai.params.ChatParam;
import com.heima.ai.response.SseResponse;
import com.heima.ai.vo.MessageDataVO;
import reactor.core.publisher.Flux;

public interface ChatService {
    /**
     * 停止聊天
     * @param sessionId 会话id
     * @return
     */
    void stop(String sessionId);
    /**
     * 流式聊天
     * @param chatSendParams 请求参数
     * @return
     */
    Flux<SseResponse<MessageDataVO<MessageTypeEnum>>> sendWithSseFlux(ChatParam chatSendParams);

}
