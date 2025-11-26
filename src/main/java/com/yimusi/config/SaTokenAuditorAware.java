package com.yimusi.config;

import cn.dev33.satoken.stp.StpUtil;
import java.util.Optional;
import org.springframework.data.domain.AuditorAware;

/**
 * 基于 Sa-Token 的审计人解析器，从当前登录上下文获取用户名。
 */
public class SaTokenAuditorAware implements AuditorAware<String> {

    private static final String DEFAULT_AUDITOR = "system";

    @Override
    public Optional<String> getCurrentAuditor() {
        if (StpUtil.isLogin()) {
            return Optional.ofNullable(StpUtil.getLoginIdAsString());
        }
        return Optional.of(DEFAULT_AUDITOR);
    }
}
