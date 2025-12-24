package com.yimusi.exception;

import com.yimusi.common.exception.*;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import com.yimusi.common.model.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * GlobalExceptionHandler 单元测试
 * 测试所有异常处理方法的覆盖率
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleBusinessException() {
        // 给定
        BusinessException exception = new ResourceNotFoundException("用户不存在");

        // 当
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleBusinessException(exception);

        // 那么
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND.getCode(), response.getBody().getCode());
        assertEquals("用户不存在", response.getBody().getMessage());
    }

    @Test
    void testHandleBadRequestException() {
        // 给定
        BusinessException exception = new BadRequestException("无效的请求参数");

        // 当
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleBusinessException(exception);

        // 那么
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.BAD_REQUEST.getCode(), response.getBody().getCode());
        assertEquals("无效的请求参数", response.getBody().getMessage());
    }

    @Test
    void testHandleValidationException() {
        // 给定
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError1 = new FieldError("user", "username", "用户名不能为空");
        FieldError fieldError2 = new FieldError("user", "password", "密码长度必须在 6 到 30 个字符之间");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList(fieldError1, fieldError2));

        // 当
        ResponseEntity<ApiResponse<Void>> response =
            exceptionHandler.handleValidationException(exception);

        // 那么
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.VALIDATION_ERROR.getCode(), response.getBody().getCode());

        Map<String, String> errors = (Map<String, String>) response.getBody().getErrors();
        assertNotNull(errors);
        assertEquals(2, errors.size());
        assertEquals("用户名不能为空", errors.get("username"));
        assertEquals("密码长度必须在 6 到 30 个字符之间", errors.get("password"));
    }

    @Test
    void testHandleConstraintViolationException() {
        // 给定
        Set<ConstraintViolation<?>> violations = new HashSet<>();

        // 创建 mock 的 ConstraintViolation
        ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
        Path path1 = mock(Path.class);
        when(path1.toString()).thenReturn("getUserById.id");
        when(violation1.getPropertyPath()).thenReturn(path1);
        when(violation1.getMessage()).thenReturn("ID 必须大于 0");

        ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);
        Path path2 = mock(Path.class);
        when(path2.toString()).thenReturn("searchUsers.username");
        when(violation2.getPropertyPath()).thenReturn(path2);
        when(violation2.getMessage()).thenReturn("用户名长度必须在 3 到 50 个字符之间");

        violations.add(violation1);
        violations.add(violation2);

        ConstraintViolationException exception = new ConstraintViolationException(violations);

        // 当
        ResponseEntity<ApiResponse<Void>> response =
            exceptionHandler.handleConstraintViolationException(exception);

        // 那么
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.VALIDATION_ERROR.getCode(), response.getBody().getCode());

        Map<String, String> errors = (Map<String, String>) response.getBody().getErrors();
        assertNotNull(errors);
        assertEquals(2, errors.size());
        assertTrue(errors.containsKey("id"));
        assertTrue(errors.containsKey("username"));
    }

    @Test
    void testHandleMissingServletRequestParameterException() {
        // 给定
        MissingServletRequestParameterException exception =
            new MissingServletRequestParameterException("page", "int");

        // 当
        ResponseEntity<ApiResponse<Void>> response =
            exceptionHandler.handleMissingServletRequestParameterException(exception);

        // 那么
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.MISSING_REQUEST_PARAMETER.getCode(), response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("page"));
    }

    @Test
    void testHandleHttpRequestMethodNotSupportedException() {
        // 给定
        HttpRequestMethodNotSupportedException exception =
            new HttpRequestMethodNotSupportedException("POST", Arrays.asList("GET", "PUT"));

        // 当
        ResponseEntity<ApiResponse<Void>> response =
            exceptionHandler.handleHttpRequestMethodNotSupportedException(exception);

        // 那么
        assertNotNull(response);
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.METHOD_NOT_ALLOWED.getCode(), response.getBody().getCode());
    }

    @Test
    void testHandleGenericException() {
        // 给定
        Exception exception = new RuntimeException("意外的系统错误");

        // 当
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleGenericException(exception);

        // 那么
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), response.getBody().getCode());
        // 消息已改为更友好的中文提示
        assertEquals("系统内部发生错误，请联系管理员", response.getBody().getMessage());
        // 验证包含错误详情
        assertNotNull(response.getBody().getErrors());
    }

    @Test
    void testHandleNullPointerException() {
        // 给定 - 测试空指针异常被通用异常处理器捕获
        NullPointerException exception = new NullPointerException("Null value encountered");

        // 当
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleGenericException(exception);

        // 那么
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), response.getBody().getCode());
    }

    @Test
    void testHandleIllegalArgumentException() {
        // 给定 - 测试非法参数异常被通用异常处理器捕获
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument provided");

        // 当
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleGenericException(exception);

        // 那么
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), response.getBody().getCode());
    }

    @Test
    void testHandleNoResourceFoundException() {
        // 给定 - 模拟客户端请求了错误的路径 /auth/login (缺少 /api 前缀)
        // NoResourceFoundException 构造函数: (HttpMethod, resourcePath)
        NoResourceFoundException exception = new NoResourceFoundException(
            org.springframework.http.HttpMethod.GET,
            "auth/login"
        );

        // 当
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleNoResourceFoundException(exception);

        // 那么
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.API_ENDPOINT_NOT_FOUND.getCode(), response.getBody().getCode());
        // 验证消息包含路径信息
        String message = response.getBody().getMessage();
        assertNotNull(message);
        assertTrue(message.contains("auth/login") || message.contains("请检查URL路径是否正确"));

        // 验证错误详情
        Map<String, Object> errors = (Map<String, Object>) response.getBody().getErrors();
        assertNotNull(errors);
        assertTrue(errors.containsKey("path"));
        assertTrue(errors.containsKey("recommendation"));
    }
}
