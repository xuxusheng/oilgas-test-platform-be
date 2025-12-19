package com.yimusi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.yimusi.common.exception.BadRequestException;
import com.yimusi.common.exception.ResourceNotFoundException;
import com.yimusi.common.util.OperatorUtil;
import com.yimusi.dto.common.PageResult;
import com.yimusi.dto.project.CreateProjectRequest;
import com.yimusi.dto.project.ProjectPageRequest;
import com.yimusi.dto.project.ProjectResponse;
import com.yimusi.dto.project.UpdateProjectRequest;
import com.yimusi.entity.Project;
import com.yimusi.entity.QProject;
import com.yimusi.mapper.ProjectMapper;
import com.yimusi.repository.ProjectRepository;
import com.yimusi.service.ProjectService;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 项目服务实现类，处理所有与项目相关的业务逻辑。
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public Project getProjectById(Long id) {
        if (id == null) {
            throw new BadRequestException("项目 ID 不能为空");
        }

        Project project = projectRepository.findById(id).orElse(null);
        if (project == null) {
            throw new ResourceNotFoundException(String.format("ID 为 %s 的项目不存在", id));
        }
        return project;
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public Project getProjectByNo(String projectNo) {
        if (projectNo == null) {
            throw new BadRequestException("项目编号不能为空");
        }
        Project project = projectRepository.findByProjectNoAndDeletedFalse(projectNo).orElse(null);
        if (project == null) {
            throw new ResourceNotFoundException(String.format("项目编号 %s 不存在", projectNo));
        }
        return project;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageResult<ProjectResponse> getProjectsPage(ProjectPageRequest request) {
        // 构建 QueryDSL 查询条件
        Predicate predicate = buildProjectPredicate(request);

        // 执行分页查询（自动处理页码转换和排序）
        Page<Project> projectPage = projectRepository.findAll(predicate, request.toJpaPageRequest());

        // 转换并返回结果
        return PageResult.from(projectPage.map(projectMapper::toResponse));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProjectResponse createProject(CreateProjectRequest createProjectRequest) {
        // 验证项目编号唯一性
        if (!validateProjectNoUnique(createProjectRequest.getProjectNo())) {
            throw new BadRequestException(String.format("项目编号 %s 已存在", createProjectRequest.getProjectNo()));
        }

        Project project = projectMapper.toEntity(createProjectRequest);
        Project savedProject = projectRepository.save(project);
        return projectMapper.toResponse(savedProject);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProjectResponse updateProject(Long id, UpdateProjectRequest updateProjectRequest) {
        if (id == null) {
            throw new BadRequestException("项目 ID 不能为空");
        }

        Project project = getProjectById(id);

        // 项目编号不可修改，所以不需要验证唯一性
        projectMapper.updateEntityFromRequest(updateProjectRequest, project);

        Project savedProject = projectRepository.save(project);
        ProjectResponse response = projectMapper.toResponse(savedProject);
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteProject(Long id) {
        if (id == null) {
            throw new BadRequestException("项目 ID 不能为空");
        }

        Project project = getProjectById(id);
        markDeleted(project);
        projectRepository.save(project);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validateProjectNoUnique(String projectNo) {
        if (projectNo == null) {
            return true;
        }
        // 返回是否唯一（不存在则唯一）
        return !projectRepository.existsByProjectNoAndDeletedFalse(projectNo);
    }

    private void markDeleted(Project project) {
        project.setDeleted(true);
        project.setDeletedAt(Instant.now());
        project.setDeletedBy(OperatorUtil.getOperator());
    }

    /**
     * 使用 QueryDSL 构建项目查询条件.
     *
     * @param request 分页查询请求
     * @return Predicate 查询条件
     */
    @NonNull
    private Predicate buildProjectPredicate(ProjectPageRequest request) {
        QProject qProject = QProject.project;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qProject.deleted.isFalse());

        // 项目编号模糊查询
        if (StrUtil.isNotBlank(request.getProjectNo())) {
            builder.and(qProject.projectNo.containsIgnoreCase(request.getProjectNo()));
        }

        // 项目名称模糊查询
        if (StrUtil.isNotBlank(request.getProjectName())) {
            builder.and(qProject.projectName.containsIgnoreCase(request.getProjectName()));
        }

        // 项目负责人模糊查询
        if (StrUtil.isNotBlank(request.getProjectLeader())) {
            builder.and(qProject.projectLeader.containsIgnoreCase(request.getProjectLeader()));
        }

        return builder;
    }
}
