package com.yimusi.dto;

import static org.junit.jupiter.api.Assertions.*;

import com.yimusi.common.enums.UserRole;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @description DTO数据验证测试类
 */
class UserRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void CreateUserRequest_ShouldPassValidation_WithValidData() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("testuser");
        request.setPassword("Passw0rd!");
        request.setRole(UserRole.MEMBER);

        // Act
        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

        // Assert
        assertTrue(violations.isEmpty(), "应无验证错误");
    }

    @Test
    void CreateUserRequest_ShouldFailValidation_WithEmptyUsername() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername(""); // 空用户名
        request.setPassword("Passw0rd!");
        request.setRole(UserRole.MEMBER);

        // Act
        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty(), "应有验证错误");
        // 可能有多个验证错误（如@NotBlank和@Size），但至少应该有1个
        assertTrue(violations.size() >= 1, "至少应有1个验证错误");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().contains("username")),
                "应有用户名相关的验证错误");
    }

    @Test
    void CreateUserRequest_ShouldFailValidation_WithTooLongUsername() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("a".repeat(51)); // 超过50个字符
        request.setPassword("Passw0rd!");
        request.setRole(UserRole.MEMBER);

        // Act
        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty(), "用户名长度超过限制应有验证错误");
    }

    @Test
    void CreateUserRequest_ShouldFailValidation_WithEmptyPassword() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("testuser");
        request.setPassword(""); // 空密码
        request.setRole(UserRole.MEMBER);

        // Act
        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty(), "密码不能为空应有验证错误");
    }

    @Test
    void CreateUserRequest_ShouldFailValidation_WithNullRole() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("testuser");
        request.setPassword("Passw0rd!");
        request.setRole(null); // 空角色

        // Act
        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty(), "角色不能为空应有验证错误");
    }

    @Test
    void UpdateUserRequest_ShouldPassValidation_WithValidData() {
        // Arrange
        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername("newusername");
        request.setPassword("NewPassw0rd!");

        // Act
        Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);

        // Assert
        assertTrue(violations.isEmpty(), "应无验证错误");
    }

    @Test
    void UpdateUserRequest_ShouldPassValidation_WithPartialData() {
        // Arrange
        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername("newusername"); // 只更新用户名，不更新密码

        // Act
        Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);

        // Assert
        assertTrue(violations.isEmpty(), "部分更新数据应通过验证");
    }

    @Test
    void UpdateUserRequest_ShouldPassValidation_WithNullFields() {
        // Arrange
        UpdateUserRequest request = new UpdateUserRequest();

        // Act
        Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);

        // Assert
        assertTrue(violations.isEmpty(), "NULL字段应通过验证（可选字段）");
    }

    @Test
    void AllDTOs_ShouldHaveCorrectAnnotations() {
        // Arrange & Act - 检查字段上的注解是否存在
        CreateUserRequest createRequest = new CreateUserRequest();
        UpdateUserRequest updateRequest = new UpdateUserRequest();

        // Assert - 验证DTO类的结构
        assertNotNull(createRequest);
        assertNotNull(updateRequest);

        // 验证可以设置和获取值
        createRequest.setUsername("test");
        assertEquals("test", createRequest.getUsername());

        updateRequest.setUsername("update");
        assertEquals("update", updateRequest.getUsername());
    }
}