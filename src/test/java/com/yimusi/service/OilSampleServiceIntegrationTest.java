package com.yimusi.service;

import static org.junit.jupiter.api.Assertions.*;

import com.yimusi.BaseIntegrationTest;
import com.yimusi.dto.common.PageResult;
import com.yimusi.dto.oilsample.CreateOilSampleRequest;
import com.yimusi.dto.oilsample.OilSamplePageRequest;
import com.yimusi.dto.oilsample.OilSampleResponse;
import com.yimusi.enums.OilSampleStatus;
import com.yimusi.enums.OilSampleUsage;
import com.yimusi.repository.OilSampleRepository;
import java.util.ArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("OilSampleService 集成测试")
class OilSampleServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private OilSampleService oilSampleService;

    @Autowired
    private OilSampleRepository oilSampleRepository;

    @BeforeEach
    void setUp() {
        oilSampleRepository.deleteAll();

        createOilSample("SAMPLE-001", "Sample A", OilSampleUsage.CLEANING, OilSampleStatus.ENABLED, 1001);
        createOilSample("SAMPLE-002", "Sample B", OilSampleUsage.CALIBRATION, OilSampleStatus.DISABLED, 1002);
        createOilSample("SAMPLE-003", "Sample C", OilSampleUsage.CLEANING, OilSampleStatus.ENABLED, 1001);
    }

    @AfterEach
    void tearDown() {
        oilSampleRepository.deleteAll();
    }

    @Test
    @DisplayName("分页查询 - 无条件")
    void testGetOilSamplesPage_NoCondition() {
        OilSamplePageRequest request = new OilSamplePageRequest();
        request.setPage(1);
        request.setSize(10);

        PageResult<OilSampleResponse> result = oilSampleService.getOilSamplesPage(request);
        assertEquals(3, result.getTotal());
    }

    @Test
    @DisplayName("分页查询 - 按编号模糊查询")
    void testGetOilSamplesPage_BySampleNo() {
        OilSamplePageRequest request = new OilSamplePageRequest();
        request.setPage(1);
        request.setSize(10);
        request.setSampleNo("001");

        PageResult<OilSampleResponse> result = oilSampleService.getOilSamplesPage(request);
        assertEquals(1, result.getTotal());
        assertEquals("SAMPLE-001", result.getContent().get(0).getSampleNo());
    }

    @Test
    @DisplayName("分页查询 - 按名称模糊查询")
    void testGetOilSamplesPage_BySampleName() {
        OilSamplePageRequest request = new OilSamplePageRequest();
        request.setPage(1);
        request.setSize(10);
        request.setSampleName("Sample");

        PageResult<OilSampleResponse> result = oilSampleService.getOilSamplesPage(request);
        assertEquals(3, result.getTotal());
    }

    @Test
    @DisplayName("分页查询 - 按用途查询")
    void testGetOilSamplesPage_ByUsage() {
        OilSamplePageRequest request = new OilSamplePageRequest();
        request.setPage(1);
        request.setSize(10);
        request.setUsage(OilSampleUsage.CLEANING);

        PageResult<OilSampleResponse> result = oilSampleService.getOilSamplesPage(request);
        assertEquals(2, result.getTotal());
    }

    @Test
    @DisplayName("分页查询 - 按状态查询")
    void testGetOilSamplesPage_ByStatus() {
        OilSamplePageRequest request = new OilSamplePageRequest();
        request.setPage(1);
        request.setSize(10);
        request.setStatus(OilSampleStatus.DISABLED);

        PageResult<OilSampleResponse> result = oilSampleService.getOilSamplesPage(request);
        assertEquals(1, result.getTotal());
        assertEquals("SAMPLE-002", result.getContent().get(0).getSampleNo());
    }

    @Test
    @DisplayName("分页查询 - 按气瓶编号查询")
    void testGetOilSamplesPage_ByCylinderNo() {
        OilSamplePageRequest request = new OilSamplePageRequest();
        request.setPage(1);
        request.setSize(10);
        request.setCylinderNo(1001);

        PageResult<OilSampleResponse> result = oilSampleService.getOilSamplesPage(request);
        assertEquals(2, result.getTotal());
    }

    @Test
    @DisplayName("分页查询 - 组合查询")
    void testGetOilSamplesPage_Combined() {
        OilSamplePageRequest request = new OilSamplePageRequest();
        request.setPage(1);
        request.setSize(10);
        request.setUsage(OilSampleUsage.CLEANING);
        request.setCylinderNo(1001);

        PageResult<OilSampleResponse> result = oilSampleService.getOilSamplesPage(request);
        assertEquals(2, result.getTotal());
    }

    private void createOilSample(String no, String name, OilSampleUsage usage, OilSampleStatus status, Integer cylinderNo) {
        CreateOilSampleRequest request = new CreateOilSampleRequest();
        request.setSampleNo(no);
        request.setSampleName(name);
        request.setUsage(usage);
        request.setCylinderNo(cylinderNo);
        request.setStatus(status);
        request.setParameters(new ArrayList<>());

        oilSampleService.createOilSample(request);
    }
}
