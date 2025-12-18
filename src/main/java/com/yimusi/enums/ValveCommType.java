package com.yimusi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 电磁阀通信类型枚举。
 */
@Getter
@AllArgsConstructor
public enum ValveCommType {

    SERIAL_MODBUS("Serial,Modbus"),

    TCP_MODBUS("Tcp,Modbus");

    private final String description;
}
