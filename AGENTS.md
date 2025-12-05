# 项目上下文：油气测试平台后端

## 项目概览
这是一个用于油气测试平台 (`oilgas-test-platform-be`) 的 **Spring Boot 3.4.1** 后端应用程序。它是一个综合性的企业级系统，功能包括身份验证、项目管理、检测设备跟踪和分布式序列生成。该项目使用 **Java 21** 并遵循整洁架构模式。

## 技术栈
- **语言:** Java 21
- **框架:** Spring Boot 3.4.1 (Spring Data JPA, Spring Web, Actuator)
- **构建工具:** Maven 3.9+ (包含 Wrapper `mvnw`)
- **数据库:** MySQL 8.0
- **缓存/锁:** Redis 7, Redisson (分布式锁)
- **认证:** Sa-Token (JWT)
- **工具库:** QueryDSL, MapStruct, Hutool, Lombok
- **测试:** JUnit 5, Testcontainers, Mockito, JaCoCo
- **格式化:** Prettier (通过 pnpm)

## 开发工作流

### 前置要求
- Java 21
- Maven (使用 `./mvnw`)
- Docker (通过 Docker Compose 或 Testcontainers 运行 MySQL/Redis)
- Node.js/pnpm (可选，用于代码格式化)

### 关键命令
| 动作 | 命令 | 描述 |
|--------|---------|-------------|
| **构建** | `./mvnw clean compile` | 编译项目 |
| **运行** | `./mvnw spring-boot:run` | 本地启动应用 |
| **测试** | `./mvnw clean test` | 运行所有测试（单元测试 + 集成测试）并检查覆盖率 |
| **快速构建** | `./mvnw clean compile -DskipTests` | 构建但不运行测试 |
| **格式化** | `pnpm run format` | 使用 Prettier 格式化 Java 代码 |
| **环境检查** | `./check-env.sh` | 验证 Java, MySQL, Redis 和端口状态 |

### 基础设施
- **Docker Compose:** `docker-compose.yml` 启动 MySQL (端口 3306) 和 Redis (端口 6379)。
- **数据库:** `oilgas_test` (用户: `yimusi`, 密码: `yimusi123456`)
- **Redis:** 密码 `redis123456`

## 项目结构
源代码位于 `src/main/java/com/yimusi/`，遵循 DDD 启发的分层架构：

- **`controller/`**: REST API 端点 (Spring MVC)。
- **`service/`**: 业务逻辑层。
- **`repository/`**: Spring Data JPA 仓库。
- **`entity/`**: JPA 实体 (数据库模型)。
- **`dto/`**: 数据传输对象 (请求/响应模型)。
- **`mapper/`**: MapStruct 接口，用于实体-DTO 转换。
- **`config/`**: Spring 配置 (Security, Redis 等)。
- **`common/`**: 共享工具类和全局异常处理。

### 编码规范
- **分页查询**: 统一使用 `request.toJpaPageRequest()` 构建请求，使用 `PageResult.from()` 封装结果，禁止手动处理页码。
- **软删除**: 实体继承 `SoftDeletableEntity`，自动处理 `deleted` 字段。

## 测试策略
- **集成测试:** 使用 **Testcontainers** 启动真实的 MySQL 和 Redis 实例。
- **覆盖率:** 由 JaCoCo 强制执行 (80% 行覆盖率, 70% 分支覆盖率)。
- **规范:** 测试代码位于 `src/test/java`。

## 配置
- **`pom.xml`**: 依赖管理。
- **`application.yml`**: 主要配置 (数据库连接, Redis, 日志)。
- **`CLAUDE.md`**: 详细的开发指南和架构文档。
