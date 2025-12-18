package com.yimusi.dto.teststation.parameter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 测试工位参数响应条目，用于返回给前端。
 * 格式示例：{"key": "CH4", "value": "0.1"}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestStationParameterResponse {

    /**
     * 参数名称，例如 CH4、C2H2 等通道标识
     */
    private String key;

    /**
     * 参数值
     */
    private String value;
}
