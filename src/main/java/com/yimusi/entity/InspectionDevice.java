package com.yimusi.entity;

import com.yimusi.entity.base.SoftDeletableEntity;
import com.yimusi.enums.InspectionDeviceStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * 代表检测设备的JPA实体。
 * 对应数据库中的 "inspection_devices" 表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "inspection_devices",
    indexes = {
        @Index(name = "idx_inspection_devices_device_no", columnList = "deviceNo"),
        @Index(name = "idx_inspection_devices_serial_number", columnList = "serialNumber"),
        @Index(name = "idx_inspection_devices_ip", columnList = "ip"),
        @Index(name = "idx_inspection_devices_project_id", columnList = "projectId")
    }
)
@SQLDelete(sql = "UPDATE inspection_devices SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
public class InspectionDevice extends SoftDeletableEntity {

    /**
     * 设备的唯一标识符，主键，自增生成。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 设备编号，全局唯一，添加时使用内置规则自动编码。
     */
    @Column(name = "device_no", nullable = false, length = 50)
    private String deviceNo;

    /**
     * 出厂编号，全局唯一。
     */
    @Column(name = "serial_number", nullable = false, length = 100)
    private String serialNumber;

    /**
     * 装置型号。
     */
    @Column(name = "device_model", length = 100)
    private String deviceModel;

    /**
     * IP 地址，全局唯一。
     */
    @Column(name = "ip", nullable = false, length = 50)
    private String ip;

    /**
     * 端口号，默认102。
     */
    @Column(name = "port", nullable = false)
    private Integer port = 102;

    /**
     * 所属项目ID，关联到 Project 表。
     */
    @Column(name = "project_id")
    private Long projectId;

    /**
     * 项目内部序号，表示设备在项目内的编号。
     */
    @Column(name = "project_internal_no")
    private Integer projectInternalNo;

    /**
     * 设备状态，枚举类型，默认值为"待检"。
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private InspectionDeviceStatus status = InspectionDeviceStatus.PENDING_INSPECTION;

    /**
     * 备注信息。
     */
    @Column(length = 500)
    private String remark;
}
