package com.yimusi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 测试工位参数条目，存储在JSON字段中，用于电磁阀控制参数和油-阀对应关系。
 * 格式示例：{"key": "CH4", "value": "0.1"}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestStationParameter implements Serializable {

    /** 参数名称，例如 CH4、C2H2 等通道标识 */
    private String key;

    /** 参数值，使用字符串存储以保持灵活性 */
    private String value;
}
