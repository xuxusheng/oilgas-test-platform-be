package com.yimusi.dto.oilsample;

import com.yimusi.entity.OilSampleParameter;
import com.yimusi.enums.OilSampleStatus;
import com.yimusi.enums.OilSampleUsage;
import lombok.Data;

import java.time.Instant;
import java.util.List;

/**
 * 油样响应 DTO
 */
@Data
public class OilSampleResponse {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 油样编号
     */
    private String sampleNo;

    /**
     * 油样名称
     */
    private String sampleName;

    /**
     * 油样用途
     */
    private OilSampleUsage usage;

    /**
     * 参数列表
     */
    private List<OilSampleParameter> parameters;

    /**
     * 油缸编号
     */
    private Integer cylinderNo;

    /**
     * 离线测试时间
     */
    private Instant offlineTestedAt;

    /**
     * 离线测试编号
     */
    private String offlineTestNo;

    /**
     * 油样状态
     */
    private OilSampleStatus status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Instant createdAt;

    /**
     * 更新时间
     */
    private Instant updatedAt;
}
