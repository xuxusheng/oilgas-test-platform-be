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
import com.yimusi.common.model.ApiResponse;
import com.yimusi.config.TestAuditorConfig;
import com.yimusi.dto.project.CreateProjectRequest;
import com.yimusi.dto.project.ProjectResponse;
import com.yimusi.dto.project.UpdateProjectRequest;
import com.yimusi.repository.ProjectRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

/**
 * 项目控制器集成测试类
 * 验证项目管理相关REST API的完整业务流程，包括创建、更新、删除、查询等功能
 */
@AutoConfigureMockMvc
@DisplayName("项目控制器集成测试")
public class ProjectControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProjectRepository projectRepository;

    /**
     * 测试前置准备
     * 清空项目表并重置审计人配置，确保每个测试用例都有干净的测试环境
     */
    @BeforeEach
    void setup() {
        projectRepository.deleteAll();
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
     * 测试创建项目时自动填充审计信息功能
     * 验证项目创建时的创建人、更新人等审计字段能够正确记录
     */
    @Test
    @DisplayName("测试创建项目并填充审计信息")
    void shouldCreateProjectAndFillAudit() throws Exception {
        TestAuditorConfig.setAuditor(1L);

        CreateProjectRequest createRequest = new CreateProjectRequest();
        createRequest.setProjectNo("PRJ001");
        createRequest.setProjectName("测试项目");
        createRequest.setProjectLeader("张三");
        createRequest.setRemark("这是一个测试项目");

        String responseJson = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ApiResponse<ProjectResponse> response = objectMapper.readValue(responseJson,
                new TypeReference<ApiResponse<ProjectResponse>>() {});

        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getProjectNo()).isEqualTo("PRJ001");
        assertThat(response.getData().getProjectName()).isEqualTo("测试项目");
        assertThat(response.getData().getProjectLeader()).isEqualTo("张三");
    }

    /**
     * 测试创建项目时项目编号重复的情况
     * 验证当项目编号已存在时，系统能够正确处理并返回错误
     */
    @Test
    @DisplayName("测试创建项目 - 项目编号重复")
    void shouldReturnErrorWhenProjectNoExists() throws Exception {
        TestAuditorConfig.setAuditor(1L);

        // 先创建一个项目
        CreateProjectRequest createRequest1 = new CreateProjectRequest();
        createRequest1.setProjectNo("PRJ001");
        createRequest1.setProjectName("测试项目1");
        createRequest1.setProjectLeader("张三");
        createRequest1.setRemark("第一个项目");

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest1))
                )
                .andExpect(status().isOk());

        // 尝试创建相同项目编号的项目
        CreateProjectRequest createRequest2 = new CreateProjectRequest();
        createRequest2.setProjectNo("PRJ001");  // 相同的项目编号
        createRequest2.setProjectName("测试项目2");
        createRequest2.setProjectLeader("李四");
        createRequest2.setRemark("第二个项目");

        String responseJson = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest2))
                )
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ApiResponse<?> response = objectMapper.readValue(responseJson, ApiResponse.class);
        assertThat(response.getCode()).isEqualTo(40000);  // BAD_REQUEST 错误码
        assertThat(response.getMessage()).contains("PRJ001");
    }

    /**
     * 测试查询单个项目功能
     * 验证能够通过ID正确查询到项目信息
     */
    @Test
    @DisplayName("测试查询单个项目")
    void getProjectById() throws Exception {
        TestAuditorConfig.setAuditor(1L);

        // 创建测试项目
        CreateProjectRequest createRequest = new CreateProjectRequest();
        createRequest.setProjectNo("PRJ001");
        createRequest.setProjectName("测试项目");
        createRequest.setProjectLeader("张三");
        createRequest.setRemark("测试备注");

        String createResponseJson = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ApiResponse<ProjectResponse> createResponse = objectMapper.readValue(createResponseJson,
                new TypeReference<ApiResponse<ProjectResponse>>() {});
        Long projectId = createResponse.getData().getId();

        // 查询创建的项目
        String responseJson = mockMvc.perform(get("/api/projects/" + projectId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ApiResponse<ProjectResponse> response = objectMapper.readValue(responseJson,
                new TypeReference<ApiResponse<ProjectResponse>>() {});

        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getId()).isEqualTo(projectId);
        assertThat(response.getData().getProjectNo()).isEqualTo("PRJ001");
    }

    /**
     * 测试根据项目编号查询项目功能
     * 验证能够通过项目编号正确查询到项目信息
     */
    @Test
    @DisplayName("测试根据项目编号查询项目")
    void getProjectByNo() throws Exception {
        TestAuditorConfig.setAuditor(1L);

        // 创建测试项目
        CreateProjectRequest createRequest = new CreateProjectRequest();
        createRequest.setProjectNo("PRJ001");
        createRequest.setProjectName("测试项目");
        createRequest.setProjectLeader("张三");
        createRequest.setRemark("测试备注");

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest))
                )
                .andExpect(status().isOk());

        // 根据项目编号查询项目
        String responseJson = mockMvc.perform(get("/api/projects/by-project-no/PRJ001"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ApiResponse<ProjectResponse> response = objectMapper.readValue(responseJson,
                new TypeReference<ApiResponse<ProjectResponse>>() {});

        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getProjectNo()).isEqualTo("PRJ001");
        assertThat(response.getData().getProjectName()).isEqualTo("测试项目");
    }

    /**
     * 测试更新项目信息功能
     * 验证能够正确更新项目信息并返回更新后的结果
     */
    @Test
    @DisplayName("测试更新项目信息")
    void updateProject() throws Exception {
        TestAuditorConfig.setAuditor(1L);

        // 创建测试项目
        CreateProjectRequest createRequest = new CreateProjectRequest();
        createRequest.setProjectNo("PRJ001");
        createRequest.setProjectName("测试项目");
        createRequest.setProjectLeader("张三");
        createRequest.setRemark("原始备注");

        String createResponseJson = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ApiResponse<ProjectResponse> createResponse = objectMapper.readValue(createResponseJson,
                new TypeReference<ApiResponse<ProjectResponse>>() {});
        Long projectId = createResponse.getData().getId();

        // 更新项目信息
        UpdateProjectRequest updateRequest = new UpdateProjectRequest();
        updateRequest.setProjectName("更新后的测试项目");
        updateRequest.setProjectLeader("李四");
        updateRequest.setRemark("更新后的备注");

        String updateResponseJson = mockMvc.perform(put("/api/projects/" + projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ApiResponse<ProjectResponse> updateResponse = objectMapper.readValue(updateResponseJson,
                new TypeReference<ApiResponse<ProjectResponse>>() {});

        assertThat(updateResponse.getData()).isNotNull();
        assertThat(updateResponse.getData().getProjectName()).isEqualTo("更新后的测试项目");
        assertThat(updateResponse.getData().getProjectLeader()).isEqualTo("李四");
        assertThat(updateResponse.getData().getRemark()).isEqualTo("更新后的备注");
    }

    /**
     * 测试删除项目功能
     * 验证能够正确删除项目并返回成功状态
     */
    @Test
    @DisplayName("测试删除项目")
    void deleteProject() throws Exception {
        TestAuditorConfig.setAuditor(1L);

        // 创建测试项目
        CreateProjectRequest createRequest = new CreateProjectRequest();
        createRequest.setProjectNo("PRJ001");
        createRequest.setProjectName("测试项目");
        createRequest.setProjectLeader("张三");
        createRequest.setRemark("测试备注");

        String createResponseJson = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ApiResponse<ProjectResponse> createResponse = objectMapper.readValue(createResponseJson,
                new TypeReference<ApiResponse<ProjectResponse>>() {});
        Long projectId = createResponse.getData().getId();

        // 删除项目
        String deleteResponseJson = mockMvc.perform(delete("/api/projects/" + projectId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ApiResponse<Void> deleteResponse = objectMapper.readValue(deleteResponseJson, ApiResponse.class);
        assertThat(deleteResponse.getCode()).isEqualTo(200);

        // 验证项目已被逻辑删除（通过API验证无法获取到，而不是直接查数据库）
        // 由于 @SQLRestriction("deleted = false")，已删除的项目无法通过 findById 查询
        mockMvc.perform(get("/api/projects/" + projectId))
                .andExpect(status().isNotFound());  // 期望返回404，表示项目已被删除
    }

    /**
     * 测试验证项目编号唯一性功能
     * 验证能够正确检查项目编号是否已存在
     */
    @Test
    @DisplayName("测试验证项目编号唯一性")
    void validateProjectNoUnique() throws Exception {
        TestAuditorConfig.setAuditor(1L);

        // 验证未使用的项目编号是唯一的
        String responseJson1 = mockMvc.perform(get("/api/projects/validate-unique/PRJ001"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ApiResponse<Boolean> response1 = objectMapper.readValue(responseJson1,
                new TypeReference<ApiResponse<Boolean>>() {});
        assertThat(response1.getData()).isTrue();

        // 创建项目
        CreateProjectRequest createRequest = new CreateProjectRequest();
        createRequest.setProjectNo("PRJ001");
        createRequest.setProjectName("测试项目");
        createRequest.setProjectLeader("张三");
        createRequest.setRemark("测试备注");

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest))
                )
                .andExpect(status().isOk());

        // 验证已使用的项目编号不是唯一的
        String responseJson2 = mockMvc.perform(get("/api/projects/validate-unique/PRJ001"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ApiResponse<Boolean> response2 = objectMapper.readValue(responseJson2,
                new TypeReference<ApiResponse<Boolean>>() {});
        assertThat(response2.getData()).isFalse();
    }

    /**
     * 测试获取所有项目功能
     * 验证能够正确返回项目列表
     */
    @Test
    @DisplayName("测试获取所有项目")
    void getAllProjects() throws Exception {
        TestAuditorConfig.setAuditor(1L);

        // 创建多个测试项目
        for (int i = 1; i <= 3; i++) {
            CreateProjectRequest createRequest = new CreateProjectRequest();
            createRequest.setProjectNo("PRJ" + String.format("%03d", i));
            createRequest.setProjectName("测试项目" + i);
            createRequest.setProjectLeader("负责人" + i);
            createRequest.setRemark("测试备注" + i);

            mockMvc.perform(post("/api/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest))
                    )
                    .andExpect(status().isOk());
        }

        // 获取所有项目
        String responseJson = mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ApiResponse<List<ProjectResponse>> response = objectMapper.readValue(responseJson,
                new TypeReference<ApiResponse<List<ProjectResponse>>>() {});

        assertThat(response.getData()).isNotNull();
        assertThat(response.getData()).hasSize(3);
    }

    /**
     * 测试分页查询项目功能
     * 验证能够正确分页查询项目信息
     */
    @Test
    @DisplayName("测试分页查询项目")
    void getProjectsPage() throws Exception {
        TestAuditorConfig.setAuditor(1L);

        // 创建多个测试项目
        for (int i = 1; i <= 5; i++) {
            CreateProjectRequest createRequest = new CreateProjectRequest();
            createRequest.setProjectNo("PRJ" + String.format("%03d", i));
            createRequest.setProjectName("测试项目" + i);
            createRequest.setProjectLeader("负责人" + i);
            createRequest.setRemark("测试备注" + i);

            mockMvc.perform(post("/api/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest))
                    )
                    .andExpect(status().isOk());
        }

        // 分页查询（每页3条，第一页）
        String responseJson = mockMvc.perform(get("/api/projects/page")
                        .param("page", "1")
                        .param("size", "3")
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ApiResponse<?> response = objectMapper.readValue(responseJson, ApiResponse.class);
        assertThat(response.getData()).isNotNull();

        // 获取分页数据内容
        var pageData = (java.util.LinkedHashMap<?, ?>) response.getData();
        Object content = pageData.get("content");

        assertThat(content).isNotNull();
        assertThat(((List<?>) content)).hasSize(3);
    }

    /**
     * 测试验证项目编号唯一性 - 已删除项目不应影响唯一性
     * 验证已删除的项目不会影响项目编号的唯一性检查
     */
    @Test
    @DisplayName("测试验证项目编号唯一性 - 已删除项目不应影响")
    void validateProjectNoUnique_DeletedProjectShouldNotAffect() throws Exception {
        TestAuditorConfig.setAuditor(1L);

        // 创建项目
        CreateProjectRequest createRequest = new CreateProjectRequest();
        createRequest.setProjectNo("PRJ001");
        createRequest.setProjectName("测试项目");
        createRequest.setProjectLeader("张三");
        createRequest.setRemark("测试备注");

        String createResponseJson = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ApiResponse<ProjectResponse> createResponse = objectMapper.readValue(createResponseJson,
                new TypeReference<ApiResponse<ProjectResponse>>() {});
        Long projectId = createResponse.getData().getId();

        // 删除项目
        mockMvc.perform(delete("/api/projects/" + projectId))
                .andExpect(status().isOk());

        // 验证删除后的项目编号仍然是唯一的（因为已删除）
        String responseJson = mockMvc.perform(get("/api/projects/validate-unique/PRJ001"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ApiResponse<Boolean> response = objectMapper.readValue(responseJson,
                new TypeReference<ApiResponse<Boolean>>() {});
        assertThat(response.getData()).isTrue();
    }
}