package com.heima.ai.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 上传场景：普通对话附件 vs 翻译场景，由意图识别结合文件类型解析为具体 MessageTypeEnum。
 */
@Getter
@AllArgsConstructor
public enum UploadIntentMode {
    /** 添加照片和文件：图片走多模态/OCR，文档走向量 */
    CHAT("CHAT", "对话附件"),
    /** 翻译：按文件类型自动区分文档翻译 / 图片翻译 */
    TRANSLATE("TRANSLATE", "智能翻译"),
    /** 明确文档翻译 */
    TRANSLATE_DOC("TRANSLATE_DOC", "文档翻译"),
    /** 明确图片翻译 */
    TRANSLATE_IMAGE("TRANSLATE_IMAGE", "图片翻译"),
    ;

    private final String code;
    private final String desc;
}
