package com.yimusi.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 油样参数条目，存储在 JSON 字段中，等价于页面上的 K-V 记录（如 CH4:0.1）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OilSampleParameter implements Serializable {

    /** 参数名称，例如 CH4、C2H2 等检测指标 */
    private String key;

    /** 参数值，保留原始输入精度，最终序列化为 {"key":"CH4","value":0.1} */
    private BigDecimal value;
}
