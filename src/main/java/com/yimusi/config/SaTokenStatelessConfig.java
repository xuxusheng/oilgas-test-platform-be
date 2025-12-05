package com.yimusi.config;

import cn.dev33.satoken.jwt.StpLogicJwtForStateless;
import cn.dev33.satoken.stp.StpLogic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sa-Token 配置：使用无状态的 JWT 模式。
 *
 * StpLogicJwtForSimple 完全基于 JWT 自包含信息，不再依赖服务端 Session/存储，
 * 适用于前后端分离的 Bearer Token 认证。
 */
@Configuration
public class SaTokenStatelessConfig {

    @Bean
    public StpLogic stpLogic() {
        return new StpLogicJwtForStateless();
    }
}
