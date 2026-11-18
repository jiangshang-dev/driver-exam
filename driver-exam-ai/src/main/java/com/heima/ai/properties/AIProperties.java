package com.heima.ai.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "material.ai.prompt")
public class AIProperties {

    private System system = new System(); // 系统提示语，用于课程推荐、购买业务

    @Data
    public static class System {
        /*
            MATERIAL_CREATE("MATERIAL_CREATE", "材料新建智能体"),
            MATERIAL_QUERY("MATERIAL_QUERY", "材料查询智能体");
         */

        private Chat routeAgent; // 路由智能体系统提示词
        private Chat chat; // 系统提示语，用于材料相关业务
        private Chat materialRecommendAgent; // 材料推荐智能体系统提示词
        private Chat materialQueryAgent; // 材料查询智能体系统提示词
        private Chat materialCreateAgent; // 材料新建智能体系统提示词
        private Chat materialPropertiesSelectAgent; // 材料知识智能体系统提示词
        private Chat text; // 文本提示语，用于问答回复、润色等文本类型的业务

        @Data
        public static class Chat {
            private String dataId;
            private String group = "DEFAULT_GROUP";
            private long timeoutMs = 20000L; // 读取的超时时间，单位毫秒
        }
    }
}