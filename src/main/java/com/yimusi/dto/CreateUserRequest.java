package com.yimusi.dto;

import com.yimusi.common.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在 3 到 20 个字符之间")
    private String username;

    /**
     * 用户密码。
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 30, message = "密码长度必须在 6 到 30 个字符之间")
    private String password;

    /**
     * 用户角色。
     */
    @NotNull(message = "用户角色不能为空")
    private UserRole role;
}
