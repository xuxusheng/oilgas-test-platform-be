package com.yimusi.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yimusi.BaseIntegrationTest;
import com.yimusi.common.model.ApiResponse;
import com.yimusi.dto.common.PageResult;
import com.yimusi.dto.teststation.CreateTestStationRequest;
import com.yimusi.dto.teststation.TestStationResponse;
import com.yimusi.dto.teststation.UpdateTestStationRequest;
import com.yimusi.dto.teststation.parameter.TestStationParameterRequest;
import com.yimusi.entity.TestStation;
import com.yimusi.enums.TestStationUsage;
import com.yimusi.enums.ValveCommType;
import com.yimusi.repository.TestStationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 测试工位控制器集成测试
 *
 * 继承 BaseIntegrationTest，使用 Testcontainers 启动真实的 MySQL 和 Redis
 * 每次测试后会自动回滚数据
 */
@AutoConfigureMockMvc
@DisplayName("测试工位控制器集成测试")
class TestStationControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestStationRepository stationRepository;

    private static int uniqueCounter = 10000;

    @BeforeEach
    void setUp() {
        // 清理数据
        stationRepository.deleteAll();
    }

    private int nextStationNo() {
        return uniqueCounter++;
    }

    @Test
    @DisplayName("查询所有工位 - 成功")
    void getAllStations_Success() throws Exception {
        // Given
        int stationNo = nextStationNo();
        TestStation station = createTestStation(stationNo, "预存工位");
        stationRepository.save(station);

        // When
        String response = mockMvc.perform(get("/api/test-stations")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Then
        ApiResponse<List<TestStationResponse>> apiResponse = objectMapper.readValue(
                response, new TypeReference<>() {}
        );
        assertThat(apiResponse.getCode()).isEqualTo(200);
        assertThat(apiResponse.getData()).hasSize(1);
        assertThat(apiResponse.getData().get(0).getStationName()).isEqualTo("预存工位");
    }

    @Test
    @DisplayName("分页查询工位 - 成功")
    void getStationsPage_Success() throws Exception {
        // Given
        int stationNo = nextStationNo();
        TestStation station = createTestStation(stationNo, "测试工位ABC");
        stationRepository.save(station);

        // When
        String response = mockMvc.perform(get("/api/test-stations/page")
                        .param("page", "1")
                        .param("size", "10")
                        .param("stationName", "ABC")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Then
        ApiResponse<PageResult<TestStationResponse>> apiResponse = objectMapper.readValue(
                response, new TypeReference<>() {}
        );
        assertThat(apiResponse.getCode()).isEqualTo(200);
        assertThat(apiResponse.getData().getContent()).hasSize(1);
        assertThat(apiResponse.getData().getContent().get(0).getStationName()).isEqualTo("测试工位ABC");
    }

    @Test
    @DisplayName("分页查询工位 - 无匹配结果")
    void getStationsPage_NoResults() throws Exception {
        String response = mockMvc.perform(get("/api/test-stations/page")
                        .param("page", "1")
                        .param("size", "10")
                        .param("stationName", "不存在的名称")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ApiResponse<PageResult<TestStationResponse>> apiResponse = objectMapper.readValue(
                response, new TypeReference<>() {}
        );
        assertThat(apiResponse.getCode()).isEqualTo(200);
        assertThat(apiResponse.getData().getContent()).isEmpty();
    }

    @Test
    @DisplayName("根据ID查询工位 - 成功")
    void getStationById_Success() throws Exception {
        int stationNo = nextStationNo();
        TestStation station = createTestStation(stationNo, "查询工位");
        TestStation saved = stationRepository.save(station);

        String response = mockMvc.perform(get("/api/test-stations/" + saved.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ApiResponse<TestStationResponse> apiResponse = objectMapper.readValue(
                response, new TypeReference<>() {}
        );
        assertThat(apiResponse.getCode()).isEqualTo(200);
        assertThat(apiResponse.getData().getId()).isEqualTo(saved.getId());
        assertThat(apiResponse.getData().getStationName()).isEqualTo("查询工位");
    }

    @Test
    @DisplayName("根据ID查询工位 - 不存在")
    void getStationById_NotFound() throws Exception {
        String response = mockMvc.perform(get("/api/test-stations/99999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ApiResponse<?> apiResponse = objectMapper.readValue(
                response, new TypeReference<>() {}
        );
        assertThat(apiResponse.getCode()).isEqualTo(40400);
    }

    @Test
    @DisplayName("创建工位 - 成功")
    void createStation_Success() throws Exception {
        CreateTestStationRequest request = new CreateTestStationRequest();
        request.setStationNo(nextStationNo());
        request.setStationName("新工位");
        request.setUsage(TestStationUsage.INHOUSE_TEST);
        request.setValveCommType(ValveCommType.SERIAL_MODBUS);
        request.setResponsiblePerson("张三");
        request.setEnabled(true);
        request.setValveControlParams(List.of(new TestStationParameterRequest("key", "value")));
        request.setOilValveMapping(List.of(new TestStationParameterRequest("oil", "valve")));

        String requestBody = objectMapper.writeValueAsString(request);
        String response = mockMvc.perform(post("/api/test-stations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ApiResponse<TestStationResponse> apiResponse = objectMapper.readValue(
                response, new TypeReference<>() {}
        );
        assertThat(apiResponse.getCode()).isEqualTo(200);
        assertThat(apiResponse.getData().getStationName()).isEqualTo("新工位");

        // 验证数据库
        List<TestStation> stations = stationRepository.findAll();
        assertThat(stations).hasSize(1);
    }

    @Test
    @DisplayName("创建工位 - 工位编号已存在")
    void createStation_DuplicateStationNo() throws Exception {
        int stationNo = nextStationNo();
        TestStation existing = createTestStation(stationNo, "已存在工位");
        stationRepository.save(existing);

        CreateTestStationRequest request = new CreateTestStationRequest();
        request.setStationNo(stationNo); // 使用相同编号
        request.setStationName("新工位");
        request.setUsage(TestStationUsage.INHOUSE_TEST);
        request.setValveCommType(ValveCommType.SERIAL_MODBUS);
        request.setResponsiblePerson("张三");
        request.setEnabled(true);
        request.setValveControlParams(List.of(new TestStationParameterRequest("key", "value")));
        request.setOilValveMapping(List.of(new TestStationParameterRequest("oil", "valve")));

        String requestBody = objectMapper.writeValueAsString(request);

        String response = mockMvc.perform(post("/api/test-stations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ApiResponse<?> apiResponse = objectMapper.readValue(
                response, new TypeReference<>() {}
        );
        assertThat(apiResponse.getCode()).isEqualTo(40000);

        assertThat(stationRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("更新工位 - 成功")
    void updateStation_Success() throws Exception {
        TestStation station = createTestStation(nextStationNo(), "原名称");
        TestStation saved = stationRepository.save(station);

        UpdateTestStationRequest updateRequest = new UpdateTestStationRequest();
        updateRequest.setStationName("新名称");
        updateRequest.setResponsiblePerson("新责任人");

        String requestBody = objectMapper.writeValueAsString(updateRequest);
        String response = mockMvc.perform(put("/api/test-stations/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ApiResponse<TestStationResponse> apiResponse = objectMapper.readValue(
                response, new TypeReference<>() {}
        );
        assertThat(apiResponse.getCode()).isEqualTo(200);
        assertThat(apiResponse.getData().getStationName()).isEqualTo("新名称");

        TestStation updated = stationRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getStationName()).isEqualTo("新名称");
    }

    @Test
    @DisplayName("更新工位 - 更改工位编号")
    void updateStation_ChangeStationNo() throws Exception {
        TestStation station = createTestStation(nextStationNo(), "工位");
        TestStation saved = stationRepository.save(station);
        int newStationNo = nextStationNo();

        UpdateTestStationRequest updateRequest = new UpdateTestStationRequest();
        updateRequest.setStationNo(newStationNo);

        String requestBody = objectMapper.writeValueAsString(updateRequest);
        String response = mockMvc.perform(put("/api/test-stations/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ApiResponse<TestStationResponse> apiResponse = objectMapper.readValue(
                response, new TypeReference<>() {}
        );
        assertThat(apiResponse.getCode()).isEqualTo(200);

        TestStation updated = stationRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getStationNo()).isEqualTo(newStationNo);
    }

    @Test
    @DisplayName("更新工位 - 新编号重复")
    void updateStation_DuplicateNewStationNo() throws Exception {
        int stationNo1 = nextStationNo();
        int stationNo2 = nextStationNo();

        TestStation station1 = createTestStation(stationNo1, "工位1");
        stationRepository.save(station1);
        TestStation station2 = createTestStation(stationNo2, "工位2");
        stationRepository.save(station2);

        UpdateTestStationRequest updateRequest = new UpdateTestStationRequest();
        updateRequest.setStationNo(stationNo2); // 重复的编号

        String requestBody = objectMapper.writeValueAsString(updateRequest);
        String response = mockMvc.perform(put("/api/test-stations/" + station1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ApiResponse<?> apiResponse = objectMapper.readValue(
                response, new TypeReference<>() {}
        );
        assertThat(apiResponse.getCode()).isEqualTo(40000);
    }

    @Test
    @DisplayName("删除工位 - 成功")
    void deleteStation_Success() throws Exception {
        TestStation station = createTestStation(nextStationNo(), "待删除工位");
        TestStation saved = stationRepository.save(station);
        Long idToDelete = saved.getId();

        String response = mockMvc.perform(delete("/api/test-stations/" + idToDelete)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ApiResponse<?> apiResponse = objectMapper.readValue(
                response, new TypeReference<>() {}
        );
        assertThat(apiResponse.getCode()).isEqualTo(200);

        // 验证软删除
        assertThat(stationRepository.findById(idToDelete)).isEmpty();
        assertThat(stationRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("验证工位编号唯一性 - 是唯一的")
    void validateStationNoUnique_Unique() throws Exception {
        int uniqueNo = nextStationNo();

        String response = mockMvc.perform(get("/api/test-stations/validate-station-no/" + uniqueNo)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ApiResponse<Boolean> apiResponse = objectMapper.readValue(
                response, new TypeReference<>() {}
        );
        assertThat(apiResponse.getCode()).isEqualTo(200);
        assertThat(apiResponse.getData()).isTrue();
    }

    @Test
    @DisplayName("验证工位编号唯一性 - 已存在")
    void validateStationNoUnique_Duplicate() throws Exception {
        int stationNo = nextStationNo();
        TestStation station = createTestStation(stationNo, "工位");
        stationRepository.save(station);

        String response = mockMvc.perform(get("/api/test-stations/validate-station-no/" + stationNo)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())  // Should throw BadRequestException
                .andReturn()
                .getResponse()
                .getContentAsString();

        ApiResponse<?> apiResponse = objectMapper.readValue(
                response, new TypeReference<>() {}
        );
        assertThat(apiResponse.getCode()).isEqualTo(40000);
    }

    @Test
    @DisplayName("完整工作流测试")
    void completeWorkflowTest() throws Exception {
        int stationNo = nextStationNo();

        // 1. 创建工位
        CreateTestStationRequest createRequest = new CreateTestStationRequest();
        createRequest.setStationNo(stationNo);
        createRequest.setStationName("工作流工位");
        createRequest.setUsage(TestStationUsage.INHOUSE_TEST);
        createRequest.setValveCommType(ValveCommType.SERIAL_MODBUS);
        createRequest.setResponsiblePerson("测试员");
        createRequest.setEnabled(true);
        createRequest.setValveControlParams(List.of(new TestStationParameterRequest("key", "value")));
        createRequest.setOilValveMapping(List.of(new TestStationParameterRequest("oil", "valve")));

        String createJson = objectMapper.writeValueAsString(createRequest);
        String createResponse = mockMvc.perform(post("/api/test-stations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ApiResponse<TestStationResponse> createApiResp = objectMapper.readValue(
                createResponse, new TypeReference<>() {}
        );
        Long createdId = createApiResp.getData().getId();

        // 2. 查询创建的工位
        String getResponse = mockMvc.perform(get("/api/test-stations/" + createdId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ApiResponse<TestStationResponse> getApiResp = objectMapper.readValue(
                getResponse, new TypeReference<>() {}
        );
        assertThat(getApiResp.getData().getStationNo()).isEqualTo(stationNo);

        // 3. 更新工位
        UpdateTestStationRequest updateRequest = new UpdateTestStationRequest();
        updateRequest.setStationName("工作流工位-更新后");
        String updateJson = objectMapper.writeValueAsString(updateRequest);

        String updateResponse = mockMvc.perform(put("/api/test-stations/" + createdId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ApiResponse<TestStationResponse> updateApiResp = objectMapper.readValue(
                updateResponse, new TypeReference<>() {}
        );
        assertThat(updateApiResp.getData().getStationName()).isEqualTo("工作流工位-更新后");

        // 4. 分页查询验证更新
        String pageResponse = mockMvc.perform(get("/api/test-stations/page")
                        .param("page", "1")
                        .param("size", "10")
                        .param("stationName", "工作流"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ApiResponse<PageResult<TestStationResponse>> pageApiResp = objectMapper.readValue(
                pageResponse, new TypeReference<>() {}
        );
        assertThat(pageApiResp.getData().getContent()).hasSize(1);
        assertThat(pageApiResp.getData().getContent().get(0).getStationName()).isEqualTo("工作流工位-更新后");

        // 5. 删除工位
        mockMvc.perform(delete("/api/test-stations/" + createdId))
                .andExpect(status().isOk());

        // 6. 验证已被删除
        mockMvc.perform(get("/api/test-stations/" + createdId))
                .andExpect(status().isNotFound());
    }

    // Helper method to create test stations
    private TestStation createTestStation(int stationNo, String stationName) {
        TestStation station = new TestStation();
        station.setStationNo(stationNo);
        station.setStationName(stationName);
        station.setUsage(TestStationUsage.INHOUSE_TEST);
        station.setValveCommType(ValveCommType.SERIAL_MODBUS);
        station.setResponsiblePerson("测试员");
        station.setEnabled(true);
        station.setValveControlParams(new java.util.ArrayList<>(List.of(
                new com.yimusi.entity.TestStationParameter("key", "value")
        )));
        station.setOilValveMapping(new java.util.ArrayList<>(List.of(
                new com.yimusi.entity.TestStationParameter("oil", "valve")
        )));
        return station;
    }
}
