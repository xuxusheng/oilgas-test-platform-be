package com.yimusi.service;

import static org.junit.jupiter.api.Assertions.*;

import com.yimusi.BaseIntegrationTest;
import com.yimusi.dto.inspection.CreateInspectionDeviceRequest;
import com.yimusi.dto.inspection.InspectionDeviceResponse;
import com.yimusi.entity.Project;
import com.yimusi.enums.InspectionDeviceStatus;
import com.yimusi.repository.InspectionDeviceRepository;
import com.yimusi.repository.ProjectRepository;
import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * InspectionDeviceService 集成测试
 * 验证完整的业务流程，包括 Redisson 锁和序号生成逻辑
 */
@DisplayName("InspectionDeviceService 集成测试")
class InspectionDeviceServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private InspectionDeviceService inspectionDeviceService;

    @Autowired
    private InspectionDeviceRepository deviceRepository;

    @Autowired
    private ProjectRepository projectRepository;

    private Long projectId;

    @BeforeEach
    void setUp() {
        // 创建一个测试项目
        Project project = new Project();
        project.setProjectName("测试项目");
        project.setProjectNo("PROJ-001");
        project.setDeleted(false);
        project.setCreatedAt(Instant.now());
        project.setCreatedBy(1L);
        Project savedProject = projectRepository.save(project);
        projectId = savedProject.getId();
    }

    @AfterEach
    void tearDown() {
        deviceRepository.deleteAll();
        projectRepository.deleteAll();
    }

    @Test
    @DisplayName("测试设备序号连续性：创建 -> 删除 -> 再创建")
    void testProjectInternalNoContinuityAfterDeletion() {
        // 1. 创建设备1
        CreateInspectionDeviceRequest req1 = createRequest("SN-001", "192.168.1.1");
        InspectionDeviceResponse resp1 = inspectionDeviceService.createDevice(req1);
        assertEquals(1, resp1.getProjectInternalNo());

        // 2. 创建设备2
        CreateInspectionDeviceRequest req2 = createRequest("SN-002", "192.168.1.2");
        InspectionDeviceResponse resp2 = inspectionDeviceService.createDevice(req2);
        assertEquals(2, resp2.getProjectInternalNo());

        // 3. 删除设备2 (它是当前最大序号)
        inspectionDeviceService.deleteDevice(resp2.getId());

        // 4. 创建设备3
        // 预期：虽然序号2被删除了，但最大序号记录应该是2，所以新设备应该是3
        CreateInspectionDeviceRequest req3 = createRequest("SN-003", "192.168.1.3");
        InspectionDeviceResponse resp3 = inspectionDeviceService.createDevice(req3);

        assertEquals(3, resp3.getProjectInternalNo(), "删除最大序号设备后，新设备序号应继续递增");
    }

    @Test
    @DisplayName("测试设备序号连续性：删除中间设备")
    void testProjectInternalNoContinuityAfterMiddleDeletion() {
        // 1. 创建设备1, 2, 3
        inspectionDeviceService.createDevice(createRequest("SN-001", "192.168.1.1")); // No: 1
        InspectionDeviceResponse resp2 = inspectionDeviceService.createDevice(createRequest("SN-002", "192.168.1.2")); // No: 2
        inspectionDeviceService.createDevice(createRequest("SN-003", "192.168.1.3")); // No: 3

        // 2. 删除中间的设备2
        inspectionDeviceService.deleteDevice(resp2.getId());

        // 3. 创建新设备
        // 预期：最大序号是3，新设备应该是4
        InspectionDeviceResponse resp4 = inspectionDeviceService.createDevice(createRequest("SN-004", "192.168.1.4"));

        assertEquals(4, resp4.getProjectInternalNo(), "删除中间设备不应影响最大序号的计算");
    }

    private CreateInspectionDeviceRequest createRequest(String sn, String ip) {
        CreateInspectionDeviceRequest request = new CreateInspectionDeviceRequest();
        request.setProjectId(projectId);
        request.setSerialNumber(sn);
        request.setIp(ip);
        request.setPort(8080);
        request.setDeviceModel("TEST-MODEL");
        request.setStatus(InspectionDeviceStatus.PENDING_INSPECTION);
        return request;
    }
}
