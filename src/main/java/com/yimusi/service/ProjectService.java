package com.yimusi.service;

import com.yimusi.dto.CreateProjectRequest;
import com.yimusi.dto.PageResult;
import com.yimusi.dto.ProjectPageRequest;
import com.yimusi.dto.ProjectResponse;
import com.yimusi.dto.UpdateProjectRequest;
import com.yimusi.entity.Project;

import java.util.List;

/**
 * 项目服务接口，定义了项目相关的业务操作。
 */
public interface ProjectService {

    /**
     * 获取所有项目列表。
     *
     * @return 包含所有项目的列表
     */
    List<Project> getAllProjects();

    /**
     * 分页查询项目列表，支持筛选。
     *
     * @param request 分页查询请求参数
     * @return 分页结果，包含项目列表及分页信息
     */
    PageResult<ProjectResponse> getProjectsPage(ProjectPageRequest request);

    /**
     * 根据项目 ID 获取项目信息。
     *
     * @param id 要查找的项目 ID
     * @return 找到的项目实体
     */
    Project getProjectById(Long id);

    /**
     * 根据项目编号获取项目信息。
     *
     * @param projectNo 项目编号
     * @return 找到的项目实体
     */
    Project getProjectByNo(String projectNo);

    /**
     * 创建一个新项目。
     *
     * @param createProjectRequest 包含新项目信息的请求体
     * @return 创建成功后的项目信息响应体
     */
    ProjectResponse createProject(CreateProjectRequest createProjectRequest);

    /**
     * 更新指定 ID 的项目信息。
     *
     * @param id                   要更新的项目 ID
     * @param updateProjectRequest 包含要更新的项目信息的请求体
     * @return 更新成功后的项目信息响应体
     */
    ProjectResponse updateProject(Long id, UpdateProjectRequest updateProjectRequest);

    /**
     * 根据 ID 删除项目。
     *
     * @param id 要删除的项目 ID
     */
    void deleteProject(Long id);

    /**
     * 恢复已软删除的项目。
     *
     * @param id 项目ID
     */
    void restoreProject(Long id);

    /**
     * 验证项目编号的唯一性。
     *
     * @param projectNo 项目编号
     * @return true 如果不存在，false 如果已存在
     */
    boolean validateProjectNoUnique(String projectNo);
}