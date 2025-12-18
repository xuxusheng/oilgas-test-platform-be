package com.yimusi.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.time.Instant;

import cn.hutool.core.collection.CollUtil;
import com.querydsl.core.types.Predicate;
import com.yimusi.common.exception.BadRequestException;
import com.yimusi.common.exception.ResourceNotFoundException;
import com.yimusi.common.util.OperatorUtil;
import com.yimusi.dto.common.PageResult;
import com.yimusi.dto.teststation.CreateTestStationRequest;
import com.yimusi.dto.teststation.TestStationPageRequest;
import com.yimusi.dto.teststation.TestStationResponse;
import com.yimusi.dto.teststation.UpdateTestStationRequest;
import com.yimusi.dto.teststation.parameter.TestStationParameterRequest;
import com.yimusi.entity.TestStation;
import com.yimusi.entity.TestStationParameter;
import com.yimusi.enums.TestStationUsage;
import com.yimusi.enums.ValveCommType;
import com.yimusi.mapper.TestStationMapper;
import com.yimusi.repository.TestStationRepository;
import com.yimusi.service.impl.TestStationServiceImpl;
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

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 测试工位服务实现类单元测试
 *
 * 测试覆盖率目标:
 * - 行覆盖率: 80%
 * - 分支覆盖率: 70%
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("测试工位服务单元测试")
class TestStationServiceImplTest {

    @Spy
    private TestStationMapper stationMapper = Mappers.getMapper(TestStationMapper.class);

    @Mock
    private TestStationRepository stationRepository;

    @InjectMocks
    private TestStationServiceImpl testStationService;

    private TestStation mockStation;
    private CreateTestStationRequest createRequest;
    private UpdateTestStationRequest updateRequest;
    private TestStationPageRequest pageRequest;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        mockStation = new TestStation();
        mockStation.setId(1L);
        mockStation.setStationNo(1001);
        mockStation.setStationName("测试工位1号");
        mockStation.setUsage(TestStationUsage.INHOUSE_TEST);
        mockStation.setValveCommType(ValveCommType.SERIAL_MODBUS);
        mockStation.setResponsiblePerson("张三");
        mockStation.setEnabled(true);
        mockStation.setValveControlParams(new ArrayList<>(List.of(
            new TestStationParameter("pressure", "0.5MPa"),
            new TestStationParameter("temperature", "25℃")
        )));
        mockStation.setOilValveMapping(new ArrayList<>(List.of(
            new TestStationParameter("oil1", "valve1"),
            new TestStationParameter("oil2", "valve2")
        )));
        mockStation.setDeleted(false);
        mockStation.setCreatedAt(Instant.now());
        mockStation.setCreatedBy(1L);

        // 创建请求
        createRequest = new CreateTestStationRequest();
        createRequest.setStationNo(1002);
        createRequest.setStationName("测试工位2号");
        createRequest.setUsage(TestStationUsage.INHOUSE_TEST);
        createRequest.setValveCommType(ValveCommType.SERIAL_MODBUS);
        createRequest.setResponsiblePerson("李四");
        createRequest.setEnabled(true);
        createRequest.setValveControlParams(List.of(
            new TestStationParameterRequest("pressure", "0.6MPa")
        ));
        createRequest.setOilValveMapping(List.of(
            new TestStationParameterRequest("oil3", "valve3")
        ));

        // 更新请求
        updateRequest = new UpdateTestStationRequest();
        updateRequest.setStationName("测试工位1号-更新");
        updateRequest.setResponsiblePerson("王五");

