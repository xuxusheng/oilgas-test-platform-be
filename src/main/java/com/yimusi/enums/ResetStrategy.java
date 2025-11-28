package com.yimusi.enums;

import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 序列号重置策略枚举
 * 定义序列号何时重置为0
 */
@Getter
public enum ResetStrategy {

    /**
     * 按日重置：每天从1开始
     */
    DAILY("按日重置", "yyyyMMdd"),

    /**
     * 按月重置：每月从1开始
     */
    MONTHLY("按月重置", "yyyyMM"),

    /**
     * 按年重置：每年从1开始
     */
    YEARLY("按年重置", "yyyy"),

    /**
     * 不重置：全局递增
     */
    NONE("不重置", "");

    private final String description;
    private final String dateFormat;

    ResetStrategy(String description, String dateFormat) {
        this.description = description;
        this.dateFormat = dateFormat;
    }

    /**
     * 判断是否需要重置序列号
     *
     * @param lastResetTime 上次重置时间
     * @return true-需要重置，false-不需要重置
     */
    public boolean needReset(Instant lastResetTime) {
        if (this == NONE || lastResetTime == null) {
            return false;
        }

        LocalDate lastResetDate = lastResetTime.atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate now = LocalDate.now(ZoneId.systemDefault());

        switch (this) {
            case DAILY:
                return !lastResetDate.equals(now);
            case MONTHLY:
                return lastResetDate.getYear() != now.getYear()
                    || lastResetDate.getMonthValue() != now.getMonthValue();
            case YEARLY:
                return lastResetDate.getYear() != now.getYear();
            default:
                return false;
        }
    }

    /**
     * 获取编号中的日期部分
     *
     * @return 日期部分字符串（如：20250128），如果不需要日期则返回空字符串
     */
    public String getDatePart() {
        if (dateFormat.isEmpty()) {
            return "";
        }
        return LocalDate.now(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(dateFormat));
    }
}