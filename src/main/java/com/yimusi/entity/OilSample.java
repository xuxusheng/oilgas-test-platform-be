package com.yimusi.entity;

import com.yimusi.entity.base.AuditableEntity;
import com.yimusi.enums.OilSampleStatus;
import com.yimusi.enums.OilSampleUsage;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 油样实体，对应页面上的油样信息记录。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "oil_samples",
    indexes = {
        @Index(name = "idx_oil_samples_sample_no", columnList = "sample_no"),
        @Index(name = "idx_oil_samples_cylinder_no", columnList = "cylinder_no"),
        @Index(name = "idx_oil_samples_usage_status", columnList = "usage_type,status")
    }
)
public class OilSample extends AuditableEntity {

    /** 主键ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 油样编号，要求业务层保证全局唯一，用户手动输入 */
    @Column(name = "sample_no", nullable = false, length = 50)
    private String sampleNo;

    /** 油样名称，用户手动输入 */
    @Column(name = "sample_name", nullable = false, length = 100)
    private String sampleName;

    /** 油样用途（清洗/标定/出厂测试/交叉敏感性测试等枚举值） */
    @Enumerated(EnumType.STRING)
    @Column(name = "usage_type", nullable = false, length = 50)
    private OilSampleUsage usage;

    /**
     * 参数列表(JSON)，记录页面上的检测指标和值。
     * 以 [{"key": "CH4", "value": 0.1}] 格式存储，与前端 K-V 表单保持一致。
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "parameters", nullable = false, columnDefinition = "json")
    private List<OilSampleParameter> parameters = new ArrayList<>();

    /** 油缸编号，用户手动输入 */
    @Column(name = "cylinder_no", nullable = false)
    private Integer cylinderNo;

    /** 离线测试时间 */
    @Column(name = "offline_tested_at")
    private Instant offlineTestedAt;

    /** 离线测试编号 */
    @Column(name = "offline_test_no", length = 100)
    private String offlineTestNo;

    /** 油样状态枚举（启用/禁用） */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OilSampleStatus status = OilSampleStatus.ENABLED;

    /** 备注 */
    @Column(name = "remark", length = 500)
    private String remark;
}
