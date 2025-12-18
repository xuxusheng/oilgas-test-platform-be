package com.yimusi.controller;

import com.yimusi.common.model.ApiResponse;
import com.yimusi.dto.common.PageResult;
import com.yimusi.dto.teststation.CreateTestStationRequest;
import com.yimusi.dto.teststation.TestStationPageRequest;
import com.yimusi.dto.teststation.TestStationResponse;
import com.yimusi.dto.teststation.UpdateTestStationRequest;
import com.yimusi.mapper.TestStationMapper;
import com.yimusi.service.TestStationService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 测试工位管理
 * 提供测试工位的增删改查及分页查询功能
 */
@RestController
@RequestMapping("/api/test-stations")
@RequiredArgsConstructor
public class TestStationController {

    private final TestStationService testStationService;
    private final TestStationMapper testStationMapper;

    /**
     * 获取所有测试工位列表
     *
     * @return {@link TestStationResponse} 列表
     */
    @GetMapping
    public ApiResponse<List<TestStationResponse>> getAllStations() {
        List<TestStationResponse> responses = testStationService
            .getAllStations()
            .stream()
            .map(testStationMapper::toResponse)
            .toList();
        return ApiResponse.success(responses);
    }

    /**
     * 分页查询测试工位列表
     *
     * @param request 分页查询请求参数
     * @return 分页结果，包含测试工位列表及分页信息
     */
    @GetMapping("/page")
    public ApiResponse<PageResult<TestStationResponse>> getStationsPage(
        @Valid TestStationPageRequest request
    ) {
        PageResult<TestStationResponse> pageResult = testStationService.getStationsPage(request);
        return ApiResponse.success(pageResult);
    }

    /**
     * 根据ID查询测试工位详情
     *
     * @param id 工位 ID
     * @return 查询到的 {@link TestStationResponse}
     */
    @GetMapping("/{id}")
    public ApiResponse<TestStationResponse> getStationById(@PathVariable Long id) {
        TestStationResponse response = testStationMapper.toResponse(testStationService.getStationById(id));
        return ApiResponse.success(response);
    }

    /**
     * 根据工位编号查询测试工位
     *
     * @param stationNo 工位编号
     * @return 查询到的 {@link TestStationResponse}
     */
    @GetMapping("/by-station-no/{stationNo}")
    public ApiResponse<TestStationResponse> getStationByStationNo(@PathVariable Integer stationNo) {
        TestStationResponse response = testStationMapper.toResponse(testStationService.getStationByStationNo(stationNo));
        return ApiResponse.success(response);
    }

    /**
     * 创建测试工位
     *
     * @param createRequest 包含工位信息的请求体
     * @return 新增的 {@link TestStationResponse}
     */
    @PostMapping
    public ApiResponse<TestStationResponse> createStation(
        @Valid @RequestBody CreateTestStationRequest createRequest
    ) {
        TestStationResponse stationResponse = testStationService.createStation(createRequest);
        return ApiResponse.success(stationResponse);
    }

    /**
     * 更新测试工位信息
     *
     * @param id            需要更新的工位 ID
     * @param updateRequest 更新字段的请求体
     * @return 更新后的 {@link TestStationResponse}
     */
    @PutMapping("/{id}")
    public ApiResponse<TestStationResponse> updateStation(
        @PathVariable Long id,
        @Valid @RequestBody UpdateTestStationRequest updateRequest
    ) {
        TestStationResponse updated = testStationService.updateStation(id, updateRequest);
        return ApiResponse.success(updated);
    }

    /**
     * 删除测试工位
     *
     * @param id 待删除的工位 ID
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteStation(@PathVariable Long id) {
        testStationService.deleteStation(id);
        return ApiResponse.success();
    }

    /**
     * 验证工位编号唯一性
     *
     * @param stationNo 工位编号
     * @return 是否唯一
     */
    @GetMapping("/validate-station-no/{stationNo}")
    public ApiResponse<Boolean> validateStationNoUnique(@PathVariable Integer stationNo) {
        boolean isUnique = testStationService.validateStationNoUnique(stationNo);
        return ApiResponse.success(isUnique);
    }
}
