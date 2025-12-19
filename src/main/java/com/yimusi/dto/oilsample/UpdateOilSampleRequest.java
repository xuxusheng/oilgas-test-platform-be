package com.yimusi.dto.oilsample;

import com.yimusi.enums.OilSampleUsage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 更新油样请求 DTO
 */
@Data
public class UpdateOilSampleRequest {

    @NotBlank(message = "油样编号不能为空")
    private String sampleNo;

    @NotBlank(message = "油样名称不能为空")
    private String sampleName;

    @NotNull(message = "用途不能为空")
    private OilSampleUsage usage;

    @NotNull(message = "参数列表不能为空")
    @Valid
    private List<CreateOilSampleRequest.ParameterItem> parameters = new ArrayList<>();

    @NotNull(message = "油缸编号不能为空")
    private Integer cylinderNo;

    private Instant offlineTestedAt;

    private String offlineTestNo;

    @NotNull(message = "启用状态不能为空")
    private Boolean enabled = true;

    private String remark;
}
