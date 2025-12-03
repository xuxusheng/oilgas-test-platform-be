package com.yimusi.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.yimusi.common.enums.UserRole;
import com.yimusi.dto.user.CreateUserRequest;
import com.yimusi.dto.user.UpdateUserRequest;
import com.yimusi.dto.user.UserResponse;
import com.yimusi.entity.User;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

/**
 * UserMapper测试类
 *
 * 测试UserMapper的映射功能：
 * - 创建用户请求(CreateUserRequest)到User实体的映射
 * - User实体到用户响应(UserResponse)的映射
 * - 更新用户请求(UpdateUserRequest)到User实体的更新操作
 * - 包含边界情况和异常场景的测试
 */
class UserMapperTest {

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Test
    @DisplayName("测试将创建用户请求映射为User实体")
    void toEntity_ShouldMapCreateUserRequestToUser() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("testuser");
        request.setPassword("Passw0rd!");
        request.setRole(UserRole.MEMBER);

        // Act
        User user = userMapper.toEntity(request);

        // Assert
        assertNotNull(user);
        assertEquals("testuser", user.getUsername());
        assertEquals("Passw0rd!", user.getPassword());
        assertEquals(UserRole.MEMBER, user.getRole());
        assertNull(user.getId());
        assertNull(user.getCreatedBy());
        assertNull(user.getCreatedAt());
    }

    @Test
    @DisplayName("测试将User实体映射为用户响应对象")
    void toResponse_ShouldMapUserToUserResponse() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setRole(UserRole.MEMBER);
        user.setDeleted(false);
        user.setCreatedBy("admin");
        user.setCreatedAt(Instant.now());
        user.setUpdatedBy("admin");
        user.setUpdatedAt(Instant.now());

        // Act
        UserResponse response = userMapper.toResponse(user);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("testuser", response.getUsername());
        assertEquals(UserRole.MEMBER, response.getRole());
    }

    @Test
    @DisplayName("测试通过更新请求更新用户实体")
    void updateEntityFromRequest_ShouldUpdateUserFromUpdateRequest() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setUsername("olduser");
        user.setPassword("oldpassword");
        user.setRole(UserRole.MEMBER);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername("newuser");
        request.setPassword("newpassword");

        // Act
        userMapper.updateEntityFromRequest(request, user);

        // Assert
        assertEquals("newuser", user.getUsername());
        assertEquals("newpassword", user.getPassword());
        assertEquals(UserRole.MEMBER, user.getRole()); // 角色应该保持不变
        assertEquals(1L, user.getId()); // ID应该保持不变
    }

    @Test
    @DisplayName("测试更新实体时处理空字段")
    void updateEntityFromRequest_ShouldHandleNullFields() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setUsername("olduser");
        user.setPassword("oldpassword");
        user.setRole(UserRole.MEMBER);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername(null); // 显式设置null
        request.setPassword(null);

        // Act
        userMapper.updateEntityFromRequest(request, user);

        // Assert
        // 验证null字段保持原有值不变
        assertEquals("olduser", user.getUsername());
        assertEquals("oldpassword", user.getPassword());
        assertEquals(UserRole.MEMBER, user.getRole());
    }

    @Test
    @DisplayName("测试只更新提供的字段")
    void updateEntityFromRequest_ShouldOnlyUpdateProvidedFields() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setUsername("olduser");
        user.setPassword("oldpassword");
        user.setRole(UserRole.ADMIN);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername("newuser");
        // password字段不设置，应该保持原值

        // Act
        userMapper.updateEntityFromRequest(request, user);

        // Assert
        assertEquals("newuser", user.getUsername()); // 应该被更新
        assertEquals("oldpassword", user.getPassword()); // 应该保持原值不变
        assertEquals(UserRole.ADMIN, user.getRole()); // 应该保持原值不变
    }

    @Test
    @DisplayName("测试批量映射多个用户为响应对象")
    void toResponse_ShouldMapMultipleUsersToUserResponses() {
        // Arrange
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");
        user1.setRole(UserRole.ADMIN);
        user1.setDeleted(false);

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setRole(UserRole.MEMBER);
        user2.setDeleted(false);

        List<User> users = List.of(user1, user2);

        // Act
        List<UserResponse> responses = users.stream()
                .map(userMapper::toResponse)
                .toList();

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(1L, responses.get(0).getId());
        assertEquals("user1", responses.get(0).getUsername());
        assertEquals(2L, responses.get(1).getId());
        assertEquals("user2", responses.get(1).getUsername());
    }

    @Test
    @DisplayName("测试不同角色的创建请求映射")
    void toEntity_ShouldMapCreateRequestWithDifferentRoles() {
        // Test ADMIN role
        CreateUserRequest adminRequest = new CreateUserRequest();
        adminRequest.setUsername("adminuser");
        adminRequest.setPassword("adminpassword");
        adminRequest.setRole(UserRole.ADMIN);

        User adminUser = userMapper.toEntity(adminRequest);

        assertNotNull(adminUser);
        assertEquals(UserRole.ADMIN, adminUser.getRole());

        // Test MEMBER role
        CreateUserRequest memberRequest = new CreateUserRequest();
        memberRequest.setUsername("memberuser");
        memberRequest.setPassword("memberpassword");
        memberRequest.setRole(UserRole.MEMBER);

        User memberUser = userMapper.toEntity(memberRequest);

        assertNotNull(memberUser);
        assertEquals(UserRole.MEMBER, memberUser.getRole());
    }

    @Test
    @DisplayName("测试删除用户的数据映射")
    void shouldNotBreakWhenMappingWithDeletedUsers() {
        // Arrange
        User deletedUser = new User();
        deletedUser.setId(1L);
        deletedUser.setUsername("deleteduser");
        deletedUser.setPassword("password");
        deletedUser.setRole(UserRole.MEMBER);
        deletedUser.setDeleted(true); // 已删除状态
        deletedUser.setCreatedBy("admin");
        deletedUser.setCreatedAt(Instant.now());

        // Act & Assert - 应该能够映射已删除的用户
        UserResponse response = userMapper.toResponse(deletedUser);
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("deleteduser", response.getUsername());
    }
}