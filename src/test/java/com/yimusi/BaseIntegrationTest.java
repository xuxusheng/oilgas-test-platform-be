package com.yimusi;

import com.yimusi.config.TestAuditorConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MySQLContainer;
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
}
