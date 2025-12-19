package com.yimusi.repository;

import com.yimusi.entity.OilSample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

/**
 * 油样仓储接口，供后续业务层使用。
 */
public interface OilSampleRepository extends
    JpaRepository<OilSample, Long>,
    JpaSpecificationExecutor<OilSample>,
    QuerydslPredicateExecutor<OilSample> {

    /**
     * 检查油样编号是否存在
     *
     * @param sampleNo 油样编号
     * @return true 如果存在，false 如果不存在
     */
    boolean existsBySampleNo(String sampleNo);

    /**
     * 检查油样编号是否存在，排除指定 ID（用于更新时校验）
     *
     * @param sampleNo 油样编号
     * @param id       要排除的 ID
     * @return true 如果存在，false 如果不存在
     */
    boolean existsBySampleNoAndIdNot(String sampleNo, Long id);
}
