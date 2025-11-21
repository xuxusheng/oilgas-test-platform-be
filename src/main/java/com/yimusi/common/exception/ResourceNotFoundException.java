package com.yimusi.common.exception;

/**
 * 资源不存在异常（404 Not Found）
 * 用于查询的资源不存在等场景
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message);
    }
}
