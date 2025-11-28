package com.yimusi.controller;

import com.yimusi.common.model.ApiResponse;
import com.yimusi.dto.CreateProjectRequest;
import com.yimusi.dto.PageResult;
import com.yimusi.dto.ProjectPageRequest;
import com.yimusi.dto.ProjectResponse;
import com.yimusi.dto.UpdateProjectRequest;
import com.yimusi.mapper.ProjectMapper;
import com.yimusi.service.ProjectService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final ProjectMapper projectMapper;

    /**
     * 获取所有项目并映射为响应 DTO。
     *
     * @return {@link ProjectResponse} 列表
     */
    @GetMapping
    public ApiResponse<List<ProjectResponse>> getAllProjects() {
        List<ProjectResponse> responses = projectService
            .getAllProjects()
            .stream()
            .map(projectMapper::toResponse)
            .toList();
        return ApiResponse.success(responses);
    }

    /**
     * 分页查询项目列表，支持筛选。
     *
     * @param request 分页查询请求参数
     * @return 分页结果，包含项目列表及分页信息
     */
    @GetMapping("/page")
    public ApiResponse<PageResult<ProjectResponse>> getProjectsPage(@Valid ProjectPageRequest request) {
        PageResult<ProjectResponse> pageResult = projectService.getProjectsPage(request);
        return ApiResponse.success(pageResult);
    }

    /**
     * 根据 ID 查询单个项目。
     *
     * @param id 项目 ID
     * @return 查询到的 {@link ProjectResponse}
     */
    @GetMapping("/{id}")
    public ApiResponse<ProjectResponse> getProjectById(@PathVariable Long id) {
        ProjectResponse response = projectMapper.toResponse(projectService.getProjectById(id));
        return ApiResponse.success(response);
    }

    /**
     * 根据项目编号查询单个项目。
     *
     * @param projectNo 项目编号
     * @return 查询到的 {@link ProjectResponse}
     */
    @GetMapping("/by-project-no/{projectNo}")
    public ApiResponse<ProjectResponse> getProjectByNo(@PathVariable String projectNo) {
        ProjectResponse response = projectMapper.toResponse(projectService.getProjectByNo(projectNo));
        return ApiResponse.success(response);
    }

    /**
     * 根据请求体创建新项目。
     *
     * @param createProjectRequest 包含项目信息的请求体
     * @return 新增的 {@link ProjectResponse}
     */
    @PostMapping
    public ApiResponse<ProjectResponse> createProject(@Valid @RequestBody CreateProjectRequest createProjectRequest) {
        ProjectResponse projectResponse = projectService.createProject(createProjectRequest);
        return ApiResponse.success(projectResponse);
    }

    /**
     * 使用提交的数据更新已有项目。
     *
     * @param id                 需要更新的项目 ID
     * @param updateProjectRequest 更新字段的请求体
     * @return 更新后的 {@link ProjectResponse}
     */
    @PutMapping("/{id}")
    public ApiResponse<ProjectResponse> updateProject(
        @PathVariable Long id,
        @Valid @RequestBody UpdateProjectRequest updateProjectRequest
    ) {
        ProjectResponse updated = projectService.updateProject(id, updateProjectRequest);
        return ApiResponse.success(updated);
    }

    /**
     * 根据 ID 删除项目，成功后返回 200。
     *
     * @param id 待删除的项目 ID
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ApiResponse.success();
    }

    /**
     * 恢复已软删除的项目。
     *
     * @param id 项目 ID
     */
    @PostMapping("/{id}/restore")
    public ApiResponse<Void> restoreProject(@PathVariable Long id) {
        projectService.restoreProject(id);
        return ApiResponse.success();
    }

    /**
     * 验证项目编号的唯一性。
     *
     * @param projectNo 项目编号
     * @return 是否唯一
     */
    @GetMapping("/validate-unique/{projectNo}")
    public ApiResponse<Boolean> validateProjectNoUnique(@PathVariable String projectNo) {
        boolean isUnique = projectService.validateProjectNoUnique(projectNo);
        return ApiResponse.success(isUnique);
    }
}
