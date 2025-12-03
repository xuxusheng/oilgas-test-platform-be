package com.yimusi.service;

import com.yimusi.enums.SequenceBizType;
import java.util.List;

/**
 * 序列号生成服务接口
 * 提供分布式环境下的序列号生成功能
 */
public interface SequenceGeneratorService {
    // ==================== 枚举便捷方法（推荐使用） ====================

    /**
     * 获取单个序列号（枚举参数版本）
     *
     * @param bizType 业务类型枚举
     * @return 序列号
     */
    String nextId(SequenceBizType bizType);

    /**
     * 批量获取序列号（枚举参数版本）
     *
     * @param bizType 业务类型枚举
     * @param count 需要获取的序列号数量
     * @return 序列号列表
     */
    List<String> nextIds(SequenceBizType bizType, int count);

    // ==================== 基础方法（字符串参数，支持动态业务类型） ====================

    /**
     * 获取单个序列号（字符串参数版本）
     * 适用于需要动态拼接 bizType 的场景（如：project_internal_1）
     *
     * @param bizType 业务类型字符串
     * @return 序列号
     */
    String nextId(String bizType);

    /**
     * 批量获取序列号（字符串参数版本）
     * 一次性从数据库获取多个连续的序列号，提升性能
     *
     * @param bizType 业务类型字符串
     * @param count 需要获取的序列号数量
     * @return 序列号列表 [start, start+1, ..., start+count-1]
     */
    List<String> nextIds(String bizType, int count);

    /**
     * 查询当前序列号值（不加锁，仅供查询）
     *
     * @param bizType 业务类型枚举
     * @return 当前序列号值，如果不存在返回0
     */
    Long getCurrentValue(SequenceBizType bizType);

    /**
     * 查询当前序列号值（不加锁，字符串版本）
     *
     * @param bizType 业务类型字符串
     * @return 当前序列号值，如果不存在返回0
     */
    Long getCurrentValue(String bizType);
}
