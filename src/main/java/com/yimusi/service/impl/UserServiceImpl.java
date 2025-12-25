package com.yimusi.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.yimusi.common.exception.BadRequestException;
import com.yimusi.common.exception.ResourceNotFoundException;
import com.yimusi.dto.auth.FirstAdminCreateRequest;
import com.yimusi.dto.auth.UserRegisterRequest;
import com.yimusi.dto.common.PageResult;
import com.yimusi.dto.user.CreateUserRequest;
import com.yimusi.dto.user.UpdateUserRequest;
import com.yimusi.dto.user.UserPageRequest;
import com.yimusi.dto.user.UserResponse;
import com.yimusi.entity.QUser;
import com.yimusi.entity.User;
import com.yimusi.enums.UserRole;
import com.yimusi.mapper.UserMapper;
import com.yimusi.repository.UserRepository;
import com.yimusi.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户服务实现类，处理所有与用户相关的业务逻辑。
 */
@Slf4j
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
    public User findByUsername(String username) {
        if (StrUtil.isBlank(username)) {
            throw new BadRequestException("用户名不能为空");
        }
        return userRepository
            .findByUsernameAndDeletedFalse(username)
            .orElseThrow(() -> new ResourceNotFoundException(String.format("用户名为 %s 的用户不存在", username)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User validateUser(String username, String password) {
        if (StrUtil.isBlank(username)) {
            throw new BadRequestException("用户名不能为空");
        }
        if (StrUtil.isBlank(password)) {
            throw new BadRequestException("密码不能为空");
        }

        User user = userRepository
            .findByUsernameAndDeletedFalse(username)
            .orElseThrow(() -> new ResourceNotFoundException(String.format("用户名为 %s 的用户不存在", username)));

        if (!user.verifyPassword(password)) {
            throw new BadRequestException("用户名或密码错误");
        }

        return user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserResponse register(UserRegisterRequest registerRequest) {
        if (!isUsernameUnique(registerRequest.getUsername())) {
            throw new BadRequestException(String.format("用户名 %s 已存在", registerRequest.getUsername()));
        }

        User user = userMapper.toEntity(registerRequest);
        user.setPassword(BCrypt.hashpw(registerRequest.getPassword()));
        user.setRole(UserRole.MEMBER);

        return userMapper.toResponse(userRepository.save(user));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserResponse createUser(CreateUserRequest createUserRequest) {
        if (!isUsernameUnique(createUserRequest.getUsername())) {
            throw new BadRequestException(String.format("用户名 %s 已存在", createUserRequest.getUsername()));
        }

        User user = userMapper.toEntity(createUserRequest);
        user.setPassword(BCrypt.hashpw(createUserRequest.getPassword()));

        return userMapper.toResponse(userRepository.save(user));
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public User getUserById(Long id) {
        if (id == null) {
            throw new BadRequestException("用户 ID 不能为空");
        }

        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            throw new ResourceNotFoundException(String.format("ID 为 %s 的用户不存在", id));
        }
        return user;
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
            throw new BadRequestException("用户 ID 不能为空");
        }

        User user = getUserById(id);

        if (updateUserRequest.getUsername() != null && !updateUserRequest.getUsername().equals(user.getUsername())) {
            if (!isUsernameUnique(updateUserRequest.getUsername())) {
                throw new BadRequestException(String.format("用户名 %s 已存在", updateUserRequest.getUsername()));
            }
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
            throw new BadRequestException("用户 ID 不能为空");
        }

        // 先查询验证存在性
        User user = getUserById(id);

        // 直接调用 repository.deleteById()，由 @SQLDelete 自动处理软删除
        userRepository.deleteById(id);

        log.info("删除用户: 用户名={}, ID={}", user.getUsername(), id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isUsernameUnique(String username) {
        if (StrUtil.isBlank(username)) {
            return true;
        }
        return !userRepository.existsByUsernameAndDeletedFalse(username);
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

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isSystemFirstDeployment() {
        long count = userRepository.count();
        log.debug("当前系统用户数量: {}", count);
        return count == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public UserResponse createFirstAdmin(FirstAdminCreateRequest request) {
        log.info("开始创建第一个管理员用户...");

        // 1. 验证系统是否确实首次部署
        if (!isSystemFirstDeployment()) {
            log.warn("系统已存在用户，拒绝创建第一个管理员");
            throw new BadRequestException("系统已存在用户，无法重复创建第一个管理员");
        }

        // 2. 验证密码一致性
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            log.warn("密码不匹配");
            throw new BadRequestException("两次输入的密码不一致");
        }

        // 3. 验证用户名唯一性（理论上总是通过）
        if (!isUsernameUnique("admin")) {
            log.error("用户名admin已存在，这不应该发生");
            throw new BadRequestException("用户名 admin 已存在");
        }

        // 4. 创建用户并分配ADMIN角色
        User user = new User();
        user.setUsername("admin");  // 固定用户名
        user.setPassword(BCrypt.hashpw(request.getPassword()));
        user.setRole(UserRole.ADMIN);  // 关键：自动分配ADMIN角色

        User savedUser = userRepository.save(user);
        log.info("✅ 系统首次部署：成功创建第一个管理员用户 - admin (ID: {})", savedUser.getId());

        return userMapper.toResponse(savedUser);
    }
}
