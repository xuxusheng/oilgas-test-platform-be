package com.yimusi.dto;

import com.yimusi.common.enums.UserRole;
import lombok.Data;

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
}
