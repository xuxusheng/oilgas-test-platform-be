package com.yimusi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.querydsl.core.types.Predicate;
import com.yimusi.common.exception.BadRequestException;
import com.yimusi.common.exception.ResourceNotFoundException;
import com.yimusi.dto.common.PageResult;
import com.yimusi.dto.inspection.CreateInspectionDeviceRequest;
import com.yimusi.dto.inspection.InspectionDevicePageRequest;
import com.yimusi.dto.inspection.InspectionDeviceResponse;
import com.yimusi.dto.inspection.UpdateInspectionDeviceRequest;
import com.yimusi.entity.InspectionDevice;
import com.yimusi.enums.InspectionDeviceStatus;
import com.yimusi.enums.SequenceBizType;
import com.yimusi.mapper.InspectionDeviceMapper;
import com.yimusi.repository.InspectionDeviceRepository;
import com.yimusi.repository.ProjectRepository;
import com.yimusi.service.impl.InspectionDeviceServiceImpl;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class InspectionDeviceServiceImplTest {

    @Mock
    private InspectionDeviceRepository deviceRepository;

    @Mock
    private SequenceGeneratorService sequenceGeneratorService;

    @Mock
    private ProjectRepository projectRepository;

    @Spy
    private InspectionDeviceMapper deviceMapper = Mappers.getMapper(InspectionDeviceMapper.class);

    @Mock
    private RedissonClient redissonClient;

    @InjectMocks
    private InspectionDeviceServiceImpl inspectionDeviceService;

    private CreateInspectionDeviceRequest createRequest;
    private InspectionDevice device;

    @BeforeEach
    void setUp() {
        createRequest = new CreateInspectionDeviceRequest();
        createRequest.setSerialNumber("SN-001");
        createRequest.setDeviceModel("MODEL-A");
        createRequest.setIp("192.168.1.10");
        createRequest.setPort(102);
        createRequest.setProjectId(100L);
        createRequest.setStatus(InspectionDeviceStatus.PENDING_INSPECTION);
        createRequest.setRemark("测试");

        device = new InspectionDevice();
        device.setId(1L);
        device.setDeviceNo("IND202501010001");
        device.setSerialNumber("SN-001");
        device.setIp("192.168.1.10");
        device.setPort(102);
        device.setProjectId(100L);
        device.setProjectInternalNo(5);
        device.setStatus(InspectionDeviceStatus.PENDING_INSPECTION);
        device.setCreatedAt(Instant.now());
        device.setDeleted(false);
    }

    @Test
    @DisplayName("分页查询检测设备")
    void getDevicesPage_ShouldReturnPageResult() {
        InspectionDevicePageRequest request = new InspectionDevicePageRequest();
        request.setPage(1);
        request.setSize(10);
        request.setDeviceNo("IND");

        Page<InspectionDevice> page = new PageImpl<>(List.of(device));
        when(deviceRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(page);

        PageResult<InspectionDeviceResponse> result = inspectionDeviceService.getDevicesPage(request);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getContent().size());
        assertEquals("IND202501010001", result.getContent().get(0).getDeviceNo());
    }

    @Test
    @DisplayName("获取单条设备详情 - 存在")
    void getDeviceById_WhenExists_ShouldReturnDevice() {
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));

        InspectionDevice result = inspectionDeviceService.getDeviceById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("获取单条设备详情 - 不存在")
    void getDeviceById_WhenNotExists_ShouldThrowException() {
        when(deviceRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> inspectionDeviceService.getDeviceById(1L));
    }

    @Test
    @DisplayName("根据编号获取设备 - 存在")
    void getDeviceByNo_WhenExists_ShouldReturnDevice() {
        when(deviceRepository.findByDeviceNoAndDeletedFalse("IND202501010001")).thenReturn(Optional.of(device));

        InspectionDevice result = inspectionDeviceService.getDeviceByNo("IND202501010001");

        assertNotNull(result);
        assertEquals("IND202501010001", result.getDeviceNo());
    }

    @Test
    @DisplayName("根据编号获取设备 - 不存在")
    void getDeviceByNo_WhenNotExists_ShouldThrowException() {
        when(deviceRepository.findByDeviceNoAndDeletedFalse("IND202501010001")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> inspectionDeviceService.getDeviceByNo("IND202501010001"));
    }

    @Test
    @DisplayName("创建检测设备 - 生成分布式设备编号并计算项目序号")
    void createDevice_WithProjectId_ShouldGenerateDeviceNoAndInternalNo() throws InterruptedException {
        // Mock 项目存储库存在检查
        when(projectRepository.existsByIdAndDeletedFalse(100L)).thenReturn(true);

        // Mock Redisson 锁
        RLock mockLock = mock(RLock.class);
        when(redissonClient.getLock("inspection-device:project-internal-no:100")).thenReturn(mockLock);
        when(mockLock.tryLock(5, 30, TimeUnit.SECONDS)).thenReturn(true);
        when(mockLock.isHeldByCurrentThread()).thenReturn(true);

        when(deviceRepository.existsBySerialNumberAndDeletedFalse("SN-001")).thenReturn(false);
        when(deviceRepository.existsByIpAndDeletedFalse("192.168.1.10")).thenReturn(false);
        when(sequenceGeneratorService.nextId(SequenceBizType.INSPECTION_DEVICE)).thenReturn("IND202501010001");
        when(deviceRepository.findMaxProjectInternalNoIncludingDeletedByProjectId(100L)).thenReturn(Optional.of(5));
        when(deviceRepository.save(any(InspectionDevice.class))).thenAnswer(invocation -> {
            InspectionDevice saved = invocation.getArgument(0);
            saved.setId(1L);
            saved.setCreatedAt(Instant.now());
            return saved;
        });

        InspectionDeviceResponse response = inspectionDeviceService.createDevice(createRequest);

        assertNotNull(response);
        assertEquals("IND202501010001", response.getDeviceNo());
        assertEquals(6, response.getProjectInternalNo());
        verify(sequenceGeneratorService).nextId(SequenceBizType.INSPECTION_DEVICE);
        verify(redissonClient).getLock("inspection-device:project-internal-no:100");
        verify(mockLock).tryLock(5, 30, TimeUnit.SECONDS);
        verify(deviceRepository).findMaxProjectInternalNoIncludingDeletedByProjectId(100L);
        verify(mockLock).unlock();
    }

    @Test
    @DisplayName("创建检测设备 - 未指定项目时不生成项目内部序号")
    void createDevice_WithoutProjectId_ShouldNotCalculateInternalNo() {
        createRequest.setProjectId(null);
        when(deviceRepository.existsBySerialNumberAndDeletedFalse("SN-001")).thenReturn(false);
        when(deviceRepository.existsByIpAndDeletedFalse("192.168.1.10")).thenReturn(false);
        when(sequenceGeneratorService.nextId(SequenceBizType.INSPECTION_DEVICE)).thenReturn("IND202501010002");
        when(deviceRepository.save(any(InspectionDevice.class))).thenAnswer(invocation -> {
            InspectionDevice saved = invocation.getArgument(0);
            saved.setId(2L);
            saved.setCreatedAt(Instant.now());
            return saved;
        });

        InspectionDeviceResponse response = inspectionDeviceService.createDevice(createRequest);

        assertNotNull(response);
        assertEquals("IND202501010002", response.getDeviceNo());
        assertNull(response.getProjectInternalNo());
        verify(deviceRepository, never()).findMaxProjectInternalNoIncludingDeletedByProjectId(anyLong());
        verify(redissonClient, never()).getLock(anyString());
    }

    @Test
    @DisplayName("更新检测设备 - 更新设备信息")
    void updateDevice_ShouldUpdateDeviceInfo() {
        UpdateInspectionDeviceRequest request = new UpdateInspectionDeviceRequest();
        request.setDeviceModel("新型号");
        request.setPort(202);
        request.setStatus(InspectionDeviceStatus.CALIBRATED);
        request.setSerialNumber("SN-001-UPDATED");
        request.setIp("192.168.1.20");

        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));
        when(deviceRepository.existsBySerialNumberAndDeletedFalse("SN-001-UPDATED")).thenReturn(false);
        when(deviceRepository.existsByIpAndDeletedFalse("192.168.1.20")).thenReturn(false);
        when(deviceRepository.save(any(InspectionDevice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InspectionDeviceResponse response = inspectionDeviceService.updateDevice(1L, request);

        assertEquals("新型号", response.getDeviceModel());
        assertEquals(202, response.getPort());
        assertEquals(InspectionDeviceStatus.CALIBRATED, response.getStatus());
        assertEquals("SN-001-UPDATED", response.getSerialNumber());
        assertEquals("192.168.1.20", response.getIp());
    }

    @Test
    @DisplayName("删除检测设备")
    void deleteDevice_ShouldMarkAsDeleted() {
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));
        when(deviceRepository.save(any(InspectionDevice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        inspectionDeviceService.deleteDevice(1L);

        assertTrue(device.getDeleted());
        assertNotNull(device.getDeletedAt());
        verify(deviceRepository).save(device);
    }

    @Test
    @DisplayName("校验出厂编号唯一性")
    void validateSerialNumberUnique_ShouldReturnCorrectResult() {
        when(deviceRepository.existsBySerialNumberAndDeletedFalse("SN-001")).thenReturn(true);
        when(deviceRepository.existsBySerialNumberAndDeletedFalse("SN-NEW")).thenReturn(false);

        assertThrows(BadRequestException.class, () -> inspectionDeviceService.validateSerialNumberUnique("SN-001"));
        assertTrue(inspectionDeviceService.validateSerialNumberUnique("SN-NEW"));
    }

    @Test
    @DisplayName("校验IP唯一性")
    void validateIpUnique_ShouldReturnCorrectResult() {
        when(deviceRepository.existsByIpAndDeletedFalse("192.168.1.10")).thenReturn(true);
        when(deviceRepository.existsByIpAndDeletedFalse("192.168.1.30")).thenReturn(false);

        assertThrows(BadRequestException.class, () -> inspectionDeviceService.validateIpUnique("192.168.1.10"));
        assertTrue(inspectionDeviceService.validateIpUnique("192.168.1.30"));
    }
}
