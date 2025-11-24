package com.yimusi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.yimusi.common.exception.BadRequestException;
import com.yimusi.common.exception.ResourceNotFoundException;
import com.yimusi.dto.CreateUserRequest;
import com.yimusi.dto.PageResult;
import com.yimusi.dto.UpdateUserRequest;
import com.yimusi.dto.UserPageRequest;
import com.yimusi.dto.UserResponse;
import com.yimusi.entity.QUser;
import com.yimusi.entity.User;
import com.yimusi.mapper.UserMapper;
import com.yimusi.repository.UserRepository;
import com.yimusi.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类，处理所有与用户相关的业务逻辑。
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public UserResponse createUser(CreateUserRequest createUserRequest) {
        User user = userMapper.toEntity(createUserRequest);
        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User getUserById(Long id) {
        if (id == null) {
            throw new BadRequestException("用户ID不能为空");
        }

        // 查询用户，如果不存在则抛出业务异常
        return userRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(String.format("ID 为 %s 的用户不存在", id)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageResult<UserResponse> getUsersPage(UserPageRequest request) {
        // 构建 QueryDSL 查询条件
        Predicate predicate = buildUserPredicate(request);

        // 执行分页查询（自动处理页码转换和排序）
        Page<User> userPage = userRepository.findAll(predicate, request.toJpaPageRequest());

        // 转换并返回结果
        return PageResult.from(userPage.map(userMapper::toResponse));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserResponse updateUser(Long id, UpdateUserRequest updateUserRequest) {
        if (id == null) {
            throw new BadRequestException("用户ID不能为空");
        }

        User user = getUserById(id);

        userMapper.updateEntityFromRequest(updateUserRequest, user);

        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteUser(Long id) {
        if (id == null) {
            throw new BadRequestException("用户ID不能为空");
        }

        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException(String.format("ID 为 %s 的用户不存在", id));
        }

        userRepository.deleteById(id);
    }

    /**
     * 使用 QueryDSL 构建用户查询条件.
     *
     * @param request 分页查询请求
     * @return Predicate 查询条件
     */
    @NonNull
    private Predicate buildUserPredicate(UserPageRequest request) {
        QUser qUser = QUser.user;
        BooleanBuilder builder = new BooleanBuilder();

        // 用户名模糊查询
        if (StrUtil.isNotBlank(request.getUsername())) {
            builder.and(qUser.username.containsIgnoreCase(request.getUsername()));
        }

        // 角色精确匹配
        if (request.getRole() != null) {
            builder.and(qUser.role.eq(request.getRole()));
        }

        return builder;
    }
}
