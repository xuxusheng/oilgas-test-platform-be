package com.yimusi.common.exception;

/**
 * 序列号生成异常
 * 用于序列号生成过程中的各类错误场景，如获取分布式锁超时、锁被中断等
 */
public class SequenceGenerationException extends BusinessException {

    public SequenceGenerationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public SequenceGenerationException(ErrorCode errorCode, String customMessage) {
        super(errorCode, customMessage);
    }
}
