package com.yimusi.repository;

import com.yimusi.entity.TestStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.Optional;

/**
 * 测试工位数据访问接口.
 * 继承 JpaRepository 提供基础 CRUD 操作.
 * 继承 QuerydslPredicateExecutor 提供 QueryDSL 动态查询能力.
 */
public interface TestStationRepository extends JpaRepository<TestStation, Long>, QuerydslPredicateExecutor<TestStation> {

    /**
     * 根据工位编号和未删除状态查找工位
     *
     * @param stationNo 工位编号
     * @return 工位实体（如果存在）
     */
    Optional<TestStation> findByStationNoAndDeletedFalse(Integer stationNo);

    /**
     * 根据工位编号和未删除状态检查工位是否存在
     *
     * @param stationNo 工位编号
     * @return 是否存在
     */
    boolean existsByStationNoAndDeletedFalse(Integer stationNo);

    /**
     * 根据责任人和未删除状态查找工位列表
     *
     * @param responsiblePerson 责任人
     * @return 工位实体列表
     */
    java.util.List<TestStation> findByResponsiblePersonAndDeletedFalse(String responsiblePerson);
}
