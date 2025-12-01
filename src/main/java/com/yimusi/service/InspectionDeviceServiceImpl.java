package com.yimusi.service;

import cn.hutool.core.util.StrUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.yimusi.common.exception.BadRequestException;
import com.yimusi.common.exception.ResourceNotFoundException;
import com.yimusi.common.util.OperatorUtil;
import com.yimusi.dto.inspection.CreateInspectionDeviceRequest;
import com.yimusi.dto.inspection.InspectionDevicePageRequest;
import com.yimusi.dto.inspection.InspectionDeviceResponse;
import com.yimusi.dto.common.PageResult;
import com.yimusi.dto.inspection.UpdateInspectionDeviceRequest;
import com.yimusi.entity.InspectionDevice;
import com.yimusi.entity.QInspectionDevice;
import com.yimusi.mapper.InspectionDeviceMapper;
import com.yimusi.repository.InspectionDeviceRepository;
import com.yimusi.service.InspectionDeviceService;
import com.yimusi.util.InspectionDeviceNoGenerator;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * 检测设备服务实现类，处理所有与检测设备相关的业务逻辑。
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class InspectionDeviceServiceImpl implements InspectionDeviceService {

    private final InspectionDeviceRepository deviceRepository;
    private final InspectionDeviceMapper deviceMapper;
    private final InspectionDeviceNoGenerator deviceNoGenerator;

    /**
     * 初始化设备编号生成器，从数据库加载最大编号
     */
    @PostConstruct
    public void initDeviceNoGenerator() {
        deviceRepository.findMaxDeviceNo().ifPresent(maxNo -> {
            deviceNoGenerator.resetSequence(maxNo);
            log.info("Initialized device number generator with max device number: {}", maxNo);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InspectionDeviceResponse createDevice(CreateInspectionDeviceRequest createRequest) {
        // 验证唯一性约束
        validateSerialNumberUnique(createRequest.getSerialNumber());
        validateIpUnique(createRequest.getIp());

        // 转换为实体
        InspectionDevice device = deviceMapper.toEntity(createRequest);

        // 生成设备编号
        device.setDeviceNo(deviceNoGenerator.generateDeviceNo());

        // 如果指定了项目ID，则生成项目内部序号
        if (device.getProjectId() != null) {
            Integer maxInternalNo = deviceRepository
                .findMaxProjectInternalNoByProjectId(device.getProjectId())
                .orElse(0);
            device.setProjectInternalNo(maxInternalNo + 1);
        }

        // 保存实体
        InspectionDevice savedDevice = deviceRepository.save(device);
        log.info("Created inspection device: {}", savedDevice.getDeviceNo());

        return deviceMapper.toResponse(savedDevice);
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public InspectionDevice getDeviceById(Long id) {
        if (id == null) {
            throw new BadRequestException("设备ID不能为空");
        }

        return deviceRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(String.format("ID 为 %s 的设备不存在", id)));
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public InspectionDevice getDeviceByNo(String deviceNo) {
        if (deviceNo == null) {
            throw new BadRequestException("设备编号不能为空");
        }
        return deviceRepository
            .findByDeviceNoAndDeletedFalse(deviceNo)
            .orElseThrow(() -> new ResourceNotFoundException(String.format("设备编号 %s 不存在", deviceNo)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<InspectionDevice> getAllDevices() {
        return deviceRepository.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageResult<InspectionDeviceResponse> getDevicesPage(InspectionDevicePageRequest request) {
        // 构建 QueryDSL 查询条件
        Predicate predicate = buildDevicePredicate(request);

        // 执行分页查询
        Page<InspectionDevice> devicePage = deviceRepository.findAll(predicate, request.toJpaPageRequest());

        // 转换并返回结果
        return PageResult.from(devicePage.map(deviceMapper::toResponse));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InspectionDeviceResponse updateDevice(Long id, UpdateInspectionDeviceRequest updateRequest) {
        if (id == null) {
            throw new BadRequestException("设备ID不能为空");
        }

        InspectionDevice device = getDeviceById(id);

        // 如果更新了出厂编号，需要验证唯一性
        if (StrUtil.isNotBlank(updateRequest.getSerialNumber()) &&
            !updateRequest.getSerialNumber().equals(device.getSerialNumber())) {
            validateSerialNumberUnique(updateRequest.getSerialNumber());
        }

        // 如果更新了IP，需要验证唯一性
        if (StrUtil.isNotBlank(updateRequest.getIp()) &&
            !updateRequest.getIp().equals(device.getIp())) {
            validateIpUnique(updateRequest.getIp());
        }

        // 如果更新了项目ID，需要重新生成项目内部序号
        if (updateRequest.getProjectId() != null &&
            !updateRequest.getProjectId().equals(device.getProjectId())) {
            Integer maxInternalNo = deviceRepository
                .findMaxProjectInternalNoByProjectId(updateRequest.getProjectId())
                .orElse(0);
            device.setProjectInternalNo(maxInternalNo + 1);
        }

        // 更新实体
        deviceMapper.updateEntityFromRequest(updateRequest, device);

        InspectionDevice savedDevice = deviceRepository.save(device);
        log.info("Updated inspection device: {}", savedDevice.getDeviceNo());

        return deviceMapper.toResponse(savedDevice);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteDevice(Long id) {
        if (id == null) {
            throw new BadRequestException("设备ID不能为空");
        }

        InspectionDevice device = getDeviceById(id);
        markDeleted(device);
        deviceRepository.save(device);
        log.info("Deleted inspection device: {}", device.getDeviceNo());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void restoreDevice(Long id) {
        if (id == null) {
            throw new BadRequestException("设备ID不能为空");
        }

        InspectionDevice device = deviceRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(String.format("ID 为 %s 的设备不存在", id)));
        device.setDeleted(false);
        device.setDeletedAt(null);
        device.setDeletedBy(null);
        deviceRepository.save(device);
        log.info("Restored inspection device: {}", device.getDeviceNo());
    }

    /**
     * 标记设备为已删除
     *
     * @param device 设备实体
     */
    private void markDeleted(InspectionDevice device) {
        device.setDeleted(true);
        device.setDeletedAt(Instant.now());
        device.setDeletedBy(OperatorUtil.getOperator());
    }

    /**
     * 使用 QueryDSL 构建设备查询条件.
     *
     * @param request 分页查询请求
     * @return Predicate 查询条件
     */
    @NonNull
    private Predicate buildDevicePredicate(InspectionDevicePageRequest request) {
        QInspectionDevice qDevice = QInspectionDevice.inspectionDevice;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qDevice.deleted.isFalse());

        // 设备编号模糊查询
        if (StrUtil.isNotBlank(request.getDeviceNo())) {
            builder.and(qDevice.deviceNo.containsIgnoreCase(request.getDeviceNo()));
        }

        // 出厂编号模糊查询
        if (StrUtil.isNotBlank(request.getSerialNumber())) {
            builder.and(qDevice.serialNumber.containsIgnoreCase(request.getSerialNumber()));
        }

        // 装置型号模糊查询
        if (StrUtil.isNotBlank(request.getDeviceModel())) {
            builder.and(qDevice.deviceModel.containsIgnoreCase(request.getDeviceModel()));
        }

        // IP精确查询
        if (StrUtil.isNotBlank(request.getIp())) {
            builder.and(qDevice.ip.eq(request.getIp()));
        }

        // 项目ID精确查询
        if (request.getProjectId() != null) {
            builder.and(qDevice.projectId.eq(request.getProjectId()));
        }

        // 状态精确查询
        if (request.getStatus() != null) {
            builder.and(qDevice.status.eq(request.getStatus()));
        }

        return builder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validateSerialNumberUnique(String serialNumber) {
        if (serialNumber == null) {
            return true;
        }
        boolean exists = deviceRepository.existsBySerialNumberAndDeletedFalse(serialNumber);
        if (exists) {
            throw new BadRequestException(String.format("出厂编号 %s 已存在", serialNumber));
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validateIpUnique(String ip) {
        if (ip == null) {
            return true;
        }
        boolean exists = deviceRepository.existsByIpAndDeletedFalse(ip);
        if (exists) {
            throw new BadRequestException(String.format("IP地址 %s 已存在", ip));
        }
        return true;
    }
}
