package com.yimusi;

import com.yimusi.config.TestAuditorConfig;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest
@Import(TestAuditorConfig.class)
public abstract class BaseIntegrationTest {

    /**
     * Defines and manages a MySQL test container.
     * The {@code @ServiceConnection} annotation automatically maps the running container's
     * connection details (JDBC URL, username, password) to the Spring Boot application's
     * datasource properties, replacing the need for {@code @DynamicPropertySource}.
     */
    @Container
    @ServiceConnection
    private static final MySQLContainer<?> MYSQL_CONTAINER = new MySQLContainer<>("mysql:8.0.36");

}
