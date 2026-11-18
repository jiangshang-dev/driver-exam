package com.heima.modules.ai.controller;

import com.heima.ai.enums.MessageTypeEnum;
import com.heima.ai.params.ChatParam;
import com.heima.ai.response.SseResponse;
import com.heima.ai.vo.MessageDataVO;
import com.heima.modules.ai.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@AllArgsConstructor
@RestController
@RequestMapping("/chat")
@Tag(name = "ChatController", description = "AI对话服务API")
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "正式对话")
    @PostMapping(value = "/send", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<SseResponse<MessageDataVO<MessageTypeEnum>>> send(@RequestBody @Valid ChatParam chatSendParams, HttpServletResponse response) {
        // 防止传输延迟、卡顿或被中间件缓存
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        // 专门用来禁用 Nginx 等反向代理服务器的响应缓冲区（Buffering）。
        response.setHeader("X-Accel-Buffering", "no");
        return chatService.sendWithSseFlux(chatSendParams);
    }

}
