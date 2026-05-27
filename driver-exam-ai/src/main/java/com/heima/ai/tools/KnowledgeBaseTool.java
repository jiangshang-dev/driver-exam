package com.heima.ai.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KnowledgeBaseTool {

    @Tool("测试Tool")
    public String filterMaterialsByMultipleCategoryPerformances(@P("性能条件列表(JSON格式：属性performanceName[性能名称],conditionType:OperatorEnum[条件类型],threshold[阈值],categoryName[材料分类名称])") String performanceConditionsJson) {
        log.info("多性能条件筛选材料参数: {}", performanceConditionsJson);
        return String.join("\n", "result");
    }

}
