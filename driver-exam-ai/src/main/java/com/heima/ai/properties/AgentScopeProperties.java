package com.heima.ai.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties; // 绑定 application.yml
import org.springframework.stereotype.Component;

/**
 * 绑定 agentscope.demo.* 配置项。
 * 通过 @ConfigurationProperties 自动映射 yaml 字段（kebab-case -> camelCase）。
 */
@Data
@Component // 注册为 Spring Bean
@ConfigurationProperties(prefix = "agentscope.ai") // 前缀与 application.yml 一致
public class AgentScopeProperties {

    private MemoryProperties memory = new MemoryProperties(); // 记忆与压缩

    @Data
    public static class MemoryProperties {
        // 是否启用小模型压缩功能开关
        private boolean compactionModelEnabled = false;
        // 压缩用小模型名称
        private String compactionModelName = "qwen/qwen3-1.7b";
        // 压缩用小模型基础URL
        private String compactionModelBaseUrl = "http://192.168.1.88:1234";
        // 压缩用小模型 API Key
        private String compactionModelApiKey = "sk-d3580966983048d99b53a77fc67e8372";

        // true 压缩
        private boolean compactionEnabled = true;
        // 消息数超过此值触发压缩
        private int compactionTriggerMessages = 3000;
        // 压缩后保留最近 N 条原文
        private int compactionKeepMessages = 1000;
        /** 按估算 token 触发，0 表示走框架动态阈值（模型窗口 - reserved） */
        private int compactionTriggerTokens = 24000;
        /** 压缩后按 token 保留尾部，>0 覆盖 keepMessages */
        private int compactionKeepTokens = 8000;
    }
}
