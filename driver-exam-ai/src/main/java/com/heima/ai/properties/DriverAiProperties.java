package com.heima.ai.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * material.ai 配置：MCP 清单 + 意图路由。
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "drive.ai")
public class DriverAiProperties {

    /**
     * 兼容旧配置：仅配置了 mcp-sse-url 时作为 material MCP 默认地址。
     */
    private String mcpSseUrl;

    private IntentConfig intent = new IntentConfig();

    private List<McpServerConfig> mcpServers = new ArrayList<>();

    @Data
    public static class IntentConfig {
        /** 是否启用意图路由；关闭则始终走 fallbackIntent */
        private boolean enabled = true;
        /** 规则未命中时是否调用小模型做 JSON 分类 */
        private boolean useLlm = false;
        /** 分类失败/unknown 时的默认意图 */
        private String fallbackIntent = "drive";
        /** intent -> 关键词列表（规则路由） */
        private Map<String, List<String>> keywords = new LinkedHashMap<>();
    }

    @Data
    public static class McpServerConfig {
        private String id;
        private String name;
        /** 命中任一意图时注册该 MCP */
        private List<String> intents = new ArrayList<>();
        private String url;
        /** sse | streamable_http */
        private String transport = "sse";
        private boolean enabled = true;
        private Map<String, String> headers = new LinkedHashMap<>();
        private long timeoutSeconds = 200;
    }
}
