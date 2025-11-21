package com.yimusi.common.exception;

/**
 * 请求参数错误异常（400 Bad Request）
 * 用于参数校验失败、参数格式错误等场景
 */
public class BadRequestException extends BusinessException {

    public BadRequestException(String message) {
        super(ErrorCode.BAD_REQUEST, message);
    }
}
