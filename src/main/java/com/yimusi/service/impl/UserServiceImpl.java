package com.yimusi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.yimusi.common.exception.BadRequestException;
import com.yimusi.common.exception.ResourceNotFoundException;
import com.yimusi.common.util.OperatorUtil;
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
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import cn.hutool.crypto.digest.BCrypt;

/**
 * 用户服务实现类，处理所有与用户相关的业务逻辑。
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public UserResponse createUser(CreateUserRequest createUserRequest) {
        validateUsernameUnique(createUserRequest.getUsername());

        User user = userMapper.toEntity(createUserRequest);
        if (createUserRequest.getPassword() != null) {
            user.setPassword(BCrypt.hashpw(createUserRequest.getPassword()));
        }
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

        if (updateUserRequest.getUsername() != null && !updateUserRequest.getUsername().equals(user.getUsername())) {
            validateUsernameUnique(updateUserRequest.getUsername());
        }

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

        User user = getUserById(id);
        markDeleted(user);
        userRepository.save(user);
    }

    @Override
    public void restoreUser(Long id) {
        if (id == null) {
            throw new BadRequestException("用户ID不能为空");
        }

        User user = userRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(String.format("ID 为 %s 的用户不存在", id)));
        user.setDeleted(false);
        user.setDeletedAt(null);
        user.setDeletedBy(null);
        userRepository.save(user);
    }

    private void markDeleted(User user) {
        user.setDeleted(true);
        user.setDeletedAt(Instant.now());
        user.setDeletedBy(OperatorUtil.getOperator());
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
        builder.and(qUser.deleted.isFalse());

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

    private void validateUsernameUnique(String username) {
        if (username == null) {
            return;
        }
        boolean exists = userRepository.existsByUsernameAndDeletedFalse(username);
        if (exists) {
            throw new BadRequestException(String.format("用户名 %s 已存在", username));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User findByUsername(String username) {
        if (username == null) {
            throw new BadRequestException("用户名不能为空");
        }
        return userRepository.findByUsernameAndDeletedFalse(username)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("用户名 %s 不存在", username)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User validateUser(String username, String password) {
        User user = findByUsername(username);
        if (!user.verifyPassword(password)) {
            throw new BadRequestException("用户名或密码错误");
        }
        return user;
    }
}
