package com.yimusi.service;

import com.yimusi.dto.CreateUserRequest;
import com.yimusi.dto.UpdateUserRequest;
import com.yimusi.dto.UserResponse;
import com.yimusi.entity.User;

import java.util.List;

/**
 * 用户服务接口，定义了用户相关的业务操作。
 */
public interface UserService {
    /**
     * 获取所有用户列表。
     *
     * @return 包含所有用户的列表
     */
    List<User> getAllUsers();

    /**
     * 根据用户ID获取用户信息。
     *
     * @param id 要查找的用户ID
     * @return 找到的用户实体
     * @throws RuntimeException 如果用户未找到
     */
    User getUserById(Long id);

    /**
     * 创建一个新用户。
     *
     * @param createUserRequest 包含新用户信息的请求体
     * @return 创建成功后的用户信息响应体
     */
    UserResponse createUser(CreateUserRequest createUserRequest);

    /**
     * 更新指定ID的用户信息。
     *
     * @param id                要更新的用户ID
     * @param updateUserRequest 包含要更新的用户信息的请求体
     * @return 更新成功后的用户信息响应体
     */
    UserResponse updateUser(Long id, UpdateUserRequest updateUserRequest);

    /**
     * 根据ID删除用户。
     *
     * @param id 要删除的用户ID
     */
    void deleteUser(Long id);
}
