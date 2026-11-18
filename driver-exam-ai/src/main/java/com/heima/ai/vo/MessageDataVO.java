package com.heima.ai.vo;

import com.heima.ai.enums.FileParseStatusEnum;
import com.heima.ai.enums.MessageTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDataVO<T> {
    @Schema(description = "消息内容")
    private String message;
    @Schema(description = "消息时间戳")
    private Long timestamp;
    @Schema(description = "消息类型")
    private MessageTypeEnum type;
    @Schema(description = "文件信息")
    private FileData fileData;
    private int eventType;
    @Schema(description = "流式子事件: thinking_delta / answer_start / answer_delta / tool_call_start / tool_call_delta / tool_call_end / tool_result_start / tool_result_end")
    private String streamEvent;
    @Schema(description = "工具调用ID")
    private String toolCallId;
    @Schema(description = "工具名称")
    private String toolCallName;
    @Schema(description = "用户消息ID")
    private String userMsgId;
    @Schema(description = "AI消息ID")
    private String assistantMsgId;

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileData {
        private String documentId;
        private FileParseStatusEnum status;
    }
}