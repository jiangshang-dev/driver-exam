package com.heima.ai.utils;

import cn.hutool.core.util.StrUtil;
import com.heima.ai.vo.ThinkingStepVO;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 思考过程编解码工具
 */
public final class ThinkingProcessCodec {

    private static final Pattern THINK_TAG_PATTERN = Pattern.compile("<think>([\\s\\S]*?)</think>", Pattern.CASE_INSENSITIVE);

    private ThinkingProcessCodec() {
    }

    /**
     * 把思考步骤编码为落库结构
     */
    public static PersistPayload encode(List<ThinkingStepVO> steps, String answer) {
        List<String> thinking = new ArrayList<>();
        if (steps != null) {
            for (ThinkingStepVO step : steps) {
                if (step == null) {
                    continue;
                }
                StringBuilder sb = new StringBuilder();
                if (StrUtil.isNotBlank(step.getTitle())) {
                    sb.append("[").append(step.getTitle()).append("] ");
                }
                if (StrUtil.isNotBlank(step.getContent())) {
                    sb.append(step.getContent());
                }
                if (sb.length() > 0) {
                    thinking.add(sb.toString());
                }
            }
        }
        return new PersistPayload(StrUtil.blankToDefault(answer, ""), thinking);
    }

    /**
     * 去掉回复中的 think 标签，只保留正文
     */
    public static String stripThinkTags(String text) {
        if (StrUtil.isBlank(text)) {
            return "";
        }
        return THINK_TAG_PATTERN.matcher(text).replaceAll("").trim();
    }

    /**
     * 从回复文本中解析 think 标签里的思考步骤
     */
    public static List<ThinkingStepVO> fromThinkTags(String text) {
        List<ThinkingStepVO> steps = new ArrayList<>();
        if (StrUtil.isBlank(text)) {
            return steps;
        }
        Matcher matcher = THINK_TAG_PATTERN.matcher(text);
        while (matcher.find()) {
            String content = matcher.group(1);
            if (StrUtil.isNotBlank(content)) {
                steps.add(ThinkingStepVO.builder().title("思考").content(content.trim()).build());
            }
        }
        return steps;
    }

    /**
     * 落库内容：正文 + 思考步骤
     */
    public record PersistPayload(String content, List<String> thinking) {
    }
}
