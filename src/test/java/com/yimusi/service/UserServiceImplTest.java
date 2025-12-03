package com.yimusi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.yimusi.common.enums.UserRole;
import com.yimusi.common.exception.BadRequestException;
import com.yimusi.common.exception.ResourceNotFoundException;
import com.yimusi.dto.user.CreateUserRequest;
import com.yimusi.dto.common.PageResult;
import com.yimusi.dto.user.UpdateUserRequest;
import com.yimusi.dto.user.UserPageRequest;
import com.yimusi.dto.user.UserResponse;
import com.yimusi.entity.User;
import com.yimusi.mapper.UserMapper;
import com.yimusi.repository.UserRepository;
import com.yimusi.service.UserServiceImpl;
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
 * 用户业务逻辑服务单元测试类
 *
 * <p>该类对 {@link com.yimusi.service.UserServiceImpl} 进行全面的单元测试，
 * 覆盖用户操作的各个场景，包括用户创建、查询、更新、删除、恢复、分页查询等业务逻辑。
 * 使用 Mockito 框架进行依赖注入和模拟，确保测试的隔离性和可靠性。</p>
 *
 * <p>测试覆盖范围:</p>
 * <ul>
 *   <li>用户创建功能：正常创建、用户名重复校验、空用户名处理</li>
 *   <li>用户查询功能：按ID查询、查询所有用户、查询不存在的用户</li>
 *   <li>用户更新功能：正常更新、同名用户更新、用户名重复校验</li>
 *   <li>用户删除与恢复：逻辑删除、硬删除ID校验、用户恢复</li>
 *   <li>分页查询功能：无过滤条件分页、用户名过滤分页、角色过滤分页</li>
 *   <li>用户验证功能：密码验证、用户名验证</li>
 *   <li>异常边界情况：空参数校验、资源不存在处理</li>
 * </ul>
 *
 * @see com.yimusi.service.UserServiceImpl
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Spy
    private UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @InjectMocks
    private UserServiceImpl userService;

    private CreateUserRequest validCreateRequest;
    private User user;

    @BeforeEach
    void setUp() {
        validCreateRequest = new CreateUserRequest();
        validCreateRequest.setUsername("testuser");
        validCreateRequest.setPassword("Passw0rd!");
        validCreateRequest.setRole(UserRole.MEMBER);

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setRole(UserRole.MEMBER);
        user.setDeleted(false);
    }

    @Test
    @DisplayName("用户创建 - 成功创建用户并返回用户响应")
    void createUser_ShouldReturnUserResponse() {
        // Arrange - 设置测试环境：模拟用户名不存在，准备保存用户
        when(userRepository.existsByUsernameAndDeletedFalse("testuser")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act - 执行用户创建操作
        UserResponse response = userService.createUser(validCreateRequest);

        // Assert - 验证用户创建成功，返回正确的用户响应
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("testuser", response.getUsername());
        verify(userRepository).existsByUsernameAndDeletedFalse("testuser");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("用户创建 - 使用重复用户名时应抛出异常")
    void createUser_WithDuplicateUsername_ShouldThrowException() {
        // Arrange - 设置测试环境：模拟用户名已存在
        when(userRepository.existsByUsernameAndDeletedFalse("testuser")).thenReturn(true);

        // Act & Assert - 执行创建操作并验证抛出BadReq被xception，且从未调用保存方法
        assertThrows(BadRequestException.class, () -> userService.createUser(validCreateRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("用户查询 - 根据有效ID查询用户")
    void getUserById_ShouldReturnUser() {
        // Arrange - 设置测试环境：模拟根据ID查询用户
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act - 执行用户查询操作
        User result = userService.getUserById(1L);

        // Assert - 验证查询返回的用户信息正确
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
    }

    @Test
    @DisplayName("用户查询 - 查询所有用户列表")
    void getAllUsers_ShouldReturnUserList() {
        // Arrange - 设置测试环境：模拟查询所有用户
        List<User> users = List.of(user);
        when(userRepository.findAll()).thenReturn(users);

        // Act - 执行查询所有用户操作
        List<User> result = userService.getAllUsers();

        // Assert - 验证查询结果包含正确数量的用户
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("用户更新 - 成功更新用户信息并返回响应")
    void updateUser_ShouldUpdateAndReturnUser() {
        // Arrange - 设置测试环境：用户存在且新用户名未被占用
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setUsername("newusername");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsernameAndDeletedFalse("newusername")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act - 执行用户更新操作
        UserResponse response = userService.updateUser(1L, updateRequest);

        // Assert - 验证用户更新成功且保存方法被调用
        assertNotNull(response);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("用户删除 - 逻辑删除指定用户")
    void deleteUser_ShouldMarkAsDeleted() {
        // Arrange - 设置测试环境：用户存在
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act - 执行用户删除操作
        userService.deleteUser(1L);

        // Assert - 验证用户被标记为已删除且保存方法被调用
        assertTrue(user.getDeleted());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("用户恢复 - 恢复已删除的用户")
    void restoreUser_ShouldClearDeletedFields() {
        // Arrange - 设置测试环境：用户处于已删除状态
        user.setDeleted(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act - 执行用户恢复操作
        userService.restoreUser(1L);

        // Assert - 验证用户被恢复且保存方法被调用
        assertFalse(user.getDeleted());
        verify(userRepository).save(user);
    }

    // === 分支覆盖测试 ===

    @Test
    @DisplayName("用户查询 - 使用空ID查询时应抛出异常")
    void getUserById_WithNullId_ShouldThrowException() {
        // Act & Assert - 执行空ID查询操作并验证抛出异常
        assertThrows(BadRequestException.class, () -> userService.getUserById(null));
        verify(userRepository, never()).findById(any());
    }

    @Test
    @DisplayName("用户查询 - 查询不存在的用户ID时应抛出资源未找到异常")
    void getUserById_WithNonexistentId_ShouldThrowException() {
        // Arrange - 设置测试环境：模拟用户不存在
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert - 执行查询操作并验证抛出资源未找到异常，且异常信息包含用户ID
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
            userService.getUserById(999L)
        );
        assertTrue(exception.getMessage().contains("999"));
    }

    @Test
    @DisplayName("用户更新 - 使用空ID更新时应抛出异常")
    void updateUser_WithNullId_ShouldThrowException() {
        // Arrange - 设置测试环境：准备更新请求
        UpdateUserRequest updateRequest = new UpdateUserRequest();

        // Act & Assert - 执行更新操作并验证抛出BadReque异常且未调用保存方法
        assertThrows(BadRequestException.class, () -> userService.updateUser(null, updateRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("用户更新 - 更新同名用户名时不应验证唯一性")
    void updateUser_WithSameUsername_ShouldNotValidateUniqueness() {
        // Arrange - 设置测试环境：新用户名与原用户名相同
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setUsername("testuser"); // 同名

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act - 执行用户更新操作（用户名相同）
        userService.updateUser(1L, updateRequest);

        // Assert - 验证未调用用户名唯一性验证方法，但调用了保存方法
        verify(userRepository, never()).existsByUsernameAndDeletedFalse(any());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("用户更新 - 更新用户名为空时不应验证唯一性")
    void updateUser_WithNullUsername_ShouldNotValidateUniqueness() {
        // Arrange - 设置测试环境：新用户名为空
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setUsername(null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act - 执行用户更新操作（用户名为空）
        userService.updateUser(1L, updateRequest);

        // Assert - 验证未调用用户名唯一性验证方法
        verify(userRepository, never()).existsByUsernameAndDeletedFalse(any());
    }

    @Test
    @DisplayName("用户更新 - 更新使用重复的用户名时应抛出异常")
    void updateUser_WithDuplicateNewUsername_ShouldThrowException() {
        // Arrange - 设置测试环境：新用户名已被占用
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setUsername("existinguser");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsernameAndDeletedFalse("existinguser")).thenReturn(true);

        // Act & Assert - 执行更新操作并验证抛出BadReque异常且未保存用户
        assertThrows(BadRequestException.class, () -> userService.updateUser(1L, updateRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("用户删除 - 使用空ID删除时应抛出异常")
    void deleteUser_WithNullId_ShouldThrowException() {
        // Act & Assert - 执行删除操作并验证抛异常且未保存用户
        assertThrows(BadRequestException.class, () -> userService.deleteUser(null));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("用户恢复 - 使用空ID恢复时应抛出异常")
    void restoreUser_WithNullId_ShouldThrowException() {
        // Act & Assert - 执行恢复操作并验证抛异常且未保存用户
        assertThrows(BadRequestException.class, () -> userService.restoreUser(null));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("用户查询 - 根据有效用户名查询用户")
    void findByUsername_WithValidUsername_ShouldReturnUser() {
        // Arrange - 设置测试环境：模拟根据用户名查询用户
        when(userRepository.findByUsernameAndDeletedFalse("testuser")).thenReturn(Optional.of(user));

        // Act - 执行根据用户名查询操作
        User result = userService.findByUsername("testuser");

        // Assert - 验证查询返回的用户信息正确
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    @DisplayName("用户查询 - 根据空用户名查询时应抛出异常")
    void findByUsername_WithNullUsername_ShouldThrowException() {
        // Act & Assert - 执行空用户名查询并验证抛异常且未查询用户
        assertThrows(BadRequestException.class, () -> userService.findByUsername(null));
        verify(userRepository, never()).findByUsernameAndDeletedFalse(any());
    }

    @Test
    @DisplayName("用户查询 - 根据不存在的用户名查询时应抛出资源未找到异常")
    void findByUsername_WithNonexistentUsername_ShouldThrowException() {
        // Arrange - 设置测试环境：模拟用户不存在
        when(userRepository.findByUsernameAndDeletedFalse("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert - 执行查询操作并验证抛出资源未找到异常，且异常信息包含用户名
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
            userService.findByUsername("nonexistent")
        );
        assertTrue(exception.getMessage().contains("nonexistent"));
    }

    @Test
    @DisplayName("用户验证 - 使用正确密码验证用户")
    void validateUser_WithCorrectPassword_ShouldReturnUser() {
        // Arrange - 设置测试环境：使用正确的密码进行验证
        // 使用 BCrypt 正确加密 "password123"
        String encodedPassword = cn.hutool.crypto.digest.BCrypt.hashpw("password123");
        user.setPassword(encodedPassword);
        when(userRepository.findByUsernameAndDeletedFalse("testuser")).thenReturn(Optional.of(user));

        // Act - 执行密码验证操作
        User result = userService.validateUser("testuser", "password123");

        // Assert - 验证密码验证成功返回用户信息
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    @DisplayName("用户验证 - 使用错误密码验证时应抛出异常")
    void validateUser_WithIncorrectPassword_ShouldThrowException() {
        // Arrange - 设置测试环境：使用错误的密码进行验证
        String encodedPassword = cn.hutool.crypto.digest.BCrypt.hashpw("password123");
        user.setPassword(encodedPassword);
        when(userRepository.findByUsernameAndDeletedFalse("testuser")).thenReturn(Optional.of(user));

        // Act & Assert - 执行密码验证操作并验证抛出BadReque异常
        assertThrows(BadRequestException.class, () -> userService.validateUser("testuser", "wrongpassword"));
    }

    @Test
    @DisplayName("用户分页查询 - 无过滤条件分页查询用户")
    void getUsersPage_WithNoFilters_ShouldReturnPagedResult() {
        // Arrange - 设置测试环境：准备分页请求无过滤条件
        UserPageRequest pageRequest = new UserPageRequest();
        pageRequest.setPage(1);
        pageRequest.setSize(10);

        List<User> users = List.of(user);
        Page<User> userPage = new PageImpl<>(users, org.springframework.data.domain.PageRequest.of(0, 10), 1);
        when(
            userRepository.findAll(
                any(com.querydsl.core.types.Predicate.class),
                any(org.springframework.data.domain.Pageable.class)
            )
        ).thenReturn(userPage);

        // Act - 执行无过滤条件的分页查询
        PageResult<UserResponse> result = userService.getUsersPage(pageRequest);

        // Assert - 验证分页查询结果正确
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getContent().size());
    }

    @Test
    @DisplayName("用户分页查询 - 使用用户名过滤分页查询用户")
    void getUsersPage_WithUsernameFilter_ShouldReturnFilteredResult() {
        // Arrange - 设置测试环境：准备带用户名过滤的分页请求
        UserPageRequest pageRequest = new UserPageRequest();
        pageRequest.setPage(1);
        pageRequest.setSize(10);
        pageRequest.setUsername("test");

        List<User> users = List.of(user);
        Page<User> userPage = new PageImpl<>(users, org.springframework.data.domain.PageRequest.of(0, 10), 1);
        when(
            userRepository.findAll(
                any(com.querydsl.core.types.Predicate.class),
                any(org.springframework.data.domain.Pageable.class)
            )
        ).thenReturn(userPage);

        // Act - 执行过滤分页查询操作
        PageResult<UserResponse> result = userService.getUsersPage(pageRequest);

        // Assert - 验证过滤分页查询结果正确
        assertNotNull(result);
        assertEquals(1, result.getTotal());
    }

    @Test
    @DisplayName("用户分页查询 - 使用角色过滤分页查询用户")
    void getUsersPage_WithRoleFilter_ShouldReturnFilteredResult() {
        // Arrange - 设置测试环境：准备带角色过滤的分页请求
        UserPageRequest pageRequest = new UserPageRequest();
        pageRequest.setPage(1);
        pageRequest.setSize(10);
        pageRequest.setRole(UserRole.MEMBER);

        List<User> users = List.of(user);
        Page<User> userPage = new PageImpl<>(users, org.springframework.data.domain.PageRequest.of(0, 10), 1);
        when(
            userRepository.findAll(
                any(com.querydsl.core.types.Predicate.class),
                any(org.springframework.data.domain.Pageable.class)
            )
        ).thenReturn(userPage);

        // Act - 执行过滤分页查询操作
        PageResult<UserResponse> result = userService.getUsersPage(pageRequest);

        // Assert - 验证过滤分页查询结果正确
        assertNotNull(result);
        assertEquals(1, result.getTotal());
    }

    @Test
    @DisplayName("用户创建 - 使用空用户名时不验证唯一性")
    void createUser_WithNullUsername_ShouldNotValidateUniqueness() {
        // Arrange - 设置测试环境：准备用户名为空的创建请求
        CreateUserRequest createRequest = new CreateUserRequest();
        createRequest.setUsername(null);
        createRequest.setPassword("password");
        createRequest.setRole(UserRole.MEMBER);

        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act - 执行用户创建操作（用户名为空）
        UserResponse response = userService.createUser(createRequest);

        // Assert - 验证未调用用户名唯一性验证方法
        assertNotNull(response);
        verify(userRepository, never()).existsByUsernameAndDeletedFalse(any());
    }
}
