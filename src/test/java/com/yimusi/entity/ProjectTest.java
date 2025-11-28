package com.yimusi.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Project实体单元测试类
 *
 * 测试项目实体的属性、继承关系和基本功能。
 */
class ProjectTest {

    private Project project;

    @BeforeEach
    void setUp() {
        project = new Project();
        project.setId(1L);
        project.setProjectNo("PRJ001");
        project.setProjectName("测试项目");
        project.setProjectLeader("张三");
        project.setRemark("这是一个测试项目");
    }

    @Test
    @DisplayName("项目实体 - 设置和获取项目编号")
    void testProjectNo() {
        assertEquals("PRJ001", project.getProjectNo());
        project.setProjectNo("PRJ002");
        assertEquals("PRJ002", project.getProjectNo());
    }

    @Test
    @DisplayName("项目实体 - 设置和获取项目名称")
    void testProjectName() {
        assertEquals("测试项目", project.getProjectName());
        project.setProjectName("更新后的项目名称");
        assertEquals("更新后的项目名称", project.getProjectName());
    }

    @Test
    @DisplayName("项目实体 - 设置和获取项目负责人")
    void testProjectLeader() {
        assertEquals("张三", project.getProjectLeader());
        project.setProjectLeader("李四");
        assertEquals("李四", project.getProjectLeader());
    }

    @Test
    @DisplayName("项目实体 - 设置和获取备注")
    void testRemark() {
        assertEquals("这是一个测试项目", project.getRemark());
        project.setRemark("更新后的备注");
        assertEquals("更新后的备注", project.getRemark());
    }

    @Test
    @DisplayName("项目实体 - 继承自BaseAuditEntity")
    void testInheritance() {
        // 测试继承的审计字段
        project.setCreatedBy("admin");
        project.setUpdatedBy("admin");
        project.setDeleted(false);

        assertEquals("admin", project.getCreatedBy());
        assertEquals("admin", project.getUpdatedBy());
        assertFalse(project.getDeleted());
    }

    @Test
    @DisplayName("项目实体 - 空值处理")
    void testNullValues() {
        Project emptyProject = new Project();

        assertNull(emptyProject.getProjectNo());
        assertNull(emptyProject.getProjectName());
        assertNull(emptyProject.getProjectLeader());
        assertNull(emptyProject.getRemark());

        // 测试继承字段默认值
        assertEquals(false, emptyProject.getDeleted());
    }

    @Test
    @DisplayName("项目实体 - ID设置")
    void testId() {
        assertEquals(1L, project.getId());
        project.setId(999L);
        assertEquals(999L, project.getId());
    }

    @Test
    @DisplayName("项目实体 - 项目编号长度边界测试")
    void testProjectNoLength() {
        // 测试很长的项目编号
        String longProjectNo = "PRJ" + "X".repeat(47);
        project.setProjectNo(longProjectNo);
        assertEquals(longProjectNo, project.getProjectNo());

        // 测试空字符串
        project.setProjectNo("");
        assertEquals("", project.getProjectNo());
    }

    @Test
    @DisplayName("项目实体 - 项目名称长度边界测试")
    void testProjectNameLength() {
        // 测试很长的项目名称
        String longProjectName = "项目名称" + "长".repeat(190);
        project.setProjectName(longProjectName);
        assertEquals(longProjectName, project.getProjectName());
    }

    @Test
    @DisplayName("项目实体 - 项目负责人长度边界测试")
    void testProjectLeaderLength() {
        // 测试没有负责人的情况
        project.setProjectLeader(null);
        assertNull(project.getProjectLeader());

        // 测试有负责人的情况
        project.setProjectLeader("王五");
        assertEquals("王五", project.getProjectLeader());
    }

    @Test
    @DisplayName("项目实体 - 备注长度边界测试")
    void testRemarkLength() {
        // 测试很长的备注
        String longRemark = "备注" + "长".repeat(498);
        project.setRemark(longRemark);
        assertEquals(longRemark, project.getRemark());

        // 测试空备注
        project.setRemark("");
        assertEquals("", project.getRemark());
    }
}