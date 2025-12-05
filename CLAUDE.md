# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a **Spring Boot backend application** for an oil and gas test platform (oilgas-test-platform-be). It implements a comprehensive enterprise system with authentication, project management, inspection device tracking, and a sophisticated distributed sequence generator. The application uses modern Java 21 with Spring Boot 3.4.1 and follows clean architecture patterns.

## Build System and Development Commands

### Prerequisites
- **Java 21** (LTS version)
- **Maven 3.9+** with the Maven Wrapper included (`mvnw`)
- **MySQL** for database
- **Redis** for distributed locking
- **pnpm** for code formatting (JavaScript/TypeScript optional)

### Core Commands
```bash
# Build the project
./mvnw clean compile

# Run comprehensive tests (includes JaCoCo coverage validation)
./mvnw clean test

# Run single test class
./mvnw -Dtest=CategoryTest test

# Run single test method
./mvnw -Dtest=UserRepositoryQueryDslTest#testFindAllByQueryDsl test

# Start the application
./mvnw spring-boot:run

# Build without tests (for faster iteration)
./mvnw clean compile -DskipTests

# Format Java code using Prettier
pnpm run format
```

### Test Coverage
- **Line coverage target**: 80%
- **Branch coverage target**: 70%
- **Coverage reports**: Available at `target/site/jacoco/index.html`
- **Validation**: Tests fail if coverage thresholds aren't met

## High-Level Architecture

### Domain-Driven Design Structure
```
src/main/java/com/yimusi/
├── Application.java                         # Spring Boot entry point
├── common/                                 # Shared utilities and exceptions
├── config/                                 # Spring configuration classes
├── controller/                             # REST API endpoints (Spring MVC)
├── dto/                                    # Data Transfer Objects
│   ├── auth/                               # Authentication DTOs
│   ├── user/                               # User management DTOs
│   ├── project/                            # Project management DTOs
│   ├── inspection/                         # Inspection device DTOs
│   └── common/                             # Shared DTOs
├── entity/                                 # JPA entities/ORM models
├── enums/                                  # Enumeration types
├── mapper/                                 # MapStruct mappers
├── repository/                             # Spring Data JPA repositories
└── service/                                # Business logic layer
```

### Key Technical Stack

#### Core Frameworks
- **Spring Boot 3.4.1** with Jakarta EE 9+ migration
- **Spring Data JPA** with Hibernate
- **Spring Web** for REST API
- **Sa-Token** for JWT authentication and authorization
- **Redisson** for distributed locks

#### Database & Persistence
- **MySQL** with JPA/Hibernate ORM
- **QueryDSL** for type-safe queries
- **Testcontainers** for integration testing
- **DDL auto-update** for development (table creation/update on startup)

#### Development Tools
- **Lombok** for reduced boilerplate
- **MapStruct** for object mapping
- **Hutool** for utility functions
- **Mockito** for mocking
- **JaCoCo** for code coverage analysis

### Authentication System

#### Sa-Token JWT Configuration
- **Token format**: JWT
- **Token lifespan**: 30 days (2,592,000 seconds)
- **Secret key**: Configurable, currently using development key
- **Endpoints**: `/api/auth/*` (login, logout, user info)
- **Protected routes**: All `/api/**` endpoints require authentication

#### Role-Based Access Control
- **ADMIN**: Full access
- **USER**: Restricted access
- Custom permission annotations can be added per endpoint

### Distributed Sequence Generator

#### Overview
A sophisticated distributed ID generation system similar to Twitter Snowflake, implemented for:
- **Inspection device numbers**: `IND202501280001` (daily reset)
- **Project internal sequences**: `1, 2, 3...` (monotonic, no reset)
- **Custom business sequences** with configurable reset strategies

#### Key Features
- **Distributed locking** using Redisson and MySQL row locks
- **Multiple reset strategies**: DAILY, MONTHLY, YEARLY, NONE
- **Batch ID generation** for performance optimization
- **Format validation** and overflow protection
- **Multi-tenancy support** through dynamic business types

