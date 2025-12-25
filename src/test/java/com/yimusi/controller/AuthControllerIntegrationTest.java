package com.yimusi.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yimusi.BaseIntegrationTest;
import com.yimusi.dto.auth.FirstAdminCreateRequest;
import com.yimusi.dto.auth.LoginRequest;
import com.yimusi.dto.auth.UserRegisterRequest;
import com.yimusi.dto.user.UserResponse;
import com.yimusi.entity.User;
import com.yimusi.enums.UserRole;
import com.yimusi.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 认证控制器集成测试类
 * 验证首次部署用户创建方案的完整业务流程
 */
@AutoConfigureMockMvc
@DisplayName("认证控制器集成测试 - 首次部署方案")
public class AuthControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    /**
     * 测试前置准备 - 清空用户表
     */
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    /**
     * 测试后置清理 - 清空用户表
     */
    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("首次部署流程 - 完整的首次部署和登录流程")
    void testFullFirstDeploymentFlow() throws Exception {
        // 1. 检测系统状态 - 应该是首次部署（无用户）
        mockMvc.perform(get("/api/auth/system-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstDeployment").value(true))
                .andExpect(jsonPath("$.data.userCount").value(0))
                .andExpect(jsonPath("$.data.message").value("系统首次部署，请创建第一个管理员账户"));

        // 2. 创建第一个管理员
        FirstAdminCreateRequest createRequest = new FirstAdminCreateRequest();
        createRequest.setPassword("admin123456");
        createRequest.setConfirmPassword("admin123456");

        String response = mockMvc.perform(post("/api/auth/init-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.role").value("ADMIN"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 解析响应获取用户ID
        UserResponse userResponse = objectMapper.readValue(
                objectMapper.readTree(response).get("data").toString(),
                UserResponse.class
        );

        // 3. 再次检测系统状态 - 不再是首次部署
        mockMvc.perform(get("/api/auth/system-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstDeployment").value(false))
                .andExpect(jsonPath("$.data.userCount").value(1))
                .andExpect(jsonPath("$.data.message").value("系统已初始化"));

        // 4. 尝试再次创建 - 应该失败
        mockMvc.perform(post("/api/auth/init-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());

        // 5. 验证可以登录
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("admin123456");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.role").value("ADMIN"));
    }

    @Test
    @DisplayName("系统状态检测 - 有用户时返回非首次部署状态")
    void testSystemStatus_WithExistingUsers() throws Exception {
        // 先创建一个用户
        FirstAdminCreateRequest createRequest = new FirstAdminCreateRequest();
        createRequest.setPassword("password123");
        createRequest.setConfirmPassword("password123");

        mockMvc.perform(post("/api/auth/init-admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)));

        // 检测系统状态
        mockMvc.perform(get("/api/auth/system-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstDeployment").value(false))
                .andExpect(jsonPath("$.data.userCount").value(1));
    }

    @Test
    @DisplayName("创建第一个管理员 - 密码不匹配时应返回错误")
    void testInitAdmin_PasswordMismatch() throws Exception {
        FirstAdminCreateRequest request = new FirstAdminCreateRequest();
        request.setPassword("password123");
        request.setConfirmPassword("different456");

        mockMvc.perform(post("/api/auth/init-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("创建第一个管理员 - 密码长度不足时应返回错误")
    void testInitAdmin_PasswordTooShort() throws Exception {
        FirstAdminCreateRequest request = new FirstAdminCreateRequest();
        request.setPassword("123"); // 只有3位
        request.setConfirmPassword("123");

        mockMvc.perform(post("/api/auth/init-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("创建第一个管理员 - 密码为空时应返回错误")
    void testInitAdmin_EmptyPassword() throws Exception {
        FirstAdminCreateRequest request = new FirstAdminCreateRequest();
        request.setPassword("");
        request.setConfirmPassword("");

        mockMvc.perform(post("/api/auth/init-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("首次部署后 - 注册功能应该可用")
    void testRegisterAfterFirstDeployment() throws Exception {
        // 1. 创建第一个管理员
        FirstAdminCreateRequest adminRequest = new FirstAdminCreateRequest();
        adminRequest.setPassword("admin123456");
        adminRequest.setConfirmPassword("admin123456");

        mockMvc.perform(post("/api/auth/init-admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminRequest)));

        // 2. 尝试注册新用户
        UserRegisterRequest registerRequest = new UserRegisterRequest();
        registerRequest.setUsername("member123");
        registerRequest.setPassword("memberpass123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("member123"))
                .andExpect(jsonPath("$.data.role").value("MEMBER")); // 注册用户默认为MEMBER角色
    }

    @Test
    @DisplayName("首次部署状态 - 通过数据库直接验证")
    void testFirstDeploymentStatus_DirectDbCheck() throws Exception {
        // 验证数据库为空
        long count = userRepository.count();
        assertThat(count).isEqualTo(0);

        // 检测系统状态
        mockMvc.perform(get("/api/auth/system-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstDeployment").value(true));

        // 创建管理员
        FirstAdminCreateRequest request = new FirstAdminCreateRequest();
        request.setPassword("testpass123");
        request.setConfirmPassword("testpass123");

        mockMvc.perform(post("/api/auth/init-admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // 验证数据库有用户且角色为ADMIN
        assertThat(userRepository.count()).isEqualTo(1);
        User admin = userRepository.findByUsernameAndDeletedFalse("admin").orElseThrow();
        assertThat(admin.getRole()).isEqualTo(UserRole.ADMIN);
    }
}
