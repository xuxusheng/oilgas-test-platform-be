package com.yimusi.dto.inspection;

import com.yimusi.enums.InspectionDeviceStatus;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用于更新检测设备的数据传输对象 (DTO)。
 * 包含可以更新的字段。
 */
@Data
public class UpdateInspectionDeviceRequest {

    /**
     * 出厂编号。
     */
    @Size(max = 100, message = "出厂编号长度不能超过 100 个字符")
    private String serialNumber;

    /**
     * 装置型号。
     */
    @Size(max = 100, message = "装置型号长度不能超过 100 个字符")
    private String deviceModel;

    /**
     * IP 地址。
     */
    @Size(max = 50, message = "IP 地址长度不能超过 50 个字符")
    private String ip;

    /**
     * 端口号。
     */
    private Integer port;

    /**
     * 设备状态。
     */
    private InspectionDeviceStatus status;

    /**
     * 备注信息。
     */
    @Size(max = 500, message = "备注长度不能超过 500 个字符")
    private String remark;
}