#### Implementation Details
- `SequenceGeneratorService` - Core service with `nextId()` and `nextIds()` methods
- `SequenceBizType` - Enum defining ID format and reset strategy
- `SequenceGenerator` - JPA entity tracking ID state
- Built-in semaphore pattern with Redis distributed locks

### API Design Patterns

#### RESTful Conventions
- **Base path**: `/api/{version}/{resource}`
- **HTTP methods**: Standard CRUD (GET, POST, PUT, DELETE)
- **Response format**: Consistent JSON structure with success/error metadata

#### Request/Response Patterns
```java
// Request DTOs (validated with Bean Validation)
@Data
public class CreateUserRequest {
    @NotBlank String username;
    @Email String email;
    @Size(min=6) String password;
}

// Response DTOs (normalized API responses)
@Data
public class UserResponse {
    Long id;
    String username;
    String email;
}
```

#### Pagination and Filtering
- Standardized pagination using `PageRequest`
- QueryDSL for complex filtering and sorting
- Spring Data JPA pageable responses

### Testing Strategy

#### Test Categories
1. **Unit Tests**: Fast, isolated component testing
2. **Repository Tests**: Database interaction testing
3. **Service Tests**: Business logic testing with mocks
4. **Integration Tests**: Full application flow with Testcontainers
5. **DTO Validation Tests**: Bean validation verification

#### Infrastructure
- **Testcontainers**: MySQL and Redis containers for integration tests
- **Transactional tests**: Automatic rollback after each test
- **Database seeding**: `@BeforeEach` setup for test data
- **MockMvc**: Web layer testing for controllers

### Configuration Management

#### Application Profiles
- **Default**: `application.yml` (development)
- **Test**: `application-test.yml` (test-specific configs)

#### Key Configuration Areas
- **Database**: Connection pooling, JPA settings
- **Redis**: Distributed locking configuration
- **Logging**: Logback with JSON format for production
- **JWT**: Token duration and security settings
- **Actuator**: Health checks and monitoring endpoints

### Database Schema

#### Core Tables
- **users**: User accounts and authentication
- **projects**: Project management
- **inspection_device**: Equipment tracking with auto-generated numbers
- **sequence_generator**: ID generation state management

#### Entity Architecture
- JPA实体统一继承 `AuditableEntity` 获取创建/更新人及时间字段
- 需要软删除能力的实体额外继承 `SoftDeletableEntity`，自动带上 `deleted*` 字段并结合 `@SQLDelete`

## Development Guidelines

### Code Quality Standards
- **Coverage minimums**: 80% lines, 70% branches
- **Code formatting**: Prettier + prettier-plugin-java
- **Null safety**: Defensive programming with Optional and null checks
- **Logging**: Structured JSON logs, appropriate log levels

### Transaction Management
- **Service methods**: Use `@Transactional` for atomic operations
- **Sequence generation**: Must be in same transaction as entity persistence
- **Read-only operations**: Use `@Transactional(readOnly = true)` when applicable

### Error Handling
- **Global exception handler**: `@ControllerAdvice` for centralized error handling
- **HTTP status codes**: Proper semantic status codes (200, 201, 400, 401, 403, 404, 500)
- **Exception types**: Custom exceptions with meaningful error messages

### Performance Considerations
- **Query optimization**: Use QueryDSL for complex queries
- **Lazy loading**: JPA default lazy loading for associations
- **Database indexing**: Automatic from unique constraints and foreign keys
- **Connection pooling**: HikariCP configuration through Spring Boot

## Documentation

Currently documented in `/docs/` directory:
- **Authentication-API.md**: Complete JWT authentication guide
- **distributed-sequence-generator.md**: Detailed architecture and usage
- **sequence-generator-optimization.md**: Performance considerations

Local development server runs on `http://localhost:8080` by default. All API endpoints include Swagger/OpenAPI documentation accessible through Actuator endpoints.

For comprehensive sequence generator design details, see `/docs/distributed-sequence-generator.md` and `docs/sequence-generator-optimization.md`.
