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
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, 40401, "User not found"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, 40101, "Invalid credentials");
    
    private final HttpStatus httpStatus;
    private final int code;
    private final String message;
}
