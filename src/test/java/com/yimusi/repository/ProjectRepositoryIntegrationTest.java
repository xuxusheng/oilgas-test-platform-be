package com.yimusi.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.yimusi.BaseIntegrationTest;
import com.yimusi.entity.Project;
import com.yimusi.entity.QProject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * ProjectRepository 集成测试
 *
 * 测试项目仓库的CRUD操作、软删除功能、唯一性检查以及QueryDSL查询能力。
 */
@Transactional
@DisplayName("ProjectRepository 集成测试")
class ProjectRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ProjectRepository projectRepository;

    private Project project1;
    private Project project2;
    private Project deletedProject;

    @BeforeEach
    void setUp() {
        // 创建测试项目
        project1 = new Project();
        project1.setProjectNo("PRJ001");
        project1.setProjectName("测试项目1");
        project1.setProjectLeader("张三");
        project1.setRemark("这是第一个测试项目");
        project1.setCreatedBy("test-user");
        project1.setCreatedAt(Instant.now());

        project2 = new Project();
        project2.setProjectNo("PRJ002");
        project2.setProjectName("测试项目2");
        project2.setProjectLeader("李四");
        project2.setRemark("这是第二个测试项目");
        project2.setCreatedBy("test-user");
        project2.setCreatedAt(Instant.now());

        deletedProject = new Project();
        deletedProject.setProjectNo("PRJ003");
        deletedProject.setProjectName("已删除项目");
        deletedProject.setProjectLeader("王五");
        deletedProject.setRemark("这个项目应该被软删除");
        deletedProject.setCreatedBy("test-user");
        deletedProject.setCreatedAt(Instant.now());
        deletedProject.setDeleted(true);
        deletedProject.setDeletedAt(Instant.now());

        // 保存项目到数据库
        projectRepository.saveAll(List.of(project1, project2, deletedProject));
    }

    @AfterEach
    void tearDown() {
        // 清理测试数据
        projectRepository.deleteAll();
    }

    @Test
    @DisplayName("项目查询 - 查找所有未删除项目")
    void findAllProjects() {
        List<Project> projects = projectRepository.findAll();

        assertEquals(2, projects.size()); // 由于@SQLRestriction("deleted = false")，只返回未删除的项目
    }

    @Test
    @DisplayName("项目查询 - 根据ID查询项目")
    void findById() {
        Optional<Project> found = projectRepository.findById(project1.getId());

        assertTrue(found.isPresent());
        assertEquals("PRJ001", found.get().getProjectNo());
        assertEquals("测试项目1", found.get().getProjectName());
    }

    @Test
    @DisplayName("项目查询 - 根据不存在的ID查询返回空")
    void findByIdWithNonExistentId() {
        Optional<Project> found = projectRepository.findById(999L);

        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("项目唯一性检查 - 检查项目编号是否存在")
    void existsByProjectNo() {
        boolean exists = projectRepository.existsByProjectNoAndDeletedFalse("PRJ001");

        assertTrue(exists);
    }

    @Test
    @DisplayName("项目唯一性检查 - 检查不存在的项目编号")
    void existsByProjectNoWithNonExistent() {
        boolean exists = projectRepository.existsByProjectNoAndDeletedFalse("PRJ999");

        assertFalse(exists);
    }

    @Test
    @DisplayName("项目唯一性检查 - 已删除的项目不应影响唯一性检查")
    void existsByProjectNoWithDeletedProject() {
        boolean exists = projectRepository.existsByProjectNoAndDeletedFalse("PRJ003");

        // 已删除项目不应影响唯一性检查
        assertFalse(exists);
    }

    @Test
    @DisplayName("项目查询 - 根据项目编号查询项目")
    void findByProjectNo() {
        Optional<Project> found = projectRepository.findByProjectNoAndDeletedFalse("PRJ001");

        assertTrue(found.isPresent());
        assertEquals("测试项目1", found.get().getProjectName());
    }

    @Test
    @DisplayName("项目查询 - 根据不存在的项目编号查询返回空")
    void findByProjectNoWithNonExistent() {
        Optional<Project> found = projectRepository.findByProjectNoAndDeletedFalse("PRJ999");

        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("项目查询 - 已删除项目不应被查询到")
    void findByProjectNoWithDeletedProject() {
        Optional<Project> found = projectRepository.findByProjectNoAndDeletedFalse("PRJ003");

        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("项目查询 - 根据项目负责人查询项目")
    void findByProjectLeader() {
        List<Project> projects = projectRepository.findByProjectLeaderAndDeletedFalse("张三");

        assertEquals(1, projects.size());
        assertEquals("PRJ001", projects.get(0).getProjectNo());
    }

    @Test
    @DisplayName("项目查询 - 根据不存在的负责人查询返回空列表")
    void findByProjectLeaderWithNonExistent() {
        List<Project> projects = projectRepository.findByProjectLeaderAndDeletedFalse("不存在的人");

        assertEquals(0, projects.size());
    }

    @Test
    @DisplayName("项目创建 - 保存新项目")
    void saveNewProject() {
        Project newProject = new Project();
        newProject.setProjectNo("PRJ004");
        newProject.setProjectName("新项目");
        newProject.setProjectLeader("新负责人");
        newProject.setRemark("新项目备注");
        newProject.setCreatedBy("test-user");
        newProject.setCreatedAt(Instant.now());

        Project saved = projectRepository.save(newProject);

        assertNotNull(saved.getId());
        assertEquals("PRJ004", saved.getProjectNo());
        assertEquals("新项目", saved.getProjectName());
    }

    @Test
    @DisplayName("项目更新 - 更新项目信息")
    void updateProject() {
        Optional<Project> found = projectRepository.findById(project1.getId());
        assertTrue(found.isPresent());

        Project project = found.get();
        project.setProjectName("更新后的项目名称");
        project.setProjectLeader("新负责人");
        project.setUpdatedBy("test-user");
        project.setUpdatedAt(Instant.now());

        Project updated = projectRepository.save(project);

        assertEquals("更新后的项目名称", updated.getProjectName());
        assertEquals("新负责人", updated.getProjectLeader());
    }

    @Test
    @DisplayName("项目删除 - 软删除项目")
    void softDeleteProject() {
        Optional<Project> found = projectRepository.findById(project1.getId());
        assertTrue(found.isPresent());

        Project project = found.get();
        project.setDeleted(true);
        project.setDeletedAt(Instant.now());
        project.setDeletedBy("test-user");

        Project deleted = projectRepository.save(project);

        assertTrue(deleted.getDeleted());
        assertNotNull(deleted.getDeletedAt());
    }

    @Test
    @DisplayName("项目查询 - QueryDSL多条件查询")
    void querydslMultipleConditions() {
        QProject qProject = QProject.project;

        // 构建查询条件：项目名称包含"测试"，项目负责人是"张三"
        BooleanExpression condition = qProject.projectName.contains("测试")
                .and(qProject.projectLeader.eq("张三"))
                .and(qProject.deleted.isFalse());

        List<Project> projects = (List<Project>) projectRepository.findAll(condition);

        assertEquals(1, projects.size());
        assertEquals("PRJ001", projects.get(0).getProjectNo());
    }

    @Test
    @DisplayName("项目查询 - 排序查询")
    void orderedQuery() {
        List<Project> projects = projectRepository.findAll(Sort.by(Sort.Direction.ASC, "projectNo"));

        assertEquals(2, projects.size()); // 由于@SQLRestriction("deleted = false")，只返回未删除的项目
        assertTrue(projects.get(0).getProjectNo().compareTo(projects.get(1).getProjectNo()) < 0);
    }

    @Test
    @DisplayName("项目更新 - 更新项目编号验证索引")
    void updateProjectNo() {
        Optional<Project> found = projectRepository.findById(project1.getId());
        assertTrue(found.isPresent());

        Project project = found.get();
        project.setProjectNo("PRJ001_UPDATED");
        project.setUpdatedBy("test-user");
        project.setUpdatedAt(Instant.now());

        Project updated = projectRepository.save(project);

        assertEquals("PRJ001_UPDATED", updated.getProjectNo());

        // 验证可以通过新编号查询到
        Optional<Project> foundByNewNo = projectRepository.findByProjectNoAndDeletedFalse("PRJ001_UPDATED");
        assertTrue(foundByNewNo.isPresent());
    }

    @Test
    @DisplayName("项目批量创建 - 同时保存多个项目")
    void saveAllProjects() {
        Project project3 = new Project();
        project3.setProjectNo("PRJ005");
        project3.setProjectName("批量项目3");
        project3.setProjectLeader("批量负责人3");
        project3.setCreatedBy("test-user");
        project3.setCreatedAt(Instant.now());

        Project project4 = new Project();
        project4.setProjectNo("PRJ006");
        project4.setProjectName("批量项目4");
        project4.setProjectLeader("批量负责人4");
        project4.setCreatedBy("test-user");
        project4.setCreatedAt(Instant.now());

        List<Project> newProjects = List.of(project3, project4);
        List<Project> saved = (List<Project>) projectRepository.saveAll(newProjects);

        assertEquals(2, saved.size());
        assertNotNull(saved.get(0).getId());
        assertNotNull(saved.get(1).getId());
    }

    @Test
    @DisplayName("项目查询 - 不存在的负责人返回空列表")
    void findByNonExistentLeader() {
        List<Project> projects = projectRepository.findByProjectLeaderAndDeletedFalse("不存在的负责人");

        assertTrue(projects.isEmpty());
    }
}