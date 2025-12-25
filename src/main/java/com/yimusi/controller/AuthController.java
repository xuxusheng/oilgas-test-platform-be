package com.yimusi.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.yimusi.common.model.ApiResponse;
import com.yimusi.dto.auth.FirstAdminCreateRequest;
import com.yimusi.dto.auth.LoginRequest;
import com.yimusi.dto.auth.LoginResponse;
import com.yimusi.dto.auth.SystemStatusResponse;
import com.yimusi.dto.user.UserResponse;
import com.yimusi.entity.User;
import com.yimusi.mapper.UserMapper;
import com.yimusi.repository.UserRepository;
import com.yimusi.service.UserService;
import jakarta.validation.Valid;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证管理
 * 提供用户登录、注册和当前用户信息查询功能
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    /**
     * 用户登录
     *
     * @param loginRequest 登录请求
     * @return 登录响应，包含token信息
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        // 验证用户
        User user = userService.validateUser(loginRequest.getUsername(), loginRequest.getPassword());

        // 使用 Sa-Token 登录 - 每次登录生成新的JWT token
        // 在纯JWT模式下，登录ID不同会生成不同的token
        StpUtil.login(user.getId());

        // 获取 token 信息
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();

        // 构建响应
        Instant now = Instant.now();
        LoginResponse response = LoginResponse.builder()
            .accessToken(tokenInfo.tokenValue)
            .tokenType("Bearer")
            .expiresIn(tokenInfo.tokenTimeout)
            .expiresAt(now.plusSeconds(tokenInfo.tokenTimeout))
            .userId(user.getId())
            .username(user.getUsername())
            .role(user.getRole())
            .loginTime(now)
            .build();

        return ApiResponse.success(response);
    }

    /**
     * 用户注册
     *
     * @param registerRequest 注册请求
     * @return 注册成功的用户信息
     */
    @PostMapping("/register")
    public ApiResponse<UserResponse> register(
        @Valid @RequestBody com.yimusi.dto.auth.UserRegisterRequest registerRequest
    ) {
        UserResponse userResponse = userService.register(registerRequest);
        return ApiResponse.success(userResponse);
    }

    /**
     * 获取当前用户信息
     *
     * @return 当前登录用户的信息
     */
    @GetMapping("/me")
    public ApiResponse<UserResponse> getCurrentUser() {
        Long userId = Long.parseLong(StpUtil.getLoginIdAsString());
        User user = userService.getUserById(userId);
        return ApiResponse.success(userMapper.toResponse(user));
    }

    /**
     * 检测系统状态（无需认证）
     * 用于前端判断是否需要引导用户创建第一个管理员
     *
     * GET /api/auth/system-status
     */
    @GetMapping("/system-status")
    public ApiResponse<SystemStatusResponse> checkSystemStatus() {
        log.debug("检测系统部署状态...");

        boolean isFirstDeployment = userService.isSystemFirstDeployment();
        long userCount = userRepository.count();

        SystemStatusResponse response = SystemStatusResponse.builder()
            .firstDeployment(isFirstDeployment)
            .userCount(userCount)
            .message(isFirstDeployment
                ? "系统首次部署，请创建第一个管理员账户"
                : "系统已初始化")
            .build();

        log.info("系统状态: 首次部署={}, 用户数量={}", isFirstDeployment, userCount);
        return ApiResponse.success(response);
    }

    /**
     * 创建第一个管理员用户（无需认证，仅在首次部署时可用）
     * 用户名固定为 "admin"
     *
     * POST /api/auth/init-admin
     *
     * 请求示例:
     * {
     *   "password": "SecurePass123",
     *   "confirmPassword": "SecurePass123"
     * }
     */
    @PostMapping("/init-admin")
    public ApiResponse<UserResponse> initFirstAdmin(
        @Valid @RequestBody FirstAdminCreateRequest request
    ) {
        log.info("收到创建第一个管理员请求");

        UserResponse userResponse = userService.createFirstAdmin(request);

        log.info("第一个管理员创建成功: username={}, id={}",
            userResponse.getUsername(), userResponse.getId());

        return ApiResponse.success(userResponse);
    }
}
