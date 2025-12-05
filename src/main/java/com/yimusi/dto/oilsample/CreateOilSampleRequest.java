package com.yimusi.dto.oilsample;

import com.yimusi.enums.OilSampleStatus;
import com.yimusi.enums.OilSampleUsage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * 创建油样请求 DTO
 */
@Data
public class CreateOilSampleRequest {

    @NotBlank(message = "油样编号不能为空")
    private String sampleNo;

    @NotBlank(message = "油样名称不能为空")
    private String sampleName;

    @NotNull(message = "用途不能为空")
    private OilSampleUsage usage;

    @Valid
    private List<ParameterItem> parameters;

    @NotNull(message = "油缸编号不能为空")
    private Integer cylinderNo;

    private Instant offlineTestedAt;

    private String offlineTestNo;

    @NotNull(message = "状态不能为空")
    private OilSampleStatus status;

    private String remark;

    /**
     * 内部类用于接收参数，与 Entity 解耦并支持校验
     */
    @Data
    public static class ParameterItem {
        @NotBlank(message = "参数名不能为空")
        @Pattern(regexp = "^(CH4|C2H2|C2H4|C2H6|H2|CO|CO2|H2O)$", message = "参数名必须是 CH4, C2H2, C2H4, C2H6, H2, CO, CO2, H2O 之一")
        private String key;

        @NotNull(message = "参数值不能为空")
        private BigDecimal value;
    }
}
