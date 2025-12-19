package com.yimusi.controller;

import com.yimusi.common.model.ApiResponse;
import com.yimusi.dto.common.PageResult;
import com.yimusi.dto.oilsample.CreateOilSampleRequest;
import com.yimusi.dto.oilsample.OilSamplePageRequest;
import com.yimusi.dto.oilsample.OilSampleResponse;
import com.yimusi.dto.oilsample.UpdateOilSampleRequest;
import com.yimusi.service.OilSampleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 油样管理接口
 * 提供油样的增删改查及分页查询功能
 */
@RestController
@RequestMapping("/api/oil-samples")
@RequiredArgsConstructor
public class OilSampleController {

    private final OilSampleService oilSampleService;

    /**
     * 分页查询油样列表
     *
     * @param request 分页查询请求参数
     * @return 分页结果，包含油样列表及分页信息
     */
    @GetMapping("/page")
    public ApiResponse<PageResult<OilSampleResponse>> getOilSamplesPage(@Valid OilSamplePageRequest request) {
        PageResult<OilSampleResponse> pageResult = oilSampleService.getOilSamplesPage(request);
        return ApiResponse.success(pageResult);
    }

    /**
     * 获取单条油样详情
     *
     * @param id 油样 ID
     * @return 查询到的 {@link OilSampleResponse}
     */
    @GetMapping("/{id}")
    public ApiResponse<OilSampleResponse> getOilSampleById(@PathVariable Long id) {
        OilSampleResponse response = oilSampleService.getOilSampleById(id);
        return ApiResponse.success(response);
    }

    /**
     * 创建油样
     *
     * @param createRequest 创建请求
     * @return 新增的 {@link OilSampleResponse}
     */
    @PostMapping
    public ApiResponse<OilSampleResponse> createOilSample(@Valid @RequestBody CreateOilSampleRequest createRequest) {
        OilSampleResponse created = oilSampleService.createOilSample(createRequest);
        return ApiResponse.success(created);
    }

    /**
     * 更新油样
     *
     * @param id            油样 ID
     * @param updateRequest 更新请求
     * @return 更新后的 {@link OilSampleResponse}
     */
    @PutMapping("/{id}")
    public ApiResponse<OilSampleResponse> updateOilSample(
        @PathVariable Long id,
        @Valid @RequestBody UpdateOilSampleRequest updateRequest
    ) {
        OilSampleResponse updated = oilSampleService.updateOilSample(id, updateRequest);
        return ApiResponse.success(updated);
    }

    /**
     * 删除油样（软删除）
     *
     * @param id 油样 ID
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteOilSample(@PathVariable Long id) {
        oilSampleService.deleteOilSample(id);
        return ApiResponse.success();
    }

    /**
     * 判断油样编号是否唯一
     *
     * @param sampleNo 油样编号
     * @return true 如果唯一，false 如果已存在
     */
    @GetMapping("/validate-unique/{sampleNo}")
    public ApiResponse<Boolean> validateSampleNoUnique(@PathVariable String sampleNo) {
        boolean isUnique = oilSampleService.isSampleNoUnique(sampleNo);
        return ApiResponse.success(isUnique);
    }

    /**
     * 启用油样
     *
     * @param id 油样 ID
     * @return 更新后的油样信息
     */
    @PatchMapping("/{id}/enable")
    public ApiResponse<OilSampleResponse> enableOilSample(@PathVariable Long id) {
        OilSampleResponse response = oilSampleService.setOilSampleEnabled(id, true);
        return ApiResponse.success(response);
    }

    /**
     * 禁用油样
     *
     * @param id 油样 ID
     * @return 更新后的油样信息
     */
    @PatchMapping("/{id}/disable")
    public ApiResponse<OilSampleResponse> disableOilSample(@PathVariable Long id) {
        OilSampleResponse response = oilSampleService.setOilSampleEnabled(id, false);
        return ApiResponse.success(response);
    }

    /**
     * 切换油样启用状态（启用↔禁用）
     *
     * @param id 油样 ID
     * @return 更新后的油样信息
     */
    @PatchMapping("/{id}/toggle")
    public ApiResponse<OilSampleResponse> toggleOilSample(@PathVariable Long id) {
        OilSampleResponse response = oilSampleService.toggleOilSampleEnabled(id);
        return ApiResponse.success(response);
    }
}

