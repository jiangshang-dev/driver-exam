package com.heima.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI 模型配置
 */
@Data
@TableName("ai_model")
public class AiModelEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String modelName;
    /** LLM / EMBEDDING / TTS */
    private String modelType;
    private String baseUrl;
    private String apiKey;
    private BigDecimal temperature;
    private BigDecimal topP;
    private BigDecimal presencePenalty;
    private BigDecimal frequencyPenalty;
    private Integer maxTokens;
    private String remark;
    private Integer sortOrder;
    private Integer isActive;
    private Integer isDeleted;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;
}
