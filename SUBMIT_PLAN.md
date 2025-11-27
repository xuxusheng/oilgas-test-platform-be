# 代码提交计划

## 提交 1: feat: 添加 Logstash JSON 格式日志配置
- 添加 logstash-logback-encoder 依赖
- 配置 logback-spring.xml 支持 JSON 格式日志
- 更新 application.yml 日志级别配置

## 提交 2: feat: 实现 Sa-Token 认证异常统一处理
- 添加 Sa-Token 相关错误码
- 实现完整的认证异常处理器（未登录、无权限、Token过期等）
- 支持账号并发登录检测

## 提交 3: refactor: 优化密码加密逻辑，移至服务层
- 移除 User 实体中的 @PrePersist 和 @PreUpdate 密码加密
- 在 UserServiceImpl.create 方法中添加密码加密
- 相关测试用例相应更新

## 提交 4: refactor: 完善测试代码注释和文档
- 为测试类添加详细的中文注释
- 改进测试方法的显示名称
- 优化测试代码结构和可读性