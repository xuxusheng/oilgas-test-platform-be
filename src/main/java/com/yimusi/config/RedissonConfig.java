package com.yimusi.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 客户端配置，基于 spring.data.redis 的连接信息。
 */
@Configuration
@ConditionalOnProperty(value = "yimusi.lock.redisson.enabled", havingValue = "true")
public class RedissonConfig {

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient(RedisProperties redisProperties) {
        Config config = new Config();
        SingleServerConfig singleServerConfig = config
            .useSingleServer()
            .setAddress(buildAddress(redisProperties))
            .setDatabase(redisProperties.getDatabase())
            .setConnectionMinimumIdleSize(4)
            .setConnectionPoolSize(16);

        if (redisProperties.getPassword() != null && !redisProperties.getPassword().isEmpty()) {
            singleServerConfig.setPassword(redisProperties.getPassword());
        }

        return Redisson.create(config);
    }

    private String buildAddress(RedisProperties redisProperties) {
        String schema = Boolean.TRUE.equals(redisProperties.getSsl()) ? "rediss://" : "redis://";
        return schema + redisProperties.getHost() + ":" + redisProperties.getPort();
    }
}
