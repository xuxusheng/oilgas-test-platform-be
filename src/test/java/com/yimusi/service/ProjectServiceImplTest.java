package com.yimusi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.yimusi.common.exception.BadRequestException;
import com.yimusi.common.exception.ResourceNotFoundException;
import com.yimusi.dto.common.PageResult;
import com.yimusi.dto.project.CreateProjectRequest;
import com.yimusi.dto.project.ProjectPageRequest;
import com.yimusi.dto.project.ProjectResponse;
import com.yimusi.dto.project.UpdateProjectRequest;
import com.yimusi.entity.Project;
import com.yimusi.mapper.ProjectMapper;
import com.yimusi.repository.ProjectRepository;
import com.yimusi.service.impl.ProjectServiceImpl;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

/**
 * 项目业务逻辑服务单元测试类
 *
 * 测试项目服务的各个功能，包括创建、查询、更新、删除等业务逻辑。
 */
@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private EntityManager entityManager;

    @Spy
    private ProjectMapper projectMapper = Mappers.getMapper(ProjectMapper.class);

    @InjectMocks
    private ProjectServiceImpl projectService;

    private CreateProjectRequest validCreateRequest;
    private Project project;

    @BeforeEach
    void setUp() {
        validCreateRequest = new CreateProjectRequest();
        validCreateRequest.setProjectNo("PRJ001");
        validCreateRequest.setProjectName("测试项目");
        validCreateRequest.setProjectLeader("张三");
        validCreateRequest.setRemark("这是一个测试项目");

        project = new Project();
        project.setId(1L);
        project.setProjectNo("PRJ001");
        project.setProjectName("测试项目");
        project.setProjectLeader("张三");
        project.setRemark("这是一个测试项目");
        project.setDeleted(false);
        project.setCreatedBy(1L);
        project.setCreatedAt(Instant.now());
    }

    @Test
    @DisplayName("项目创建 - 成功创建项目并返回响应")
    void createProject_ShouldReturnProjectResponse() {
        // Arrange - 设置测试环境：模拟项目编号不存在，准备保存项目
        when(projectRepository.existsByProjectNoAndDeletedFalse("PRJ001")).thenReturn(false);
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        // Act - 执行项目创建操作
        ProjectResponse response = projectService.createProject(validCreateRequest);

        // Assert - 验证项目创建成功，返回正确的项目响应
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("PRJ001", response.getProjectNo());
        assertEquals("测试项目", response.getProjectName());
        verify(projectRepository).existsByProjectNoAndDeletedFalse("PRJ001");
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    @DisplayName("项目创建 - 使用重复项目编号时应抛出异常")
    void createProject_WithDuplicateProjectNo_ShouldThrowException() {
        // Arrange - 设置测试环境：模拟项目编号已存在
        when(projectRepository.existsByProjectNoAndDeletedFalse("PRJ001")).thenReturn(true);

        // Act & Assert - 执行创建操作并验证抛出BadRequestException，且从未调用保存方法
        assertThrows(BadRequestException.class, () -> projectService.createProject(validCreateRequest));
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    @DisplayName("项目查询 - 根据有效ID查询项目")
    void getProjectById_ShouldReturnProject() {
        // Arrange - 设置测试环境：模拟根据ID查询项目
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        // Act - 执行项目查询操作
        Project result = projectService.getProjectById(1L);

        // Assert - 验证查询返回的项目信息正确
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("PRJ001", result.getProjectNo());
    }

    @Test
    @DisplayName("项目查询 - 查询所有项目列表")
    void getAllProjects_ShouldReturnProjectList() {
        // Arrange - 设置测试环境：模拟查询所有项目
        List<Project> projects = List.of(project);
        when(projectRepository.findAll()).thenReturn(projects);

        // Act - 执行查询所有项目操作
        List<Project> result = projectService.getAllProjects();

        // Assert - 验证查询结果包含正确数量的项目
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(projectRepository).findAll();
    }

    @Test
    @DisplayName("项目查询 - 根据项目编号查询项目")
    void getProjectByNo_ShouldReturnProject() {
        // Arrange - 设置测试环境：模拟根据项目编号查询项目
        when(projectRepository.findByProjectNoAndDeletedFalse("PRJ001")).thenReturn(Optional.of(project));

        // Act - 执行根据项目编号查询操作
        Project result = projectService.getProjectByNo("PRJ001");

        // Assert - 验证查询返回的项目信息正确
        assertNotNull(result);
        assertEquals("PRJ001", result.getProjectNo());
        assertEquals("测试项目", result.getProjectName());
    }

    @Test
    @DisplayName("项目更新 - 成功更新项目信息并返回响应")
    void updateProject_ShouldUpdateAndReturnProject() {
        // Arrange - 设置测试环境：项目存在
        UpdateProjectRequest updateRequest = new UpdateProjectRequest();
        updateRequest.setProjectName("更新后的项目名称");
        updateRequest.setProjectLeader("李四");
        updateRequest.setRemark("更新后的备注");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        // Act - 执行项目更新操作
        ProjectResponse response = projectService.updateProject(1L, updateRequest);

        // Assert - 验证项目更新成功且保存方法被调用
        assertNotNull(response);
        assertEquals("更新后的项目名称", response.getProjectName());
        assertEquals("李四", response.getProjectLeader());
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    @DisplayName("项目删除 - 逻辑删除指定项目")
    void deleteProject_ShouldMarkAsDeleted() {
        // Arrange - 设置测试环境：项目存在
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        // Act - 执行项目删除操作
        projectService.deleteProject(1L);

        // Assert - 验证项目被标记为已删除且保存方法被调用
        assertTrue(project.getDeleted());
        verify(projectRepository).save(project);
    }

    // === 边界情况测试 ===

    @Test
    @DisplayName("项目查询 - 使用空ID查询时应抛出异常")
    void getProjectById_WithNullId_ShouldThrowException() {
        // Act & Assert - 执行空ID查询操作并验证抛出异常
        assertThrows(BadRequestException.class, () -> projectService.getProjectById(null));
        verify(projectRepository, never()).findById(any());
    }

    @Test
    @DisplayName("项目查询 - 查询不存在的项目ID时应抛出资源未找到异常")
    void getProjectById_WithNonexistentId_ShouldThrowException() {
        // Arrange - 设置测试环境：模拟项目不存在
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert - 执行查询操作并验证抛出资源未找到异常，且异常信息包含项目ID
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
            projectService.getProjectById(999L)
        );
        assertTrue(exception.getMessage().contains("999"));
    }

    @Test
    @DisplayName("项目查询 - 使用空项目编号查询时应抛出异常")
    void getProjectByNo_WithNullProjectNo_ShouldThrowException() {
        // Act & Assert - 执行空项目编号查询并验证抛出异常且未查询项目
        assertThrows(BadRequestException.class, () -> projectService.getProjectByNo(null));
        verify(projectRepository, never()).findByProjectNoAndDeletedFalse(any());
    }

    @Test
    @DisplayName("项目查询 - 根据不存在的项目编号查询时应抛出资源未找到异常")
    void getProjectByNo_WithNonexistentProjectNo_ShouldThrowException() {
        // Arrange - 设置测试环境：模拟项目不存在
        when(projectRepository.findByProjectNoAndDeletedFalse("PRJ999")).thenReturn(Optional.empty());

        // Act & Assert - 执行查询操作并验证抛出资源未找到异常，且异常信息包含项目编号
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
            projectService.getProjectByNo("PRJ999")
        );
        assertTrue(exception.getMessage().contains("PRJ999"));
    }

    @Test
    @DisplayName("项目更新 - 使用空ID更新时应抛出异常")
    void updateProject_WithNullId_ShouldThrowException() {
        // Arrange - 设置测试环境：准备更新请求
        UpdateProjectRequest updateRequest = new UpdateProjectRequest();

        // Act & Assert - 执行更新操作并验证抛出BadRequestException且未调用保存方法
        assertThrows(BadRequestException.class, () -> projectService.updateProject(null, updateRequest));
        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("项目删除 - 使用空ID删除时应抛出异常")
    void deleteProject_WithNullId_ShouldThrowException() {
        // Act & Assert - 执行删除操作并验证抛出异常且未保存项目
        assertThrows(BadRequestException.class, () -> projectService.deleteProject(null));
        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("项目唯一性验证 - 验证项目编号唯一性")
    void validateProjectNoUnique_ShouldReturnTrue() {
        // Arrange - 设置测试环境：模拟项目编号不存在
        when(projectRepository.existsByProjectNoAndDeletedFalse("PRJ999")).thenReturn(false);

        // Act - 执行唯一性验证
        boolean result = projectService.validateProjectNoUnique("PRJ999");

        // Assert - 验证项目编号唯一
        assertTrue(result);
    }

    @Test
    @DisplayName("项目唯一性验证 - 重复项目编号应返回false")
    void validateProjectNoUnique_WithDuplicate_ShouldReturnFalse() {
        // Arrange - 设置测试环境：模拟项目编号已存在
        when(projectRepository.existsByProjectNoAndDeletedFalse("PRJ001")).thenReturn(true);

        // Act - 执行唯一性验证
        boolean result = projectService.validateProjectNoUnique("PRJ001");

        // Assert - 验证返回false（不唯一）
        assertFalse(result);
    }

    @Test
    @DisplayName("项目唯一性验证 - 空项目编号不验证")
    void validateProjectNoUnique_WithNull_ShouldReturnTrue() {
        // Act - 执行空项目编号验证
        boolean result = projectService.validateProjectNoUnique(null);

        // Assert - 验证空项目编号直接返回true
        assertTrue(result);
    }

    @Test
    @DisplayName("项目唯一性验证 - 已删除项目不影响唯一性验证")
    void validateProjectNoUnique_WithDeletedProject_ShouldReturnTrue() {
        // Arrange - 设置测试环境：模拟项目编号对应项目已删除
        when(projectRepository.existsByProjectNoAndDeletedFalse("DEL001")).thenReturn(false);

        // Act - 执行唯一性验证
        boolean result = projectService.validateProjectNoUnique("DEL001");

        // Assert - 验证已删除项目不影响唯一性
        assertTrue(result);
    }

    @Test
    @DisplayName("项目分页查询 - 无过滤条件分页查询项目")
    void getProjectsPage_WithNoFilters_ShouldReturnPagedResult() {
        // Arrange - 设置测试环境：准备分页请求无过滤条件
        ProjectPageRequest pageRequest = new ProjectPageRequest();
        pageRequest.setPage(1);
        pageRequest.setSize(10);

        List<Project> projects = List.of(project);
        Page<Project> projectPage = new PageImpl<>(projects, PageRequest.of(0, 10), 1);
        when(
            projectRepository.findAll(
                any(com.querydsl.core.types.Predicate.class),
                any(org.springframework.data.domain.Pageable.class)
            )
        ).thenReturn(projectPage);

        // Act - 执行无过滤条件的分页查询
        PageResult<ProjectResponse> result = projectService.getProjectsPage(pageRequest);

        // Assert - 验证分页查询结果正确
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getContent().size());
    }

    @Test
    @DisplayName("项目分页查询 - 使用项目编号过滤分页查询项目")
    void getProjectsPage_WithProjectNoFilter_ShouldReturnFilteredResult() {
        // Arrange - 设置测试环境：准备带项目编号过滤的分页请求
        ProjectPageRequest pageRequest = new ProjectPageRequest();
        pageRequest.setPage(1);
        pageRequest.setSize(10);
        pageRequest.setProjectNo("PRJ");

        List<Project> projects = List.of(project);
        Page<Project> projectPage = new PageImpl<>(projects, PageRequest.of(0, 10), 1);
        when(
            projectRepository.findAll(
                any(com.querydsl.core.types.Predicate.class),
                any(org.springframework.data.domain.Pageable.class)
            )
        ).thenReturn(projectPage);

        // Act - 执行过滤分页查询操作
        PageResult<ProjectResponse> result = projectService.getProjectsPage(pageRequest);

        // Assert - 验证过滤分页查询结果正确
        assertNotNull(result);
        assertEquals(1, result.getTotal());
    }

    @Test
    @DisplayName("项目分页查询 - 使用项目负责人过滤分页查询项目")
    void getProjectsPage_WithProjectLeaderFilter_ShouldReturnFilteredResult() {
        // Arrange - 设置测试环境：准备带项目负责人过滤的分页请求
        ProjectPageRequest pageRequest = new ProjectPageRequest();
        pageRequest.setPage(1);
        pageRequest.setSize(10);
        pageRequest.setProjectLeader("张三");

        List<Project> projects = List.of(project);
        Page<Project> projectPage = new PageImpl<>(projects, PageRequest.of(0, 10), 1);
        when(
            projectRepository.findAll(
                any(com.querydsl.core.types.Predicate.class),
                any(org.springframework.data.domain.Pageable.class)
            )
        ).thenReturn(projectPage);

        // Act - 执行过滤分页查询操作
        PageResult<ProjectResponse> result = projectService.getProjectsPage(pageRequest);

        // Assert - 验证过滤分页查询结果正确
        assertNotNull(result);
        assertEquals(1, result.getTotal());
    }

    @Test
    @DisplayName("项目创建 - 使用空项目编号时不验证唯一性")
    void createProject_WithNullProjectNo_ShouldNotValidateUniqueness() {
        // Arrange - 设置测试环境：准备项目编号为空的创建请求
        CreateProjectRequest createRequest = new CreateProjectRequest();
        createRequest.setProjectNo(null);
        createRequest.setProjectName("项目名称");
        createRequest.setProjectLeader("负责人");
        createRequest.setRemark("备注");

        when(projectRepository.save(any(Project.class))).thenReturn(project);

        // Act - 执行项目创建操作（项目编号为空）
        ProjectResponse response = projectService.createProject(createRequest);

        // Assert - 验证未调用项目编号唯一性验证方法
        assertNotNull(response);
        verify(projectRepository, never()).existsByProjectNoAndDeletedFalse(any());
    }
}
