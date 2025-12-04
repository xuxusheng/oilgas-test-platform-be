# Docker Compose 开发环境配置

这个文档描述了如何使用 Docker Compose 快速启动本地开发环境的 Redis 和 MySQL 依赖。

## 快速开始

### 1. 启动服务

```bash
docker-compose up -d
```

### 2. 查看服务状态

```bash
docker-compose ps
```

### 3. 查看日志

```bash
docker-compose logs -f
```

### 4. 停止服务

```bash
docker-compose down
```

或者要同时删除数据卷：

```bash
docker-compose down -v
```

## 服务配置

### MySQL 配置

- **端口**: `3306`
- **数据库名**: `oilgas_test`
- **用户名**: `yimusi`
- **密码**: `yimusi123456`
- **Root 密码**: `root123456`

### Redis 配置

- **端口**: `6379`
- **密码**: `redis123456`

## 数据持久化

使用 Docker 卷来持久化数据：

- MySQL 数据: `mysql_data`
- Redis 数据: `redis_data`

## 数据库初始化

### 自动建表

使用 JPA 的 `spring.jpa.hibernate.ddl-auto=update` 自动创建和更新表结构。启动应用时会自动：

1. 创建所有数据库表
2. 更新表结构（添加列、修改列定义等）
3. 创建必要的索引和约束

### 测试数据

测试数据通过以下方式生成：
- **单元测试**: 使用 `@BeforeEach` 动态创建测试数据
- **集成测试**: 使用 `@Testcontainers` + 初始化脚本
- **开发环境**: 可以通过 API 接口创建测试数据

### 首次启动验证

1. 启动 MySQL 和 Redis:
   ```bash
   docker-compose up -d
   ```

2. 启动 Spring Boot 应用:
   ```bash
   ./mvnw spring-boot:run
   ```

3. 应用启动时会自动创建以下表：
   - `users` - 用户表
   - `projects` - 项目表
   - `inspection_device` - 检测设备表
   - `sequence_generator` - 序列生成器表

## Spring Boot 配置

在 `application.yml` 中配置数据库连接：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/oilgas_test
    username: yimusi
    password: yimusi123456

  redis:
    host: localhost
    port: 6379
    password: redis123456
```

## 健康检查

- MySQL: `mysqladmin ping -h localhost -u root -proot123456`
- Redis: `redis-cli -a redis123456 ping`

## 开发环境初始化脚本建议（可选）

如果需要初始化测试数据，可以创建一个专用的初始化配置类：

```java
@Configuration
public class TestDataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            // 创建默认管理员用户
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@yimusi.com");
            admin.setPassword(passwordEncoder.encode("admin123456"));
            admin.setRole("ADMIN");
            userRepository.save(admin);

            // 创建演示用户
            User demo = new User();
            demo.setUsername("demo");
            demo.setEmail("demo@yimusi.com");
            demo.setPassword(passwordEncoder.encode("admin123456"));
            demo.setRole("USER");
            userRepository.save(demo);
        }
    }
}
```

## 故障排查

### 端口冲突

如果 3306 或 6379 端口被占用，可以修改 `docker-compose.yml` 中的端口映射：

```yaml
ports:
  - "3307:3306"  # 修改为 3307
```

### 容器启动失败

查看详细日志：

```bash
docker-compose logs mysql
docker-compose logs redis
```

### 清理并重新开始

完全重置环境：

```bash
docker-compose down -v
docker-compose up -d
```

## 开发工作流程

1. **启动依赖服务**: `docker-compose up -d`
2. **验证连接**: 确认 MySQL 和 Redis 连接正常
3. **启动 Spring Boot 应用**: `./mvnw spring-boot:run`
4. **开发调试**: 正常进行开发和测试
5. **停止项目**: `docker-compose down`

## 注意事项

1. **首次启动需要等待**: MySQL 和 Redis 的健康检查可能需要几秒钟
2. **数据备份**: 重要数据请定期备份
3. **网络配置**: 所有服务都在 `oilgas-network` 网络中
4. **版本兼容**: 确保 Docker Engine 版本支持 v3.8 的 compose 文件格式