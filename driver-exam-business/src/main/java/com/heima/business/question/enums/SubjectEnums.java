package com.heima.business.question.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

/**
 * 科目枚举类
 */
@Getter
public enum SubjectEnums {

    ONE(1, "科目一"),
    FOUR(4, "科目四");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String name;

    SubjectEnums(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static SubjectEnums getEnumByCode(Integer code) {
        for (SubjectEnums statusEnum : SubjectEnums.values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum;
            }
        }
        return null;
    }

    public static boolean contains(String name) {
        List<String> names = Arrays.stream(SubjectEnums.values()).map(SubjectEnums::name).collect(Collectors.toList());
        return names.contains(name);
    }

    public Integer getValue() {
        return code;
    }
    
    @Override
    public String toString() {
        return code.toString();
    }
}