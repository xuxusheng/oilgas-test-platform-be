package com.yimusi.entity;

import com.yimusi.common.enums.UserRole;
import jakarta.persistence.*;
import lombok.Data;

/**
 * 代表用户的JPA实体。
 * 对应数据库中的 "users" 表。
 */
@Data
@Entity
@Table(name = "users")
public class User {

    /**
     * 用户的唯一标识符，主键，自增生成。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户的登录名，必须唯一且不能为空。
     */
    @Column(unique = true, nullable = false, length = 50)
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
