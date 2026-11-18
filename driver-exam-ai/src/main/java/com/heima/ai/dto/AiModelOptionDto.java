package com.heima.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 向量模型参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiModelOptionDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private String apiKey;
    private String baseUrl;
    private String modelName;
    /** 超时时间，单位秒 */
    private Integer timeout;
    /** 检索返回条数 */
    private Integer topNumber;
    /** 相似度阈值 */
    private Double similarity;
}
