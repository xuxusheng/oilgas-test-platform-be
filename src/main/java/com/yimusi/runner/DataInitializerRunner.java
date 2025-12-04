package com.yimusi.runner;

import com.yimusi.entity.SequenceGenerator;
import com.yimusi.enums.SequenceBizType;
import com.yimusi.repository.SequenceGeneratorRepository;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 应用启动时的数据初始化器
 * <p>
 * 该 Runner 会在 Spring Boot 应用启动成功后自动执行。
 * 它的主要职责是检查并初始化系统中必须的基础数据，例如序列号生成器的配置。
 * 通过实现幂等性检查（检查数据是否已存在），可以确保每次重启应用都不会重复插入数据，避免了启动错误。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializerRunner implements ApplicationRunner {

    private final SequenceGeneratorRepository sequenceGeneratorRepository;

    @Override
    public void run(ApplicationArguments args) {
        log.info("开始执行应用数据初始化任务...");

        // 遍历所有 SequenceBizType 枚举，为每一种类型都进行初始化检查
        Arrays.stream(SequenceBizType.values()).forEach(this::initializeSequence);

        log.info("数据初始化任务执行完毕。");
    }

    /**
     * 初始化单个序列号生成器配置
     *
     * @param bizType 业务类型枚举
     */
    private void initializeSequence(SequenceBizType bizType) {
        // 使用 bizType 的 code 作为唯一的业务标识
        String bizTypeCode = bizType.getCode();

        // 关键步骤：通过业务标识查询数据库，检查该序列号配置是否已存在
        Optional<SequenceGenerator> existingSequence = sequenceGeneratorRepository.findByBizType(bizTypeCode);

        if (existingSequence.isEmpty()) {
            // 如果不存在，则创建一个新的 SequenceGenerator 实例并进行持久化
            SequenceGenerator newSequence = new SequenceGenerator();
            newSequence.setBizType(bizTypeCode);
            newSequence.setResetStrategy(bizType.getResetStrategy());
            newSequence.setCurrentValue(0L); // 初始值设为 0

            sequenceGeneratorRepository.save(newSequence);
            log.info("成功初始化序列号 [{}]", bizTypeCode);
        } else {
            // 如果已存在，则跳过
            log.info("序列号 [{}] 已存在, 跳过初始化。", bizTypeCode);
        }
    }
}
