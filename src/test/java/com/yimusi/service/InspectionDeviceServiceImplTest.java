package com.yimusi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.yimusi.dto.inspection.CreateInspectionDeviceRequest;
import com.yimusi.dto.inspection.InspectionDeviceResponse;
import com.yimusi.dto.inspection.UpdateInspectionDeviceRequest;
import com.yimusi.entity.InspectionDevice;
import com.yimusi.entity.Project;
import com.yimusi.enums.InspectionDeviceStatus;
import com.yimusi.enums.SequenceBizType;
import com.yimusi.mapper.InspectionDeviceMapper;
import com.yimusi.repository.InspectionDeviceRepository;
import com.yimusi.repository.ProjectRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @InjectMocks
    private InspectionDeviceServiceImpl inspectionDeviceService;

    private CreateInspectionDeviceRequest createRequest;

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
    }

    @Test
    @DisplayName("创建检测设备 - 生成分布式设备编号并计算项目序号")
    void createDevice_WithProjectId_ShouldGenerateDeviceNoAndInternalNo() {
        when(deviceRepository.existsBySerialNumberAndDeletedFalse("SN-001")).thenReturn(false);
        when(deviceRepository.existsByIpAndDeletedFalse("192.168.1.10")).thenReturn(false);
        when(sequenceGeneratorService.nextId(SequenceBizType.INSPECTION_DEVICE)).thenReturn("IND202501010001");
        when(projectRepository.lockById(100L)).thenReturn(Optional.of(mockProject(100L)));
        when(deviceRepository.findMaxProjectInternalNoByProjectId(100L)).thenReturn(Optional.of(5));
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
        verify(deviceRepository).findMaxProjectInternalNoByProjectId(100L);
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
        verify(deviceRepository, never()).findMaxProjectInternalNoByProjectId(anyLong());
    }

    @Test
    @DisplayName("更新检测设备 - 更换项目时重新计算项目序号")
    void updateDevice_WhenProjectChanges_ShouldRecalculateInternalNo() {
        InspectionDevice existing = new InspectionDevice();
        existing.setId(10L);
        existing.setDeviceNo("IND202501010001");
        existing.setSerialNumber("SN-001");
        existing.setIp("192.168.1.10");
        existing.setPort(102);
        existing.setProjectId(100L);
        existing.setProjectInternalNo(3);
        existing.setStatus(InspectionDeviceStatus.PENDING_INSPECTION);
        existing.setCreatedAt(Instant.now());

        UpdateInspectionDeviceRequest request = new UpdateInspectionDeviceRequest();
        request.setProjectId(200L);

        when(deviceRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(projectRepository.lockById(200L)).thenReturn(Optional.of(mockProject(200L)));
        when(deviceRepository.findMaxProjectInternalNoByProjectId(200L)).thenReturn(Optional.of(8));
        when(deviceRepository.save(any(InspectionDevice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InspectionDeviceResponse response = inspectionDeviceService.updateDevice(10L, request);

        assertEquals(200L, response.getProjectId());
        assertEquals(9, response.getProjectInternalNo());
        verify(deviceRepository).findMaxProjectInternalNoByProjectId(200L);
    }

    private Project mockProject(Long id) {
        Project project = new Project();
        project.setId(id);
        return project;
    }

}
