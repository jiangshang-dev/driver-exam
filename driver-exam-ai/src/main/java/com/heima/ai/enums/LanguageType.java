package com.heima.ai.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LanguageType {
    @Schema(description = "简体中文")
    SIMPLIFIED_CHINESE("SIMPLIFIED_CHINESE", "简体中文"),
    @Schema(description = "繁体中文")
    TRADITIONAL_CHINESE("TRADITIONAL_CHINESE", "繁体中文"),
    @Schema(description = "英文")
    ENGLISH("ENGLISH", "英文"),
    @Schema(description = "日语")
    JAPANESE("JAPANESE", "日语"),
    @Schema(description = "韩语")
    KOREAN("KOREAN", "韩语"),
    @Schema(description = "法语")
    FRENCH("FRENCH", "法语"),
    @Schema(description = "西班牙语")
    SPANISH("SPANISH", "西班牙语"),
    @Schema(description = "意大利语")
    ITALIAN("ITALIAN", "意大利语"),
    @Schema(description = "德语")
    GERMAN("GERMAN", "德语"),
    @Schema(description = "俄语")
    RUSSIAN("RUSSIAN", "俄语"),
    @Schema(description = "葡萄牙语")
    PORTUGUESE("PORTUGUESE", "葡萄牙语"),
    @Schema(description = "泰语")
    THAI("THAI", "泰语"),
    @Schema(description = "阿拉伯语")
    ARABIC("ARABIC", "阿拉伯语"),
    @Schema(description = "希腊语")
    GREEK("GREEK", "希腊语"),
    @Schema(description = "丹麦语")
    DANISH("DANISH", "丹麦语"),
    @Schema(description = "传统蒙古语")
    MONGOLIAN("MONGOLIAN", "传统蒙古语"),
    @Schema(description = "瑞典语")
    SWEDISH("SWEDISH", "瑞典语"),
    @Schema(description = "芬兰语")
    FINNISH("FINNISH", "芬兰语"),
    @Schema(description = "新蒙古文")
    MONGOLIAN_NEW("MONGOLIAN_NEW", "新蒙古文"),
    @Schema(description = "波兰语")
    POLISH("POLISH", "波兰语"),
    ;

    private final String value;
    private final String desc;

}
