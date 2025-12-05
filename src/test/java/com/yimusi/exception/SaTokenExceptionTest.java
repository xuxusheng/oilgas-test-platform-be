package com.yimusi.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.exception.SaTokenException;
import com.yimusi.common.exception.GlobalExceptionHandler;
import com.yimusi.common.model.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sa-Token 异常处理器测试类
 * 验证新增的 sa-token 异常处理功能是否正常工作
 */
public class SaTokenExceptionTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleNotLoginException() {
        // 模拟 NotLoginException
        NotLoginException ex = new NotLoginException("token 已过期", "user", NotLoginException.TOKEN_TIMEOUT);

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleNotLoginException(ex);

        assertEquals(40102, response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("用户未登录或登录已过期"));
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testHandleNotPermissionException() {
        // 模拟 NotPermissionException
        NotPermissionException ex = new NotPermissionException("user:delete", "user");

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleNotPermissionException(ex);

        assertEquals(40301, response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("用户权限不足"));
        assertTrue(response.getBody().getMessage().contains("user:delete"));
        assertEquals(403, response.getStatusCode().value());

        // 验证 details 中包含权限信息
        Map<String, Object> details = (Map<String, Object>) response.getBody().getErrors();
        assertEquals("user:delete", details.get("requiredPermission"));
        assertEquals("请联系管理员申请相关权限", details.get("recommendation"));
    }

    @Test
    void testHandleNotRoleException() {
        // 模拟 NotRoleException
        NotRoleException ex = new NotRoleException("admin", "user");

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleNotRoleException(ex);

        assertEquals(40302, response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("用户角色不符"));
        assertTrue(response.getBody().getMessage().contains("admin"));
        assertEquals(403, response.getStatusCode().value());

        // 验证details中包含角色信息
        Map<String, Object> details = (Map<String, Object>) response.getBody().getErrors();
        assertEquals("admin", details.get("requiredRole"));
        assertEquals("您的角色权限不足，请联系管理员", details.get("recommendation"));
    }

    @Test
    void testHandleConcurrentLoginException() {
        // 模拟账号被顶下线的情况
        NotLoginException ex = new NotLoginException("账号已被其他设备登录", "user", NotLoginException.BE_REPLACED);

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleNotLoginException(ex);

        assertEquals(40106, response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("账号在其他设备登录"));
        assertEquals(401, response.getStatusCode().value());

        // 验证details中包含并发登录信息
        Map<String, Object> details = (Map<String, Object>) response.getBody().getErrors();
        assertEquals("其他设备", details.get("concurrentDevice"));
        assertEquals("您的账号已在其他设备登录，如非本人操作请注意账号安全", details.get("recommendation"));
    }

    @Test
    void testHandleSaTokenException() {
        // 模拟通用token异常
        SaTokenException ex = new SaTokenException("无效的 token 格式");

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleSaTokenException(ex);

        assertEquals(40103, response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("Token 验证失败"));
        assertTrue(response.getBody().getMessage().contains("无效的 token 格式"));
        assertEquals(401, response.getStatusCode().value());

        // 验证details中包含错误信息
        Map<String, Object> details = (Map<String, Object>) response.getBody().getErrors();
        assertEquals("未知的Token异常", details.get("type"));
        assertEquals("请清除本地Token后重新登录", details.get("recommendation"));
    }

    @Test
    void testErrorResponseStructure() {
        // 验证所有sa-token异常都返回统一的错误响应结构
        NotLoginException notLoginEx = new NotLoginException("未登录", "user", NotLoginException.NOT_TOKEN);
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleNotLoginException(notLoginEx);

        ApiResponse<Void> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.getCode() > 0);
        assertNotNull(body.getMessage());
        assertNotNull(body.getErrors());

        Map<String, Object> errorDetails = (Map<String, Object>) body.getErrors();
        assertTrue(errorDetails.containsKey("type"));
        assertTrue(errorDetails.containsKey("message"));
        assertTrue(errorDetails.containsKey("timestamp"));
        assertTrue(errorDetails.containsKey("recommendation"));
    }
}
