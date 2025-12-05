package com.yimusi.service.impl;

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
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
        Specification<OilSample> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(request.getSampleNo())) {
                predicates.add(cb.like(root.get("sampleNo"), "%" + request.getSampleNo() + "%"));
            }
            if (StringUtils.hasText(request.getSampleName())) {
                predicates.add(cb.like(root.get("sampleName"), "%" + request.getSampleName() + "%"));
            }
            if (request.getUsage() != null) {
                predicates.add(cb.equal(root.get("usage"), request.getUsage()));
            }
            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }
            if (request.getCylinderNo() != null) {
                predicates.add(cb.equal(root.get("cylinderNo"), request.getCylinderNo()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<OilSample> page = oilSampleRepository.findAll(spec, request.toJpaPageRequest("createdAt"));

        // 使用全局方法封装返回结果
        return PageResult.from(page.map(oilSampleMapper::toResponse));
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
     * 批量删除油样（软删除）
     *
     * @param ids 油样 ID 列表
     */
    @Override
    @Transactional
    public void batchDeleteOilSamples(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        // 批量删除，Hibernate 会处理 @SQLDelete
        oilSampleRepository.deleteAllById(ids);
    }

    /**
     * 校验编号唯一性接口
     *
     * @param sampleNo 油样编号
     * @return true 如果不存在（可用），false 如果已存在（不可用）
     */
    @Override
    @Transactional(readOnly = true)
    public boolean validateSampleNoUnique(String sampleNo) {
        return !oilSampleRepository.existsBySampleNo(sampleNo);
    }
}
