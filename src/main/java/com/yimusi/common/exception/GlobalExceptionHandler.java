package com.yimusi.common.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.exception.NotSafeException;
import cn.dev33.satoken.exception.SaTokenException;
import com.yimusi.common.model.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理自定义业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        log.warn("Business exception occurred: {}", ex.getMessage());
        ApiResponse<Void> apiResponse = ApiResponse.error(ex.getCode(), ex.getMessage());
        return new ResponseEntity<>(apiResponse, ex.getHttpStatus());
    }

    /**
     * 处理未登录异常 (排除账号在其他设备登录的情况)
     */
    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotLoginException(NotLoginException ex) {
        // 如果是账号被顶下线的情况，交给并发登录异常处理器处理
        if (NotLoginException.BE_REPLACED.equals(ex.getType())) {
            return handleConcurrentLoginException(ex);
        }
        ErrorCode errorCode = ErrorCode.NOT_LOGIN;

        Map<String, Object> errorDetails = new LinkedHashMap<>();
        errorDetails.put("type", ex.getType());
        errorDetails.put("message", ex.getMessage());
        errorDetails.put("timestamp", new Date());
        errorDetails.put("recommendation", "请重新登录或联系管理员");

        String errorMessage = String.format(
            "%s - Token类型: %s, 账号类型: %s, 提示: %s",
            errorCode.getMessage(),
            ex.getType(),
            ex.getLoginType(),
            ex.getMessage()
        );

        log.warn("用户未登录异常 - 类型: {}, 账号类型: {} | {}", ex.getType(), ex.getLoginType(), ex.getMessage());

        ApiResponse<Void> apiResponse = ApiResponse.error(errorCode.getCode(), errorMessage, errorDetails);
        return new ResponseEntity<>(apiResponse, errorCode.getHttpStatus());
    }

    /**
     * 处理权限不足异常
     */
    @ExceptionHandler(NotPermissionException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotPermissionException(NotPermissionException ex) {
        ErrorCode errorCode = ErrorCode.NO_PERMISSION;

        Map<String, Object> errorDetails = new LinkedHashMap<>();
        errorDetails.put("requiredPermission", ex.getPermission());
        errorDetails.put("message", ex.getMessage());
        errorDetails.put("timestamp", new Date());
        errorDetails.put("recommendation", "请联系管理员申请相关权限");

        String errorMessage = String.format(
            "%s - 缺失权限: %s, 账号类型: %s, 提示: %s",
            errorCode.getMessage(),
            ex.getPermission(),
            ex.getLoginType(),
            ex.getMessage()
        );

        log.warn(
            "用户权限不足 - 需要权限: {}, 账号类型: {} | {}",
            ex.getPermission(),
            ex.getLoginType(),
            ex.getMessage()
        );

        ApiResponse<Void> apiResponse = ApiResponse.error(errorCode.getCode(), errorMessage, errorDetails);
        return new ResponseEntity<>(apiResponse, errorCode.getHttpStatus());
    }

    /**
     * 处理角色不符异常
     */
    @ExceptionHandler(NotRoleException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotRoleException(NotRoleException ex) {
        ErrorCode errorCode = ErrorCode.NO_ROLE;

        Map<String, Object> errorDetails = new LinkedHashMap<>();
        errorDetails.put("requiredRole", ex.getRole());
        errorDetails.put("message", ex.getMessage());
        errorDetails.put("timestamp", new Date());
        errorDetails.put("recommendation", "您的角色权限不足，请联系管理员");

        String errorMessage = String.format(
            "%s - 需要角色: %s, 账号类型: %s, 提示: %s",
            errorCode.getMessage(),
            ex.getRole(),
            ex.getLoginType(),
            ex.getMessage()
        );

        log.warn("用户角色不符 - 需要角色: {}, 账号类型: {} | {}", ex.getRole(), ex.getLoginType(), ex.getMessage());

        ApiResponse<Void> apiResponse = ApiResponse.error(errorCode.getCode(), errorMessage, errorDetails);
        return new ResponseEntity<>(apiResponse, errorCode.getHttpStatus());
    }

    /**
     * 处理Token过期异常
     */
    @ExceptionHandler(NotSafeException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotSafeException(NotSafeException ex) {
        ErrorCode errorCode = ErrorCode.TOKEN_EXPIRED;

        Map<String, Object> errorDetails = new LinkedHashMap<>();
        errorDetails.put("tokenValue", ex.getTokenValue());
        errorDetails.put("service", ex.getService());
        errorDetails.put("message", ex.getMessage());
        errorDetails.put("timestamp", new Date());
        errorDetails.put("recommendation", "请重新获取有效的Token");

        String errorMessage = String.format(
            "%s - Token值: %s, 服务: %s, 账号类型: %s, 提示: %s",
            errorCode.getMessage(),
            ex.getTokenValue(),
            ex.getService(),
            ex.getLoginType(),
            ex.getMessage()
        );

        log.warn(
            "Token安全异常 - Token值: {}, 服务: {}, 账号类型: {} | {}",
            ex.getTokenValue(),
            ex.getService(),
            ex.getLoginType(),
            ex.getMessage()
        );

        ApiResponse<Void> apiResponse = ApiResponse.error(errorCode.getCode(), errorMessage, errorDetails);
        return new ResponseEntity<>(apiResponse, errorCode.getHttpStatus());
    }

    /**
     * 处理账号在其他设备登录异常
     */
    public ResponseEntity<ApiResponse<Void>> handleConcurrentLoginException(NotLoginException ex) {
        ErrorCode errorCode = ErrorCode.CONCURRENT_LOGIN;

        Map<String, Object> errorDetails = new LinkedHashMap<>();
        errorDetails.put("type", ex.getType());
        errorDetails.put("message", ex.getMessage());
        errorDetails.put("timestamp", new Date());
        errorDetails.put("concurrentDevice", "其他设备");
        errorDetails.put("recommendation", "您的账号已在其他设备登录，如非本人操作请注意账号安全");

        String errorMessage = String.format(
            "%s - 账号已被在其他设备登录, Token类型: %s, 账号类型: %s, 提示: %s",
            errorCode.getMessage(),
            ex.getType(),
            ex.getLoginType(),
            ex.getMessage()
        );

        log.warn(
            "账号并发登录异常 - Token类型: {}, 账号类型: {} | {}",
            ex.getType(),
            ex.getLoginType(),
            ex.getMessage()
        );

        ApiResponse<Void> apiResponse = ApiResponse.error(errorCode.getCode(), errorMessage, errorDetails);
        return new ResponseEntity<>(apiResponse, errorCode.getHttpStatus());
    }

    /**
     * 处理通用Token异常
     */
    @ExceptionHandler(SaTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleSaTokenException(SaTokenException ex) {
        ErrorCode errorCode = ErrorCode.INVALID_TOKEN;

        Map<String, Object> errorDetails = new LinkedHashMap<>();
        errorDetails.put("type", "未知的Token异常");
        errorDetails.put("message", ex.getMessage());
        errorDetails.put("timestamp", new Date());
        errorDetails.put("recommendation", "请清除本地Token后重新登录");

        String errorMessage = String.format("Token 验证失败 - %s", ex.getMessage());

        log.error("Sa-Token通用异常", ex);

        ApiResponse<Void> apiResponse = ApiResponse.error(errorCode.getCode(), errorMessage, errorDetails);
        return new ResponseEntity<>(apiResponse, errorCode.getHttpStatus());
    }

    /**
     * 处理 @RequestBody 参数校验异常 (返回结构化的错误信息)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex
            .getBindingResult()
            .getFieldErrors()
            .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
        log.warn("Validation failed for @RequestBody: {}", errors);
        ApiResponse<Void> apiResponse = ApiResponse.error(errorCode.getCode(), errorCode.getMessage(), errors);
        return new ResponseEntity<>(apiResponse, errorCode.getHttpStatus());
    }

    /**
     * 处理 @RequestParam 和 @PathVariable 参数校验异常 (返回结构化的错误信息)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, String> errors = ex
            .getConstraintViolations()
            .stream()
            .collect(
                Collectors.toMap(
                    violation ->
                        violation
                            .getPropertyPath()
                            .toString()
                            .substring(violation.getPropertyPath().toString().lastIndexOf('.') + 1),
                    ConstraintViolation::getMessage,
                    (existing, replacement) -> existing,
                    LinkedHashMap::new
                )
            );

        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
        log.warn("Validation failed for @RequestParam/@PathVariable: {}", errors);
        ApiResponse<Void> apiResponse = ApiResponse.error(errorCode.getCode(), errorCode.getMessage(), errors);
        return new ResponseEntity<>(apiResponse, errorCode.getHttpStatus());
    }

    /**
     * 处理缺少请求参数的异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameterException(
        MissingServletRequestParameterException ex
    ) {
        ErrorCode errorCode = ErrorCode.MISSING_REQUEST_PARAMETER;
        String message = String.format("Missing parameter: %s", ex.getParameterName());
        log.warn(message);
        ApiResponse<Void> apiResponse = ApiResponse.error(errorCode.getCode(), message);
        return new ResponseEntity<>(apiResponse, errorCode.getHttpStatus());
    }

    /**
     * 处理不支持的HTTP请求方法
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupportedException(
        HttpRequestMethodNotSupportedException ex
    ) {
        ErrorCode errorCode = ErrorCode.METHOD_NOT_ALLOWED;
        log.warn("Method not supported: {}", ex.getMethod());
        ApiResponse<Void> apiResponse = ApiResponse.error(errorCode.getCode(), errorCode.getMessage());
        return new ResponseEntity<>(apiResponse, errorCode.getHttpStatus());
    }

    /**
     * 处理所有其他未捕获的异常 (兜底)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        log.error("An unexpected error occurred", ex);
        ApiResponse<Void> apiResponse = ApiResponse.error(errorCode.getCode(), errorCode.getMessage());
        return new ResponseEntity<>(apiResponse, errorCode.getHttpStatus());
    }
}
