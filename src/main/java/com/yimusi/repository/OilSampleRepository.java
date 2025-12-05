package com.yimusi.repository;

import com.yimusi.entity.OilSample;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 油样仓储接口，供后续业务层使用。
 */
public interface OilSampleRepository extends JpaRepository<OilSample, Long> {
}
