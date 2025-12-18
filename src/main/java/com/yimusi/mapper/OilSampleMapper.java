package com.yimusi.mapper;

import com.yimusi.dto.oilsample.CreateOilSampleRequest;
import com.yimusi.dto.oilsample.OilSampleResponse;
import com.yimusi.dto.oilsample.UpdateOilSampleRequest;
import com.yimusi.entity.OilSample;
import com.yimusi.entity.OilSampleParameter;
import java.util.ArrayList;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * 油样 Mapper
 */
@Mapper(componentModel = "spring", config = BaseMapperConfig.class)
public interface OilSampleMapper {

    /**
     * Entity 转 Response
     */
    OilSampleResponse toResponse(OilSample oilSample);

    /**
     * CreateRequest 转 Entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    OilSample toEntity(CreateOilSampleRequest request);

    /**
     * UpdateRequest 更新 Entity
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntityFromRequest(UpdateOilSampleRequest request, @MappingTarget OilSample oilSample);

    @AfterMapping
    default void ensureNonNullCollections(@MappingTarget OilSample oilSample) {
        if (oilSample.getParameters() == null) {
            oilSample.setParameters(new ArrayList<>());
        }
    }

    /**
     * ParameterItem 转 OilSampleParameter
     * MapStruct 会自动调用此方法进行 List 元素的转换
     */
    default OilSampleParameter map(CreateOilSampleRequest.ParameterItem item) {
        if (item == null) {
            return null;
        }
        return new OilSampleParameter(item.getKey(), item.getValue());
    }
}
