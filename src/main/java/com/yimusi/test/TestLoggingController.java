package com.yimusi.test;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试日志功能的控制器
 * 用于验证MDC和Logback配置
 */
@Slf4j
@RestController
@RequestMapping("/api/test")
public class TestLoggingController {

    @GetMapping("/log")
    public Map<String, Object> testLog() {
        // 添加自定义MDC上下文
        MDC.put("customField", "test-value");
        MDC.put("operation", "test-logging");

        log.debug("This is a debug message");
        log.info("This is an info message");
        log.warn("This is a warning message");

        Map<String, Object> result = new HashMap<>();
        result.put("message", "日志测试完成");
        result.put("requestId", MDC.get("requestId"));
        result.put("userId", MDC.get("userId"));
        result.put("ip", MDC.get("ip"));

        return result;
    }

    @GetMapping("/error")
    public Map<String, Object> testErrorLog() {
        log.error("Test error log with stack trace", new RuntimeException("Test exception"));

        Map<String, Object> result = new HashMap<>();
        result.put("message", "错误日志测试完成");
        return result;
    }

    @GetMapping("/mdc")
    public Map<String, Object> testMDC() {
        log.info("MDC context test - collecting current context");

        Map<String, Object> context = new HashMap<>();
        context.put("requestId", MDC.get("requestId"));
        context.put("userId", MDC.get("userId"));
        context.put("ip", MDC.get("ip"));
        context.put("method", MDC.get("method"));
        context.put("uri", MDC.get("uri"));
        context.put("startTime", MDC.get("startTime"));

        return context;
    }
}