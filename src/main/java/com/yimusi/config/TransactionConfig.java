package com.yimusi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 事务配置类
 * 提供自定义的 TransactionTemplate 配置
 */
@Configuration
public class TransactionConfig {

    /**
     * 配置全局 TransactionTemplate Bean
     *
     * @param transactionManager 平台事务管理器
     * @return 配置好的 TransactionTemplate
     */
    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);

        // 设置为读写事务（默认就是 false）
        template.setReadOnly(false);

        // 设置事务传播行为为 REQUIRED（默认就是这个）
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        return template;
    }
}