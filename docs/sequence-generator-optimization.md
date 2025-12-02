# 序号生成器优化方案 - Redisson 分布式锁

> 将串行控制下沉至 Redis，支持多实例部署，同时保持数据库逻辑简单可控。

---

## 一、痛点回顾

- 依赖数据库 `SELECT ... FOR UPDATE` 容易在初始化阶段触发 gap lock/死锁，且吞吐完全受限于单行锁。
- JVM 本地锁只能在单实例下生效，一旦扩容到多实例就会出现重复号段或写冲突。
- 需要一个“多节点共享、可配置超时/租约”的锁方案，才能兼顾一致性与扩展性。

---

## 二、方案概述

1. 引入 Redisson 作为分布式锁组件（依赖 `org.redisson:redisson`，复用 Spring `spring.data.redis.*` 配置）。
2. `SequenceGeneratorServiceImpl` 在生成序列前，通过 `RLock.tryLock(waitTime)` 获取分布式锁：
   - `waitTime`：最长等待时间（默认 5s）；
   - 锁持有时间交给 Redisson 看门狗续约（默认 30s，可通过 `lock-watchdog-timeout` 调整）。
3. 锁获取成功后，在同一个方法内执行事务逻辑：
   - 查询或初始化 `SequenceGenerator`；
   - 执行重置策略、溢出校验；
   - 更新 `currentValue` 并保存；
   - 生成连续号段返回。
4. 无论成功或异常，都会在 `finally` 中释放锁；若线程被中断，会及时抛出异常并重置中断标记。

---

## 三、关键代码

**Redisson 配置（单节点示例）**

```java
@Configuration
@ConditionalOnProperty(value = "yimusi.lock.redisson.enabled", havingValue = "true")
class RedissonConfig {

    @Value("${yimusi.lock.redisson.lock-watchdog-timeout:PT30S}")
    private Duration lockWatchdogTimeout;

    @Bean(destroyMethod = "shutdown")
    RedissonClient redissonClient(RedisProperties redis) {
        Config config = new Config();
        config.setLockWatchdogTimeout(lockWatchdogTimeout.toMillis());
        SingleServerConfig single = config.useSingleServer()
            .setAddress((redis.isSsl() ? "rediss://" : "redis://") + redis.getHost() + ":" + redis.getPort())
            .setDatabase(redis.getDatabase())
            .setConnectionMinimumIdleSize(4)
            .setConnectionPoolSize(16);
        if (redis.getPassword() != null && !redis.getPassword().isEmpty()) {
            single.setPassword(redis.getPassword());
        }
        return Redisson.create(config);
    }
}
```

**服务层加锁**

```java
private List<Long> generateSequences(String bizType, int count) {
    validateParams(bizType, count);
    RLock lock = redissonClient.getLock("seq:lock:" + bizType);
    boolean acquired = false;
    try {
        acquired = lock.tryLock(waitTime.toMillis(), TimeUnit.MILLISECONDS);
        if (!acquired) {
            throw new IllegalStateException("获取分布式锁超时: " + bizType);
        }
        return transactionTemplate.execute(status -> generateSequencesInternal(bizType, count));
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new IllegalStateException("等待分布式锁被中断: " + bizType, e);
    } finally {
        if (acquired && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
```

`generateSequencesInternal` 与之前一致，负责读写数据库。

---

## 四、配置要求

`application.yml` 增加：

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      database: 0

yimusi:
  lock:
    redisson:
      enabled: true
      wait-time: PT5S                # 可选，ISO-8601 Duration
      lock-watchdog-timeout: PT30S   # 看门狗续约周期
```

部署时只需按需调整 Redis 地址和认证信息。

---

## 五、优势与风险

**优势**
- 支持多实例横向扩展，锁粒度仍按 `bizType` 控制。
- 通过等待时间和看门狗续约周期的组合，可兼顾吞吐和容错。
- 出现锁获取超时、中断等情况时，业务能快速感知并决定重试或上报。

**风险/注意事项**
- Redis 必须高可用，否则锁不可用会导致服务启动失败；必要时配合哨兵或 Redis Cluster。
- 看门狗续约受限于客户端线程是否存活；若线程被长时间阻塞或挂起，仍可能导致锁提前释放。
- 仍需对数据库的唯一键约束、重试等逻辑保持健壮，避免锁失效时产生数据不一致。

---

## 六、总结

- 将串行控制迁移到 Redisson 分布式锁，彻底摆脱数据库死锁与单实例限制。
- 方案保持数据库逻辑简单，只依赖普通 CRUD 便能完成号段生成。
- 通过配置项即可扩展/调整锁策略，是符合业界实践的可演进方案。
