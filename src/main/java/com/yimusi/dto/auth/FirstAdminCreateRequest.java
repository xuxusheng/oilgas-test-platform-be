package com.yimusi.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 首次管理员创建请求DTO
 * 用户名固定为"admin"，由后端自动设置
 */
@Data
public class FirstAdminCreateRequest {

    /**
     * 密码（6-30字符）
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 30, message = "密码长度必须在 6 到 30 个字符之间")
    private String password;

    /**
     * 确认密码
     */
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
}
