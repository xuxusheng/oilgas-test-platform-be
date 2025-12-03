package com.yimusi.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yimusi.BaseIntegrationTest;
import com.yimusi.common.enums.UserRole;
import com.yimusi.common.model.ApiResponse;
import com.yimusi.config.TestAuditorConfig;
import com.yimusi.dto.user.CreateUserRequest;
import com.yimusi.dto.user.UpdateUserRequest;
import com.yimusi.dto.user.UserResponse;
import com.yimusi.entity.User;
import com.yimusi.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 用户控制器集成测试类
 * 验证用户管理相关REST API的完整业务流程，包括创建、更新、删除、查询等功能
 *
 * @author yimusi团队
 * @date 2025-11-25
 */
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.yml")
@DisplayName("用户控制器集成测试")
public class UserControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    /**
     * 测试前置准备
     * 清空用户表并重置审计人配置，确保每个测试用例都有干净的测试环境
     */
    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        TestAuditorConfig.clearAuditor();
    }

    /**
     * 测试后置清理
     * 清理审计人配置，防止测试间的数据污染
     */
    @AfterEach
    void cleanAuditor() {
        TestAuditorConfig.clearAuditor();
    }

    /**
     * 测试创建用户时自动填充审计信息功能
     * 验证用户创建时的创建人、更新人等审计字段能够正确记录
     */
    @Test
    @DisplayName("测试创建用户并填充审计信息")
    void shouldCreateUserAndFillAudit() throws Exception {
        TestAuditorConfig.setAuditor("creator");

        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("alice");
        request.setPassword("password");
        request.setRole(UserRole.ADMIN);

        String response = mockMvc
            .perform(
                post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        ApiResponse<UserResponse> apiResponse =
            objectMapper.readValue(response, new TypeReference<>() {});
        User saved = userRepository.findById(apiResponse.getData().getId()).orElseThrow();

        assertThat(saved.getCreatedBy()).isEqualTo("creator");
        assertThat(saved.getDeleted()).isFalse();
    }

    /**
     * 测试更新用户时刷新审计信息功能
     * 验证用户更新时的更新人等审计字段能够正确记录
     */
    @Test
    @DisplayName("测试更新用户并刷新审计信息")
    void shouldUpdateUserAndRefreshAuditor() throws Exception {
        TestAuditorConfig.setAuditor("updater");
        User user = seedUser("bob", UserRole.MEMBER);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername("bob-new");
        request.setRole(UserRole.ADMIN);

        mockMvc
            .perform(
                put("/api/users/" + user.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isOk());

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updated.getUpdatedBy()).isEqualTo("updater");
        assertThat(updated.getUsername()).isEqualTo("bob-new");
    }

    /**
     * 测试用户软删除和恢复功能
     * 验证用户删除后不可查询，且恢复操作返回未找到的状态
     */
    @Test
    @DisplayName("测试用户软删除和恢复")
    void shouldSoftDeleteAndRestoreUser() throws Exception {
        TestAuditorConfig.setAuditor("operator");
        User user = seedUser("charlie", UserRole.MEMBER);

        mockMvc.perform(delete("/api/users/" + user.getId())).andExpect(status().isOk());

        assertThat(userRepository.findById(user.getId())).isEmpty();

        mockMvc.perform(post("/api/users/" + user.getId() + "/restore")).andExpect(status().isNotFound());
    }

    /**
     * 测试拒绝重复用户名功能
     * 验证当用户名已存在且处于激活状态时，系统应该拒绝创建重复用户
     */
    @Test
    @DisplayName("测试拒绝重复用户名")
    void shouldRejectDuplicateUsernameWhenActive() throws Exception {
        TestAuditorConfig.setAuditor("creator");
        seedUser("daisy", UserRole.ADMIN);

        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("daisy");
        request.setPassword("pwd");
        request.setRole(UserRole.ADMIN);

        mockMvc
            .perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    /**
     * 测试只列出激活用户功能
     * 验证用户查询接口只返回激活状态的用户，已删除的用户不应该出现在结果中
     */
    @Test
    @DisplayName("测试只查询激活用户")
    void shouldListOnlyActiveUsers() throws Exception {
        TestAuditorConfig.setAuditor("creator");
        seedUser("eve", UserRole.MEMBER);
        User deleted = seedUser("frank", UserRole.MEMBER);
        userRepository.deleteById(deleted.getId());

        String response = mockMvc.perform(get("/api/users")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        ApiResponse<List<UserResponse>> apiResponse =
            objectMapper.readValue(response, new TypeReference<>() {});

        assertThat(apiResponse.getData()).hasSize(1);
        assertThat(apiResponse.getData().getFirst().getUsername()).isEqualTo("eve");
    }

    /**
     * 测试工具方法：创建测试用户
     *
     * @param username 用户名
     * @param role     用户角色
     * @return 创建的用户实体
     */
    private User seedUser(String username, UserRole role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("pwd");
        user.setRole(role);
        return userRepository.save(user);
    }
}
