package com.yimusi.service;

import com.yimusi.dto.common.PageResult;
import com.yimusi.dto.teststation.CreateTestStationRequest;
import com.yimusi.dto.teststation.TestStationPageRequest;
import com.yimusi.dto.teststation.TestStationResponse;
import com.yimusi.dto.teststation.UpdateTestStationRequest;
import com.yimusi.entity.TestStation;

import java.util.List;

/**
 * 测试工位服务接口，定义了测试工位相关的业务操作。
 */
public interface TestStationService {

    /**
     * 获取所有测试工位列表。
     *
     * @return 包含所有测试工位的列表
     */
    List<TestStation> getAllStations();

    /**
     * 分页查询测试工位列表，支持筛选。
     *
     * @param request 分页查询请求参数
     * @return 分页结果，包含测试工位列表及分页信息
     */
    PageResult<TestStationResponse> getStationsPage(TestStationPageRequest request);

    /**
     * 根据工位 ID 获取测试工位信息。
     *
     * @param id 要查找的工位 ID
     * @return 找到的工位实体
     */
    TestStation getStationById(Long id);

    /**
     * 根据工位编号获取测试工位信息。
     *
     * @param stationNo 工位编号
     * @return 找到的工位实体
     */
    TestStation getStationByStationNo(Integer stationNo);

    /**
     * 创建一个新的测试工位。
     *
     * @param createRequest 包含新工位信息的请求体
     * @return 创建成功后的工位信息响应体
     */
    TestStationResponse createStation(CreateTestStationRequest createRequest);

    /**
     * 更新指定 ID 的测试工位信息。
     *
     * @param id            要更新的工位 ID
     * @param updateRequest 包含要更新的工位信息的请求体
     * @return 更新成功后的工位信息响应体
     */
    TestStationResponse updateStation(Long id, UpdateTestStationRequest updateRequest);

    /**
     * 根据 ID 删除测试工位（软删除）。
     *
     * @param id 要删除的工位 ID
     */
    void deleteStation(Long id);

    /**
     * 验证工位编号的唯一性。
     *
     * @param stationNo 工位编号
     * @return true 如果不存在，false 如果已存在
     */
    boolean validateStationNoUnique(Integer stationNo);
}
