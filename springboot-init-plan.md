# Spring Boot 后端应用初始化方案

## 1. 项目概述

本项目是一个基于 Spring Boot 3 和 JDK 25 的现代化后端应用。选用 Maven 作为构建工具，整合了数据持久化、Web开发、参数校验及监控等核心功能。

- **核心框架**: Spring Boot 3.x
- **开发语言**: Java 25
- **构建工具**: Maven
- **数据库**: MySQL
- **数据访问**:
    - **JPA (Java Persistence API)**: ORM 标准
    - **QueryDSL**: 类型安全的动态查询框架
- **工具库**:
    - **Lombok**: 简化样板代码
    - **Hutool**: Java工具类库
- **Web**:
    - **Spring Web**: 提供 RESTful API 能力
    - **Validation**: 统一参数校验
    - **Actuator**: 应用监控与健康检查
- **测试**:
    - **Spring Test**: 单元与集成测试
    - **Testcontainers**: 在测试中使用真实的MySQL Docker容器

方案遵循您的要求，排除了 Spring Security 和 springdoc-openapi，并提供了统一响应和全局异常处理的最佳实践。

## 2. 建议的项目结构

为了支持更健壮的异常处理，我们在 `exception` 包下新增 `ErrorCode` 枚举。

```
/wuhan-yimusi
└── springboot-app/
    ├── src/
    │   ├── main/
    │   │   ├── java/
    │   │   │   └── com/
    │   │   │       └── yimusi/
    │   │   │           ├── common/
    │   │   │           │   ├── exception/
    │   │   │           │   │   ├── ErrorCode.java              # 统一错误码枚举
    │   │   │           │   │   ├── BusinessException.java      # 自定义业务异常
    │   │   │           │   │   └── GlobalExceptionHandler.java # 全局异常处理器
    │   │   │           │   └── model/
    │   │   │           │       └── ApiResponse.java          # 统一响应封装类
    │   │   │           ├── controller/
    │   │   │           ├── service/
    │   │   │           ├── repository/
    │   │   │           └── entity/
    │   │   │           └── Application.java                # Spring Boot 启动类
    │   │   └── resources/
    │   │       └── application.yml                         # YAML 配置文件
    │   └── test/
    │       └── java/
    └── pom.xml                                             # Maven 配置文件
```

## 3. Maven `pom.xml` 完整配置

