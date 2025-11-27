package com.yimusi.config;

import com.yimusi.common.log.MDCFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 过滤器配置类
 * 配置全局过滤器：MDC Filter
 */
@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<MDCFilter> mdCFilterRegistration(MDCFilter mdcFilter) {
        FilterRegistrationBean<MDCFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(mdcFilter);
        registration.addUrlPatterns("/*");
        registration.setName("MDCFilter");
        registration.setOrder(1); // 最先执行
        return registration;
    }
}