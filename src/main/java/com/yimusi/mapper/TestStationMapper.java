package com.yimusi.mapper;

import com.yimusi.dto.teststation.CreateTestStationRequest;
import com.yimusi.dto.teststation.TestStationResponse;
import com.yimusi.dto.teststation.UpdateTestStationRequest;
import com.yimusi.dto.teststation.parameter.TestStationParameterRequest;
import com.yimusi.dto.teststation.parameter.TestStationParameterResponse;
import com.yimusi.entity.TestStation;
import com.yimusi.entity.TestStationParameter;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * MapStruct映射器接口，用于在TestStation实体和DTO之间进行转换。
 */
@Mapper(componentModel = "spring", config = BaseMapperConfig.class)
public interface TestStationMapper {

    /**
     * 将TestStation实体转换为TestStationResponse DTO。
     *
     * @param testStation 要转换的TestStation实体
     * @return 转换后的TestStationResponse DTO
     */
    TestStationResponse toResponse(TestStation testStation);

    /**
     * 将CreateTestStationRequest DTO转换为TestStation实体。
     * 在创建新实体时，忽略ID字段（ID由数据库自动生成）。
     *
     * @param createRequest 包含新测试工位数据的DTO
     * @return 转换后的TestStation实体
     */
    @Mapping(target = "id", ignore = true)
    TestStation toEntity(CreateTestStationRequest createRequest);

    /**
     * 从UpdateTestStationRequest DTO更新一个已存在的TestStation实体。
     * 在更新时，忽略ID字段的映射（ID不可修改）。
     *
     * @param updateRequest 包含更新数据的DTO
     * @param testStation   要被更新的目标TestStation实体
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntityFromRequest(UpdateTestStationRequest updateRequest, @MappingTarget TestStation testStation);

    /**
     * 将TestStationParameterRequest转换为TestStationParameter。
     *
     * @param request 请求对象
     * @return 实体对象
     */
    TestStationParameter toParameterEntity(TestStationParameterRequest request);

    /**
     * 将TestStationParameter转换为TestStationParameterResponse。
     *
     * @param parameter 实体对象
     * @return 响应对象
     */
    TestStationParameterResponse toParameterResponse(TestStationParameter parameter);

    /**
     * 批量转换参数列表。
     *
     * @param requests 请求对象列表
     * @return 实体对象列表
     */
    List<TestStationParameter> toParameterEntityList(List<TestStationParameterRequest> requests);

    /**
     * 批量转换参数响应列表。
     *
     * @param parameters 实体对象列表
     * @return 响应对象列表
     */
    List<TestStationParameterResponse> toParameterResponseList(List<TestStationParameter> parameters);
}
