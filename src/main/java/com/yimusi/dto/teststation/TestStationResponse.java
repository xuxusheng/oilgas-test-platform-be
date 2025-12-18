package com.yimusi.dto.teststation;

import com.yimusi.dto.teststation.parameter.TestStationParameterResponse;
import com.yimusi.enums.TestStationUsage;
import com.yimusi.enums.ValveCommType;
import lombok.Data;

import java.time.Instant;
import java.util.List;

/**
 * 用于返回测试工位信息的数据传输对象 (DTO)。
 * 包含测试工位的完整信息。
 */
@Data
public class TestStationResponse {

    /**
     * 工位的唯一标识符。
     */
    private Long id;

    /**
     * 工位编号。
     */
    private Integer stationNo;

    /**
     * 工位名称。
     */
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
    private List<TestStationParameterResponse> valveControlParams;

    /**
     * 油-阀对应关系。
     */
    private List<TestStationParameterResponse> oilValveMapping;

    /**
     * 责任人。
     */
    private String responsiblePerson;

    /**
     * 是否启用。
     */
    private Boolean enabled;

    /**
     * 创建者用户ID。
     */
    private Long createdBy;

    /**
     * 创建时间。
     */
    private Instant createdAt;

    /**
     * 更新者用户ID。
     */
    private Long updatedBy;

    /**
     * 更新时间。
     */
    private Instant updatedAt;
}
