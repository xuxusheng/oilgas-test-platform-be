package com.yimusi.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.yimusi.BaseIntegrationTest;
import com.yimusi.entity.QUser;
import com.yimusi.entity.User;
import com.yimusi.enums.UserRole;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * UserRepository QueryDSL多条件查询测试
 * 覆盖 QueryDSL 的常见过滤能力：多条件组合、模糊/精确匹配、角色过滤以及软删除隔离。
 */
@Transactional
@DisplayName("UserRepository QueryDSL多条件查询测试")
class UserRepositoryQueryDslTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    private User adminUser;
    private User memberUser;
    private User anotherMember;
    private User deletedUser;

    /**
     * 构建管理员、普通会员和软删除用户，确保测试覆盖不同角色与删除状态。
     */
    @BeforeEach
    void setUp() {
        // 创建管理员、普通用户以及软删除用户，以覆盖常见的过滤组合
        adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setPassword("adminpassword");
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setDeleted(false);
        adminUser.setCreatedBy(1L);
        adminUser.setCreatedAt(Instant.now());

        memberUser = new User();
        memberUser.setUsername("member1");
        memberUser.setPassword("memberpassword");
        memberUser.setRole(UserRole.MEMBER);
        memberUser.setDeleted(false);
        memberUser.setCreatedBy(1L);
        memberUser.setCreatedAt(Instant.now());

        anotherMember = new User();
        anotherMember.setUsername("user123");
        anotherMember.setPassword("userpassword");
        anotherMember.setRole(UserRole.MEMBER);
        anotherMember.setDeleted(false);
        anotherMember.setCreatedBy(1L);
        anotherMember.setCreatedAt(Instant.now());

        deletedUser = new User();
        deletedUser.setUsername("deleteduser");
        deletedUser.setPassword("deletedpassword");
        deletedUser.setRole(UserRole.MEMBER);
        deletedUser.setDeleted(true);
        deletedUser.setCreatedBy(1L);
        deletedUser.setCreatedAt(Instant.now());
        deletedUser.setDeletedAt(Instant.now());
        deletedUser.setDeletedBy(1L);

        // 保存所有测试数据
        userRepository.save(adminUser);
        userRepository.save(memberUser);
        userRepository.save(anotherMember);
        userRepository.save(deletedUser);
    }

    /**
     * 清理每个用例写入的用户，避免数据串联。
     */
    @AfterEach
    void tearDown() {
        // 清理测试数据
        userRepository.deleteAll();
    }

    /**
     * 测试 existsByUsernameAndDeletedFalse 方法
     * 验证已被软删除的用户名不会被视为活跃用户。
     */
    @Test
    @DisplayName("测试 existsByUsernameAndDeletedFalse 仅返回未删除用户")
    void findByIdAndDeletedFalse_ShouldFindActiveUser() {
        // Test existsByUsernameAndDeletedFalse
        boolean existsAdmin = userRepository.existsByUsernameAndDeletedFalse("admin");
        boolean existsDeleted = userRepository.existsByUsernameAndDeletedFalse("deleteduser");

        assertTrue(existsAdmin, "应该能找到存在的管理员用户");
        assertFalse(existsDeleted, "不应该找到删除的用户");
    }

    /**
     * 验证 QueryDSL 能够通过角色 + 删除状态的组合条件精准过滤用户。
     */
    @Test
    @DisplayName("测试 QueryDSL 多条件过滤 MEMBER 未删除用户")
    void QueryDSL_MultipleRestrictions_ShouldFindUsersWithConditions() {
        QUser qUser = QUser.user;

        // QueryDSL: 查找角色为MEMBER且未删除的用户
        BooleanExpression memberCondition = qUser.role.eq(UserRole.MEMBER)
                .and(qUser.deleted.isFalse());

        List<User> members = (List<User>) userRepository.findAll(memberCondition);

        assertEquals(2, members.size(), "应该有2个成员用户");
        assertTrue(members.stream().allMatch(u -> u.getRole() == UserRole.MEMBER),
                "所有结果应该是MEMBER角色");
        assertTrue(members.stream().allMatch(u -> !u.getDeleted()),
                "所有结果应该是未删除的");
    }

    /**
     * 测试 QueryDSL startsWith 条件
     * 确认模糊匹配的同时仍然排除软删除用户。
     */
    @Test
    @DisplayName("测试 QueryDSL 用户名前缀匹配并过滤软删除")
    void QueryDSL_UsernameStartsWith_ShouldFindUsers() {
        QUser qUser = QUser.user;

        // QueryDSL：查找用户名以"ad"开头的用户
        BooleanExpression usernameStartsWith = qUser.username.startsWith("ad").and(qUser.deleted.isFalse());
        List<User> adUsers = (List<User>) userRepository.findAll(usernameStartsWith);

        assertEquals(1, adUsers.size(), "应该有1个以ad开头的用户名");
        assertEquals("admin", adUsers.get(0).getUsername());
    }

    /**
     * 验证基础的未删除过滤器
     * 确保默认查询只返回活跃数据。
     */
    @Test
    @DisplayName("测试 QueryDSL 不返回软删除用户")
    void QueryDSL_AllUsers_ShouldOnlyReturnNonDeleted() {
        QUser qUser = QUser.user;
        BooleanExpression notDeleted = qUser.deleted.isFalse();

        List<User> allActiveUsers = (List<User>) userRepository.findAll(notDeleted);

        assertEquals(3, allActiveUsers.size(), "应该有3个活跃用户");
        assertTrue(allActiveUsers.stream().noneMatch(User::getDeleted),
                "所有活跃用户都不应标记为删除");
    }

    /**
     * 测试 QueryDSL in 条件
     * 确认多角色过滤可以与软删除过滤叠加。
     */
    @Test
    @DisplayName("测试 QueryDSL 支持多角色组合过滤")
    void QueryDSL_FindByMultipleRoles() {
        QUser qUser = QUser.user;

        // 查找ADMIN或MEMBER角色的用户
        BooleanExpression multiRoleCondition = qUser.role.in(UserRole.ADMIN, UserRole.MEMBER)
                .and(qUser.deleted.isFalse());

        List<User> multiRoleUsers = (List<User>) userRepository.findAll(multiRoleCondition);

        assertEquals(3, multiRoleUsers.size(), "应该有3个不同角色的用户");
    }

    /**
     * 测试 QueryDSL 精确匹配条件
     * 确保用户名精确匹配时仍遵从删除状态过滤。
     */
    @Test
    @DisplayName("测试 QueryDSL 用户名精确匹配且排除软删除")
    void QueryDSL_UsernameExactMatch() {
        QUser qUser = QUser.user;

        BooleanExpression exactMatch = qUser.username.eq("member1").and(qUser.deleted.isFalse());
        List<User> exactUsers = (List<User>) userRepository.findAll(exactMatch);

        assertEquals(1, exactUsers.size());
        assertEquals("member1", exactUsers.get(0).getUsername());
        assertEquals(UserRole.MEMBER, exactUsers.get(0).getRole());
    }

    /**
     * 测试删除后的再次查询
     * 验证删库操作会影响后续 QueryDSL 条件查询的结果集。
     */
    @Test
    @DisplayName("测试删除后 QueryDSL 查询的活跃用户数量会减少")
    void deleteAndQuery_ShouldReduceActiveUserCount() {
        QUser qUser = QUser.user;
        BooleanExpression notDeleted = qUser.deleted.isFalse();

        // 初始活跃用户应该是3个
        List<User> initialActiveUsers = (List<User>) userRepository.findAll(notDeleted);
        assertEquals(3, initialActiveUsers.size(), "初始应该有3个活跃用户");

        // 删除一个用户
        userRepository.delete(memberUser);

        // 验证活跃用户数量会减少
        List<User> allActiveUsers = (List<User>) userRepository.findAll(notDeleted);
        assertEquals(2, allActiveUsers.size(), "删除后应该有2个活跃用户");

        // 验证删除的用户不再包含在查询结果中
        assertFalse(allActiveUsers.stream().anyMatch(u -> u.getId().equals(memberUser.getId())),
                "删除的用户不应出现在活跃用户列表中");
    }
}
