package com.yimusi.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.yimusi.BaseIntegrationTest;
import com.yimusi.entity.InspectionDevice;
import com.yimusi.enums.InspectionDeviceStatus;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * InspectionDeviceRepository 集成测试
 * 重点验证原生 SQL 查询是否能正确绕过软删除机制
 */
@Transactional
@DisplayName("InspectionDeviceRepository 集成测试")
class InspectionDeviceRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private InspectionDeviceRepository deviceRepository;

    @AfterEach
    void tearDown() {
        deviceRepository.deleteAll();
    }

    @Test
    @DisplayName("测试 findMaxProjectInternalNoIncludingDeletedByProjectId 包含已删除设备")
    void testFindMaxIncludingDeleted() {
        Long projectId = 100L;

        // 1. 创建并保存3个设备，序号分别为 1, 2, 3
        createAndSaveDevice(projectId, 1, false);
        createAndSaveDevice(projectId, 2, false);
        InspectionDevice device3 = createAndSaveDevice(projectId, 3, false);

        // 2. 验证当前最大序号为 3
        Optional<Integer> maxNo = deviceRepository.findMaxProjectInternalNoIncludingDeletedByProjectId(projectId);
        assertTrue(maxNo.isPresent());
        assertEquals(3, maxNo.get());

        // 3. 软删除序号为 3 的设备
        device3.setDeleted(true);
        device3.setDeletedAt(Instant.now());
        deviceRepository.save(device3);

        // 4. 再次查询最大序号，应该仍然是 3 (包含已删除的)
        Optional<Integer> maxNoAfterDelete = deviceRepository.findMaxProjectInternalNoIncludingDeletedByProjectId(projectId);
        assertTrue(maxNoAfterDelete.isPresent());
        assertEquals(3, maxNoAfterDelete.get(), "即使最大序号的设备被删除，查询结果也应该包含它");
    }

    @Test
    @DisplayName("测试 findMaxProjectInternalNoIncludingDeletedByProjectId 在无数据时返回空")
    void testFindMaxWithNoData() {
        Long projectId = 200L;
        Optional<Integer> maxNo = deviceRepository.findMaxProjectInternalNoIncludingDeletedByProjectId(projectId);
        assertTrue(maxNo.isEmpty());
    }

    @Test
    @DisplayName("测试 findMaxProjectInternalNoIncludingDeletedByProjectId 仅有已删除数据时")
    void testFindMaxWithOnlyDeletedData() {
        Long projectId = 300L;

        // 创建一个已删除的设备，序号为 5
        createAndSaveDevice(projectId, 5, true);

        Optional<Integer> maxNo = deviceRepository.findMaxProjectInternalNoIncludingDeletedByProjectId(projectId);
        assertTrue(maxNo.isPresent());
        assertEquals(5, maxNo.get());
    }

    private InspectionDevice createAndSaveDevice(Long projectId, Integer internalNo, boolean deleted) {
        InspectionDevice device = new InspectionDevice();
        device.setDeviceNo("DEV-" + projectId + "-" + internalNo);
        device.setProjectId(projectId);
        device.setProjectInternalNo(internalNo);
        device.setSerialNumber("SN-" + projectId + "-" + internalNo);
        device.setIp("192.168." + (projectId % 255) + "." + internalNo);
        device.setPort(8080);
        device.setStatus(InspectionDeviceStatus.PENDING_INSPECTION);
        device.setDeleted(deleted);
        if (deleted) {
            device.setDeletedAt(Instant.now());
            device.setDeletedBy(1L);
        }
        return deviceRepository.save(device);
    }
}
