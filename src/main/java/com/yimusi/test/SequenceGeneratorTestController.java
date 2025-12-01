package com.yimusi.test;

import com.yimusi.enums.SequenceBizType;
import com.yimusi.service.SequenceGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 序列号生成器测试控制器
 * 用于验证分布式序列号生成功能
 */
@Slf4j
@RestController
@RequestMapping("/api/test/sequence")
@RequiredArgsConstructor
public class SequenceGeneratorTestController {

    private final SequenceGeneratorService sequenceGeneratorService;

    /**
     * 获取单个序列号（设备编号）
     */
    @GetMapping("/device-no")
    public Map<String, Object> getDeviceNo() {
        String deviceNo = sequenceGeneratorService.nextId(SequenceBizType.INSPECTION_DEVICE);
        
        log.info("生成设备编号: {}", deviceNo);
        
        Map<String, Object> result = new HashMap<>();
        result.put("deviceNo", deviceNo);
        result.put("message", "设备编号生成成功");
        return result;
    }

    /**
     * 批量生成设备编号
     */
    @GetMapping("/device-nos")
    public Map<String, Object> getDeviceNos(@RequestParam(defaultValue = "5") int count) {
        List<String> deviceNos = sequenceGeneratorService.nextIds(
            SequenceBizType.INSPECTION_DEVICE,
            count
        );
        
        log.info("批量生成设备编号: {}", deviceNos);
        
        Map<String, Object> result = new HashMap<>();
        result.put("deviceNos", deviceNos);
        result.put("count", deviceNos.size());
        result.put("message", "批量生成设备编号成功");
        return result;
    }

    /**
     * 获取项目内部序号
     */
    @GetMapping("/project-internal")
    public Map<String, Object> getProjectInternalNo(
            @RequestParam Long projectId,
            @RequestParam(defaultValue = "1") int count) {
        
        String bizType = "project_internal_" + projectId;
        List<String> internalNos = sequenceGeneratorService.nextIds(bizType, count);
        
        log.info("生成项目内部序号: projectId={}, sequenceNos={}", projectId, internalNos);
        
        Map<String, Object> result = new HashMap<>();
        result.put("projectId", projectId);
        result.put("sequenceNos", internalNos);
        result.put("count", internalNos.size());
        result.put("bizType", bizType);
        result.put("message", "项目内部序号生成成功");
        return result;
    }

    /**
     * 查询当前序列号值
     */
    @GetMapping("/current-value")
    public Map<String, Object> getCurrentValue(
            @RequestParam(required = false) String bizType,
            @RequestParam(required = false) SequenceBizType enumBizType) {
        
        Long currentValue;
        String description;
        
        if (enumBizType != null) {
            currentValue = sequenceGeneratorService.getCurrentValue(enumBizType);
            description = enumBizType.getDescription();
            bizType = enumBizType.getCode();
        } else if (bizType != null) {
            currentValue = sequenceGeneratorService.getCurrentValue(bizType);
            description = "动态业务类型";
        } else {
            return Map.of(
                "error", "请提供 bizType 或 enumBizType 参数",
                "success", false
            );
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("bizType", bizType);
        result.put("description", description);
        result.put("currentValue", currentValue);
        result.put("success", true);
        return result;
    }

    /**
     * 并发测试（模拟多个请求同时生成序列号）
     */
    @GetMapping("/stress-test")
    public Map<String, Object> stressTest(
            @RequestParam(defaultValue = "10") int threadCount,
            @RequestParam(defaultValue = "10") int countPerThread) throws InterruptedException {
        
        log.info("开始压力测试: threadCount={}, countPerThread={}", threadCount, countPerThread);
        
        long startTime = System.currentTimeMillis();
        int totalRequests = threadCount * countPerThread;
        
        // 使用多线程模拟并发
        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                try {
                    List<String> deviceNos = sequenceGeneratorService.nextIds(
                        SequenceBizType.INSPECTION_DEVICE,
                        countPerThread
                    );
                    log.info("线程 {} 生成设备编号: {}", threadIndex, deviceNos);
                } catch (Exception e) {
                    log.error("线程 {} 生成失败", threadIndex, e);
                }
            });
        }
        
        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }
        
        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        log.info("压力测试完成: 总请求数={}, 耗时={}ms, QPS={}",
            totalRequests, duration, totalRequests * 1000.0 / duration);
        
        Map<String, Object> result = new HashMap<>();
        result.put("threadCount", threadCount);
        result.put("countPerThread", countPerThread);
        result.put("totalRequests", totalRequests);
        result.put("duration", duration + "ms");
        result.put("qps", String.format("%.2f", totalRequests * 1000.0 / duration));
        result.put("message", "压力测试完成");
        return result;
    }

    /**
     * 获取所有序列号生成器的统计信息
     */
    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> result = new HashMap<>();
        
        for (SequenceBizType type : SequenceBizType.values()) {
            Long currentValue = sequenceGeneratorService.getCurrentValue(type);
            Map<String, Object> typeInfo = new HashMap<>();
            typeInfo.put("description", type.getDescription());
            typeInfo.put("prefix", type.getPrefix());
            typeInfo.put("sequenceLength", type.getSequenceLength());
            typeInfo.put("resetStrategy", type.getResetStrategy().name());
            typeInfo.put("currentValue", currentValue);
            
            result.put(type.getCode(), typeInfo);
        }
        
        return result;
    }
}
