package com.yimusi.dto.inspection;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yimusi.enums.InspectionDeviceStatus;
import lombok.Data;

import java.time.Instant;

/**
 * 用于返回检测设备信息的数据传输对象 (DTO)。
 * 包含检测设备的完整信息。
 */
@Data
public class InspectionDeviceResponse {

    /**
     * 设备的唯一标识符。
     */
    private Long id;

    /**
     * 设备编号。
     */
    private String deviceNo;

    /**
     * 出厂编号。
     */
    private String serialNumber;

    /**
     * 装置型号。
     */
    private String deviceModel;

    /**
     * IP 地址。
     */
    private String ip;

    /**
     * 端口号。
     */
    private Integer port;

    /**
     * 所属项目ID。
     */
    private Long projectId;

    /**
     * 项目内部序号。
     */
    private Integer projectInternalNo;

    /**
     * 设备状态。
     */
    private InspectionDeviceStatus status;

    /**
     * 备注信息。
     */
    private String remark;

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
