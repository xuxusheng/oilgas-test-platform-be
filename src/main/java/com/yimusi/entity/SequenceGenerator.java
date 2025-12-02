package com.yimusi.entity;

import com.yimusi.enums.ResetStrategy;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.Data;

/**
 * 序列号生成器实体
 * 对应数据库表 sequence_generator
 */
@Data
@Entity
@Table(
    name = "sequence_generator",
    uniqueConstraints = { @UniqueConstraint(name = "uk_biz_type", columnNames = { "biz_type" }) }
)
public class SequenceGenerator {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 业务类型
     */
    @Column(name = "biz_type", nullable = false, length = 50, unique = true)
    private String bizType;

    /**
     * 重置策略
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "reset_strategy", nullable = false, length = 20)
    private ResetStrategy resetStrategy = ResetStrategy.NONE;

    /**
     * 当前序列值
     */
    @Column(name = "current_value", nullable = false)
    private Long currentValue = 0L;

    /**
     * 上次重置时间（用于判断是否需要重置）
     */
    @Column(name = "last_reset_time")
    private Instant lastResetTime;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * 实体创建前自动设置创建时间
     */
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    /**
     * 实体更新前自动设置更新时间
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
