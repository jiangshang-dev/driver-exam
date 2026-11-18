package com.heima.ai.factory;

import cn.hutool.core.util.StrUtil;
import com.heima.ai.entity.AiModelEntity;
import com.heima.ai.properties.AiModelProperties;
import com.heima.ai.service.AiModelService;
import io.agentscope.core.formatter.openai.OpenAIChatFormatter;
import io.agentscope.core.model.GenerateOptions;
import io.agentscope.core.model.OpenAIChatModel;
import io.agentscope.core.model.transport.HttpTransportConfig;
import io.agentscope.core.model.transport.HttpVersion;
import io.agentscope.core.model.transport.JdkHttpTransport;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * AgentModel 模型工程
 * 优先使用后台「模型配置」中启用的普通大模型，未配置时回退到 drive.ai.model
 */
@Slf4j
@Component
public class AgentModelFactory {

    @Resource
    private AiModelProperties aiModelProperties;

    @Resource
    private AiModelService aiModelService;

    /**
     * 构建 OpenAIChatModel
     */
    public OpenAIChatModel createChatModel(String modelId, boolean enableThinking) {
        AiModelEntity active = null;
        try {
            active = aiModelService.getActive("LLM");
        } catch (Exception e) {
            log.warn("读取已启用模型失败，使用配置文件中的模型", e);
        }

        String modelName = aiModelProperties.getModelName();
        String baseUrl = aiModelProperties.getBaseUrl();
        String apiKey = aiModelProperties.getApiKey();
        GenerateOptions.Builder options = GenerateOptions.builder()
                .additionalBodyParams(Map.of("chat_template_kwargs", Map.of("enable_thinking", enableThinking)));

        if (active != null) {
            modelName = active.getModelName();
            baseUrl = active.getBaseUrl();
            apiKey = StrUtil.isNotBlank(active.getApiKey()) ? active.getApiKey() : apiKey;
            applyOption(options, active.getTemperature(), active.getTopP(),
                    active.getPresencePenalty(), active.getFrequencyPenalty(), active.getMaxTokens());
            log.debug("使用后台启用的模型: {} ({})", modelName, baseUrl);
        } else if (modelId != null && !modelId.isBlank() && !modelId.equals(modelName)) {
            log.debug("请求的模型ID [{}] 未单独配置，使用默认模型 [{}]", modelId, modelName);
        }

        return OpenAIChatModel.builder()
                .modelName(modelName)
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .formatter(new OpenAIChatFormatter())
                .httpTransport(
                        JdkHttpTransport.builder()
                                .config(HttpTransportConfig.builder().httpVersion(HttpVersion.HTTP_1_1).build())
                                .build()
                )
                .generateOptions(options.build())
                .stream(true)
                .build();
    }

    private void applyOption(GenerateOptions.Builder builder, BigDecimal temperature, BigDecimal topP,
                             BigDecimal presencePenalty, BigDecimal frequencyPenalty, Integer maxTokens) {
        if (temperature != null) {
            builder.temperature(temperature.doubleValue());
        }
        if (topP != null) {
            builder.topP(topP.doubleValue());
        }
        if (presencePenalty != null) {
            builder.presencePenalty(presencePenalty.doubleValue());
        }
        if (frequencyPenalty != null) {
            builder.frequencyPenalty(frequencyPenalty.doubleValue());
        }
        if (maxTokens != null && maxTokens > 0) {
            builder.maxTokens(maxTokens);
        }
    }
}
