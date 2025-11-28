package com.yimusi.service;

import com.yimusi.common.exception.BadRequestException;
import com.yimusi.entity.SequenceGenerator;
import com.yimusi.enums.SequenceBizType;
import com.yimusi.repository.SequenceGeneratorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 序列号生成服务
 * 提供分布式环境下的序列号生成功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SequenceGeneratorService {

    private final SequenceGeneratorRepository sequenceGeneratorRepository;

    // ==================== 枚举便捷方法（推荐使用） ====================

    /**
     * 获取单个序列号（枚举参数版本）
     *
     * @param bizType 业务类型枚举
     * @return 序列号
     */
    public Long nextId(SequenceBizType bizType) {
        return nextIds(bizType, 1).get(0);
    }

    /**
     * 批量获取序列号（枚举参数版本）
     *
     * @param bizType 业务类型枚举
     * @param count 需要获取的序列号数量
     * @return 序列号列表
     */
    public List<Long> nextIds(SequenceBizType bizType, int count) {
        return nextIds(bizType.getCode(), count);
    }

    /**
     * 获取格式化的序列号字符串（单个）
     * 业务代码推荐使用此方法
     *
     * @param bizType 业务类型枚举
     * @return 格式化后的序列号字符串（如：IND202501280001）
     */
    public String nextFormattedId(SequenceBizType bizType) {
        Long seqNo = nextId(bizType);
        return bizType.formatSequenceNo(seqNo);
    }

    /**
     * 批量获取格式化的序列号字符串
     *
     * @param bizType 业务类型枚举
     * @param count 需要获取的数量
     * @return 格式化后的序列号字符串列表
     */
    public List<String> nextFormattedIds(SequenceBizType bizType, int count) {
        List<Long> seqNos = nextIds(bizType, count);
        List<String> result = new ArrayList<>(count);
        for (Long seqNo : seqNos) {
            result.add(bizType.formatSequenceNo(seqNo));
        }
        return result;
    }

    // ==================== 基础方法（字符串参数，支持动态业务类型） ====================

    /**
     * 获取单个序列号（字符串参数版本）
     * 适用于需要动态拼接 bizType 的场景（如：project_internal_1）
     *
     * @param bizType 业务类型字符串
     * @return 序列号
     */
    public Long nextId(String bizType) {
        return nextIds(bizType, 1).get(0);
    }

    /**
     * 批量获取序列号（字符串参数版本）
     * 一次性从数据库获取多个连续的序列号，提升性能
     *
     * @param bizType 业务类型字符串
     * @param count 需要获取的序列号数量
     * @return 序列号列表 [start, start+1, ..., start+count-1]
     * @throws BadRequestException 如果参数非法
     */
    @Transactional(rollbackFor = Exception.class)
    public List<Long> nextIds(String bizType, int count) {
        // 1. 参数校验
        validateParams(bizType, count);

        // 2. 使用悲观锁查询或创建记录
        SequenceGenerator sequence = sequenceGeneratorRepository
            .findByBizTypeForUpdate(bizType)
            .orElseGet(() -> initializeSequence(bizType));

        // 3. 判断是否需要重置或初始化
        if (sequence.getResetStrategy().needReset(sequence.getLastResetTime())) {
            // 需要重置
            if (log.isInfoEnabled()) {
                log.info("序列号需要重置: bizType={}, strategy={}, lastResetTime={}",
                        bizType, sequence.getResetStrategy(), sequence.getLastResetTime());
            }
            sequence.setCurrentValue(0L);
            sequence.setLastResetTime(Instant.now());
        } else if (sequence.getLastResetTime() == null) {
            // 首次使用，初始化 lastResetTime
            sequence.setLastResetTime(Instant.now());
            if (log.isInfoEnabled()) {
                log.info("首次使用，初始化 lastResetTime: bizType={}", bizType);
            }
        }

        // 4. 计算序列号范围
        long start = sequence.getCurrentValue() + 1;
        long end = sequence.getCurrentValue() + count;

        // 5. 检查序列号是否溢出（如果是在枚举中的业务类型）
        try {
            SequenceBizType enumType = SequenceBizType.fromCode(bizType);
            if (enumType.getSequenceLength() > 0) {
                long maxValue = (long) Math.pow(10, enumType.getSequenceLength()) - 1;
                if (end > maxValue) {
                    throw new BadRequestException(String.format(
                        "序列号溢出: bizType=%s, currentValue=%d, count=%d, maxValue=%d",
                        bizType, sequence.getCurrentValue(), count, maxValue
                    ));
                }
            }
        } catch (IllegalArgumentException e) {
            // 不在枚举中的业务类型，不检查溢出
            if (log.isDebugEnabled()) {
                log.debug("业务类型不在枚举中，跳过溢出检查: bizType={}", bizType);
            }
        }

        // 6. 更新数据库中的当前值并保存
        sequence.setCurrentValue(end);
        sequenceGeneratorRepository.save(sequence);

        if (log.isDebugEnabled()) {
            log.debug("序列号记录更新成功: bizType={}, oldValue={}, newValue={}",
                    bizType, sequence.getCurrentValue() - count, end);
        }

        // 7. 生成序列号列表
        List<Long> result = new ArrayList<>(count);
        for (long i = start; i <= end; i++) {
            result.add(i);
        }

        if (log.isInfoEnabled()) {
            log.info("生成序列号成功: bizType={}, range=[{}, {}], count={}, currentValue={}",
                    bizType, start, end, count, end);
        }

        return result;
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
        try {
            SequenceBizType enumType = SequenceBizType.fromCode(bizType);
            sequence.setResetStrategy(enumType.getResetStrategy());
        } catch (IllegalArgumentException e) {
            // 如果不在枚举中，默认不重置（支持动态 bizType）
            sequence.setResetStrategy(com.yimusi.enums.ResetStrategy.NONE);
            if (log.isInfoEnabled()) {
                log.info("业务类型不在枚举中，使用默认策略 NONE: bizType={}", bizType);
            }
        }

        try {
            SequenceGenerator saved = sequenceGeneratorRepository.save(sequence);
            if (log.isInfoEnabled()) {
                log.info("初始化序列号记录: bizType={}, resetStrategy={}",
                        bizType, saved.getResetStrategy());
            }
            return saved;
        } catch (DataIntegrityViolationException e) {
            // 并发场景下可能出现唯一键冲突，重新查询
            if (log.isWarnEnabled()) {
                log.warn("初始化序列号记录时发生唯一键冲突，尝试重新查询: bizType={}", bizType);
            }
            return sequenceGeneratorRepository
                .findByBizTypeForUpdate(bizType)
                .orElseThrow(() -> new BadRequestException(
                    String.format("初始化序列号失败: bizType=%s", bizType)
                ));
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

        if (bizType.length() > 50) {
            throw new BadRequestException("业务类型长度不能超过50个字符");
        }

        if (count <= 0) {
            throw new BadRequestException("获取数量必须大于0");
        }

        if (count > 10000) {
            throw new BadRequestException("单次获取数量不能超过10000");
        }
    }

    /**
     * 查询当前序列号值（不加锁，仅供查询）
     *
     * @param bizType 业务类型枚举
     * @return 当前序列号值，如果不存在返回0
     */
    public Long getCurrentValue(SequenceBizType bizType) {
        return sequenceGeneratorRepository
            .findByBizType(bizType.getCode())
            .map(SequenceGenerator::getCurrentValue)
            .orElse(0L);
    }

    /**
     * 查询当前序列号值（不加锁，字符串版本）
     *
     * @param bizType 业务类型字符串
     * @return 当前序列号值，如果不存在返回0
     */
    public Long getCurrentValue(String bizType) {
        return sequenceGeneratorRepository
            .findByBizType(bizType)
            .map(SequenceGenerator::getCurrentValue)
            .orElse(0L);
    }
}