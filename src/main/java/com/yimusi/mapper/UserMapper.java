package com.yimusi.mapper;

import com.yimusi.dto.user.CreateUserRequest;
import com.yimusi.dto.user.UpdateUserRequest;
import com.yimusi.dto.user.UserResponse;
import com.yimusi.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.lang.NonNull;

/**
 * MapStruct映射器接口，用于在User实体和DTO之间进行转换。
 */
@Mapper(componentModel = "spring", config = BaseMapperConfig.class)
public interface UserMapper {
    /**
     * 将User实体转换为UserResponse DTO。
     *
     * @param user 要转换的User实体
     * @return 转换后的UserResponse DTO
     */
    UserResponse toResponse(User user);

    /**
     * 将CreateUserRequest DTO转换为User实体。
     * 在创建新实体时，忽略ID字段。
     *
     * @param createUserRequest 包含新用户数据的DTO
     * @return 转换后的User实体
     */
    @NonNull
    @Mapping(target = "id", ignore = true)
    User toEntity(CreateUserRequest createUserRequest);

    /**
     * 将UserRegisterRequest DTO转换为User实体。
     *
     * @param registerRequest 注册请求DTO
     * @return 转换后的User实体
     */
    @NonNull
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true) // 角色在Service层手动设置为MEMBER
    User toEntity(com.yimusi.dto.auth.UserRegisterRequest registerRequest);

    /**
     * 从UpdateUserRequest DTO更新一个已存在的User实体。
     * 在更新时，忽略ID字段的映射。
     *
     * @param updateUserRequest 包含更新数据的DTO
     * @param user              要被更新的目标User实体
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntityFromRequest(UpdateUserRequest updateUserRequest, @MappingTarget User user);
}
