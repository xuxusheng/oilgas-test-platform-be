package com.yimusi.dto.user;

import com.yimusi.enums.UserRole;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用于更新用户数据的数据传输对象 (DTO)。
 * 包含可以被更新的字段。
 * 注意：更新时字段为可选，因此不使用 @NotBlank 或 @NotNull，只限制格式。
 */
@Data
public class UpdateUserRequest {

    /**
     * 新的用户名。
     */
    @Size(min = 3, max = 20, message = "用户名长度必须在 3 到 20 个字符之间")
    private String username;

    /**
     * 新的用户密码。
     */
    @Size(min = 6, max = 30, message = "密码长度必须在 6 到 30 个字符之间")
    private String password;

    /**
     * 新的用户角色。
     */
    private UserRole role;
}
