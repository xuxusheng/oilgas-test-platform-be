package com.yimusi.service;

import com.yimusi.common.exception.BadRequestException;
import com.yimusi.common.exception.ErrorCode;
import com.yimusi.common.exception.SequenceGenerationException;
import com.yimusi.entity.SequenceGenerator;
import com.yimusi.enums.ResetStrategy;
import com.yimusi.enums.SequenceBizType;
import com.yimusi.repository.SequenceGeneratorRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 序列号生成服务实现类
 * 提供分布式环境下的序列号生成功能
 * 通过依赖注入 TransactionTemplate 实现更简洁的事务管理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SequenceGeneratorServiceImpl implements SequenceGeneratorService {

    private static final int MAX_BIZ_TYPE_LENGTH = 50;
    private static final int MAX_BATCH_SIZE = 10000;

    private final SequenceGeneratorRepository sequenceGeneratorRepository;
    private final RedissonClient redissonClient;
    private final TransactionTemplate transactionTemplate;

    @Value("${yimusi.lock.redisson.wait-time:PT5S}")
    private Duration waitTime;

    // ==================== 枚举便捷方法（推荐使用） ====================

    /**
     * {@inheritDoc}
     *
     * <p>实现说明：通过调用 {@link #nextIds(SequenceBizType, int)} 获取单个序列号
     */
    @Override
    public String nextId(SequenceBizType bizType) {
        return nextIds(bizType, 1).get(0);
    }

    /**
     * {@inheritDoc}
     *
     * <p>实现说明：使用枚举类型的格式化方法生成带前缀和固定长度的序列号
     */
    @Override
    public List<String> nextIds(SequenceBizType bizType, int count) {
        List<Long> sequenceNumbers = generateSequences(bizType.getCode(), count);
        return sequenceNumbers.stream().map(bizType::formatSequenceNo).collect(Collectors.toList());
    }

    // ==================== 基础方法（字符串参数，支持动态业务类型） ====================

    /**
     * {@inheritDoc}
     *
     * <p>实现说明：通过调用 {@link #nextIds(String, int)} 获取单个序列号
     */
    @Override
    public String nextId(String bizType) {
        return nextIds(bizType, 1).get(0);
    }

    /**
     * {@inheritDoc}
     *
     * <p>实现说明：如果业务类型在枚举中定义则使用枚举的格式化方法，否则返回纯数字字符串
     */
    @Override
    public List<String> nextIds(String bizType, int count) {
        List<Long> sequenceNumbers = generateSequences(bizType, count);
        return formatSequences(bizType, sequenceNumbers);
    }

    /**
     * 核心序列号生成逻辑，使用分布式锁保证并发安全
     *
     * <p>执行流程：
     * <ol>
     *   <li>参数校验</li>
     *   <li>获取分布式锁（Redisson）</li>
     *   <li>在事务中生成序列号</li>
     *   <li>释放锁</li>
     * </ol>
     *
     * @param bizType 业务类型编码
     * @param count 需要生成的序列号数量
     * @return 生成的序列号列表（Long类型，未格式化）
     * @throws BadRequestException 参数校验失败
     * @throws SequenceGenerationException 获取锁超时或被中断
     */
    private List<Long> generateSequences(String bizType, int count) {
        // 1. 参数校验
        validateParams(bizType, count);

        String lockName = "seq:lock:" + bizType;
        RLock lock = redissonClient.getLock(lockName);
        boolean acquired = false;
        try {
            acquired = lock.tryLock(waitTime.toMillis(), TimeUnit.MILLISECONDS);
            if (!acquired) {
                throw new SequenceGenerationException(
                    ErrorCode.SEQUENCE_LOCK_TIMEOUT,
                    "获取分布式锁超时: bizType=" + bizType
                );
            }
            // 2. 在事务中生成序列号
            return transactionTemplate.execute(status -> generateSequencesInternal(bizType, count));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new SequenceGenerationException(
                ErrorCode.SEQUENCE_LOCK_INTERRUPTED,
                "等待分布式锁被中断: bizType=" + bizType
            );
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                try {
                    lock.unlock();
                } catch (Exception unlockEx) {
                    log.warn(
                        "释放分布式锁失败: lockName={}, bizType={}, isLocked={}",
                        lockName,
                        bizType,
                        lock.isLocked(),
                        unlockEx
                    );
                }
            }
        }
    }

    /**
     * 事务中的序列号生成逻辑
     * 注意：此方法必须在分布式锁和事务的保护下调用
     *
     * @param bizType 业务类型编码
     * @param count 需要生成的序列号数量
     * @return 生成的序列号列表
     */
    private List<Long> generateSequencesInternal(String bizType, int count) {
        // 1. 查找或初始化序列号记录
        SequenceGenerator sequence = sequenceGeneratorRepository
            .findByBizType(bizType)
            .orElseGet(() -> initializeSequence(bizType));

        // 2. 判断是否需要重置或初始化
        if (sequence.getResetStrategy().needReset(sequence.getLastResetTime())) {
            // 需要重置
            log.info(
                "序列号需要重置: bizType={}, strategy={}, lastResetTime={}",
                bizType,
                sequence.getResetStrategy(),
                sequence.getLastResetTime()
            );
            sequence.setCurrentValue(0L);
            sequence.setLastResetTime(Instant.now());
        } else if (sequence.getLastResetTime() == null) {
            // 首次使用，初始化 lastResetTime
            sequence.setLastResetTime(Instant.now());
            log.info("首次使用，初始化 lastResetTime: bizType={}", bizType);
        }

        // 3. 计算序列号范围
        long start = sequence.getCurrentValue() + 1;
        long end = sequence.getCurrentValue() + count;

        // 4. 检查序列号是否溢出（如果是在枚举中的业务类型）
        checkOverflow(bizType, end);

        // 5. 更新数据库中的当前值并保存
        sequence.setCurrentValue(end);
        sequenceGeneratorRepository.save(sequence);

        log.debug(
            "序列号记录更新成功: bizType={}, oldValue={}, newValue={}",
            bizType,
            sequence.getCurrentValue() - count,
            end
        );

        // 6. 生成序列号列表
        List<Long> result = new ArrayList<>(count);
        for (long i = start; i <= end; i++) {
            result.add(i);
        }

        log.info(
            "生成序列号成功: bizType={}, range=[{}, {}], count={}, currentValue={}",
            bizType,
            start,
            end,
            count,
            end
        );

        return result;
    }

    /**
     * 将原始序列值转换为最终字符串
     * 如果业务类型在枚举中定义，则使用枚举的格式化方法；否则返回纯数字字符串
     *
     * @param bizType 业务类型编码
     * @param sequenceNumbers 原始序列号列表
     * @return 格式化后的序列号字符串列表
     */
    private List<String> formatSequences(String bizType, List<Long> sequenceNumbers) {
        SequenceBizType enumType = SequenceBizType.findByCode(bizType).orElse(null);
        if (enumType == null) {
            return sequenceNumbers.stream().map(String::valueOf).collect(Collectors.toList());
        }
        return sequenceNumbers.stream().map(enumType::formatSequenceNo).collect(Collectors.toList());
    }

    /**
     * 初始化序列号记录
     * 从枚举中获取重置策略，如果不在枚举中则默认为 NONE
     *
     * @param bizType 业务类型编码
     * @return 初始化的序列号记录
     */
    private SequenceGenerator initializeSequence(String bizType) {
        SequenceGenerator sequence = new SequenceGenerator();
        sequence.setBizType(bizType);
        sequence.setCurrentValue(0L);

        // 尝试从枚举获取重置策略
        SequenceBizType enumType = SequenceBizType.findByCode(bizType).orElse(null);
        if (enumType != null) {
            sequence.setResetStrategy(enumType.getResetStrategy());
        } else {
            sequence.setResetStrategy(ResetStrategy.NONE);
            log.info("业务类型不在枚举中，使用默认策略 NONE: bizType={}", bizType);
        }

        try {
            SequenceGenerator saved = sequenceGeneratorRepository.save(sequence);
            log.info("初始化序列号记录: bizType={}, resetStrategy={}", bizType, saved.getResetStrategy());
            return saved;
        } catch (DataIntegrityViolationException e) {
            log.warn("初始化序列号记录时发生异常，尝试重新查询: bizType={}", bizType);
            return sequenceGeneratorRepository
                .findByBizType(bizType)
                .orElseThrow(() -> new BadRequestException(String.format("初始化序列号失败: bizType=%s", bizType)));
        }
    }

    /**
     * 检查序列号是否溢出
     *
     * @param bizType 业务类型
     * @param end 序列号结束值
     */
    private void checkOverflow(String bizType, long end) {
        SequenceBizType enumType = SequenceBizType.findByCode(bizType).orElse(null);
        if (enumType == null || enumType.getSequenceLength() <= 0) {
            return;
        }

        long maxValue = (long) Math.pow(10, enumType.getSequenceLength()) - 1;
        if (end > maxValue) {
            log.warn("序列号达到定义长度限制: bizType={}, end={}, maxValue={}", bizType, end, maxValue);
        }
    }

    /**
     * 参数校验
     *
     * @param bizType 业务类型字符串
     * @param count 数量
     * @throws BadRequestException 如果参数非法
     */
    private void validateParams(String bizType, int count) {
        if (bizType == null || bizType.trim().isEmpty()) {
            throw new BadRequestException("业务类型不能为空");
        }

        // 防止sql注入，bizType 只允许字母、数字、下划线
        if (!bizType.matches("^[a-zA-Z0-9_]+$")) {
            throw new BadRequestException("业务类型只能包含字母、数字和下划线");
        }

        if (bizType.length() > MAX_BIZ_TYPE_LENGTH) {
            throw new BadRequestException("业务类型长度不能超过" + MAX_BIZ_TYPE_LENGTH + "个字符");
        }

        if (count <= 0) {
            throw new BadRequestException("获取数量必须大于0");
        }

        if (count > MAX_BATCH_SIZE) {
            throw new BadRequestException("单次获取数量不能超过" + MAX_BATCH_SIZE);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>实现说明：从数据库查询序列号记录的当前值，如果不存在则返回0
     */
    @Override
    public Long getCurrentValue(SequenceBizType bizType) {
        return sequenceGeneratorRepository
            .findByBizType(bizType.getCode())
            .map(SequenceGenerator::getCurrentValue)
            .orElse(0L);
    }

    /**
     * {@inheritDoc}
     *
     * <p>实现说明：从数据库查询序列号记录的当前值，如果不存在则返回0
     */
    @Override
    public Long getCurrentValue(String bizType) {
        return sequenceGeneratorRepository.findByBizType(bizType).map(SequenceGenerator::getCurrentValue).orElse(0L);
    }
}
