package com.heima.business.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/*
0-下架，1-正常，2-审核中，3-转码中
 */
@Getter
public enum TrafficStatus {
    
    DOWN(0, "下架"),
    NORMAL(1, "正常"),
    AUDITING(2, "审核中"),
    TRANSCODING(3, "转码中");
    
    private final Integer code;
    private final String message;
    
    TrafficStatus(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static TrafficStatus getEnumByCode(Integer code) {
        for (TrafficStatus statusEnum : TrafficStatus.values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum;
            }
        }
        return null;
    }

    public static boolean contains(String name) {
        List<String> names = Arrays.stream(TrafficStatus.values()).map(TrafficStatus::name).collect(Collectors.toList());
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
