package com.yimusi.entity;

import cn.hutool.crypto.digest.BCrypt;
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
@Table(name = "users", indexes = { @Index(name = "idx_users_username", columnList = "username") })
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
     * 用户的登录名，在未删除的用户中必须唯一，不能为空。
     * 唯一性由业务层代码保证。
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

    /**
     * 验证密码是否正确
     * @param plainPassword 明文密码
     * @return 密码是否匹配
     */
    public boolean verifyPassword(String plainPassword) {
        return BCrypt.checkpw(plainPassword, this.password);
    }
}
