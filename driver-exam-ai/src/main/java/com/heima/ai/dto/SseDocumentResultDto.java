package com.heima.ai.dto;

import com.heima.ai.enums.MessageTypeEnum;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Data
@Builder
public class SseDocumentResultDto {
    private SseEmitter emitter;
    private String documentId;
    private String fileName;
    private String originalFileUrl;
    private String conversionFileUrl;
    private MessageTypeEnum messageType;
}
