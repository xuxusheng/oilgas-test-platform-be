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
}