package com.yimusi.auth;

import com.yimusi.BaseIntegrationTest;
import com.yimusi.controller.AuthController;
import com.yimusi.dto.LoginRequest;
import com.yimusi.entity.User;
import com.yimusi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 认证集成测试
 */
class AuthIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AuthController authController;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // 清空数据库并创建测试用户
        userRepository.deleteAll();

        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setRole(com.yimusi.common.enums.UserRole.MEMBER);

        userRepository.save(testUser);
    }

    @Test
    void testLoginSuccess() {
        // 给定
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        // 当
        ResponseEntity<com.yimusi.dto.LoginResponse> response = authController.login(loginRequest);

        // 那么
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("testuser", response.getBody().getUsername());
        assertNotNull(response.getBody().getAccessToken());
        assertEquals("Bearer", response.getBody().getTokenType());
    }

    @Test
    void testLoginWithWrongPassword() {
        // 给定
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrongpassword");

        // 当 & 那么
        Exception exception = assertThrows(Exception.class, () -> {
            authController.login(loginRequest);
        });

        assertTrue(exception.getMessage().contains("用户名或密码错误"));
    }

    @Test
    void testLogout() {
        // 给定 - 先登录
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");
        authController.login(loginRequest);

        // 当 - 登出
        ResponseEntity<Void> response = authController.logout();

        // 那么
        assertEquals(204, response.getStatusCode().value());
    }

    @Test
    void testPasswordHashing() {
        // 测试密码加密功能
        User user = new User();
        user.setUsername("newuser");
        user.setPassword("mypassword");
        user.setRole(com.yimusi.common.enums.UserRole.MEMBER);

        User savedUser = userRepository.save(user);

        // 那么密码应该被加密，而不是明文
        assertNotEquals("mypassword", savedUser.getPassword());
        assertTrue(savedUser.getPassword().startsWith("$2a$"));
    }
}