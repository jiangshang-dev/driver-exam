package com.heima.ai.utils;

import cn.hutool.core.util.StrUtil;
import com.heima.ai.vo.ThinkingStepVO;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.ThinkingBlock;

import java.util.ArrayList;
import java.util.List;

/**
 * 从 Agent 上下文中提取每个 assistant 轮次的思考步骤
 */
public final class AgentThinkingExtractor {

    private AgentThinkingExtractor() {
    }

    public static List<List<ThinkingStepVO>> extractPerAssistantTurn(List<Msg> context) {
        List<List<ThinkingStepVO>> turns = new ArrayList<>();
        if (context == null || context.isEmpty()) {
            return turns;
        }
        for (Msg msg : context) {
            if (msg == null || msg.getRole() != MsgRole.ASSISTANT) {
                continue;
            }
            List<ThinkingStepVO> steps = new ArrayList<>();
            List<ThinkingBlock> blocks = msg.getContentBlocks(ThinkingBlock.class);
            if (blocks != null) {
                for (ThinkingBlock block : blocks) {
                    if (block != null && StrUtil.isNotBlank(block.getThinking())) {
                        steps.add(ThinkingStepVO.builder().title("思考").content(block.getThinking()).build());
                    }
                }
            }
            if (!steps.isEmpty()) {
                turns.add(steps);
            }
        }
        return turns;
    }
}
