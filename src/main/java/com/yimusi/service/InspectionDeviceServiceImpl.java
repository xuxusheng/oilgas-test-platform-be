package com.yimusi.service;

import cn.hutool.core.util.StrUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.yimusi.common.exception.BadRequestException;
import com.yimusi.common.exception.ResourceNotFoundException;
import com.yimusi.common.util.OperatorUtil;
import com.yimusi.dto.common.PageResult;
import com.yimusi.dto.inspection.CreateInspectionDeviceRequest;
import com.yimusi.dto.inspection.InspectionDevicePageRequest;
import com.yimusi.dto.inspection.InspectionDeviceResponse;
import com.yimusi.dto.inspection.UpdateInspectionDeviceRequest;
import com.yimusi.entity.InspectionDevice;
import com.yimusi.entity.QInspectionDevice;
import com.yimusi.enums.SequenceBizType;
import com.yimusi.mapper.InspectionDeviceMapper;
import com.yimusi.repository.InspectionDeviceRepository;
import com.yimusi.repository.ProjectRepository;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final SequenceGeneratorService sequenceGeneratorService;
    private final ProjectRepository projectRepository;
    private final RedissonClient redissonClient;

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
        device.setDeviceNo(sequenceGeneratorService.nextId(SequenceBizType.INSPECTION_DEVICE));

        // 生成项目内部序号
        device.setProjectInternalNo(generateProjectInternalNo(device.getProjectId()));

        InspectionDevice savedDevice = deviceRepository.save(device);
        log.info("创建待检设备: {}", savedDevice.getDeviceNo());

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

        InspectionDevice device = deviceRepository.findById(id).orElse(null);
        if (device == null) {
            throw new ResourceNotFoundException(String.format("ID 为 %s 的设备不存在", id));
        }
        return device;
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
        InspectionDevice device = deviceRepository.findByDeviceNoAndDeletedFalse(deviceNo).orElse(null);
        if (device == null) {
            throw new ResourceNotFoundException(String.format("设备编号 %s 不存在", deviceNo));
        }
        return device;
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
        if (
            StrUtil.isNotBlank(updateRequest.getSerialNumber()) &&
            !updateRequest.getSerialNumber().equals(device.getSerialNumber())
        ) {
            validateSerialNumberUnique(updateRequest.getSerialNumber());
        }

        // 如果更新了 IP，需要验证唯一性
        if (StrUtil.isNotBlank(updateRequest.getIp()) && !updateRequest.getIp().equals(device.getIp())) {
            validateIpUnique(updateRequest.getIp());
        }

        // 如果更新了项目 ID，需要重新生成项目内部序号
        if (updateRequest.getProjectId() != null && !updateRequest.getProjectId().equals(device.getProjectId())) {
            device.setProjectInternalNo(generateProjectInternalNo(updateRequest.getProjectId()));
        }

        // 更新实体
        deviceMapper.updateEntityFromRequest(updateRequest, device);

        InspectionDevice savedDevice = deviceRepository.save(device);
        log.info("更新检测设备: {}", savedDevice.getDeviceNo());

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
        log.info("删除检测设备: {}", device.getDeviceNo());
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
        log.info("恢复检测设备: {}", device.getDeviceNo());
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

    private Integer generateProjectInternalNo(Long projectId) {
        if (projectId == null) {
            throw new BadRequestException("projectId cannot be null");
        }

        // 先进行非锁依赖的验证，避免不必要地持有锁
        if (!projectRepository.existsByIdAndDeletedFalse(projectId)) {
            throw new BadRequestException(String.format("ID 为 %s 的项目不存在或已删除", projectId));
        }

        String lockName = "inspection-device:project-internal-no:" + projectId;
        RLock lock = redissonClient.getLock(lockName);

        try {
            // 尝试获取锁：等待5秒，锁自动释放时间为30秒
            // 使用 tryLock 可以避免在分布式环境下线程永久阻塞
            if (!lock.tryLock(5, 30, TimeUnit.SECONDS)) {
                throw new BadRequestException("系统繁忙，获取项目锁定失败，请稍后重试");
            }

            int maxInternalNo = deviceRepository
                .findMaxProjectInternalNoIncludingDeletedByProjectId(projectId)
                .orElse(0);

            return maxInternalNo + 1;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BadRequestException("获取项目锁定被中断，请稍后重试");
        } finally {
            // 仅在当前线程持有锁的情况下才释放锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
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

        // 项目 ID 精确查询
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
            throw new BadRequestException(String.format("IP 地址 %s 已存在", ip));
        }
        return true;
    }
}
