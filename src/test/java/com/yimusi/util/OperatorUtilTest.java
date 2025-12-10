package com.yimusi.util;

import cn.dev33.satoken.stp.StpUtil;
import com.yimusi.common.util.OperatorUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 操作者工具类单元测试
 *
 * <p>测试 {@link OperatorUtil} 的各种场景，包括：</p>
 * <ul>
 *   <li>正常登录状态下的操作者ID获取</li>
 *   <li>未登录状态下的默认返回</li>
 *   <li>异常处理场景</li>
 *   <li>测试场景下的降级处理</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class OperatorUtilTest {

    private MockedStatic<StpUtil> mockedStpUtil;

    @BeforeEach
    void setUp() {
        // 创建静态mock
        mockedStpUtil = mockStatic(StpUtil.class);
    }

    @AfterEach
    void tearDown() {
        // 关闭静态mock
        if (mockedStpUtil != null) {
            mockedStpUtil.close();
        }
    }

    @Test
    @DisplayName("获取操作者ID - 已登录状态下返回登录用户ID")
    void getOperator_WhenLoggedIn_ShouldReturnUserId() {
        // Arrange - 设置测试环境：用户已登录，返回登录用户ID
        mockedStpUtil.when(StpUtil::isLogin).thenReturn(true);
        mockedStpUtil.when(StpUtil::getLoginIdAsLong).thenReturn(123L);

        // Act - 执行获取操作者ID操作
        Long result = OperatorUtil.getOperator();

        // Assert - 验证返回正确的登录用户ID
        assertNotNull(result);
        assertEquals(123L, result);
        mockedStpUtil.verify(StpUtil::isLogin);
        mockedStpUtil.verify(StpUtil::getLoginIdAsLong);
    }

    @Test
    @DisplayName("获取操作者ID - 未登录状态下返回系统标识")
    void getOperator_WhenNotLoggedIn_ShouldReturnSystem() {
        // Arrange - 设置测试环境：用户未登录
        mockedStpUtil.when(StpUtil::isLogin).thenReturn(false);

        // Act - 执行获取操作者ID操作
        Long result = OperatorUtil.getOperator();

        // Assert - 验证返回系统标识
        assertNotNull(result);
        assertEquals(0L, result);
        mockedStpUtil.verify(StpUtil::isLogin);
        mockedStpUtil.verify(StpUtil::getLoginIdAsLong, never());
    }

    @Test
    @DisplayName("获取操作者ID - isLogin方法异常时返回系统标识")
    void getOperator_WhenIsLoginThrowsException_ShouldReturnSystem() {
        // Arrange - 设置测试环境：isLogin方法抛出异常
        mockedStpUtil.when(StpUtil::isLogin).thenThrow(new RuntimeException("SaToken context not available"));

        // Act - 执行获取操作者ID操作
        Long result = OperatorUtil.getOperator();

        // Assert - 验证异常处理后返回系统标识
        assertNotNull(result);
        assertEquals(0L, result);
        mockedStpUtil.verify(StpUtil::isLogin);
    }

    @Test
    @DisplayName("获取操作者ID - getLoginIdAsLong方法异常时返回系统标识")
    void getOperator_WhenGetLoginIdAsLongThrowsException_ShouldReturnSystem() {
        // Arrange - 设置测试环境：getLoginIdAsLong方法抛出异常，但isLogin返回true
        mockedStpUtil.when(StpUtil::isLogin).thenReturn(true);
        mockedStpUtil.when(StpUtil::getLoginIdAsLong).thenThrow(new RuntimeException("Token expired"));

        // Act - 执行获取操作者ID操作
        Long result = OperatorUtil.getOperator();

        // Assert - 验证异常处理后返回系统标识
        assertNotNull(result);
        assertEquals(0L, result);
        mockedStpUtil.verify(StpUtil::isLogin);
        mockedStpUtil.verify(StpUtil::getLoginIdAsLong);
    }

    @Test
    @DisplayName("获取操作者ID - 任意异常情况下都应返回系统标识")
    void getOperator_WhenAnyExceptionOccurs_ShouldReturnSystem() {
        // Arrange - 设置测试环境：各种异常情况
        String[] exceptions = {
            "NullPointerException",
            "IllegalStateException",
            "SecurityException",
            "CustomException"
        };

        for (String exceptionType : exceptions) {
            // 为每次异常测试重置mock
            mockedStpUtil.close();
            mockedStpUtil = mockStatic(StpUtil.class);

            mockExceptionBasedOnType(exceptionType);

            // Act
            Long result = OperatorUtil.getOperator();

            // Assert
            assertNotNull(result, "Should return system for " + exceptionType);
            assertEquals(0L, result, "Should return system for " + exceptionType);
        }
    }

    @Test
    @DisplayName("获取操作者ID - 测试不同长度的用户ID")
    void getOperator_WithVariousUserIdLengths_ShouldReturnCorrectResult() {
        // 测试不同长度的用户ID (Long类型)
        Long[] testUserIds = {
            1L,                    // 单数字
            123456789L,             // 普通长度
            1234567890123456789L,   // 长ID
            999999999999999999L     // 超长ID
        };

        for (Long userId : testUserIds) {
            // 为每次测试重置mock
            mockedStpUtil.close();
            mockedStpUtil = mockStatic(StpUtil.class);

            mockedStpUtil.when(StpUtil::isLogin).thenReturn(true);
            mockedStpUtil.when(StpUtil::getLoginIdAsLong).thenReturn(userId);

            // Act
            Long result = OperatorUtil.getOperator();

            // Assert
            assertNotNull(result);
            assertEquals(userId, result, "Should return the exact user ID: " + userId);
        }
    }

    @Test
    @DisplayName("获取操作者ID - 多次调用应保持结果一致")
    void getOperator_MultipleCalls_ShouldReturnConsistentResults() {
        // Arrange - 设置已登录状态
        mockedStpUtil.when(StpUtil::isLogin).thenReturn(true);
        mockedStpUtil.when(StpUtil::getLoginIdAsLong).thenReturn(123L);

        // Act & Assert - 多次调用应返回一致结果
        for (int i = 0; i < 10; i++) {
            Long result = OperatorUtil.getOperator();
            assertNotNull(result);
            assertEquals(123L, result, "Call " + (i + 1) + " should return consistent result");
        }

        // 验证确实只调用了一次登录检查方法（由于静态mock的特性）
        mockedStpUtil.verify(StpUtil::isLogin, atLeast(1));
    }

    /**
     * 根据异常类型设置相应的mock行为
     *
     * @param exceptionType 异常类型
     */
    private void mockExceptionBasedOnType(String exceptionType) {
        switch (exceptionType) {
            case "NullPointerException":
                mockedStpUtil.when(StpUtil::isLogin).thenThrow(new NullPointerException());
                break;
            case "IllegalStateException":
                mockedStpUtil.when(StpUtil::isLogin).thenThrow(new IllegalStateException("Invalid state"));
                break;
            case "SecurityException":
                mockedStpUtil.when(StpUtil::isLogin).thenThrow(new SecurityException("Access denied"));
                break;
            case "CustomException":
                mockedStpUtil.when(StpUtil::isLogin).thenThrow(new RuntimeException("Custom error"));
                break;
            default:
                mockedStpUtil.when(StpUtil::isLogin).thenThrow(new RuntimeException("Unknown exception"));
        }
    }
}