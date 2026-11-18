package com.heima.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * AI 会话消息
 */
@Data
@TableName("ai_chat_message")
public class AiChatMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.INPUT)
    private String id;
    private String sessionId;
    /** user / assistant */
    private String role;
    private String content;
    /** 思考过程 JSON 数组 */
    private String thinking;
    private String toolLink;
    /** 附件 JSON 数组 */
    private String files;
    private Long sort;
    private String translateDocid;
    private String translateDocTargetLanguage;
    private String translateDocSourceLanguage;
    private Long createTime;
}
