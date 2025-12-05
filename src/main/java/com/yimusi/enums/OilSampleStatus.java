package com.yimusi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 油样状态枚举。
 */
@Getter
@AllArgsConstructor
public enum OilSampleStatus {

    ENABLED("启用"),

    DISABLED("禁用");

    private final String description;
}
