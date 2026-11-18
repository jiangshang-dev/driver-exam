package com.heima.ai.config;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.heima.ai.properties.AIProperties;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Getter
@Configuration
@RequiredArgsConstructor
public class SystemPromptConfig {

    private final AIProperties aiProperties;

    // 使用原子引用，保证线程安全
    private final AtomicReference<String> chatSystemMessage = new AtomicReference<>();
    // 路由智能体提示词
    private final AtomicReference<String> routeAgentSystemMessage = new AtomicReference<>();
    // 材料查询智能体
    private final AtomicReference<String> materialQueryAgentSystemMessage = new AtomicReference<>();
    private final AtomicReference<String> textSystemMessage = new AtomicReference<>();

    @PostConstruct // 初始化时加载配置
    public void init() {
        AIProperties.System system = aiProperties.getSystem() == null ? new AIProperties.System() : aiProperties.getSystem();
        loadConfig(system.getChat(), chatSystemMessage);
        loadConfig(system.getRouteAgent(), routeAgentSystemMessage);
        loadConfig(system.getMaterialQueryAgent(), materialQueryAgentSystemMessage);
        loadConfig(system.getText(), textSystemMessage);

        // 未配置外部配置中心时，使用 classpath 下的 system.txt 作为默认系统提示词
        if (StrUtil.isBlank(chatSystemMessage.get())) {
            chatSystemMessage.set(readClasspath("system.txt"));
        }
        if (StrUtil.isBlank(materialQueryAgentSystemMessage.get())) {
            materialQueryAgentSystemMessage.set(chatSystemMessage.get());
        }
        log.info("AI 系统提示词加载完成, chatPromptLength={}",
                chatSystemMessage.get() == null ? 0 : chatSystemMessage.get().length());
    }

    private void loadConfig(AIProperties.System.Chat chatConfig, AtomicReference<String> target) {
        if (chatConfig == null) {
            return;
        }
        // 当前项目未接入配置中心，dataId/group 仅保留配置项
        log.debug("加载系统提示词配置 dataId={}, group={}", chatConfig.getDataId(), chatConfig.getGroup());
    }

    private String readClasspath(String path) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            if (!resource.exists()) {
                return "";
            }
            return IoUtil.read(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("读取系统提示词文件失败: {}", path, e);
            return "";
        }
    }
}
