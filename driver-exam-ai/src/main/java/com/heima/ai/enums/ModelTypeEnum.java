package com.heima.ai.enums;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Schema(description = "文档状态枚举", enumAsRef = true)
@Getter
@AllArgsConstructor
public enum ModelTypeEnum {
    /**
     * 大语言模型
     */
    @Schema(description = "大语言模型") LLM(1, "大语言模型"),
    /**
     * 向量空间模型
     */
    @Schema(description = "向量空间模型") VSM(2, "向量空间模型"),
    /**
     * 重排模型
     */
    @Schema(description = "重排模型") PRM(3, "重排模型"),
    /**
     * 多模态模型
     */
    @Schema(description = "多模态模型") MFM(4, "多模态模型");

    /**
     * code码
     */
    private final Integer code;
    /**
     * 描述、备注、注释
     */
    private final String desc;

    public static ModelTypeEnum getEnumByCode(Integer code) {
        for (ModelTypeEnum type : ModelTypeEnum.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    public static boolean contains(String name) {
        List<String> names = Arrays.stream(ModelTypeEnum.values()).map(ModelTypeEnum::name).toList();
        return names.contains(name);
    }

}
