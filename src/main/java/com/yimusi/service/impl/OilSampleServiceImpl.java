package com.yimusi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.yimusi.common.exception.BusinessException;
import com.yimusi.common.exception.ErrorCode;
import com.yimusi.common.exception.ResourceNotFoundException;
import com.yimusi.dto.common.PageResult;
import com.yimusi.dto.oilsample.CreateOilSampleRequest;
import com.yimusi.dto.oilsample.OilSamplePageRequest;
import com.yimusi.dto.oilsample.OilSampleResponse;
import com.yimusi.dto.oilsample.UpdateOilSampleRequest;
import com.yimusi.entity.OilSample;
import com.yimusi.mapper.OilSampleMapper;
import com.yimusi.repository.OilSampleRepository;
import com.yimusi.service.OilSampleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.yimusi.entity.QOilSample.oilSample;

/**
 * 油样管理服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OilSampleServiceImpl implements OilSampleService {

    private final OilSampleRepository oilSampleRepository;
    private final OilSampleMapper oilSampleMapper;

    /**
     * 分页查询油样列表
     *
     * @param request 分页查询请求参数
     * @return 分页结果
     */
    @Override
    @Transactional(readOnly = true)
    public PageResult<OilSampleResponse> getOilSamplesPage(OilSamplePageRequest request) {
        // 使用 QueryDSL 构建查询条件
        Predicate predicate = buildOilSamplePredicate(request);

        Page<OilSample> page = oilSampleRepository.findAll(
            predicate,
            request.toJpaPageRequest("createdAt")
        );

        // 使用全局方法封装返回结果
        return PageResult.from(page.map(oilSampleMapper::toResponse));
    }

    /**
     * 使用 QueryDSL 构建油样查询条件
     *
     * @param request 分页查询请求对象
     * @return Predicate 查询条件
     */
    private Predicate buildOilSamplePredicate(OilSamplePageRequest request) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(oilSample.deleted.isFalse());

        // 油样编号模糊查询
        if (StrUtil.isNotBlank(request.getSampleNo())) {
            builder.and(oilSample.sampleNo.containsIgnoreCase(request.getSampleNo()));
        }

        // 油样名称模糊查询
        if (StrUtil.isNotBlank(request.getSampleName())) {
            builder.and(oilSample.sampleName.containsIgnoreCase(request.getSampleName()));
        }

        // 用途精确查询
        if (request.getUsage() != null) {
            builder.and(oilSample.usage.eq(request.getUsage()));
        }

        // 启用状态精确查询
        if (request.getEnabled() != null) {
            builder.and(oilSample.enabled.eq(request.getEnabled()));
        }

        // 油缸编号精确查询
        if (request.getCylinderNo() != null) {
            builder.and(oilSample.cylinderNo.eq(request.getCylinderNo()));
        }

        return builder;
    }

    /**
     * 获取单条油样详情
     *
     * @param id 油样 ID
     * @return 油样详情
     */
    @Override
    @Transactional(readOnly = true)
    public OilSampleResponse getOilSampleById(Long id) {
        OilSample oilSample = oilSampleRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("OilSample not found with id: " + id));
        return oilSampleMapper.toResponse(oilSample);
    }

    /**
     * 创建油样
     *
     * @param request 创建请求
     * @return 创建后的油样详情
     */
    @Override
    @Transactional
    public OilSampleResponse createOilSample(CreateOilSampleRequest request) {
        // 1. 校验编号唯一性
        if (oilSampleRepository.existsBySampleNo(request.getSampleNo())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "油样编号已存在: " + request.getSampleNo());
        }

        // 2. 转换并保存
        OilSample oilSample = oilSampleMapper.toEntity(request);
        oilSample = oilSampleRepository.save(oilSample);

        return oilSampleMapper.toResponse(oilSample);
    }

    /**
     * 更新油样
     *
     * @param id      油样 ID
     * @param request 更新请求
     * @return 更新后的油样详情
     */
    @Override
    @Transactional
    public OilSampleResponse updateOilSample(Long id, UpdateOilSampleRequest request) {
        OilSample oilSample = oilSampleRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("OilSample not found with id: " + id));

        // 1. 校验编号唯一性（排除自身）
        if (oilSampleRepository.existsBySampleNoAndIdNot(request.getSampleNo(), id)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "油样编号已存在: " + request.getSampleNo());
        }

        // 2. 更新实体
        oilSampleMapper.updateEntityFromRequest(request, oilSample);
        oilSample = oilSampleRepository.save(oilSample);

        return oilSampleMapper.toResponse(oilSample);
    }

    /**
     * 删除单条油样（软删除）
     *
     * @param id 油样 ID
     */
    @Override
    @Transactional
    public void deleteOilSample(Long id) {
        if (!oilSampleRepository.existsById(id)) {
            throw new ResourceNotFoundException("OilSample not found with id: " + id);
        }
        oilSampleRepository.deleteById(id);
    }

    /**
     * 判断油样编号是否唯一
     *
     * @param sampleNo 油样编号
     * @return true 如果唯一（不存在），false 如果已存在
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isSampleNoUnique(String sampleNo) {
        if (sampleNo == null) {
            return true;
        }
        return !oilSampleRepository.existsBySampleNo(sampleNo);
    }

    /**
     * 设置油样启用状态
     *
     * @param id 油样 ID
     * @param enabled true=启用, false=禁用
     * @return 更新后的油样响应
     */
    @Override
    @Transactional
    public OilSampleResponse setOilSampleEnabled(Long id, boolean enabled) {
        if (id == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "油样 ID 不能为空");
        }

        OilSample oilSample = oilSampleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("OilSample not found with id: " + id));

        oilSample.setEnabled(enabled);
        oilSample = oilSampleRepository.save(oilSample);

        log.info("油样状态变更: ID={}, 油样编号={}, 新状态={}",
            id, oilSample.getSampleNo(), enabled ? "启用" : "禁用");

        return oilSampleMapper.toResponse(oilSample);
    }

    /**
     * 切换油样启用状态（启用↔禁用）
     *
     * @param id 油样 ID
     * @return 更新后的油样响应
     */
    @Override
    @Transactional
    public OilSampleResponse toggleOilSampleEnabled(Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "油样 ID 不能为空");
        }

        OilSample oilSample = oilSampleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("OilSample not found with id: " + id));

        // 切换状态
        boolean newEnabled = !oilSample.getEnabled();
        oilSample.setEnabled(newEnabled);
        oilSample = oilSampleRepository.save(oilSample);

        log.info("油样状态切换: ID={}, 油样编号={}, 新状态={}",
            id, oilSample.getSampleNo(), newEnabled ? "启用" : "禁用");

        return oilSampleMapper.toResponse(oilSample);
    }
}
