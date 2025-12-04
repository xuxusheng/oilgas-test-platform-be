package com.yimusi.mapper;

import com.yimusi.dto.project.CreateProjectRequest;
import com.yimusi.dto.project.UpdateProjectRequest;
import com.yimusi.dto.project.ProjectResponse;
import com.yimusi.entity.Project;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.lang.NonNull;

/**
 * MapStruct映射器接口，用于在Project实体和DTO之间进行转换。
 */
@Mapper(componentModel = "spring", config = BaseMapperConfig.class)
public interface ProjectMapper {

    /**
     * 将Project实体转换为ProjectResponse DTO。
     *
     * @param project 要转换的Project实体
     * @return 转换后的ProjectResponse DTO
     */
    ProjectResponse toResponse(Project project);

    /**
     * 将CreateProjectRequest DTO转换为Project实体。
     * 在创建新实体时，忽略ID字段。
     *
     * @param createProjectRequest 包含新项目数据的DTO
     * @return 转换后的Project实体
     */
    @NonNull
    @Mapping(target = "id", ignore = true)
    Project toEntity(CreateProjectRequest createProjectRequest);

    /**
     * 从UpdateProjectRequest DTO更新一个已存在的Project实体。
     * 在更新时，忽略ID字段的映射。
     *
     * @param updateProjectRequest 包含更新数据的DTO
     * @param project              要被更新的目标Project实体
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntityFromRequest(UpdateProjectRequest updateProjectRequest, @MappingTarget Project project);
}