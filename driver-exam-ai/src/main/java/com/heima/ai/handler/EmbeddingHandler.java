package com.heima.ai.handler;

import cn.hutool.core.util.StrUtil;
import com.heima.ai.dto.AiModelOptionDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 向量工具类
 */
@Slf4j
@Component
public class EmbeddingHandler {

    /**
     * 构造向量模型参数
     */
    public static AiModelOptionDto buildModelOptions(String modelName, String baseUrl, String apiKey) {
        return AiModelOptionDto.builder()
                .modelName(modelName)
                .baseUrl(baseUrl)
                .apiKey(StrUtil.isNotBlank(apiKey) ? apiKey : null)
                .topNumber(5)
                .similarity(0.75)
                .build();
    }
}
