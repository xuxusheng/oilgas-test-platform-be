package com.yimusi.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.yimusi.dto.auth.LoginRequest;
import com.yimusi.dto.auth.LoginResponse;
import com.yimusi.dto.user.UserResponse;
import com.yimusi.entity.User;
import com.yimusi.mapper.UserMapper;
import com.yimusi.service.UserService;
import jakarta.validation.Valid;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器，处理用户登录、登出等认证相关操作
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final UserMapper userMapper;

    /**
     * 用户登录
     *
     * @param loginRequest 登录请求
     * @return 登录响应，包含token信息
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        // 验证用户
        User user = userService.validateUser(loginRequest.getUsername(), loginRequest.getPassword());

        // 使用 Sa-Token 登录
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

        return ResponseEntity.ok(response);
    }

    /**
     * 用户注册
     *
     * @param registerRequest 注册请求
     * @return 注册成功的用户信息
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
        @Valid @RequestBody com.yimusi.dto.auth.UserRegisterRequest registerRequest
    ) {
        UserResponse userResponse = userService.register(registerRequest);
        return ResponseEntity.ok(userResponse);
    }

    /**
     * 用户登出
     *
     * @return 响应结果
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        StpUtil.logout();
        return ResponseEntity.noContent().build();
    }

    /**
     * 获取当前用户信息
     *
     * @return 当前登录用户的信息
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        Long userId = Long.parseLong(StpUtil.getLoginIdAsString());
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(userMapper.toResponse(user));
    }
}
