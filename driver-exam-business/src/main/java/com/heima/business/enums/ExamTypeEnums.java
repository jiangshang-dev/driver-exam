package com.heima.business.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

/**
 * 考试类型枚举 c1、c2、c3、c4
 */
@Getter
public enum ExamTypeEnums {

    C1("c1", "c1"),
    C2("c2", "c2"),
    B1("b1", "b1"),
    B2("b2", "b2"),
    A1("a1", "a1"),
    A2("a2", "a2");

    @EnumValue
    @JsonValue
    private String code;
    private String name;

    ExamTypeEnums(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static ExamTypeEnums getEnumByName(String name) {
        for (ExamTypeEnums statusEnum : ExamTypeEnums.values()) {
            if (statusEnum.name().equalsIgnoreCase(name)) {
                return statusEnum;
            }
        }
        return null;
    }

    public static boolean contains(String name) {
        List<String> names = Arrays.stream(ExamTypeEnums.values()).map(ExamTypeEnums::name).collect(Collectors.toList());
        return names.contains(name);
    }

    public String getValue() {
        return code;
    }
    
    @Override
    public String toString() {
        return code;
    }
}