        // 分页请求
        pageRequest = new TestStationPageRequest();
        pageRequest.setPage(1);
        pageRequest.setSize(10);
        pageRequest.setStationName("测试");
    }

    @Test
    @DisplayName("获取所有工位 - 成功")
    void getAllStations_Success() {
        // Given
        List<TestStation> stations = List.of(mockStation);
        when(stationRepository.findAll()).thenReturn(stations);

        // When
        List<TestStation> result = testStationService.getAllStations();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(mockStation.getStationNo(), result.get(0).getStationNo());
        verify(stationRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("获取所有工位 - 空列表")
    void getAllStations_EmptyList() {
        // Given
        when(stationRepository.findAll()).thenReturn(List.of());

        // When
        List<TestStation> result = testStationService.getAllStations();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(stationRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("分页查询工位 - 成功")
    void getStationsPage_Success() {
        // Given
        Page<TestStation> page = new PageImpl<>(List.of(mockStation), PageRequest.of(0, 10), 1);
        when(stationRepository.findAll(any(Predicate.class), any(PageRequest.class))).thenReturn(page);

        // When
        PageResult<TestStationResponse> result = testStationService.getStationsPage(pageRequest);

        // Then
        assertNotNull(result);
        /* PageResult使用content字段存储数据,不是data */
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getTotal());

        // 验证 QueryDSL Predicate 被正确构建
        verify(stationRepository).findAll(any(Predicate.class), any(PageRequest.class));
    }

    @Test
    @DisplayName("分页查询工位 - 无结果")
    void getStationsPage_EmptyPage() {
        // Given
        Page<TestStation> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(stationRepository.findAll(any(Predicate.class), any(PageRequest.class))).thenReturn(page);

        // When
        PageResult<TestStationResponse> result = testStationService.getStationsPage(pageRequest);

        // Then
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotal());
    }

    @Test
    @DisplayName("根据ID查询工位 - 成功")
    void getStationById_Success() {
        // Given
        when(stationRepository.findById(1L)).thenReturn(Optional.of(mockStation));

        // When
        TestStation result = testStationService.getStationById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(mockStation.getStationNo(), result.getStationNo());
        verify(stationRepository).findById(1L);
    }

    @Test
    @DisplayName("根据ID查询工位 - ID为空")
    void getStationById_NullId() {
        // When & Then
        assertThrows(BadRequestException.class, () -> testStationService.getStationById(null));
    }

    @Test
    @DisplayName("根据ID查询工位 - 工位不存在")
    void getStationById_NotFound() {
        // Given
        when(stationRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> testStationService.getStationById(999L));
    }

    @Test
    @DisplayName("根据工位编号查询 - 成功")
    void getStationByStationNo_Success() {
        // Given
        when(stationRepository.findByStationNoAndDeletedFalse(1001)).thenReturn(Optional.of(mockStation));

        // When
        TestStation result = testStationService.getStationByStationNo(1001);

        // Then
        assertNotNull(result);
        assertEquals(1001, result.getStationNo());
        verify(stationRepository).findByStationNoAndDeletedFalse(1001);
    }

    @Test
    @DisplayName("根据工位编号查询 - 编号为空")
    void getStationByStationNo_NullStationNo() {
        // When & Then
        assertThrows(BadRequestException.class, () -> testStationService.getStationByStationNo(null));
    }

    @Test
    @DisplayName("根据工位编号查询 - 工位不存在")
    void getStationByStationNo_NotFound() {
        // Given
        when(stationRepository.findByStationNoAndDeletedFalse(9999)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> testStationService.getStationByStationNo(9999));
    }

    @Test
    @DisplayName("创建工位 - 成功")
    void createStation_Success() {
        // Given
        when(stationRepository.existsByStationNoAndDeletedFalse(1002)).thenReturn(false);

        // 创建预期保存的实体
        TestStation expectedStation = new TestStation();
        expectedStation.setStationNo(createRequest.getStationNo());
        expectedStation.setStationName(createRequest.getStationName());
        expectedStation.setEnabled(true);

        when(stationRepository.save(any(TestStation.class))).thenReturn(mockStation);

        // When
        TestStationResponse response = testStationService.createStation(createRequest);

        // Then
        assertNotNull(response);
        assertEquals(mockStation.getStationNo(), response.getStationNo());

        // 验证唯一性检查
        verify(stationRepository).existsByStationNoAndDeletedFalse(1002);

        // 验证保存被调用
        ArgumentCaptor<TestStation> captor = ArgumentCaptor.forClass(TestStation.class);
        verify(stationRepository).save(captor.capture());

        @SuppressWarnings("unchecked")
        TestStation saved = captor.getValue();
        assertEquals(createRequest.getStationNo(), saved.getStationNo());
        assertEquals(true, saved.getEnabled()); // 默认启用
    }

    @Test
    @DisplayName("创建工位 - 工位编号已存在")
    void createStation_DuplicateStationNo() {
        // Given
        when(stationRepository.existsByStationNoAndDeletedFalse(1002)).thenReturn(true);

        // When & Then
        assertThrows(BadRequestException.class, () -> testStationService.createStation(createRequest));
        verify(stationRepository, never()).save(any(TestStation.class));
    }

    @Test
    @DisplayName("创建工位 - 禁用状态")
    void createStation_DisabledStatus() {
        // Given
        createRequest.setEnabled(false);
        when(stationRepository.existsByStationNoAndDeletedFalse(1002)).thenReturn(false);
        when(stationRepository.save(any(TestStation.class))).thenReturn(mockStation);

        // When
        TestStationResponse response = testStationService.createStation(createRequest);

        // Then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<TestStation> captor = ArgumentCaptor.forClass(TestStation.class);
        verify(stationRepository).save(captor.capture());

        TestStation saved = captor.getValue();
        assertEquals(false, saved.getEnabled());
    }

    @Test
    @DisplayName("创建工位 - 参数为空列表")
    void createStation_NullParams() {
        // Given
        createRequest.setValveControlParams(null);
        createRequest.setOilValveMapping(null);
        when(stationRepository.existsByStationNoAndDeletedFalse(1002)).thenReturn(false);
        when(stationRepository.save(any(TestStation.class))).thenReturn(mockStation);

        // When
        TestStationResponse response = testStationService.createStation(createRequest);

        // Then
        assertNotNull(response);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<TestStation> captor = ArgumentCaptor.forClass(TestStation.class);
        verify(stationRepository).save(captor.capture());

        TestStation saved = captor.getValue();
        assertTrue(CollUtil.isEmpty(saved.getValveControlParams()));
        assertTrue(CollUtil.isEmpty(saved.getOilValveMapping()));
    }

    @Test
    @DisplayName("更新工位 - 成功")
    void updateStation_Success() {
        // Given
        when(stationRepository.findById(1L)).thenReturn(Optional.of(mockStation));
        updateRequest.setStationNo(1001); // 保持原有编号
        when(stationRepository.save(any(TestStation.class))).thenReturn(mockStation);

        // When
        TestStationResponse response = testStationService.updateStation(1L, updateRequest);

        // Then
        assertNotNull(response);
        verify(stationRepository).findById(1L);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<TestStation> captor = ArgumentCaptor.forClass(TestStation.class);
        verify(stationRepository).save(captor.capture());

        // 更新请求只设置了部分字段,需要验证实际被更新的字段
        // 由于 updateEntityFromRequest 是 MapStruct 方法,这里我们只验证 save 被调用
        verify(stationRepository, atLeastOnce()).save(any(TestStation.class));
    }

    @Test
    @DisplayName("更新工位 - 更改工位编号")
    void updateStation_ChangeStationNo() {
        // Given
        when(stationRepository.findById(1L)).thenReturn(Optional.of(mockStation));
        updateRequest.setStationNo(2002); // 更改编号
        when(stationRepository.existsByStationNoAndDeletedFalse(2002)).thenReturn(false);
        when(stationRepository.save(any(TestStation.class))).thenReturn(mockStation);

        // When
        TestStationResponse response = testStationService.updateStation(1L, updateRequest);

        // Then
        verify(stationRepository).existsByStationNoAndDeletedFalse(2002);
        verify(stationRepository).save(any(TestStation.class));
    }

    @Test
    @DisplayName("更新工位 - 新编号已存在")
    void updateStation_DuplicateNewStationNo() {
        // Given
        when(stationRepository.findById(1L)).thenReturn(Optional.of(mockStation));
        updateRequest.setStationNo(2002);
        when(stationRepository.existsByStationNoAndDeletedFalse(2002)).thenReturn(true);

        // When & Then
        assertThrows(BadRequestException.class, () -> testStationService.updateStation(1L, updateRequest));
        verify(stationRepository, never()).save(any(TestStation.class));
    }

    @Test
    @DisplayName("更新工位 - 更新参数列表")
    void updateStation_UpdateParams() {
        // Given - 准备一个可变的 mockStation 用于更新操作
        TestStation mutableStation = new TestStation();
        mutableStation.setId(1L);
        mutableStation.setStationNo(1001);
        mutableStation.setStationName("测试工位1号");
        mutableStation.setResponsiblePerson("张三");
        mutableStation.setValveControlParams(new ArrayList<>(List.of(
            new TestStationParameter("pressure", "0.5MPa")
        )));
        mutableStation.setOilValveMapping(new ArrayList<>(List.of(
            new TestStationParameter("oil1", "valve1")
        )));

        when(stationRepository.findById(1L)).thenReturn(Optional.of(mutableStation));
        updateRequest.setValveControlParams(List.of(
            new TestStationParameterRequest("pressure", "0.8MPa"),
            new TestStationParameterRequest("temperature", "30℃")
        ));
        updateRequest.setOilValveMapping(List.of(
            new TestStationParameterRequest("oilX", "valveX")
        ));
        when(stationRepository.save(any(TestStation.class))).thenReturn(mutableStation);

        // When
        testStationService.updateStation(1L, updateRequest);

        // Then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<TestStation> captor = ArgumentCaptor.forClass(TestStation.class);
        verify(stationRepository).save(captor.capture());

        // 验证 save 被调用,参数列表转换逻辑在 Service 中完成
        TestStation updated = captor.getValue();
        assertNotNull(updated.getValveControlParams());
        assertEquals(2, updated.getValveControlParams().size());
        assertEquals(1, updated.getOilValveMapping().size());
    }

    @Test
    @DisplayName("更新工位 - ID为空")
    void updateStation_NullId() {
        // When & Then
        assertThrows(BadRequestException.class, () -> testStationService.updateStation(null, updateRequest));
    }

    @Test
    @DisplayName("更新工位 - ID不存在")
    void updateStation_NotFound() {
        // Given
        when(stationRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> testStationService.updateStation(999L, updateRequest));
    }

    @Test
    @DisplayName("删除工位 - 成功")
    void deleteStation_Success() {
        // Given
        when(stationRepository.findById(1L)).thenReturn(Optional.of(mockStation));
        when(stationRepository.save(any(TestStation.class))).thenReturn(mockStation);

        // When
        testStationService.deleteStation(1L);

        // Then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<TestStation> captor = ArgumentCaptor.forClass(TestStation.class);
        verify(stationRepository).save(captor.capture());

        TestStation deleted = captor.getValue();
        // 验证软删除字段被设置
        assertEquals(true, deleted.getDeleted());
        assertNotNull(deleted.getDeletedAt());
        assertNotNull(deleted.getDeletedBy());
        verify(stationRepository).findById(1L);
    }

    @Test
    @DisplayName("删除工位 - ID为空")
    void deleteStation_NullId() {
        // When & Then
        assertThrows(BadRequestException.class, () -> testStationService.deleteStation(null));
    }

    @Test
    @DisplayName("删除工位 - ID不存在")
    void deleteStation_NotFound() {
        // Given
        when(stationRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> testStationService.deleteStation(999L));
        verify(stationRepository, never()).save(any(TestStation.class));
    }

    @Test
    @DisplayName("验证工位编号唯一性 - 是唯一的")
    void validateStationNoUnique_Unique() {
        // Given
        when(stationRepository.existsByStationNoAndDeletedFalse(1002)).thenReturn(false);

        // When
        boolean result = testStationService.validateStationNoUnique(1002);

        // Then
        assertTrue(result);
        verify(stationRepository).existsByStationNoAndDeletedFalse(1002);
    }

    @Test
    @DisplayName("验证工位编号唯一性 - 已存在")
    void validateStationNoUnique_Duplicate() {
        // Given
        when(stationRepository.existsByStationNoAndDeletedFalse(1001)).thenReturn(true);

        // When & Then
        assertThrows(BadRequestException.class, () -> testStationService.validateStationNoUnique(1001));
    }

    @Test
    @DisplayName("验证工位编号唯一性 - 编号为空")
    void validateStationNoUnique_Null() {
        // When
        boolean result = testStationService.validateStationNoUnique(null);

        // Then
        assertTrue(result);
        verify(stationRepository, never()).existsByStationNoAndDeletedFalse(any());
    }

    @Test
    @DisplayName("标记删除 - 验证字段设置")
    void markDeleted_FieldsSetCorrectly() {
        // Given: 模拟测试 markDeleted 方法被调用
        TestStation station = new TestStation();
        station.setId(1L);
        station.setStationNo(1001);

        when(stationRepository.findById(1L)).thenReturn(Optional.of(station));
        when(stationRepository.save(any(TestStation.class))).thenReturn(station);

        // When: 调用删除
        testStationService.deleteStation(1L);

        // Then: 验证软删除字段
        @SuppressWarnings("unchecked")
        ArgumentCaptor<TestStation> captor = ArgumentCaptor.forClass(TestStation.class);
        verify(stationRepository).save(captor.capture());

        TestStation deleted = captor.getValue();
        assertEquals(true, deleted.getDeleted());
        assertNotNull(deleted.getDeletedAt());
        assertNotNull(deleted.getDeletedBy());
    }

    @Test
    @DisplayName("完整工作流 - 创建到删除")
    void completeWorkflow_CreateToDelete() {
        // Given: 创建工位
        // 创建请求使用 stationNo=1002
        createRequest.setStationNo(1002);

        when(stationRepository.existsByStationNoAndDeletedFalse(1002)).thenReturn(false);
        TestStation newStation = new TestStation();
        newStation.setId(3L);
        newStation.setStationNo(1002);
        newStation.setValveControlParams(new ArrayList<>());
        newStation.setOilValveMapping(new ArrayList<>());
        when(stationRepository.save(any(TestStation.class))).thenReturn(newStation);

        // When: 创建
        TestStationResponse createResponse = testStationService.createStation(createRequest);
        Long stationId = newStation.getId();

        // Then: 创建成功
        assertNotNull(createResponse);

        // Given: 更新工位
        when(stationRepository.findById(stationId)).thenReturn(Optional.of(newStation));
        UpdateTestStationRequest updateReq = new UpdateTestStationRequest();
        updateReq.setStationName("更新后的名称");
        when(stationRepository.save(any(TestStation.class))).thenReturn(newStation);

        // When: 更新
        TestStationResponse updateResponse = testStationService.updateStation(stationId, updateReq);

        // Then: 更新成功
        verify(stationRepository, times(2)).save(any(TestStation.class));

        // Given: 删除工位
        when(stationRepository.findById(stationId)).thenReturn(Optional.of(newStation));

        // When: 删除
        testStationService.deleteStation(stationId);

        // Then: 删除成功
        verify(stationRepository, times(3)).save(any(TestStation.class));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<TestStation> captor = ArgumentCaptor.forClass(TestStation.class);
        verify(stationRepository, times(3)).save(captor.capture());

        TestStation lastSaved = captor.getValue();
        assertEquals(true, lastSaved.getDeleted());
    }
}
