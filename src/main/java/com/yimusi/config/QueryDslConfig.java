package com.yimusi.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 为 QueryDSL 提供核心配置.
 * <p>
 * 这个配置类向 Spring 上下文注册了一个 {@link JPAQueryFactory} 的 Bean.
 * 该 Bean 是构建类型安全的 JPA 查询所必需的, 可以被注入到任何 Spring 组件中 (例如, service 或 repository).
 */
@Configuration
public class QueryDslConfig {

    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}
