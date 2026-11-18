package com.heima.ai.params;

import com.heima.ai.enums.MessageTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description: 发送消息的入参
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChatParam {

    @Schema(description = "思考模式")
    protected Boolean enableThinking;

    @Schema(description = "是否启用联网搜索 MCP")
    protected Boolean enableWebSearch;

    @NotBlank(message = "对话内容不能为空！")
    @Schema(description = "用户输入的聊天内容")
    private String message;

    @NotBlank(message = "会话id不能为空！")
    @Schema(description = "对话会话ID")
    private String sessionId;

    @Schema(description = "模型id")
    private String modelId;

    @Schema(description = "消息类型")
    private MessageTypeEnum messageType;

    @Schema(description = "是否发起对话")
    private Boolean isAfreshChat = false;

}
