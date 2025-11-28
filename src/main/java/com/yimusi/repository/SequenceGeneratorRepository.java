package com.yimusi.repository;

import com.yimusi.entity.SequenceGenerator;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 序列号生成器 Repository
 */
@Repository
public interface SequenceGeneratorRepository extends JpaRepository<SequenceGenerator, Long> {

    /**
     * 使用悲观锁查询序列号记录
     * FOR UPDATE 会锁定该行，直到事务提交
     *
     * @param bizType 业务类型
     * @return 序列号记录（如果存在）
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SequenceGenerator s WHERE s.bizType = :bizType")
    Optional<SequenceGenerator> findByBizTypeForUpdate(@Param("bizType") String bizType);

    /**
     * 普通查询（不加锁）
     *
     * @param bizType 业务类型
     * @return 序列号记录（如果存在）
     */
    Optional<SequenceGenerator> findByBizType(String bizType);
}