package com.yimusi.config;

import com.yimusi.common.util.OperatorUtil;
import java.util.Optional;
import org.springframework.data.domain.AuditorAware;

/**
 * 基于 Sa-Token 的审计人解析器，从当前登录上下文获取用户ID。
 */
public class SaTokenAuditorAware implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {
        return Optional.ofNullable(OperatorUtil.getOperator());
    }
}
