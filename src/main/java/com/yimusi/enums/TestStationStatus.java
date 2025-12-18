package com.yimusi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 测试工位状态枚举。
 */
@Getter
@AllArgsConstructor
public enum TestStationStatus {

    ENABLED("启用"),

    DISABLED("禁用");

    private final String description;
}
