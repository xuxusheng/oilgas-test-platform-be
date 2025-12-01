package com.yimusi.service;

import com.yimusi.BaseIntegrationTest;
import com.yimusi.enums.SequenceBizType;
import com.yimusi.repository.SequenceGeneratorRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 序列号生成器服务集成测试
 */
@Transactional
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=true"
})
class SequenceGeneratorServiceTest extends BaseIntegrationTest {

    @Autowired
    private SequenceGeneratorService sequenceGeneratorService;

    @Autowired
    private SequenceGeneratorRepository sequenceGeneratorRepository;

    @BeforeEach
    void setUp() {
        // 清理所有序列号记录
        sequenceGeneratorRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        sequenceGeneratorRepository.deleteAll();
    }

    @Test
    void testNextId() {
        String deviceNo = sequenceGeneratorService.nextId(SequenceBizType.INSPECTION_DEVICE);
        assertNotNull(deviceNo);
        assertTrue(deviceNo.startsWith("IND"));
        assertEquals(15, deviceNo.length());
    }

    @Test
    void testNextIds() {
        List<String> deviceNos = sequenceGeneratorService.nextIds(SequenceBizType.INSPECTION_DEVICE, 2);
        assertNotNull(deviceNos);
        assertEquals(2, deviceNos.size());

        for (String deviceNo : deviceNos) {
            assertTrue(deviceNo.startsWith("IND"));
            assertEquals(15, deviceNo.length());
        }

        assertTrue(deviceNos.get(1).compareTo(deviceNos.get(0)) > 0);
    }

    @Test
    void testNextIdWithDynamicBizType() {
        Long projectId = 123L;
        String bizType = "project_internal_" + projectId;
        
        String id = sequenceGeneratorService.nextId(bizType);
        assertEquals("1", id);

        String id2 = sequenceGeneratorService.nextId(bizType);
        assertEquals("2", id2);
    }

    @Test
    void testGetCurrentValue() {
        // 先生成一些序列号
        sequenceGeneratorService.nextIds(SequenceBizType.INSPECTION_DEVICE, 2);
        
        Long currentValue = sequenceGeneratorService.getCurrentValue(
            SequenceBizType.INSPECTION_DEVICE
        );
        
        assertEquals(2L, currentValue);
    }

    @Test
    void testBatchCreate() {
        // 模拟批量创建设备
        int batchSize = 2;
        List<String> deviceNos = sequenceGeneratorService.nextIds(
            SequenceBizType.INSPECTION_DEVICE,
            batchSize
        );
        
        assertEquals(batchSize, deviceNos.size());
        
        // 验证没有重复
        long uniqueCount = deviceNos.stream().distinct().count();
        assertEquals(batchSize, uniqueCount);
    }
}
