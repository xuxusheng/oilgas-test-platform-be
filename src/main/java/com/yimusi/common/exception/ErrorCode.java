package com.yimusi.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // --- 系统级错误 ---
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 50000, "Internal Server Error"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, 40000, "Bad Request"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, 40500, "Request method not supported"),

    // --- 参数校验错误 ---
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, 40001, "Validation failed"),
    MISSING_REQUEST_PARAMETER(HttpStatus.BAD_REQUEST, 40002, "Missing request parameter"),

    // --- 业务错误 (示例) ---
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, 40400, "Resource not found"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, 40101, "Invalid credentials"),

    // --- Sa-Token 相关错误 ---
    NOT_LOGIN(HttpStatus.UNAUTHORIZED, 40102, "用户未登录或登录已过期"),
    NO_PERMISSION(HttpStatus.FORBIDDEN, 40301, "用户权限不足"),
    NO_ROLE(HttpStatus.FORBIDDEN, 40302, "用户角色不符"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, 40103, "Token无效或已被禁用"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, 40104, "Token已过期"),
    DEVICE_MISMATCH(HttpStatus.UNAUTHORIZED, 40105, "设备信息不匹配"),
    CONCURRENT_LOGIN(HttpStatus.UNAUTHORIZED, 40106, "账号在其他设备登录，当前设备已下线");

    private final HttpStatus httpStatus;
    private final int code;
    private final String message;
}
