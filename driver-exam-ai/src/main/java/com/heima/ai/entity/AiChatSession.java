package com.heima.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * AI 会话
 */
@Data
@TableName("ai_chat_session")
public class AiChatSession implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.INPUT)
    private String id;
    private Integer userId;
    private String title;
    private Integer messageCount;
    private Long createTime;
    private Long updateTime;
}
