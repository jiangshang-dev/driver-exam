package com.heima.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum RecordTypeEnums {
    
    WRONG(1, "错题"),
    COLLECT(0, "收藏");

    @EnumValue
    @JsonValue
    private final Integer value;
    private final String name;
    
    RecordTypeEnums(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public static RecordTypeEnums getEnumByName(String name) {
        for (RecordTypeEnums statusEnum : RecordTypeEnums.values()) {
            if (statusEnum.name().equalsIgnoreCase(name)) {
                return statusEnum;
            }
        }
        return null;
    }

    public static boolean contains(String name) {
        List<String> names = Arrays.stream(RecordTypeEnums.values()).map(RecordTypeEnums::name).collect(Collectors.toList());
        return names.contains(name);
    }

    @Override
    public String toString() {
        return value.toString();
    }

}
