package com.heima.ai.enums;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Schema(description = "文档切分策略枚举", enumAsRef = true)
@Getter
@AllArgsConstructor
public enum SegmentStrategyEnum {
    /**
     * 结构层级
     */
    @Schema(description = "层级结构") LEVEL(1, "层级结构"),
    /**
     * 段落
     */
    @Schema(description = "段落") PARAGRAPH(2, "段落"),
    /**
     * 固定字符
     */
    @Schema(description = "固定字符") CHARACTER(3, "固定字符"),
    /**
     * QA
     */
    @Schema(description = "QA") QA(4, "Q&A"),
    /**
     * 语义感知
     */
    @Schema(description = "语义感知") SEMANTICS(5, "语义感知");

    /**
     * code码
     */
    private final Integer code;
    /**
     * 描述、备注、注释
     */
    private final String desc;

    public static SegmentStrategyEnum getEnumByCode(Integer code) {
        for (SegmentStrategyEnum segmentStrategyEnum : SegmentStrategyEnum.values()) {
            if (segmentStrategyEnum.getCode().equals(code)) {
                return segmentStrategyEnum;
            }
        }
        return null;
    }

    public static boolean contains(String name) {
        List<String> names = Arrays.stream(SegmentStrategyEnum.values()).map(SegmentStrategyEnum::name).toList();
        return names.contains(name);
    }

    public Integer getValue() {
        return code;
    }
}