这是项目核心的 `pom.xml` 文件，已包含所有必需的依赖和 QueryDSL 的构建插件。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- Spring Boot 父项目 -->
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.5</version> <!-- 使用最新的 Spring Boot 3 版本 -->
        <relativePath/>
    </parent>

    <groupId>com.yimusi</groupId>
    <artifactId>springboot-app</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <name>My Spring Boot Application</name>
    <description>A modern Spring Boot application</description>

    <properties>
        <!-- 使用 JDK 25 -->
        <java.version>25</java.version>
        <!-- QueryDSL 版本 -->
        <querydsl.version>5.1.0</querydsl.version>
        <!-- Hutool 版本 -->
        <hutool.version>5.8.27</hutool.version>
        <!-- Testcontainers 版本 -->
        <testcontainers.version>1.19.8</testcontainers.version>
    </properties>

    <dependencies>
        <!-- Web 开发 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- JPA 数据持久化 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- 参数校验 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- 应用监控 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <!-- MySQL 数据库驱动 -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- Hutool 工具库 (按需引入) -->
        <dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-core</artifactId>
            <version>${hutool.version}</version>
        </dependency>

        <!-- QueryDSL 核心依赖 -->
        <dependency>
            <groupId>com.querydsl</groupId>
            <artifactId>querydsl-jpa</artifactId>
            <classifier>jakarta</classifier>
            <version>${querydsl.version}</version>
        </dependency>

        <!-- QueryDSL 注解处理器 (用于生成Q类) -->
        <dependency>
            <groupId>com.querydsl</groupId>
            <artifactId>querydsl-apt</artifactId>
            <version>${querydsl.version}</version>
            <classifier>jakarta</classifier>
            <scope>provided</scope>
        </dependency>

        <!-- Spring Boot 测试 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <!-- Testcontainers for JUnit 5 -->
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>${testcontainers.version}</version>
            <scope>test</scope>
        </dependency>

        <!-- Testcontainers MySQL 模块 -->
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>mysql</artifactId>
            <version>${testcontainers.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- Spring Boot Maven 插件 -->
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>

            <!-- QueryDSL Maven 插件 (用于自动生成 Q-types) -->
            <plugin>
                <groupId>com.querydsl</groupId>
                <artifactId>querydsl-maven-plugin</artifactId>
                <version>${querydsl.version}</version>
                <executions>
                    <execution>
                        <phase>generate-sources</phase>
                        <goals>
                            <goal>java-classes</goal>
                        </goals>
                        <configuration>
                            <outputDirectory>target/generated-sources/java</outputDirectory>
                            <!-- 指定实体类所在的包 -->
                            <packages>
                                <package>com.yimusi.entity</package>
                            </packages>
                            <!-- 指定使用更精确的 Jakarta JPA 注解处理器 -->
                            <processor>com.querydsl.apt.jakarta.JakartaJPAAnnotationProcessor</processor>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

## 4. 核心代码实现

本节提供了经过优化的核心代码，包括统一响应、错误码管理和全局异常处理。

### 4.1. 统一响应类 (`ApiResponse.java`)

这个类用于封装所有API的返回结果。我们为其增加了一个可以携带 `data` 的 `error` 方法，用于在参数校验失败时返回结构化的错误详情。

```java
package com.yimusi.common.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApiResponse<T> {

    private static final int SUCCESS_CODE = 200;
    private static final String SUCCESS_MESSAGE = "Success";

    private int code;
    private String message;
    private T data;

    private ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(SUCCESS_CODE, SUCCESS_MESSAGE, data);
    }

    public static <T> ApiResponse<T> success() {
        return success(null);
    }
    
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    public static <T> ApiResponse<T> error(int code, String message, T data) {
        return new ApiResponse<>(code, message, data);
    }
}
```

### 4.2. 统一错误码枚举 (`ErrorCode.java`)

使用枚举来集中管理所有错误码，使得代码更清晰、可维护性更高。

```java
package com.yimusi.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // --- 系统级错误 ---
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 50000, "Internal Server Error"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, 40000, "Bad Request"),
    METHOD_NOT_SUPPORTED(HttpStatus.METHOD_NOT_SUPPORTED, 40500, "Request method not supported"),
    
    // --- 参数校验错误 ---
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, 40001, "Validation failed"),
    MISSING_REQUEST_PARAMETER(HttpStatus.BAD_REQUEST, 40002, "Missing request parameter"),

    // --- 业务错误 (示例) ---
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, 40401, "User not found"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, 40101, "Invalid credentials");
    
    private final HttpStatus httpStatus;
    private final int code;
    private final String message;
}
```

### 4.3. 自定义业务异常 (`BusinessException.java`)

改造 `BusinessException`，使其直接与 `ErrorCode` 枚举关联。

```java
package com.yimusi.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {

    private final int code;
    private final HttpStatus httpStatus;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.httpStatus = errorCode.getHttpStatus();
        this.code = errorCode.getCode();
    }
    
    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.httpStatus = errorCode.getHttpStatus();
        this.code = errorCode.getCode();
    }
}
```

### 4.4. 全局异常处理器 (`GlobalExceptionHandler.java`)

升级后的处理器覆盖了更多异常类型，并返回 `ResponseEntity` 以动态控制HTTP状态码。对于参数校验失败的情况，它会返回结构化的错误信息。

```java
package com.yimusi.common.exception;

import com.yimusi.common.model.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理自定义业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        log.warn("Business exception occurred: {}", ex.getMessage());
        ApiResponse<Void> apiResponse = ApiResponse.error(ex.getCode(), ex.getMessage());
        return new ResponseEntity<>(apiResponse, ex.getHttpStatus());
    }

    /**
     * 处理 @RequestBody 参数校验异常 (返回结构化的错误信息)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
        log.warn("Validation failed for @RequestBody: {}", errors);
        ApiResponse<Map<String, String>> apiResponse = ApiResponse.error(errorCode.getCode(), errorCode.getMessage(), errors);
        return new ResponseEntity<>(apiResponse, errorCode.getHttpStatus());
    }

    /**
     * 处理 @RequestParam 和 @PathVariable 参数校验异常 (返回结构化的错误信息)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString().substring(violation.getPropertyPath().toString().lastIndexOf('.') + 1),
                        ConstraintViolation::getMessage
                ));

        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
        log.warn("Validation failed for @RequestParam/@PathVariable: {}", errors);
        ApiResponse<Map<String, String>> apiResponse = ApiResponse.error(errorCode.getCode(), errorCode.getMessage(), errors);
        return new ResponseEntity<>(apiResponse, errorCode.getHttpStatus());
    }

    /**
     * 处理缺少请求参数的异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        ErrorCode errorCode = ErrorCode.MISSING_REQUEST_PARAMETER;
        String message = String.format("Missing parameter: %s", ex.getParameterName());
        log.warn(message);
        ApiResponse<Void> apiResponse = ApiResponse.error(errorCode.getCode(), message);
        return new ResponseEntity<>(apiResponse, errorCode.getHttpStatus());
    }

    /**
     * 处理不支持的HTTP请求方法
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        ErrorCode errorCode = ErrorCode.METHOD_NOT_SUPPORTED;
        log.warn("Method not supported: {} - {}", ex.getMethod(), ex.getRequestURL());
        ApiResponse<Void> apiResponse = ApiResponse.error(errorCode.getCode(), errorCode.getMessage());
        return new ResponseEntity<>(apiResponse, errorCode.getHttpStatus());
    }

    /**
     * 处理所有其他未捕获的异常 (兜底)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        log.error("An unexpected error occurred", ex);
        ApiResponse<Void> apiResponse = ApiResponse.error(errorCode.getCode(), errorCode.getMessage());
        return new ResponseEntity<>(apiResponse, errorCode.getHttpStatus());
    }
}
```

## 5. 配置文件 (`application.yml`)

提供一个基础的配置文件模板。

```yaml
# 服务器配置
server:
  port: 8080

# 应用配置
spring:
  application:
    name: springboot-app
  # --- 数据源配置 ---
  datasource:
    url: jdbc:mysql://localhost:3306/your_database_name?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8
    username: your_username
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  # --- JPA/Hibernate 配置 ---
  jpa:
    hibernate:
      # ddl-auto: [create | create-drop | update | validate | none]
      #   - update: 启动时自动更新表结构（开发方便）
      #   - validate: 启动时校验表结构
      #   - none: 生产环境推荐，更安全
      ddl-auto: update
    # 在控制台显示执行的SQL语句
    show-sql: true
    properties:
      hibernate:
        format_sql: true

# --- Actuator 监控端点配置 ---
management:
  endpoints:
    web:
      exposure:
        # [安全警告] 开发时可设为 "*"，生产环境强烈建议改为 ["health", "info", "prometheus"]
        include: "*"
  endpoint:
    health:
      # 默认只显示基本信息，保护敏感数据
      show-details: never
```

## 6. 后续步骤

1.  **创建项目**: 根据此方案，您可以使用 Spring Initializr (https://start.spring.io/) 生成基础项目骨架，然后将本 `pom.xml` 的内容覆盖进去，并创建上述Java类。
2.  **配置数据库**: 修改 `application.yml` 中的数据库连接信息。
3.  **创建实体**: 在 `entity` 包下创建您的JPA实体类。
4.  **构建项目**: 运行 `mvn clean install`，QueryDSL插件会自动在 `target/generated-sources/java` 目录下生成Q类型类。
5.  **开始编码**: 创建 Repository, Service, 和 Controller，开始您的业务开发。

这份方案为您搭建了一个坚实且现代化的后端开发起点。
