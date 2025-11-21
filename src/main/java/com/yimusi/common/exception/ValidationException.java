package com.yimusi.common.exception;

/**
 * 数据校验异常（400 Bad Request）
 * 用于业务数据校验失败等场景
 */
public class ValidationException extends BusinessException {

    public ValidationException(String message) {
        super(ErrorCode.VALIDATION_ERROR, message);
    }
}
