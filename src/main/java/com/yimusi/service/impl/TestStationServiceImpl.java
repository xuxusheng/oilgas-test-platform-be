package com.yimusi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.yimusi.common.exception.BadRequestException;
import com.yimusi.common.exception.ResourceNotFoundException;
import com.yimusi.dto.common.PageResult;
import com.yimusi.dto.teststation.CreateTestStationRequest;
import com.yimusi.dto.teststation.TestStationPageRequest;
import com.yimusi.dto.teststation.TestStationResponse;
import com.yimusi.dto.teststation.UpdateTestStationRequest;
import com.yimusi.dto.teststation.parameter.TestStationParameterRequest;
import com.yimusi.entity.TestStation;
import com.yimusi.entity.TestStationParameter;
import com.yimusi.mapper.TestStationMapper;
import com.yimusi.repository.TestStationRepository;
import com.yimusi.service.TestStationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.yimusi.entity.QTestStation.testStation;

/**
 * 测试工位服务实现类，处理所有与测试工位相关的业务逻辑。
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TestStationServiceImpl implements TestStationService {

    private final TestStationRepository stationRepository;
    private final TestStationMapper stationMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<TestStation> getAllStations() {
        return stationRepository.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PageResult<TestStationResponse> getStationsPage(TestStationPageRequest request) {
        // 构建 QueryDSL 查询条件
        Predicate predicate = buildStationPredicate(request);

        // 执行分页查询，按创建时间倒序
        PageRequest pageRequest = request.toJpaPageRequest("createdAt");
        Page<TestStation> stationPage = stationRepository.findAll(predicate, pageRequest);

        // 转换并返回结果
        return PageResult.from(stationPage.map(stationMapper::toResponse));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public TestStation getStationById(Long id) {
        if (id == null) {
            throw new BadRequestException("工位 ID 不能为空");
        }

        TestStation station = stationRepository.findById(id).orElse(null);
        if (station == null) {
            throw new ResourceNotFoundException(String.format("ID 为 %s 的工位不存在", id));
        }
        return station;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public TestStation getStationByStationNo(Integer stationNo) {
        if (stationNo == null) {
            throw new BadRequestException("工位编号不能为空");
        }
        TestStation station = stationRepository.findByStationNoAndDeletedFalse(stationNo).orElse(null);
        if (station == null) {
            throw new ResourceNotFoundException(String.format("工位编号 %s 不存在", stationNo));
        }
        return station;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestStationResponse createStation(CreateTestStationRequest createRequest) {
        // 验证唯一性约束 - 需要手动抛出异常
        if (!isStationNoUnique(createRequest.getStationNo())) {
            throw new BadRequestException(String.format("工位编号 %s 已存在", createRequest.getStationNo()));
        }

        // 转换为实体
        TestStation station = stationMapper.toEntity(createRequest);

        // 处理参数列表转换
        if (createRequest.getValveControlParams() != null) {
            station.setValveControlParams(mapToParameters(createRequest.getValveControlParams()));
        }
        if (createRequest.getOilValveMapping() != null) {
            station.setOilValveMapping(mapToParameters(createRequest.getOilValveMapping()));
        }

        // MapStruct 会处理 enabled 字段的映射，此处无需额外转换
        // 如果没有明确启用状态，默认为启用（由实体类默认值处理）
        if (createRequest.getEnabled() == null) {
            station.setEnabled(true);
        }

        TestStation savedStation = stationRepository.save(station);
        log.info("创建测试工位: 工位编号={}, 工位名称={}, 启用状态={}",
            savedStation.getStationNo(), savedStation.getStationName(), savedStation.getEnabled());

        return stationMapper.toResponse(savedStation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestStationResponse updateStation(Long id, UpdateTestStationRequest updateRequest) {
        if (id == null) {
            throw new BadRequestException("工位 ID 不能为空");
        }

        TestStation station = getStationById(id);

        // 如果更新了工位编号，需要验证唯一性
        if (updateRequest.getStationNo() != null && !updateRequest.getStationNo().equals(station.getStationNo())) {
            if (!isStationNoUnique(updateRequest.getStationNo())) {
                throw new BadRequestException(String.format("工位编号 %s 已存在", updateRequest.getStationNo()));
            }
        }

        // 更新实体
        stationMapper.updateEntityFromRequest(updateRequest, station);

        // 处理参数列表更新
        if (updateRequest.getValveControlParams() != null) {
            station.setValveControlParams(mapToParameters(updateRequest.getValveControlParams()));
        }
        if (updateRequest.getOilValveMapping() != null) {
            station.setOilValveMapping(mapToParameters(updateRequest.getOilValveMapping()));
        }

        TestStation savedStation = stationRepository.save(station);
        log.info("更新测试工位: 工位编号={}, 工位名称={}", savedStation.getStationNo(), savedStation.getStationName());

        return stationMapper.toResponse(savedStation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteStation(Long id) {
        if (id == null) {
            throw new BadRequestException("工位 ID 不能为空");
        }

        // 先查询验证存在性
        TestStation station = getStationById(id);

        // 直接调用 repository.deleteById()，由 @SQLDelete 自动处理软删除
        stationRepository.deleteById(id);

        log.info("删除测试工位: 工位编号={}, 工位名称={}", station.getStationNo(), station.getStationName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isStationNoUnique(Integer stationNo) {
        if (stationNo == null) {
            return true;
        }
        // 只返回布尔值，不抛出异常
        return !stationRepository.existsByStationNoAndDeletedFalse(stationNo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestStationResponse setStationEnabled(Long id, boolean enabled) {
        if (id == null) {
            throw new BadRequestException("工位 ID 不能为空");
        }

        TestStation station = getStationById(id);
        station.setEnabled(enabled);
        TestStation saved = stationRepository.save(station);

        log.info("工位状态变更: ID={}, 工位编号={}, 新状态={}",
            id, station.getStationNo(), enabled ? "启用" : "禁用");

        return stationMapper.toResponse(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestStationResponse toggleStationEnabled(Long id) {
        if (id == null) {
            throw new BadRequestException("工位 ID 不能为空");
        }

        TestStation station = getStationById(id);

        // 切换状态
        boolean newEnabled = !station.getEnabled();
        station.setEnabled(newEnabled);
        TestStation saved = stationRepository.save(station);

        log.info("工位状态切换: ID={}, 工位编号={}, 新状态={}",
            id, station.getStationNo(), newEnabled ? "启用" : "禁用");

        return stationMapper.toResponse(saved);
    }

    /**
     * 将参数请求列表转换为参数实体列表
     *
     * @param requests 参数请求列表
     * @return 参数实体列表
     */
    private List<TestStationParameter> mapToParameters(List<TestStationParameterRequest> requests) {
        if (requests == null) {
            return new ArrayList<>();
        }
        return requests.stream()
            .map(request -> {
                TestStationParameter param = new TestStationParameter();
                param.setKey(request.getKey());
                param.setValue(request.getValue());
                return param;
            })
            .collect(Collectors.toList());
    }

    /**
     * 使用 QueryDSL 构建测试工位查询条件，支持多字段模糊查询和精确查询
     * <p>
     * 构建包含工位编号、工位名称的模糊查询，以及用途、通信类型、责任人、状态的精确查询。
     * 默认只查询未删除的工位记录。
     * </p>
     *
     * @param request 分页查询请求对象
     * @return Predicate 查询条件
     */
    private Predicate buildStationPredicate(TestStationPageRequest request) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(testStation.deleted.isFalse());

        // 工位编号精确查询
        if (request.getStationNo() != null) {
            builder.and(testStation.stationNo.eq(request.getStationNo()));
        }

        // 工位名称模糊查询
        if (StrUtil.isNotBlank(request.getStationName())) {
            builder.and(testStation.stationName.containsIgnoreCase(request.getStationName()));
        }

        // 用途精确查询
        if (request.getUsage() != null) {
            builder.and(testStation.usage.eq(request.getUsage()));
        }

        // 电磁阀通信类型精确查询
        if (request.getValveCommType() != null) {
            builder.and(testStation.valveCommType.eq(request.getValveCommType()));
        }

        // 责任人模糊查询
        if (StrUtil.isNotBlank(request.getResponsiblePerson())) {
            builder.and(testStation.responsiblePerson.containsIgnoreCase(request.getResponsiblePerson()));
        }

        // 启用状态精确查询
        if (request.getEnabled() != null) {
            builder.and(testStation.enabled.eq(request.getEnabled()));
        }

        return builder;
    }
}
