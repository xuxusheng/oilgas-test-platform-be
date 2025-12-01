package com.yimusi.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 检测设备编号生成器
 * 编号规则：IND + YYYYMMDD + 4位流水号
 * 例如：IND202501280001
 */
@Slf4j
@Component
public class InspectionDeviceNoGenerator {

    private static final String PREFIX = "IND";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final int SEQUENCE_LENGTH = 4;

    private final AtomicInteger sequence = new AtomicInteger(0);
    private String currentDate = "";

    /**
     * 生成检测设备编号
     *
     * @return 生成的设备编号
     */
    public synchronized String generateDeviceNo() {
        String today = LocalDate.now().format(DATE_FORMATTER);

        // 如果日期变更，重置序列号
        if (!today.equals(currentDate)) {
            currentDate = today;
            sequence.set(0);
        }

        // 递增序列号
        int seq = sequence.incrementAndGet();

        // 格式化序列号，不足位数前补0
        String sequenceStr = String.format("%0" + SEQUENCE_LENGTH + "d", seq);

        String deviceNo = PREFIX + currentDate + sequenceStr;
        log.debug("Generated inspection device number: {}", deviceNo);

        return deviceNo;
    }

    /**
     * 根据现有最大编号重置序列号
     * 用于系统启动时从数据库加载最大编号
     *
     * @param maxDeviceNo 当前数据库中最大的设备编号
     */
    public synchronized void resetSequence(String maxDeviceNo) {
        if (maxDeviceNo == null || maxDeviceNo.length() < PREFIX.length() + 8 + SEQUENCE_LENGTH) {
            return;
        }

        try {
            String dateStr = maxDeviceNo.substring(PREFIX.length(), PREFIX.length() + 8);
            String seqStr = maxDeviceNo.substring(PREFIX.length() + 8);

            String today = LocalDate.now().format(DATE_FORMATTER);

            // 只有当日期相同时才重置序列号
            if (dateStr.equals(today)) {
                int seq = Integer.parseInt(seqStr);
                sequence.set(seq);
                currentDate = today;
                log.info("Reset inspection device sequence to {} for date {}", seq, today);
            }
        } catch (Exception e) {
            log.warn("Failed to reset sequence from max device number: {}", maxDeviceNo, e);
        }
    }
}
