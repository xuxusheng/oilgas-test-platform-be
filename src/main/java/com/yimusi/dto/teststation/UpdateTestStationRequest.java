package com.yimusi.dto.teststation;

import com.yimusi.dto.teststation.parameter.TestStationParameterRequest;
import com.yimusi.enums.TestStationUsage;
import com.yimusi.enums.ValveCommType;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 用于更新测试工位的数据传输对象 (DTO)。
 * 包含可以更新的字段。
 */
@Data
public class UpdateTestStationRequest {

    /**
     * 工位编号。
     */
    private Integer stationNo;

    /**
     * 工位名称。
     */
    @Size(max = 100, message = "工位名称长度不能超过 100 个字符")
    private String stationName;

    /**
     * 工位用途。
     */
    private TestStationUsage usage;

    /**
     * 电磁阀通信类型。
     */
    private ValveCommType valveCommType;

    /**
     * 电磁阀控制参数。
     */
    private List<TestStationParameterRequest> valveControlParams;

    /**
     * 油-阀对应关系。
     */
    private List<TestStationParameterRequest> oilValveMapping;

    /**
     * 责任人。
     */
    @Size(max = 50, message = "责任人长度不能超过 50 个字符")
    private String responsiblePerson;

    /**
     * 是否启用。
     */
    private Boolean enabled;
}
