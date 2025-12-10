package com.yimusi.dto.user;

import com.yimusi.enums.UserRole;

import lombok.Data;

import java.time.Instant;

/**
 * 用于返回用户公开信息的数据传输对象 (DTO)。
 * 不包含密码等敏感信息。
 */
@Data
public class UserResponse {

    /**
     * 用户的唯一标识符。
     */
    private Long id;

    /**
     * 用户的登录名。
     */
    private String username;

    /**
     * 用户的角色。
     */
    private UserRole role;

    /**
     * 创建时间。
     */
    private Instant createdAt;

    /**
     * 最后更新时间。
     */
    private Instant updatedAt;

    /**
     * 创建者用户ID。
     */
    private Long createdBy;

    /**
     * 最后更新者用户ID。
     */
    private Long updatedBy;
}
