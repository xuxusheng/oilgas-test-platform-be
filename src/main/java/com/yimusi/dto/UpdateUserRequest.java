package com.yimusi.dto;

import com.yimusi.common.enums.UserRole;
import lombok.Data;

/**
 * 用于更新用户数据的数据传输对象 (DTO)。
 * 包含可以被更新的字段。
 */
@Data
public class UpdateUserRequest {
    /**
     * 新的用户名。
     */
    private String username;
    /**
     * 新的用户密码。
     */
    private String password;
    /**
     * 新的用户角色。
     */
    private UserRole role;
}
