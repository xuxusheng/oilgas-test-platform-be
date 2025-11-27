package com.yimusi.auth;

import cn.hutool.crypto.digest.BCrypt;
import com.yimusi.BaseIntegrationTest;
import com.yimusi.controller.AuthController;
import com.yimusi.controller.UserController;
import com.yimusi.dto.CreateUserRequest;
import com.yimusi.dto.LoginRequest;
import com.yimusi.dto.LoginResponse;
import com.yimusi.entity.User;
import com.yimusi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 认证集成测试类
 * 验证用户认证相关功能的完整业务流程，包括登录、登出、密码验证等功能
 */
@DisplayName("认证集成测试")
class AuthIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AuthController authController;

    @Autowired
    private UserController userController;

    @Autowired
    private UserRepository userRepository;

    /**
     * 测试前置准备
     * 清空用户表并创建预置测试用户，确保每个测试用例有干净的测试环境
     */
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword(BCrypt.hashpw("password123"));
        testUser.setRole(com.yimusi.common.enums.UserRole.MEMBER);

        userRepository.save(testUser);
    }

    /**
     * 测试用户登录成功场景
     * 验证使用正确的用户名和密码能够成功登录并获取认证令牌
     */
    @Test
    @DisplayName("测试用户登录成功")
    void shouldLoginSuccessfully() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        ResponseEntity<LoginResponse> response = authController.login(loginRequest);

        assertEquals(200, response.getStatusCode().value());
        LoginResponse loginResponse = response.getBody();
        assertNotNull(loginResponse);
        assertEquals("testuser", loginResponse.getUsername());
        assertNotNull(loginResponse.getAccessToken());
        assertEquals("Bearer", loginResponse.getTokenType());
    }

    /**
     * 测试使用错误密码登录的场景
     * 验证当用户提供错误密码时，系统应该拒绝登录请求并返回适当的错误信息
     */
    @Test
    @DisplayName("测试使用错误密码登录")
    void shouldRejectLoginWithWrongPassword() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrongpassword");

        Exception exception = assertThrows(Exception.class, () -> {
            authController.login(loginRequest);
        });

        assertTrue(exception.getMessage().contains("用户名或密码错误"));
    }

    /**
     * 测试用户登出功能
     * 验证已登录用户能够成功登出，系统应该返回204状态码
     */
    @Test
    @DisplayName("测试用户登出")
    void shouldLogoutSuccessfully() {
        // 先登录
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");
        authController.login(loginRequest);

        // 登出
        ResponseEntity<Void> response = authController.logout();

        assertEquals(204, response.getStatusCode().value());
    }

    /**
     * 测试用户创建时密码自动加密功能
     * 验证通过用户创建接口创建用户时，密码应该被自动加密而不是明文存储
     */
    @Test
    @DisplayName("测试用户创建时密码自动加密")
    void shouldHashPasswordWhenCreatingUser() {
        CreateUserRequest createRequest = new CreateUserRequest();
        createRequest.setUsername("newuser");
        createRequest.setPassword("mypassword");
        createRequest.setRole(com.yimusi.common.enums.UserRole.MEMBER);

        com.yimusi.common.model.ApiResponse<com.yimusi.dto.UserResponse> apiResponse = userController.createUser(createRequest);
        User savedUser = userRepository.findById(apiResponse.getData().getId()).orElseThrow();

        // 密码应该被加密，而不是明文
        assertNotEquals("mypassword", savedUser.getPassword());
        assertTrue(savedUser.getPassword().startsWith("$2a$"));
    }
}