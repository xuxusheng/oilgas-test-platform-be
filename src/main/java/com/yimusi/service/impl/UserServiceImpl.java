package com.yimusi.service.impl;

import com.yimusi.dto.CreateUserRequest;
import com.yimusi.dto.UpdateUserRequest;
import com.yimusi.dto.UserResponse;
import com.yimusi.entity.User;
import com.yimusi.mapper.UserMapper;
import com.yimusi.repository.UserRepository;
import com.yimusi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

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
    public UserResponse updateUser(Long id, UpdateUserRequest updateUserRequest) {
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
        userRepository.deleteById(id);
    }
}
