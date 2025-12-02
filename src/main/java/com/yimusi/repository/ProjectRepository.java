package com.yimusi.repository;

import com.yimusi.entity.Project;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import jakarta.persistence.LockModeType;

/**
 * 项目数据访问接口.
 * 继承 JpaRepository 提供基础 CRUD 操作.
 * 继承 QuerydslPredicateExecutor 提供 QueryDSL 动态查询能力.
 */
public interface ProjectRepository extends JpaRepository<Project, Long>, QuerydslPredicateExecutor<Project> {

    /**
     * 根据项目编号和未删除状态检查项目是否存在
     *
     * @param projectNo 项目编号
     * @return 是否存在
     */
    boolean existsByProjectNoAndDeletedFalse(String projectNo);

    /**
     * 根据项目编号和未删除状态查找项目
     *
     * @param projectNo 项目编号
     * @return 项目实体（如果存在）
     */
    Optional<Project> findByProjectNoAndDeletedFalse(String projectNo);

    /**
     * 根据项目名称和未删除状态查找项目（支持项目名称重复的场景）
     *
     * @param projectName 项目名称
     * @return 项目实体列表
     */
    List<Project> findByProjectNameAndDeletedFalse(String projectName);

    /**
     * 根据项目负责人和未删除状态查找项目
     *
     * @param projectLeader 项目负责人
     * @return 项目实体列表
     */
    List<Project> findByProjectLeaderAndDeletedFalse(String projectLeader);

    /**
     * 为指定项目获取悲观写锁，避免并发写入造成的冲突
     *
     * @param projectId 项目ID
     * @return 项目实体（仅用于锁定）
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Project p where p.id = :projectId")
    Optional<Project> lockById(Long projectId);
}
