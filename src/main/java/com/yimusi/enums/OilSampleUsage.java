package com.yimusi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 油样用途枚举。
 */
@Getter
@AllArgsConstructor
public enum OilSampleUsage {

    CLEANING("清洗"),

    CALIBRATION("标定"),

    FACTORY_TEST("出厂测试"),

    CROSS_SENSITIVITY_TEST("交叉敏感性测试");

    private final String description;
}
