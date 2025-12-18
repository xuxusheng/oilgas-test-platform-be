package com.yimusi.entity;

import com.yimusi.entity.base.SoftDeletableEntity;
import com.yimusi.enums.TestStationUsage;
import com.yimusi.enums.ValveCommType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

/**
 * 测试工位实体，对应数据库中的 "test_stations" 表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "test_stations",
    indexes = {
        @Index(name = "idx_test_stations_station_no", columnList = "stationNo"),
        @Index(name = "idx_test_stations_responsible_person", columnList = "responsiblePerson"),
        @Index(name = "idx_test_stations_usage_status", columnList = "usage_type,status")
    }
)
@SQLDelete(sql = "UPDATE test_stations SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
public class TestStation extends SoftDeletableEntity {

    /** 主键ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 工位编号，全局唯一，用户手动输入 */
    @Column(name = "station_no", nullable = false, unique = true)
    private Integer stationNo;

    /** 工位名称 */
    @Column(name = "station_name", nullable = false, length = 100)
    private String stationName;

    /** 工位用途枚举（厂内测试/研发测试） */
    @Enumerated(EnumType.STRING)
    @Column(name = "usage_type", nullable = false, length = 50)
    private TestStationUsage usage;

    /** 电磁阀通信类型枚举（Serial,Modbus/Tcp,Modbus） */
    @Enumerated(EnumType.STRING)
    @Column(name = "valve_comm_type", nullable = false, length = 50)
    private ValveCommType valveCommType;

    /**
     * 电磁阀控制参数(JSON)，格式：[{"key": "CH4", "value": "0.1"}]。
     * 记录电磁阀的控制参数，采用键值对形式存储。
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "valve_control_params", nullable = false, columnDefinition = "json")
    private List<TestStationParameter> valveControlParams = new ArrayList<>();

    /**
     * 油-阀对应关系(JSON)，格式：[{"key": "CH4", "value": "0.1"}]。
     * 记录油样通道与电磁阀的映射关系。
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "oil_valve_mapping", nullable = false, columnDefinition = "json")
    private List<TestStationParameter> oilValveMapping = new ArrayList<>();

    /** 责任人，用户输入 */
    @Column(name = "responsible_person", nullable = false, length = 50)
    private String responsiblePerson;

    /**
     * 工位是否启用
     * true - 启用（正常可用）
     * false - 禁用（不可使用，但数据可见）
     */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;
}
