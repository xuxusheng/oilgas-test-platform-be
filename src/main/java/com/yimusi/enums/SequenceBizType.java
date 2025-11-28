package com.yimusi.enums;

import com.yimusi.common.exception.BadRequestException;
import lombok.Getter;

/**
 * 序列号业务类型枚举
 * 用于管理系统中需要生成序列号的各种业务场景
 */
@Getter
public enum SequenceBizType {

    /**
     * 检测设备编号
     * 格式：IND + YYYYMMDD + 4位流水号
     * 示例：IND202501280001
     * 说明：按日期重置，每天从0001开始
     */
    INSPECTION_DEVICE("inspection_device", "检测设备编号", "IND", 4, ResetStrategy.DAILY),

    /**
     * 项目内部序号
     * 格式：纯数字
     * 示例：1, 2, 3...
     * 说明：不重置，全局递增（如需按项目隔离，使用字符串API动态拼接bizType）
     */
    PROJECT_INTERNAL("project_internal", "项目内部序号", "", 0, ResetStrategy.NONE);

    private final String code;
    private final String description;
    private final String prefix;
    private final int sequenceLength;
    private final ResetStrategy resetStrategy;

    SequenceBizType(String code, String description, String prefix, int sequenceLength, ResetStrategy resetStrategy) {
        this.code = code;
        this.description = description;
        this.prefix = prefix;
        this.sequenceLength = sequenceLength;
        this.resetStrategy = resetStrategy;
    }

    /**
     * 格式化序列号
     *
     * @param seqNo 序列号
     * @return 格式化后的完整编号
     * @throws BadRequestException 如果序列号超出长度限制
     */
    public String formatSequenceNo(Long seqNo) {
        String datePart = resetStrategy.getDatePart();

        // 检查序列号是否超出长度限制
        if (sequenceLength > 0) {
            long maxValue = (long) Math.pow(10, sequenceLength) - 1;
            if (seqNo > maxValue) {
                throw new BadRequestException(String.format(
                    "序列号 %d 超出最大长度限制（%d位，最大值：%d）",
                    seqNo, sequenceLength, maxValue
                ));
            }
            String seqStr = String.format("%0" + sequenceLength + "d", seqNo);
            return prefix + datePart + seqStr;
        } else {
            // 不限制长度，直接返回
            return prefix + seqNo;
        }
    }

    /**
     * 根据编码获取枚举
     *
     * @param code 业务类型编码
     * @return 对应的枚举值
     * @throws IllegalArgumentException 如果编码不存在
     */
    public static SequenceBizType fromCode(String code) {
        for (SequenceBizType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的业务类型: " + code);
    }
}