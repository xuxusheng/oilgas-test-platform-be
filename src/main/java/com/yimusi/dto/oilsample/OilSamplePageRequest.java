package com.yimusi.dto.oilsample;

import com.yimusi.dto.common.PageRequest;
import com.yimusi.enums.OilSampleStatus;
import com.yimusi.enums.OilSampleUsage;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 油样分页查询请求 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OilSamplePageRequest extends PageRequest {

    /**
     * 油样编号（模糊查询）
     */
    private String sampleNo;

    /**
     * 油样名称（模糊查询）
     */
    private String sampleName;

    /**
     * 用途筛选
     */
    private OilSampleUsage usage;

    /**
     * 状态筛选
     */
    private OilSampleStatus status;

    /**
     * 油缸编号筛选
     */
    private Integer cylinderNo;
}
