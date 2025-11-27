package com.yimusi.exception;

import com.yimusi.common.exception.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 自定义异常类单元测试
 */
class CustomExceptionTest {

    @Test
    void testBusinessExceptionWithErrorCode() {
        // 给定
        ErrorCode errorCode = ErrorCode.RESOURCE_NOT_FOUND;

        // 当
        BusinessException exception = new BusinessException(errorCode);

        // 那么
        assertEquals(errorCode.getMessage(), exception.getMessage());
        assertEquals(errorCode.getCode(), exception.getCode());
        assertEquals(errorCode.getHttpStatus(), exception.getHttpStatus());
    }

    @Test
    void testBusinessExceptionWithCustomMessage() {
        // 给定
        ErrorCode errorCode = ErrorCode.RESOURCE_NOT_FOUND;
        String customMessage = "用户 ID 为 123 的资源不存在";

        // 当
        BusinessException exception = new BusinessException(errorCode, customMessage);

        // 那么
        assertEquals(customMessage, exception.getMessage());
        assertEquals(errorCode.getCode(), exception.getCode());
        assertEquals(errorCode.getHttpStatus(), exception.getHttpStatus());
    }

    @Test
    void testResourceNotFoundException() {
        // 给定
        String message = "用户不存在";

        // 当
        ResourceNotFoundException exception = new ResourceNotFoundException(message);

        // 那么
        assertEquals(message, exception.getMessage());
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND.getCode(), exception.getCode());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertInstanceOf(BusinessException.class, exception);
    }

    @Test
    void testBadRequestException() {
        // 给定
        String message = "无效的请求参数";

        // 当
        BadRequestException exception = new BadRequestException(message);

        // 那么
        assertEquals(message, exception.getMessage());
        assertEquals(ErrorCode.BAD_REQUEST.getCode(), exception.getCode());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertInstanceOf(BusinessException.class, exception);
    }

    @Test
    void testValidationException() {
        // 给定
        String message = "字段验证失败";

        // 当
        ValidationException exception = new ValidationException(message);

        // 那么
        assertEquals(message, exception.getMessage());
        assertEquals(ErrorCode.VALIDATION_ERROR.getCode(), exception.getCode());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertInstanceOf(BusinessException.class, exception);
    }

    @Test
    void testErrorCodeValues() {
        // 测试所有 ErrorCode 枚举值
        ErrorCode[] allCodes = ErrorCode.values();
        assertTrue(allCodes.length > 0);

        // 测试每个枚举值都有完整的属性
        for (ErrorCode code : allCodes) {
            assertNotNull(code.getHttpStatus());
            assertTrue(code.getCode() > 0);
            assertNotNull(code.getMessage());
            assertFalse(code.getMessage().isEmpty());
        }
    }

    @Test
    void testErrorCodeSpecificValues() {
        // 测试 INTERNAL_SERVER_ERROR
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus());
        assertEquals(50000, ErrorCode.INTERNAL_SERVER_ERROR.getCode());

        // 测试 BAD_REQUEST
        assertEquals(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST.getHttpStatus());
        assertEquals(40000, ErrorCode.BAD_REQUEST.getCode());

        // 测试 RESOURCE_NOT_FOUND
        assertEquals(HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND.getHttpStatus());
        assertEquals(40400, ErrorCode.RESOURCE_NOT_FOUND.getCode());

        // 测试 VALIDATION_ERROR
        assertEquals(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR.getHttpStatus());
        assertEquals(40001, ErrorCode.VALIDATION_ERROR.getCode());

        // 测试 INVALID_CREDENTIALS
        assertEquals(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_CREDENTIALS.getHttpStatus());
        assertEquals(40101, ErrorCode.INVALID_CREDENTIALS.getCode());
    }

    @Test
    void testExceptionInheritance() {
        // 测试异常继承关系
        ResourceNotFoundException notFoundEx = new ResourceNotFoundException("test");
        assertTrue(notFoundEx instanceof BusinessException);
        assertTrue(notFoundEx instanceof RuntimeException);

        BadRequestException badRequestEx = new BadRequestException("test");
        assertTrue(badRequestEx instanceof BusinessException);
        assertTrue(badRequestEx instanceof RuntimeException);

        ValidationException validationEx = new ValidationException("test");
        assertTrue(validationEx instanceof BusinessException);
        assertTrue(validationEx instanceof RuntimeException);
    }
}
