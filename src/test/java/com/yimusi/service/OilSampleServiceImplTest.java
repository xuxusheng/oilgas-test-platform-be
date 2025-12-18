package com.yimusi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.yimusi.common.exception.BusinessException;
import com.yimusi.common.exception.ResourceNotFoundException;
import com.yimusi.dto.common.PageResult;
import com.yimusi.dto.oilsample.CreateOilSampleRequest;
import com.yimusi.dto.oilsample.OilSamplePageRequest;
import com.yimusi.dto.oilsample.OilSampleResponse;
import com.yimusi.dto.oilsample.UpdateOilSampleRequest;
import com.yimusi.entity.OilSample;
import com.yimusi.enums.OilSampleStatus;
import com.yimusi.enums.OilSampleUsage;
import com.yimusi.mapper.OilSampleMapper;
import com.yimusi.repository.OilSampleRepository;
import com.yimusi.service.impl.OilSampleServiceImpl;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class OilSampleServiceImplTest {

    @Mock
    private OilSampleRepository oilSampleRepository;

    @Spy
    private OilSampleMapper oilSampleMapper = Mappers.getMapper(OilSampleMapper.class);

    @InjectMocks
    private OilSampleServiceImpl oilSampleService;

    private OilSample oilSample;
    private CreateOilSampleRequest createRequest;
    private UpdateOilSampleRequest updateRequest;

    @BeforeEach
    void setUp() {
        oilSample = new OilSample();
        oilSample.setId(1L);
        oilSample.setSampleNo("SAMPLE-001");
        oilSample.setSampleName("Test Sample");
        oilSample.setUsage(OilSampleUsage.FACTORY_TEST);
        oilSample.setStatus(OilSampleStatus.ENABLED);
        oilSample.setCylinderNo(1001);
        oilSample.setCreatedAt(Instant.now());

        createRequest = new CreateOilSampleRequest();
        createRequest.setSampleNo("SAMPLE-001");
        createRequest.setSampleName("Test Sample");
        createRequest.setUsage(OilSampleUsage.FACTORY_TEST);
        createRequest.setCylinderNo(1001);

        updateRequest = new UpdateOilSampleRequest();
        updateRequest.setSampleNo("SAMPLE-001-UPDATED");
        updateRequest.setSampleName("Updated Sample");
    }

    @Test
    @DisplayName("分页查询油样列表")
    void getOilSamplesPage_ShouldReturnPageResult() {
        OilSamplePageRequest request = new OilSamplePageRequest();
        request.setPage(1);
        request.setSize(10);
        request.setSampleNo("SAMPLE");

        Page<OilSample> page = new PageImpl<>(List.of(oilSample));
        when(oilSampleRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);

        PageResult<OilSampleResponse> result = oilSampleService.getOilSamplesPage(request);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getContent().size());
        assertEquals("SAMPLE-001", result.getContent().get(0).getSampleNo());
    }

    @Test
    @DisplayName("获取单条油样详情 - 存在")
    void getOilSampleById_WhenExists_ShouldReturnResponse() {
        when(oilSampleRepository.findById(1L)).thenReturn(Optional.of(oilSample));

        OilSampleResponse response = oilSampleService.getOilSampleById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("SAMPLE-001", response.getSampleNo());
    }

    @Test
    @DisplayName("获取单条油样详情 - 不存在")
    void getOilSampleById_WhenNotExists_ShouldThrowException() {
        when(oilSampleRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> oilSampleService.getOilSampleById(1L));
    }

    @Test
    @DisplayName("创建油样 - 成功")
    void createOilSample_WhenValid_ShouldReturnResponse() {
        when(oilSampleRepository.existsBySampleNo(createRequest.getSampleNo())).thenReturn(false);
        when(oilSampleRepository.save(any(OilSample.class))).thenAnswer(invocation -> {
            OilSample saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        OilSampleResponse response = oilSampleService.createOilSample(createRequest);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(createRequest.getSampleNo(), response.getSampleNo());
    }

    @Test
    @DisplayName("创建油样 - parameters 为 null 时保存为空列表")
    void createOilSample_WhenParametersNull_ShouldPersistEmptyList() {
        createRequest.setStatus(OilSampleStatus.ENABLED);
        createRequest.setParameters(null);

        when(oilSampleRepository.existsBySampleNo(createRequest.getSampleNo())).thenReturn(false);
        when(oilSampleRepository.save(any(OilSample.class))).thenAnswer(invocation -> {
            OilSample saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        oilSampleService.createOilSample(createRequest);

        ArgumentCaptor<OilSample> captor = ArgumentCaptor.forClass(OilSample.class);
        verify(oilSampleRepository).save(captor.capture());
        assertNotNull(captor.getValue().getParameters());
        assertTrue(captor.getValue().getParameters().isEmpty());
    }

    @Test
    @DisplayName("创建油样 - 编号已存在")
    void createOilSample_WhenSampleNoExists_ShouldThrowException() {
        when(oilSampleRepository.existsBySampleNo(createRequest.getSampleNo())).thenReturn(true);

        assertThrows(BusinessException.class, () -> oilSampleService.createOilSample(createRequest));
    }

    @Test
    @DisplayName("更新油样 - 成功")
    void updateOilSample_WhenValid_ShouldReturnResponse() {
        when(oilSampleRepository.findById(1L)).thenReturn(Optional.of(oilSample));
        when(oilSampleRepository.existsBySampleNoAndIdNot(updateRequest.getSampleNo(), 1L)).thenReturn(false);
        when(oilSampleRepository.save(any(OilSample.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OilSampleResponse response = oilSampleService.updateOilSample(1L, updateRequest);

        assertNotNull(response);
        assertEquals(updateRequest.getSampleNo(), response.getSampleNo());
        assertEquals(updateRequest.getSampleName(), response.getSampleName());
    }

    @Test
    @DisplayName("更新油样 - 编号冲突")
    void updateOilSample_WhenSampleNoConflict_ShouldThrowException() {
        when(oilSampleRepository.findById(1L)).thenReturn(Optional.of(oilSample));
        when(oilSampleRepository.existsBySampleNoAndIdNot(updateRequest.getSampleNo(), 1L)).thenReturn(true);

        assertThrows(BusinessException.class, () -> oilSampleService.updateOilSample(1L, updateRequest));
    }

    @Test
    @DisplayName("删除油样 - 成功")
    void deleteOilSample_WhenExists_ShouldDelete() {
        when(oilSampleRepository.existsById(1L)).thenReturn(true);

        oilSampleService.deleteOilSample(1L);

        verify(oilSampleRepository).deleteById(1L);
    }

    @Test
    @DisplayName("删除油样 - 不存在")
    void deleteOilSample_WhenNotExists_ShouldThrowException() {
        when(oilSampleRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> oilSampleService.deleteOilSample(1L));
    }

    @Test
    @DisplayName("校验编号唯一性")
    void validateSampleNoUnique_ShouldReturnCorrectResult() {
        when(oilSampleRepository.existsBySampleNo("SAMPLE-001")).thenReturn(true);
        when(oilSampleRepository.existsBySampleNo("SAMPLE-NEW")).thenReturn(false);

        assertFalse(oilSampleService.validateSampleNoUnique("SAMPLE-001"));
        assertTrue(oilSampleService.validateSampleNoUnique("SAMPLE-NEW"));
    }
}
