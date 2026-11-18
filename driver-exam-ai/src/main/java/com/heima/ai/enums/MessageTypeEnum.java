package com.heima.ai.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MessageTypeEnum {
    TEXT("TEXT", "普通聊天"),
    IMAGE_TEXT("IMAGE_TEXT", "上传图片聊天"),
    AUTO("AUTO", "自动识别"),
    ;
    private final String code;
    private final String desc;

}
