package com.heima.ai.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 大模型配置（OpenAI 兼容协议）
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "drive.ai.model")
public class AiModelProperties {

    /** 模型服务地址，如 https://dashscope.aliyuncs.com/compatible-mode/v1 */
    private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";

    private String apiKey;

    private String modelName = "qwen-plus";

    /** 请求超时时间，单位秒 */
    private Integer timeoutSeconds = 900;

    /** 是否默认开启思考模式 */
    private Boolean enableThinking = false;
}
