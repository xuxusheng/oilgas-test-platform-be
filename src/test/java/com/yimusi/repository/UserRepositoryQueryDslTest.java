package com.yimusi.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.yimusi.BaseIntegrationTest;
import com.yimusi.common.enums.UserRole;
import com.yimusi.entity.QUser;
import com.yimusi.entity.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @description  UserRepository QueryDSL多条件查询测试
 */
@Transactional
class UserRepositoryQueryDslTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    private User adminUser;
    private User memberUser;
    private User anotherMember;
    private User deletedUser;

    @BeforeEach
    void setUp() {
        // 创建测试数据
        adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setPassword("adminpassword");
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setDeleted(false);
        adminUser.setCreatedBy("system");
        adminUser.setCreatedAt(Instant.now());

        memberUser = new User();
        memberUser.setUsername("member1");
        memberUser.setPassword("memberpassword");
        memberUser.setRole(UserRole.MEMBER);
        memberUser.setDeleted(false);
        memberUser.setCreatedBy("system");
        memberUser.setCreatedAt(Instant.now());

        anotherMember = new User();
        anotherMember.setUsername("user123");
        anotherMember.setPassword("userpassword");
        anotherMember.setRole(UserRole.MEMBER);
        anotherMember.setDeleted(false);
        anotherMember.setCreatedBy("system");
        anotherMember.setCreatedAt(Instant.now());

        deletedUser = new User();
        deletedUser.setUsername("deleteduser");
        deletedUser.setPassword("deletedpassword");
        deletedUser.setRole(UserRole.MEMBER);
        deletedUser.setDeleted(true);
        deletedUser.setCreatedBy("system");
        deletedUser.setCreatedAt(Instant.now());
        deletedUser.setDeletedAt(Instant.now());
        deletedUser.setDeletedBy("admin");

        // 保存所有测试数据
        userRepository.save(adminUser);
        userRepository.save(memberUser);
        userRepository.save(anotherMember);
        userRepository.save(deletedUser);
    }

    @AfterEach
    void tearDown() {
        // 清理测试数据
        userRepository.deleteAll();
    }

    @Test
    void findByIdAndDeletedFalse_ShouldFindActiveUser() {
        // Test existsByUsernameAndDeletedFalse
        boolean existsAdmin = userRepository.existsByUsernameAndDeletedFalse("admin");
        boolean existsDeleted = userRepository.existsByUsernameAndDeletedFalse("deleteduser");

        assertTrue(existsAdmin, "应该能找到存在的管理员用户");
        assertFalse(existsDeleted, "不应该找到删除的用户");
    }

    @Test
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

    @Test
    void QueryDSL_UsernameStartsWith_ShouldFindUsers() {
        QUser qUser = QUser.user;

        // QueryDSL：查找用户名以"ad"开头的用户
        BooleanExpression usernameStartsWith = qUser.username.startsWith("ad").and(qUser.deleted.isFalse());
        List<User> adUsers = (List<User>) userRepository.findAll(usernameStartsWith);

        assertEquals(1, adUsers.size(), "应该有1个以ad开头的用户名");
        assertEquals("admin", adUsers.get(0).getUsername());
    }

    @Test
    void QueryDSL_AllUsers_ShouldOnlyReturnNonDeleted() {
        QUser qUser = QUser.user;
        BooleanExpression notDeleted = qUser.deleted.isFalse();

        List<User> allActiveUsers = (List<User>) userRepository.findAll(notDeleted);

        assertEquals(3, allActiveUsers.size(), "应该有3个活跃用户");
        assertTrue(allActiveUsers.stream().noneMatch(User::getDeleted),
                "所有活跃用户都不应标记为删除");
    }

    @Test
    void QueryDSL_FindByMultipleRoles() {
        QUser qUser = QUser.user;

        // 查找ADMIN或MEMBER角色的用户
        BooleanExpression multiRoleCondition = qUser.role.in(UserRole.ADMIN, UserRole.MEMBER)
                .and(qUser.deleted.isFalse());

        List<User> multiRoleUsers = (List<User>) userRepository.findAll(multiRoleCondition);

        assertEquals(3, multiRoleUsers.size(), "应该有3个不同角色的用户");
    }

    @Test
    void QueryDSL_UsernameExactMatch() {
        QUser qUser = QUser.user;

        BooleanExpression exactMatch = qUser.username.eq("member1").and(qUser.deleted.isFalse());
        List<User> exactUsers = (List<User>) userRepository.findAll(exactMatch);

        assertEquals(1, exactUsers.size());
        assertEquals("member1", exactUsers.get(0).getUsername());
        assertEquals(UserRole.MEMBER, exactUsers.get(0).getRole());
    }

    @Test
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