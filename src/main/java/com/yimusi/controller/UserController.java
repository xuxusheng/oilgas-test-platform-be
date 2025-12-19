package com.yimusi.controller;

import com.yimusi.common.model.ApiResponse;
import com.yimusi.dto.common.PageResult;
import com.yimusi.dto.user.CreateUserRequest;
import com.yimusi.dto.user.UpdateUserRequest;
import com.yimusi.dto.user.UserPageRequest;
import com.yimusi.dto.user.UserResponse;
import com.yimusi.mapper.UserMapper;
import com.yimusi.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理接口
 * 提供用户的增删改查及分页查询功能
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    /**
     * 获取所有用户列表
     *
     * @return {@link UserResponse} 列表
     */
    @GetMapping
    public ApiResponse<List<UserResponse>> getAllUsers() {
        List<UserResponse> responses = userService.getAllUsers().stream().map(userMapper::toResponse).toList();
        return ApiResponse.success(responses);
    }

    /**
     * 分页查询用户列表
     *
     * @param request 分页查询请求参数
     * @return 分页结果，包含用户列表及分页信息
     */
    @GetMapping("/page")
    public ApiResponse<PageResult<UserResponse>> getUsersPage(@Valid UserPageRequest request) {
        PageResult<UserResponse> pageResult = userService.getUsersPage(request);
        return ApiResponse.success(pageResult);
    }

    /**
     * 根据ID查询用户详情
     *
     * @param id 用户 ID
     * @return 查询到的 {@link UserResponse}
     */
    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse response = userMapper.toResponse(userService.getUserById(id));
        return ApiResponse.success(response);
    }

    /**
     * 创建新用户
     *
     * @param createUserRequest 包含用户信息的请求体
     * @return 新增的 {@link UserResponse}
     */
    @PostMapping
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody CreateUserRequest createUserRequest) {
        UserResponse userResponse = userService.createUser(createUserRequest);
        return ApiResponse.success(userResponse);
    }

    /**
     * 更新用户信息
     *
     * @param id 需要更新的用户 ID
     * @param updateUserRequest 更新字段的请求体
     * @return 更新后的 {@link UserResponse}
     */
    @PutMapping("/{id}")
    public ApiResponse<UserResponse> updateUser(
        @PathVariable Long id,
        @Valid @RequestBody UpdateUserRequest updateUserRequest
    ) {
        UserResponse updated = userService.updateUser(id, updateUserRequest);
        return ApiResponse.success(updated);
    }

    /**
     * 删除用户
     *
     * @param id 待删除的用户 ID
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success();
    }

    /**
     * 判断用户名是否唯一
     *
     * @param username 用户名
     * @return 是否唯一
     */
    @GetMapping("/validate-username/{username}")
    public ApiResponse<Boolean> validateUsernameUnique(@PathVariable String username) {
        boolean isUnique = userService.isUsernameUnique(username);
        return ApiResponse.success(isUnique);
    }
}
