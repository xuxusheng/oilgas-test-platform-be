# CLAUDE.md

此文件为 Claude Code (claude.ai/code) 在处理此仓库代码时提供指导。

## 项目概览

这是一个用于油气测试平台 (oilgas-test-platform-be) 的 **Spring Boot 后端应用程序**。它实现了一个综合性的企业级系统，包括身份验证、项目管理、检测设备跟踪以及复杂的分布式序列生成器。该应用程序使用现代 Java 21 和 Spring Boot 3.4.1，并遵循整洁架构模式。

## 构建系统和开发命令

### 前置要求
- **Java 21** (LTS 版本)
- **Maven 3.9+** (包含 Maven Wrapper `mvnw`)
- **Docker** 和 **Docker Compose** (用于容器化依赖服务)
- **MySQL** 用于数据库 (可通过 Docker 启动)
- **Redis** 用于分布式锁 (可通过 Docker 启动)
- **pnpm** 用于代码格式化 (JavaScript/TypeScript 可选)

### 核心命令

#### 依赖服务管理
```bash
# 启动仅依赖服务 (MySQL, Redis) - 适合本地开发
docker-compose -f docker-compose.deps.yml up -d

# 启动完整应用栈 (应用 + 依赖) - 适合完整环境测试
docker-compose up -d

# 停止服务
docker-compose down                    # 完整环境
docker-compose -f docker-compose.deps.yml down  # 仅依赖

# 查看服务状态
docker-compose ps
```

#### 应用构建与运行
```bash
# 构建项目
./mvnw clean compile

# 运行综合测试 (包含 JaCoCo 覆盖率验证)
./mvnw clean test

# 运行单个测试类
./mvnw -Dtest=CategoryTest test

# 运行单个测试方法
./mvnw -Dtest=UserRepositoryQueryDslTest#testFindAllByQueryDsl test

# 启动应用程序 (需先启动依赖服务)
./mvnw spring-boot:run

# 不带测试构建 (用于快速迭代)
./mvnw clean compile -DskipTests

# 使用 Prettier 格式化 Java 代码
pnpm run format
```

### 测试覆盖率
- **行覆盖率目标**: 80%
- **分支覆盖率目标**: 70%
- **覆盖率报告**: 位于 `target/site/jacoco/index.html`
- **验证**: 如果未达到覆盖率阈值，测试将失败

## 高层架构

### 领域驱动设计结构
```
src/main/java/com/yimusi/
├── Application.java                         # Spring Boot 入口点
├── common/                                 # 共享工具类和异常
├── config/                                 # Spring 配置类
├── controller/                             # REST API 端点 (Spring MVC)
├── dto/                                    # 数据传输对象
│   ├── auth/                               # 认证 DTO
│   ├── user/                               # 用户管理 DTO
│   ├── project/                            # 项目管理 DTO
│   ├── inspection/                         # 检测设备 DTO
│   ├── oilsample/                          # 油样管理 DTO
│   ├── teststation/                        # 测试工位 DTO
│   │   └── parameter/                      # 工位参数 DTO
│   └── common/                             # 共享 DTO
├── entity/                                 # JPA 实体/ORM 模型
├── enums/                                  # 枚举类型
├── mapper/                                 # MapStruct 映射器
├── repository/                             # Spring Data JPA 仓库
└── service/                                # 业务逻辑层
```

### 关键技术栈

#### 核心框架
- **Spring Boot 3.4.1** 迁移至 Jakarta EE 9+
- **Spring Data JPA** 使用 Hibernate
- **Spring Web** 用于 REST API
- **Sa-Token** 用于 JWT 认证和授权
- **Redisson** 用于分布式锁

#### 数据库与持久化
- **MySQL** 使用 JPA/Hibernate ORM
- **QueryDSL** 用于类型安全的查询
- **Testcontainers** 用于集成测试
- **DDL 自动更新** 用于开发 (启动时创建/更新表)

#### 开发工具
- **Lombok** 减少样板代码
- **MapStruct** 用于对象映射
- **Hutool** 用于工具函数
- **Mockito** 用于模拟
- **JaCoCo** 用于代码覆盖率分析

### 认证系统

#### Sa-Token JWT 配置
- **令牌格式**: JWT
- **令牌寿命**: 30 天 (2,592,000 秒)
- **密钥**: 可配置，目前使用开发密钥
- **端点**: `/api/auth/*` (登录, 登出, 用户信息)
- **受保护路由**: 所有 `/api/**` 端点都需要认证

