package com.heima.ai.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FileParseStatusEnum {
    @Schema(description = "上传中")
    FILE_UPLOADING("FILE_UPLOADING", "上传中"),
    @Schema(description = "文档翻译")
    FILE_TRANSLATE("FILE_TRANSLATE", "翻译中"),
    @Schema(description = "翻译完成")
    FILE_TRANSLATE_COMPLETED("FILE_TRANSLATE_COMPLETED", "翻译完成"),
    @Schema(description = "翻译中止")
    FILE_TRANSLATE_STOP("FILE_TRANSLATE_STOP", "翻译中止"),
    @Schema(description = "文件解析")
    DOCUMENT_PARSING("DOCUMENT_PARSING", "解析中"),
    @Schema(description = "解析完成")
    COMPLETED("COMPLETED", "解析完成"),
    @Schema(description = "解析失败")
    FAILED("FAILED", "解析失败"),
    ;

    private final String code;
    private final String desc;
}
