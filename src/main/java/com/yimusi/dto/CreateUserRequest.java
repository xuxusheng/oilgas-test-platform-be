package com.yimusi.dto;

import com.yimusi.common.enums.UserRole;
import lombok.Data;

/**
 * 用于创建新用户的数据传输对象 (DTO)。
 * 包含创建用户所必需的字段。
 */
@Data
public class CreateUserRequest {
    /**
     * 用户名，必须唯一。
     */
    private String username;
    /**
     * 用户密码。
     */
    private String password;
    /**
     * 用户角色。
     */
    private UserRole role;
}
