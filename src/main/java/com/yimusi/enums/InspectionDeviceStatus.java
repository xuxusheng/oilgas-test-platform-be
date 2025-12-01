package com.yimusi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 检测设备状态枚举
 */
@Getter
@AllArgsConstructor
public enum InspectionDeviceStatus {

    /**
     * 待检
     */
    PENDING_INSPECTION("待检"),

    /**
     * 在检
     */
    UNDER_INSPECTION("在检"),

    /**
     * 已标定
     */
    CALIBRATED("已标定"),

    /**
     * 出厂合格
     */
    FACTORY_QUALIFIED("出厂合格"),

    /**
     * 出厂不合格
     */
    FACTORY_UNQUALIFIED("出厂不合格"),

    /**
     * 返修
     */
    UNDER_REPAIR("返修"),

    /**
     * 预留一
     */
    RESERVED_ONE("预留一"),

    /**
     * 预留二
     */
    RESERVED_TWO("预留二");

    private final String description;
}
