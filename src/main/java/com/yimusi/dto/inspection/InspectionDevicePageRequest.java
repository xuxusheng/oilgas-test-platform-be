package com.yimusi.dto.inspection;

import com.yimusi.dto.common.PageRequest;
import com.yimusi.enums.InspectionDeviceStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 检测设备分页查询请求参数.
 * 继承自 PageRequest，支持分页、排序及检测设备特定的查询条件.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InspectionDevicePageRequest extends PageRequest {

    /**
     * 设备编号（模糊查询）.
     * 为空时不作为查询条件.
     */
    private String deviceNo;

    /**
     * 出厂编号（模糊查询）.
     * 为空时不作为查询条件.
     */
    private String serialNumber;

    /**
     * 装置型号（模糊查询）.
     * 为空时不作为查询条件.
     */
    private String deviceModel;

    /**
     * IP 地址（精确查询）.
     * 为空时不作为查询条件.
     */
    private String ip;

    /**
     * 所属项目ID（精确查询）.
     * 为空时不作为查询条件.
     */
    private Long projectId;

    /**
     * 设备状态（精确查询）.
     * 为空时不作为查询条件.
     */
    private InspectionDeviceStatus status;
}
