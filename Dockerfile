# 多阶段构建：使用 Maven 构建应用
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B && \
    cd target && \
    jar -xf *.jar

# 运行时镜像
FROM eclipse-temurin:21-jre-jammy

LABEL maintainer="yimusi <your-email@example.com>" \
      version="1.0.0" \
      description="OilGas Test Platform Backend"

WORKDIR /app

# 创建非 root 用户
RUN groupadd -r yimusi -g 1001 && \
    useradd -r -u 1001 -g yimusi yimusi

# 复制构建产物
COPY --from=builder /app/target/*.jar app.jar
COPY --from=builder /app/target/BOOT-INF/lib ./lib
COPY --from=builder /app/target/BOOT-INF/classes ./classes
COPY --from=builder /app/target/META-INF ./META-INF

# 创建日志目录并设置权限
RUN mkdir -p /app/logs && \
    chown -R yimusi:yimusi /app

USER yimusi
EXPOSE 8080

# JVM 参数（可被环境变量覆盖）
ENV JAVA_OPTS="--XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -Dspring.profiles.active=prod"

# 启动命令（使用 shell 格式以支持环境变量展开）
CMD ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]