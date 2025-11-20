package com.yimusi.common.exception;

import com.yimusi.common.model.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
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
     * 处理 @RequestBody 参数校验异常 (返回结构化的错误信息)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
        MethodArgumentNotValidException ex
    ) {
        Map<String, String> errors = new HashMap<>();
        ex
            .getBindingResult()
            .getFieldErrors()
            .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
        log.warn("Validation failed for @RequestBody: {}", errors);
        ApiResponse<Map<String, String>> apiResponse = ApiResponse.error(
            errorCode.getCode(),
            errorCode.getMessage(),
            errors
        );
        return new ResponseEntity<>(apiResponse, errorCode.getHttpStatus());
    }

    /**
     * 处理 @RequestParam 和 @PathVariable 参数校验异常 (返回结构化的错误信息)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolationException(
        ConstraintViolationException ex
    ) {
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
                    ConstraintViolation::getMessage
                )
            );

        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
        log.warn("Validation failed for @RequestParam/@PathVariable: {}", errors);
        ApiResponse<Map<String, String>> apiResponse = ApiResponse.error(
            errorCode.getCode(),
            errorCode.getMessage(),
            errors
        );
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
