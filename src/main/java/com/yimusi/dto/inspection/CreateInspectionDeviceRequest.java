package com.yimusi.dto.inspection;

import com.yimusi.enums.InspectionDeviceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用于创建新检测设备的数据传输对象 (DTO)。
 * 包含创建检测设备所必需的字段。
 */
@Data
public class CreateInspectionDeviceRequest {

    /**
     * 出厂编号，必须全局唯一。
     */
    @NotBlank(message = "出厂编号不能为空")
    @Size(max = 100, message = "出厂编号长度不能超过 100 个字符")
    private String serialNumber;

    /**
     * 装置型号。
     */
    @Size(max = 100, message = "装置型号长度不能超过 100 个字符")
    private String deviceModel;

    /**
     * IP 地址，必须全局唯一。
     */
    @NotBlank(message = "IP 地址不能为空")
    @Size(max = 50, message = "IP 地址长度不能超过 50 个字符")
    private String ip;

    /**
     * 端口号，默认102。
     */
    @NotNull(message = "端口号不能为空")
    private Integer port = 102;

    /**
     * 所属项目ID。
     */
    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    /**
     * 设备状态，默认值为"待检"。
     */
    private InspectionDeviceStatus status = InspectionDeviceStatus.PENDING_INSPECTION;

    /**
     * 备注信息。
     */
    @Size(max = 500, message = "备注长度不能超过 500 个字符")
    private String remark;
}
