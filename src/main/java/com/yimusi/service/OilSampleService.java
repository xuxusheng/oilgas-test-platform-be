package com.yimusi.service;

import com.yimusi.dto.common.PageResult;
import com.yimusi.dto.oilsample.CreateOilSampleRequest;
import com.yimusi.dto.oilsample.OilSamplePageRequest;
import com.yimusi.dto.oilsample.OilSampleResponse;
import com.yimusi.dto.oilsample.UpdateOilSampleRequest;

/**
 * 油样管理服务接口
 */
public interface OilSampleService {

    /**
     * 分页查询油样列表
     *
     * @param request 分页查询请求参数
     * @return 分页结果
     */
    PageResult<OilSampleResponse> getOilSamplesPage(OilSamplePageRequest request);

    /**
     * 获取单条油样详情
     *
     * @param id 油样 ID
     * @return 油样详情
     */
    OilSampleResponse getOilSampleById(Long id);

    /**
     * 创建油样
     *
     * @param request 创建请求
     * @return 创建后的油样详情
     */
    OilSampleResponse createOilSample(CreateOilSampleRequest request);

    /**
     * 更新油样
     *
     * @param id      油样 ID
     * @param request 更新请求
     * @return 更新后的油样详情
     */
    OilSampleResponse updateOilSample(Long id, UpdateOilSampleRequest request);

    /**
     * 删除单条油样（软删除）
     *
     * @param id 油样 ID
     */
    void deleteOilSample(Long id);

    /**
     * 校验编号唯一性接口
     *
     * @param sampleNo 油样编号
     * @return true 如果不存在（可用），false 如果已存在（不可用）
     */
    boolean validateSampleNoUnique(String sampleNo);
}
