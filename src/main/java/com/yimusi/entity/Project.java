package com.yimusi.entity;

import com.yimusi.entity.base.SoftDeletableEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * 代表项目的JPA实体。
 * 对应数据库中的 "projects" 表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "projects", indexes = { @Index(name = "idx_projects_project_no", columnList = "projectNo") })
@SQLDelete(sql = "UPDATE projects SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
public class Project extends SoftDeletableEntity {

    /**
     * 项目的唯一标识符，主键，自增生成。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 项目编号，全球唯一，用户手动输入。
     * 唯一性由业务层代码控制。
     */
    @Column(name = "project_no", nullable = false, length = 50)
    private String projectNo;

    /**
     * 项目名称，可以重复。
     */
    @Column(name = "project_name", length = 200)
    private String projectName;

    /**
     * 项目负责人。
     */
    @Column(name = "project_leader", length = 100)
    private String projectLeader;

    /**
     * 备注信息。
     */
    @Column(length = 500)
    private String remark;
}
