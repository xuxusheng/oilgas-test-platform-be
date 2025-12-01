package com.yimusi.common.log;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.UUID;

/**
 * MDC 过滤器 - 自动添加请求上下文到日志
 *
 * 功能：
 * 1. 生成唯一追踪ID (traceId)，用于分布式链路追踪
 * 2. 记录用户信息 (userId) - 如果已登录
 * 3. 记录客户端IP地址和请求方法
 * 4. 记录请求耗时并自动选择日志级别
 * 5. 支持通过 HTTP 头传播 traceId (X-Trace-ID)
 * 6. 自动清理 MDC 上下文，避免内存泄漏
 *
 * 最佳实践：
 * - 与 logback-spring.xml 中的 MDC 字段保持一致
 * - 使用异步日志避免性能影响
 * - 支持分布式追踪系统集成（如 Zipkin, Jaeger）
 */
@Component
@Slf4j
public class MDCFilter implements Filter {

    // MDC 字段名称（与 logback-spring.xml 保持一致）
    private static final String TRACE_ID = "traceId";
    private static final String USER_ID = "userId";
    private static final String USER_IP = "ip";
    private static final String REQUEST_METHOD = "method";
    private static final String REQUEST_URI = "uri";
    private static final String REQUEST_START_TIME = "startTime";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        long startTime = System.currentTimeMillis();

        try {
            // 设置请求信息到MDC
            setupMDC(httpRequest, httpResponse);

            // 包装响应以支持多次读取（用于日志记录）
            ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(httpRequest);
            ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(httpResponse);

            chain.doFilter(requestWrapper, responseWrapper);

            // 记录请求完成日志
            logRequestCompletion(requestWrapper, responseWrapper, startTime);

            // 重要：将缓存的响应体写回到实际响应中
            responseWrapper.copyBodyToResponse();

        } catch (Exception e) {
            MDC.put("error", e.getMessage());
            throw e;
        } finally {
            // 清理MDC
            clearMDC();
        }
    }

    private void setupMDC(HttpServletRequest request, HttpServletResponse response) {
        // 生成追踪ID (traceId)
        String traceId = generateTraceId(request);
        MDC.put(TRACE_ID, traceId);

        // 设置IP地址
        String ipAddress = getClientIpAddress(request);
        MDC.put(USER_IP, ipAddress);

        // 设置HTTP方法
        MDC.put(REQUEST_METHOD, request.getMethod());

        // 设置请求URI
        MDC.put(REQUEST_URI, request.getRequestURI());

        // 设置用户信息（如果已登录）
        if (StpUtil.isLogin()) {
            MDC.put(USER_ID, StpUtil.getLoginIdAsString());
        }

        // 设置开始时间
        MDC.put(REQUEST_START_TIME, String.valueOf(System.currentTimeMillis()));

        // 设置响应头-用于前端跟踪（使用 X-Trace-ID）
        response.setHeader("X-Trace-ID", traceId);
        response.setHeader("X-Request-ID", traceId); // 兼容旧版本
    }

    private void clearMDC() {
        MDC.remove(TRACE_ID);
        MDC.remove(USER_ID);
        MDC.remove(USER_IP);
        MDC.remove(REQUEST_METHOD);
        MDC.remove(REQUEST_URI);
        MDC.remove(REQUEST_START_TIME);
        MDC.remove("error");
    }

    private void logRequestCompletion(ContentCachingRequestWrapper request,
                                      ContentCachingResponseWrapper response,
                                      long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        int status = response.getStatus();

        // 根据状态码选择日志级别
        if (status >= 400 && status < 500) {
            log.warn("Request completed - Status: {} | Duration: {}ms | Method: {} | URI: {}",
                    status, duration, MDC.get(REQUEST_METHOD), MDC.get(REQUEST_URI));
        } else if (status >= 500) {
            String error = MDC.get("error");
            if (error == null) error = "Unknown";
            log.error("Request failed - Status: {} | Duration: {}ms | Method: {} | URI: {} | Error: {}",
                    status, duration, MDC.get(REQUEST_METHOD), MDC.get(REQUEST_URI), error);
        } else {
            log.info("Request completed - Status: {} | Duration: {}ms | Method: {} | URI: {}",
                    status, duration, MDC.get(REQUEST_METHOD), MDC.get(REQUEST_URI));
        }
    }

    /**
     * 生成唯一追踪ID (traceId)
     * 优先从请求头获取，支持分布式追踪传播
     */
    private String generateTraceId(HttpServletRequest request) {
        // 优先从 X-Trace-ID 获取（标准做法）
        String traceId = request.getHeader("X-Trace-ID");
        if (traceId == null || traceId.trim().isEmpty()) {
            // 兼容旧的 X-Request-ID
            traceId = request.getHeader("X-Request-ID");
        }
        if (traceId == null || traceId.trim().isEmpty()) {
            // 生成新的 traceId
            traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        }
        return traceId;
    }

    /**
     * 获取客户端IP地址（支持代理）
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] ipHeaders = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR"
        };

        for (String header : ipHeaders) {
            String ip = request.getHeader(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                // 处理多代理情况，取第一个IP
                if (ip.contains(",")) {
                    return ip.split(",")[0].trim();
                }
                return ip;
            }
        }
        return request.getRemoteAddr();
    }
}