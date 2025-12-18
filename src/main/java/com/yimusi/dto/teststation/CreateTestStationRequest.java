package com.yimusi.dto.teststation;

import com.yimusi.dto.teststation.parameter.TestStationParameterRequest;
import com.yimusi.enums.TestStationUsage;
import com.yimusi.enums.ValveCommType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

/**
 * 用于创建新测试工位的数据传输对象 (DTO)。
 * 包含创建测试工位所必需的字段。
 */
@Data
public class CreateTestStationRequest {

    /**
     * 工位编号，必须全局唯一。
     */
    @NotNull(message = "工位编号不能为空")
    private Integer stationNo;

    /**
     * 工位名称。
     */
    @NotBlank(message = "工位名称不能为空")
    @Size(max = 100, message = "工位名称长度不能超过 100 个字符")
    private String stationName;

    /**
     * 工位用途（厂内测试/研发测试）。
     */
    @NotNull(message = "工位用途不能为空")
    private TestStationUsage usage;

    /**
     * 电磁阀通信类型。
     */
    @NotNull(message = "电磁阀通信类型不能为空")
    private ValveCommType valveCommType;

    /**
     * 电磁阀控制参数。
     */
    @NotEmpty(message = "电磁阀控制参数不能为空")
    private List<TestStationParameterRequest> valveControlParams;

    /**
     * 油-阀对应关系。
     */
    @NotEmpty(message = "油-阀对应关系不能为空")
    private List<TestStationParameterRequest> oilValveMapping;

    /**
     * 责任人。
     */
    @NotBlank(message = "责任人不能为空")
    @Size(max = 50, message = "责任人长度不能超过 50 个字符")
    private String responsiblePerson;

    /**
     * 是否启用，默认为 true。
     */
    @NotNull(message = "启用状态不能为空")
    private Boolean enabled = true;
}
