package com.yimusi.dto.auth;

import com.yimusi.common.enums.UserRole;
import java.time.Instant;
import lombok.Builder;
import lombok.Data;

/**
 * 用户登录响应DTO
 */
@Data
@Builder
public class LoginResponse {

    /**
     * 访问token
     */
    private String accessToken;

    /**
     * token类型
     */
    private String tokenType;

    /**
     * 令牌剩余有效期（秒）
     */
    private Long expiresIn;

    /**
     * 令牌绝对过期时间
     */
    private Instant expiresAt;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户角色
     */
    private UserRole role;

    /**
     * 登录时间
     */
    private Instant loginTime;
}
