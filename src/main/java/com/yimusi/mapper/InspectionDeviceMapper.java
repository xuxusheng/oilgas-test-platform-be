package com.yimusi.mapper;

import com.yimusi.dto.inspection.CreateInspectionDeviceRequest;
import com.yimusi.dto.inspection.InspectionDeviceResponse;
import com.yimusi.dto.inspection.UpdateInspectionDeviceRequest;
import com.yimusi.entity.InspectionDevice;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.lang.NonNull;

/**
 * MapStruct映射器接口，用于在InspectionDevice实体和DTO之间进行转换。
 */
@Mapper(componentModel = "spring")
public interface InspectionDeviceMapper {

    /**
     * 将InspectionDevice实体转换为InspectionDeviceResponse DTO。
     *
     * @param inspectionDevice 要转换的InspectionDevice实体
     * @return 转换后的InspectionDeviceResponse DTO
     */
    InspectionDeviceResponse toResponse(InspectionDevice inspectionDevice);

    /**
     * 将CreateInspectionDeviceRequest DTO转换为InspectionDevice实体。
     * 在创建新实体时，忽略ID和deviceNo字段（设备编号由系统生成）。
     *
     * @param createRequest 包含新检测设备数据的DTO
     * @return 转换后的InspectionDevice实体
     */
    @NonNull
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deviceNo", ignore = true)
    @Mapping(target = "projectInternalNo", ignore = true)
    InspectionDevice toEntity(CreateInspectionDeviceRequest createRequest);

    /**
     * 从UpdateInspectionDeviceRequest DTO更新一个已存在的InspectionDevice实体。
     * 在更新时，忽略ID和deviceNo字段的映射（设备编号不可修改）。
     *
     * @param updateRequest 包含更新数据的DTO
     * @param device        要被更新的目标InspectionDevice实体
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deviceNo", ignore = true)
    @Mapping(target = "projectInternalNo", ignore = true)
    void updateEntityFromRequest(UpdateInspectionDeviceRequest updateRequest, @MappingTarget InspectionDevice device);
}
