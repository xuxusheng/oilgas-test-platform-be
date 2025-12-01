package com.yimusi;

import com.yimusi.config.TestAuditorConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * 基础集成测试类
 * 所有的集成测试都应该继承这个类，它提供了共享的测试容器和Spring Boot测试上下文
 */
@Testcontainers
@ActiveProfiles("test")
@SpringBootTest
@Import(TestAuditorConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class BaseIntegrationTest {

    /**
     * 定义和管理MySQL测试容器
     * @ServiceConnection 注解自动将运行中的容器连接信息（JDBC URL、用户名、密码）映射到
     * Spring Boot应用程序的数据源属性，无需使用@DynamicPropertySource
     * 容器是静态的，在所有测试类之间共享
     */
    @Container
    @ServiceConnection
    @SuppressWarnings("resource") // 资源由 Testcontainers 扩展自动管理
    private static final MySQLContainer<?> MYSQL_CONTAINER = new MySQLContainer<>("mysql:8.0.36").withReuse(false);

    /**
     * 定义和管理Redis测试容器
     * 为了支持 Redisson 分布式锁功能的集成测试
     * 使用 @DynamicPropertySource 动态配置 Redis 连接
     */
    @Container
    @SuppressWarnings("resource")
    private static final GenericContainer<?> REDIS_CONTAINER = new GenericContainer<>("redis:7.0")
            .withReuse(false)
            .withExposedPorts(6379)
            .withLogConsumer(new Slf4jLogConsumer(org.slf4j.LoggerFactory.getLogger(GenericContainer.class)));

    /**
     * 配置动态属性，将测试容器的连接信息注入到 Spring 环境中
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // 配置 Redis 连接信息
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379));
        registry.add("spring.data.redis.database", () -> 0);
    }
}
