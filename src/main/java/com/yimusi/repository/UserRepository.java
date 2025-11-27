package com.yimusi.repository;

import com.yimusi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.Optional;

/**
 * 用户数据访问接口.
 * 继承 JpaRepository 提供基础 CRUD 操作.
 * 继承 QuerydslPredicateExecutor 提供 QueryDSL 动态查询能力.
 */
public interface UserRepository extends JpaRepository<User, Long>, QuerydslPredicateExecutor<User> {

    boolean existsByUsernameAndDeletedFalse(String username);

    Optional<User> findByUsernameAndDeletedFalse(String username);

    void deleteByUsername(String username);
}
