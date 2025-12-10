package com.yimusi.controller;

import com.yimusi.common.model.ApiResponse;
import com.yimusi.dto.common.PageResult;
import com.yimusi.dto.inspection.CreateInspectionDeviceRequest;
import com.yimusi.dto.inspection.InspectionDevicePageRequest;
import com.yimusi.dto.inspection.InspectionDeviceResponse;
import com.yimusi.dto.inspection.UpdateInspectionDeviceRequest;
import com.yimusi.mapper.InspectionDeviceMapper;
import com.yimusi.service.InspectionDeviceService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 待检设备管理接口
 * 提供检测设备的增删改查及分页查询功能
 */
@RestController
@RequestMapping("/api/inspection-devices")
@RequiredArgsConstructor
public class InspectionDeviceController {

    private final InspectionDeviceService deviceService;
    private final InspectionDeviceMapper deviceMapper;

    /**
     * 获取所有检测设备并映射为响应 DTO。
     *
     * @return {@link InspectionDeviceResponse} 列表
     */
    @GetMapping
    public ApiResponse<List<InspectionDeviceResponse>> getAllDevices() {
        List<InspectionDeviceResponse> responses = deviceService
            .getAllDevices()
            .stream()
            .map(deviceMapper::toResponse)
            .toList();
        return ApiResponse.success(responses);
    }

    /**
     * 分页查询检测设备列表，支持筛选。
     *
     * @param request 分页查询请求参数
     * @return 分页结果，包含检测设备列表及分页信息
     */
    @GetMapping("/page")
    public ApiResponse<PageResult<InspectionDeviceResponse>> getDevicesPage(
        @Valid InspectionDevicePageRequest request
    ) {
        PageResult<InspectionDeviceResponse> pageResult = deviceService.getDevicesPage(request);
        return ApiResponse.success(pageResult);
    }

    /**
     * 根据 ID 查询单个检测设备。
     *
     * @param id 设备 ID
     * @return 查询到的 {@link InspectionDeviceResponse}
     */
    @GetMapping("/{id}")
    public ApiResponse<InspectionDeviceResponse> getDeviceById(@PathVariable Long id) {
        InspectionDeviceResponse response = deviceMapper.toResponse(deviceService.getDeviceById(id));
        return ApiResponse.success(response);
    }

    /**
     * 根据设备编号查询单个检测设备。
     *
     * @param deviceNo 设备编号
     * @return 查询到的 {@link InspectionDeviceResponse}
     */
    @GetMapping("/by-device-no/{deviceNo}")
    public ApiResponse<InspectionDeviceResponse> getDeviceByNo(@PathVariable String deviceNo) {
        InspectionDeviceResponse response = deviceMapper.toResponse(deviceService.getDeviceByNo(deviceNo));
        return ApiResponse.success(response);
    }

    /**
     * 根据请求体创建新检测设备。
     *
     * @param createRequest 包含设备信息的请求体
     * @return 新增的 {@link InspectionDeviceResponse}
     */
    @PostMapping
    public ApiResponse<InspectionDeviceResponse> createDevice(
        @Valid @RequestBody CreateInspectionDeviceRequest createRequest
    ) {
        InspectionDeviceResponse deviceResponse = deviceService.createDevice(createRequest);
        return ApiResponse.success(deviceResponse);
    }

    /**
     * 使用提交的数据更新已有检测设备。
     *
     * @param id            需要更新的设备 ID
     * @param updateRequest 更新字段的请求体
     * @return 更新后的 {@link InspectionDeviceResponse}
     */
    @PutMapping("/{id}")
    public ApiResponse<InspectionDeviceResponse> updateDevice(
        @PathVariable Long id,
        @Valid @RequestBody UpdateInspectionDeviceRequest updateRequest
    ) {
        InspectionDeviceResponse updated = deviceService.updateDevice(id, updateRequest);
        return ApiResponse.success(updated);
    }

    /**
     * 根据 ID 删除检测设备，成功后返回 200。
     *
     * @param id 待删除的设备 ID
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDevice(@PathVariable Long id) {
        deviceService.deleteDevice(id);
        return ApiResponse.success();
    }

    /**
     * 恢复已软删除的检测设备。
     *
     * @param id 设备 ID
     */
    @PostMapping("/{id}/restore")
    public ApiResponse<Void> restoreDevice(@PathVariable Long id) {
        deviceService.restoreDevice(id);
        return ApiResponse.success();
    }

    /**
     * 验证出厂编号的唯一性。
     *
     * @param serialNumber 出厂编号
     * @return 是否唯一
     */
    @GetMapping("/validate-serial-number/{serialNumber}")
    public ApiResponse<Boolean> validateSerialNumberUnique(@PathVariable String serialNumber) {
        boolean isUnique = deviceService.validateSerialNumberUnique(serialNumber);
        return ApiResponse.success(isUnique);
    }

    /**
     * 验证 IP 地址的唯一性。
     *
     * @param ip IP 地址
     * @return 是否唯一
     */
    @GetMapping("/validate-ip/{ip}")
    public ApiResponse<Boolean> validateIpUnique(@PathVariable String ip) {
        boolean isUnique = deviceService.validateIpUnique(ip);
        return ApiResponse.success(isUnique);
    }
}
