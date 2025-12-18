package com.yimusi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 测试工位用途枚举。
 */
@Getter
@AllArgsConstructor
public enum TestStationUsage {

    INHOUSE_TEST("厂内测试"),

    RND_TEST("研发测试");

    private final String description;
}
