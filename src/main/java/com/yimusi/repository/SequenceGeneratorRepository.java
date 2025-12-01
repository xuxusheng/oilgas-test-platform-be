package com.yimusi.repository;

import com.yimusi.entity.SequenceGenerator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 序列号生成器 Repository
 */
@Repository
public interface SequenceGeneratorRepository extends JpaRepository<SequenceGenerator, Long> {

    /**
     * 普通查询（不加锁）
     *
     * @param bizType 业务类型
     * @return 序列号记录（如果存在）
     */
    Optional<SequenceGenerator> findByBizType(String bizType);
}