#### 基于角色的访问控制
- **ADMIN**: 完全访问权限
- **USER**: 受限访问权限
- 每个端点可以添加自定义权限注解

### 分布式序列生成器

#### 概览
一个类似于 Twitter Snowflake 的复杂分布式 ID 生成系统，实现用于：
- **检测设备编号**: `IND202501280001` (每日重置)
- **项目内部序列**: `1, 2, 3...` (单调递增，不重置)
- **自定义业务序列** 具有可配置的重置策略

#### 关键特性
- **分布式锁** 使用 Redisson 和 MySQL 行锁
- **多种重置策略**: DAILY (每日), MONTHLY (每月), YEARLY (每年), NONE (不重置)
- **批量 ID 生成** 用于性能优化
- **格式验证** 和溢出保护
- **多租户支持** 通过动态业务类型

#### 实现细节
- `SequenceGeneratorService` - 核心服务，包含 `nextId()` 和 `nextIds()` 方法
- `SequenceBizType` - 定义 ID 格式和重置策略的枚举
- `SequenceGenerator` - 跟踪 ID 状态的 JPA 实体
- 内置信号量模式与 Redis 分布式锁

### API 设计模式

#### RESTful 规范
- **基础路径**: `/api/{version}/{resource}`
- **HTTP 方法**: 标准 CRUD (GET, POST, PUT, DELETE)
- **响应格式**: 包含成功/错误元数据的一致 JSON 结构

#### 请求/响应模式
```java
// 请求 DTO (使用 Bean Validation 验证)
@Data
public class CreateUserRequest {
    @NotBlank String username;
    @Email String email;
    @Size(min=6) String password;
}

// 响应 DTO (标准化的 API 响应)
@Data
public class UserResponse {
    Long id;
    String username;
    String email;
}
```

#### 分页和过滤
- 使用 `PageRequest` 进行标准化分页
- QueryDSL 用于复杂的过滤和排序
- Spring Data JPA 可分页响应

#### 分页查询编码规范
在 Service 层实现分页查询时，**禁止手动处理页码和封装结果**，应统一使用 DTO 基类提供的方法：
1. **构建分页请求**：使用 `request.toJpaPageRequest("默认排序字段")` 自动生成 JPA `PageRequest`。
2. **封装返回结果**：使用 `PageResult.from(page.map(mapper::toResponse))` 一步完成 DTO 转换和结果封装。

### 测试策略

#### 测试类别
1. **单元测试**: 快速、隔离的组件测试
2. **仓库测试**: 数据库交互测试
3. **服务测试**: 使用 Mock 的业务逻辑测试
4. **集成测试**: 使用 Testcontainers 的完整应用流程测试
5. **DTO 验证测试**: Bean 验证校验

#### 基础设施
- **Testcontainers**: 用于集成测试的 MySQL 和 Redis 容器
- **事务性测试**: 每次测试后自动回滚
- **数据库播种**: `@BeforeEach` 设置测试数据
- **MockMvc**: 控制器的 Web 层测试

### 配置管理

#### 应用 Profile
- **默认**: `application.yml` (开发环境)
- **测试**: `application-test.yml` (测试专用配置)

#### 关键配置区域
- **数据库**: 连接池, JPA 设置
- **Redis**: 分布式锁配置
- **日志**: 生产环境使用 JSON 格式的 Logback
- **JWT**: 令牌持续时间和安全设置
- **Actuator**: 健康检查和监控端点

### 数据库模式

#### 核心表
- **users**: 用户账户和认证
- **projects**: 项目管理
- **inspection_device**: 带有自动生成编号的设备跟踪
- **oil_samples**: 油样管理，支持 JSON 参数存储
- **test_stations**: 测试工位管理，支持电磁阀参数和油阀映射
- **sequence_generator**: ID 生成状态管理

#### 实体架构
- JPA 实体统一继承 `AuditableEntity` 获取创建/更新人及时间字段
- 需要软删除能力的实体额外继承 `SoftDeletableEntity`，自动带上 `deleted*` 字段并结合 `@SQLDelete`

## 开发指南

