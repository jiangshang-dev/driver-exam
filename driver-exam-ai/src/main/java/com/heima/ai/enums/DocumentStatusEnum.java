package com.heima.ai.enums;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Schema(description = "文档状态枚举", enumAsRef = true)
@Getter
@AllArgsConstructor
public enum DocumentStatusEnum {
    /**
     * 启用
     */
    @Schema(description = "已上传") UPLOADED(1, "已上传"),
    /**
     * 解析中
     */
    @Schema(description = "解析中") PARSING(2, "解析中"),
    /**
     * 存储中
     */
    @Schema(description = "存储中") STORAGE(3, "存储中"),
    /**
     * 已完成
     */
    @Schema(description = "已完成") COMPLETE(4, "已完成");

    /**
     * code码
     */
    private final Integer code;
    /**
     * 描述、备注、注释
     */
    private final String desc;

    public static DocumentStatusEnum getEnumByCode(Integer code) {
        for (DocumentStatusEnum segmentStrategyEnum : DocumentStatusEnum.values()) {
            if (segmentStrategyEnum.getCode().equals(code)) {
                return segmentStrategyEnum;
            }
        }
        return null;
    }

    public static boolean contains(String name) {
        List<String> names = Arrays.stream(DocumentStatusEnum.values()).map(DocumentStatusEnum::name).toList();
        return names.contains(name);
    }

    public Integer getValue() {
        return code;
    }
}
