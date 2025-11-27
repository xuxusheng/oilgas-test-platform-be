package com.yimusi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.yimusi.common.exception.BadRequestException;
import com.yimusi.common.exception.ResourceNotFoundException;
import com.yimusi.dto.CreateUserRequest;
import com.yimusi.dto.PageResult;
import com.yimusi.dto.UpdateUserRequest;
import com.yimusi.dto.UserPageRequest;
import com.yimusi.dto.UserResponse;
import com.yimusi.entity.User;
import com.yimusi.common.enums.UserRole;
import com.yimusi.mapper.UserMapper;
import com.yimusi.repository.UserRepository;
import com.yimusi.service.impl.UserServiceImpl;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
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
    void createUser_ShouldReturnUserResponse() {
        // Arrange
        when(userRepository.existsByUsernameAndDeletedFalse("testuser")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        UserResponse response = userService.createUser(validCreateRequest);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("testuser", response.getUsername());
        verify(userRepository).existsByUsernameAndDeletedFalse("testuser");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_WithDuplicateUsername_ShouldThrowException() {
        // Arrange
        when(userRepository.existsByUsernameAndDeletedFalse("testuser")).thenReturn(true);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> userService.createUser(validCreateRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_ShouldReturnUser() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        User result = userService.getUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void getAllUsers_ShouldReturnUserList() {
        // Arrange
        List<User> users = List.of(user);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository).findAll();
    }

    @Test
    void updateUser_ShouldUpdateAndReturnUser() {
        // Arrange
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setUsername("newusername");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsernameAndDeletedFalse("newusername")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        UserResponse response = userService.updateUser(1L, updateRequest);

        // Assert
        assertNotNull(response);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deleteUser_ShouldMarkAsDeleted() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        userService.deleteUser(1L);

        // Assert
        assertTrue(user.getDeleted());
        verify(userRepository).save(user);
    }

    @Test
    void restoreUser_ShouldClearDeletedFields() {
        // Arrange
        user.setDeleted(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        userService.restoreUser(1L);

        // Assert
        assertFalse(user.getDeleted());
        verify(userRepository).save(user);
    }

    // === 分支覆盖测试 ===

    @Test
    void getUserById_WithNullId_ShouldThrowException() {
        // Act & Assert
        assertThrows(BadRequestException.class, () -> userService.getUserById(null));
        verify(userRepository, never()).findById(any());
    }

    @Test
    void getUserById_WithNonexistentId_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> userService.getUserById(999L)
        );
        assertTrue(exception.getMessage().contains("999"));
    }

    @Test
    void updateUser_WithNullId_ShouldThrowException() {
        // Arrange
        UpdateUserRequest updateRequest = new UpdateUserRequest();

        // Act & Assert
        assertThrows(BadRequestException.class, () -> userService.updateUser(null, updateRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_WithSameUsername_ShouldNotValidateUniqueness() {
        // Arrange
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setUsername("testuser"); // 同名

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        userService.updateUser(1L, updateRequest);

        // Assert
        verify(userRepository, never()).existsByUsernameAndDeletedFalse(any());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_WithNullUsername_ShouldNotValidateUniqueness() {
        // Arrange
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setUsername(null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        userService.updateUser(1L, updateRequest);

        // Assert
        verify(userRepository, never()).existsByUsernameAndDeletedFalse(any());
    }

    @Test
    void updateUser_WithDuplicateNewUsername_ShouldThrowException() {
        // Arrange
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setUsername("existinguser");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsernameAndDeletedFalse("existinguser")).thenReturn(true);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> userService.updateUser(1L, updateRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_WithNullId_ShouldThrowException() {
        // Act & Assert
        assertThrows(BadRequestException.class, () -> userService.deleteUser(null));
        verify(userRepository, never()).save(any());
    }

    @Test
    void restoreUser_WithNullId_ShouldThrowException() {
        // Act & Assert
        assertThrows(BadRequestException.class, () -> userService.restoreUser(null));
        verify(userRepository, never()).save(any());
    }

    @Test
    void findByUsername_WithValidUsername_ShouldReturnUser() {
        // Arrange
        when(userRepository.findByUsernameAndDeletedFalse("testuser")).thenReturn(Optional.of(user));

        // Act
        User result = userService.findByUsername("testuser");

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void findByUsername_WithNullUsername_ShouldThrowException() {
        // Act & Assert
        assertThrows(BadRequestException.class, () -> userService.findByUsername(null));
        verify(userRepository, never()).findByUsernameAndDeletedFalse(any());
    }

    @Test
    void findByUsername_WithNonexistentUsername_ShouldThrowException() {
        // Arrange
        when(userRepository.findByUsernameAndDeletedFalse("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> userService.findByUsername("nonexistent")
        );
        assertTrue(exception.getMessage().contains("nonexistent"));
    }

    @Test
    void validateUser_WithCorrectPassword_ShouldReturnUser() {
        // Arrange
        // 使用 BCrypt 正确加密 "password123"
        String encodedPassword = cn.hutool.crypto.digest.BCrypt.hashpw("password123");
        user.setPassword(encodedPassword);
        when(userRepository.findByUsernameAndDeletedFalse("testuser")).thenReturn(Optional.of(user));

        // Act
        User result = userService.validateUser("testuser", "password123");

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void validateUser_WithIncorrectPassword_ShouldThrowException() {
        // Arrange
        String encodedPassword = cn.hutool.crypto.digest.BCrypt.hashpw("password123");
        user.setPassword(encodedPassword);
        when(userRepository.findByUsernameAndDeletedFalse("testuser")).thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> userService.validateUser("testuser", "wrongpassword"));
    }

    @Test
    void getUsersPage_WithNoFilters_ShouldReturnPagedResult() {
        // Arrange
        UserPageRequest pageRequest = new UserPageRequest();
        pageRequest.setPage(1);
        pageRequest.setSize(10);

        List<User> users = List.of(user);
        Page<User> userPage = new PageImpl<>(users, org.springframework.data.domain.PageRequest.of(0, 10), 1);
        when(userRepository.findAll(any(com.querydsl.core.types.Predicate.class), any(org.springframework.data.domain.Pageable.class)))
            .thenReturn(userPage);

        // Act
        PageResult<UserResponse> result = userService.getUsersPage(pageRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getContent().size());
    }

    @Test
    void getUsersPage_WithUsernameFilter_ShouldReturnFilteredResult() {
        // Arrange
        UserPageRequest pageRequest = new UserPageRequest();
        pageRequest.setPage(1);
        pageRequest.setSize(10);
        pageRequest.setUsername("test");

        List<User> users = List.of(user);
        Page<User> userPage = new PageImpl<>(users, org.springframework.data.domain.PageRequest.of(0, 10), 1);
        when(userRepository.findAll(any(com.querydsl.core.types.Predicate.class), any(org.springframework.data.domain.Pageable.class)))
            .thenReturn(userPage);

        // Act
        PageResult<UserResponse> result = userService.getUsersPage(pageRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotal());
    }

    @Test
    void getUsersPage_WithRoleFilter_ShouldReturnFilteredResult() {
        // Arrange
        UserPageRequest pageRequest = new UserPageRequest();
        pageRequest.setPage(1);
        pageRequest.setSize(10);
        pageRequest.setRole(UserRole.MEMBER);

        List<User> users = List.of(user);
        Page<User> userPage = new PageImpl<>(users, org.springframework.data.domain.PageRequest.of(0, 10), 1);
        when(userRepository.findAll(any(com.querydsl.core.types.Predicate.class), any(org.springframework.data.domain.Pageable.class)))
            .thenReturn(userPage);

        // Act
        PageResult<UserResponse> result = userService.getUsersPage(pageRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotal());
    }

    @Test
    void createUser_WithNullUsername_ShouldNotValidateUniqueness() {
        // Arrange
        CreateUserRequest createRequest = new CreateUserRequest();
        createRequest.setUsername(null);
        createRequest.setPassword("password");
        createRequest.setRole(UserRole.MEMBER);

        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        UserResponse response = userService.createUser(createRequest);

        // Assert
        assertNotNull(response);
        verify(userRepository, never()).existsByUsernameAndDeletedFalse(any());
    }
}