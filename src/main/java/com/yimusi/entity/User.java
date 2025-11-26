package com.yimusi.entity;

import com.yimusi.common.enums.UserRole;
import com.yimusi.entity.base.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * 代表用户的JPA实体。
 * 对应数据库中的 "users" 表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_username_deleted", columnList = "username, deleted", unique = true)
})
@SQLDelete(sql = "UPDATE users SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
public class User extends BaseAuditEntity {

    /**
     * 用户的唯一标识符，主键，自增生成。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户的登录名，未删除数据中必须唯一且不能为空。
     */
    @Column(nullable = false, length = 50)
    private String username;

    /**
     * 用户的加密密码，不能为空。
     */
    @Column(nullable = false)
    private String password;

    /**
     * 用户的角色，不能为空。
     * 以字符串形式存储在数据库中。
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;
}
