package com.yimusi.dto.teststation;

import com.yimusi.dto.common.PageRequest;
import com.yimusi.enums.TestStationUsage;
import com.yimusi.enums.ValveCommType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 测试工位分页查询请求参数.
 * 继承自 PageRequest，支持分页、排序及测试工位特定的查询条件.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TestStationPageRequest extends PageRequest {

    /**
     * 工位编号（精确查询）.
     * 为空时不作为查询条件.
     */
    private Integer stationNo;

    /**
     * 工位名称（模糊查询）.
     * 为空时不作为查询条件.
     */
    private String stationName;

    /**
     * 工位用途（精确查询）.
     * 为空时不作为查询条件.
     */
    private TestStationUsage usage;

    /**
     * 电磁阀通信类型（精确查询）.
     * 为空时不作为查询条件.
     */
    private ValveCommType valveCommType;

    /**
     * 责任人（模糊查询）.
     * 为空时不作为查询条件.
     */
    private String responsiblePerson;

    /**
     * 是否启用（精确查询）.
     * 为空时不作为查询条件.
     */
    private Boolean enabled;
}