### 代码质量标准
- **最低覆盖率**: 80% 行覆盖率, 70% 分支覆盖率
- **代码格式化**: Prettier + prettier-plugin-java
- **空安全**: 使用 Optional 和空检查进行防御性编程
- **日志**: 结构化 JSON 日志，适当的日志级别

### 事务管理
- **服务方法**: 使用 `@Transactional` 进行原子操作
- **序列生成**: 必须与实体持久化在同一事务中
- **只读操作**: 适用时使用 `@Transactional(readOnly = true)`

### 错误处理
- **全局异常处理器**: `@ControllerAdvice` 用于集中式错误处理
- **HTTP 状态码**: 正确的语义状态码 (200, 201, 400, 401, 403, 404, 500)
- **异常类型**: 带有有意义错误消息的自定义异常

### 性能考虑
- **查询优化**: 使用 QueryDSL 进行复杂查询
- **懒加载**: 关联关系默认使用 JPA 懒加载
- **数据库索引**: 根据唯一约束和外键自动生成
- **连接池**: 通过 Spring Boot 配置 HikariCP

## Docker 部署配置

### 文件说明
- **`docker-compose.deps.yml`**: 仅包含依赖服务 (MySQL, Redis)，适合本地开发
- **`docker-compose.yml`**: 完整应用栈，包含应用容器 + 依赖服务，适合完整环境测试

### 配置详情
- **MySQL**: 端口 3306, 数据库 `oilgas_test`, 用户 `yimusi`
- **Redis**: 端口 6379, 密码 `redis123456`
- **应用**: 端口 8080, 使用 `prod` profile

### 开发工作流
1. **本地开发** (推荐): `docker-compose.deps.yml` + 本地运行应用
2. **完整测试**: `docker-compose.yml` 运行完整容器栈

## 文档

目前在 `/docs/` 目录下的文档：
- **Authentication-API.md**: 完整的 JWT 认证指南
- **design/oil_sample_service_design.md**: 油样管理 Service 层设计方案

本地开发服务器默认运行在 `http://localhost:8080`。所有 API 端点都包含 Swagger/OpenAPI 文档，可通过 Actuator 端点访问。

### 新增业务模块

#### 油样管理 (OilSample)
- **实体**: `OilSample` - 支持 JSON 格式存储动态参数列表
- **参数验证**: 支持 CH4, C2H2, C2H4, C2H6, H2, CO, CO2, H2O 等参数键
- **唯一性约束**: `sampleNo` 全局唯一
- **软删除**: 继承 `SoftDeletableEntity`

#### 测试工位管理 (TestStation)
- **实体**: `TestStation` - 支持电磁阀控制参数和油阀映射关系
- **枚举类型**:
  - `TestStationUsage`: INHOUSE_TEST(厂内测试), RND_TEST(研发测试)
  - `ValveCommType`: SERIAL_MODBUS(Serial,Modbus), TCP_MODBUS(Tcp,Modbus)
- **查询特性**: 使用 QueryDSL 支持多字段组合查询
- **软删除**: 继承 `SoftDeletableEntity`

### 分页查询规范

所有分页查询统一使用以下模式：
```java
// Service 层
PageResult<XXXResponse> getXXXPage(XXXPageRequest request) {
    // 1. 构建查询条件 (Specification 或 QueryDSL Predicate)
    // 2. 使用 request.toJpaPageRequest("默认排序字段") 构建分页
    // 3. 使用 PageResult.from(page.map(mapper::toResponse)) 封装结果
}
```

### API 端点示例

#### 油样管理
- `GET /api/oil-samples/page` - 分页查询
- `GET /api/oil-samples/{id}` - 详情查询
- `POST /api/oil-samples` - 创建油样
- `PUT /api/oil-samples/{id}` - 更新油样
- `DELETE /api/oil-samples/{id}` - 删除油样
- `GET /api/oil-samples/validate-unique/{sampleNo}` - 验证编号唯一性

#### 测试工位管理
- `GET /api/test-stations` - 获取所有工位
- `GET /api/test-stations/page` - 分页查询
- `GET /api/test-stations/{id}` - 根据ID查询
- `GET /api/test-stations/by-station-no/{stationNo}` - 根据工位编号查询
- `POST /api/test-stations` - 创建工位
- `PUT /api/test-stations/{id}` - 更新工位
- `DELETE /api/test-stations/{id}` - 删除工位
- `GET /api/test-stations/validate-station-no/{stationNo}` - 验证工位编号唯一性
