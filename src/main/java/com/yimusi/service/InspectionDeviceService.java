package com.yimusi.service;

import com.yimusi.dto.inspection.CreateInspectionDeviceRequest;
import com.yimusi.dto.inspection.InspectionDevicePageRequest;
import com.yimusi.dto.inspection.InspectionDeviceResponse;
import com.yimusi.dto.common.PageResult;
import com.yimusi.dto.inspection.UpdateInspectionDeviceRequest;
import com.yimusi.entity.InspectionDevice;

import java.util.List;

/**
 * 检测设备服务接口，定义了检测设备相关的业务操作。
 */
public interface InspectionDeviceService {

    /**
     * 获取所有检测设备列表。
     *
     * @return 包含所有检测设备的列表
     */
    List<InspectionDevice> getAllDevices();

    /**
     * 分页查询检测设备列表，支持筛选。
     *
     * @param request 分页查询请求参数
     * @return 分页结果，包含检测设备列表及分页信息
     */
    PageResult<InspectionDeviceResponse> getDevicesPage(InspectionDevicePageRequest request);

    /**
     * 根据设备 ID 获取检测设备信息。
     *
     * @param id 要查找的设备 ID
     * @return 找到的设备实体
     */
    InspectionDevice getDeviceById(Long id);

    /**
     * 根据设备编号获取检测设备信息。
     *
     * @param deviceNo 设备编号
     * @return 找到的设备实体
     */
    InspectionDevice getDeviceByNo(String deviceNo);

    /**
     * 创建一个新的检测设备。
     *
     * @param createRequest 包含新设备信息的请求体
     * @return 创建成功后的设备信息响应体
     */
    InspectionDeviceResponse createDevice(CreateInspectionDeviceRequest createRequest);

    /**
     * 更新指定 ID 的检测设备信息。
     *
     * @param id            要更新的设备 ID
     * @param updateRequest 包含要更新的设备信息的请求体
     * @return 更新成功后的设备信息响应体
     */
    InspectionDeviceResponse updateDevice(Long id, UpdateInspectionDeviceRequest updateRequest);

    /**
     * 根据 ID 删除检测设备（软删除）。
     *
     * @param id 要删除的设备 ID
     */
    void deleteDevice(Long id);

    /**
     * 恢复已软删除的检测设备。
     *
     * @param id 设备ID
     */
    void restoreDevice(Long id);

    /**
     * 验证出厂编号的唯一性。
     *
     * @param serialNumber 出厂编号
     * @return true 如果不存在，false 如果已存在
     */
    boolean validateSerialNumberUnique(String serialNumber);

    /**
     * 验证 IP 地址的唯一性。
     *
     * @param ip IP 地址
     * @return true 如果不存在，false 如果已存在
     */
    boolean validateIpUnique(String ip);
}
