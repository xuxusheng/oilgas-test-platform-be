package com.yimusi.dto.teststation.parameter;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 测试工位参数请求条目，用于创建或更新请求。
 * 格式示例：{"key": "CH4", "value": "0.1"}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestStationParameterRequest {

    /**
     * 参数名称，例如 CH4、C2H2 等通道标识
     */
    @NotBlank(message = "参数名称不能为空")
    private String key;

    /**
     * 参数值
     */
    @NotBlank(message = "参数值不能为空")
    private String value;
}
