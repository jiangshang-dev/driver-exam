package com.heima.ai.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LikedEnum {
    @Schema(description = "点踩")
    UNLIKED("UNLIKED", "点踩"),
    @Schema(description = "点赞")
    LIKED("LIKED", "点赞"),
    CANCEL_LIKED("CANCEL_LIKED", "取消点赞/点踩"),
    ;

    private final String code;
    private final String desc;
}
