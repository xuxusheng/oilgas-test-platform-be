package com.yimusi.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yimusi.BaseIntegrationTest;
import com.yimusi.common.model.ApiResponse;
import com.yimusi.config.TestAuditorConfig;
import com.yimusi.dto.common.PageResult;
import com.yimusi.dto.oilsample.CreateOilSampleRequest;
import com.yimusi.dto.oilsample.OilSampleResponse;
import com.yimusi.dto.oilsample.UpdateOilSampleRequest;
import com.yimusi.enums.OilSampleStatus;
import com.yimusi.enums.OilSampleUsage;
import com.yimusi.repository.OilSampleRepository;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@DisplayName("油样控制器集成测试")
public class OilSampleControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OilSampleRepository oilSampleRepository;

    @BeforeEach
    void setup() {
        oilSampleRepository.deleteAll();
        TestAuditorConfig.clearAuditor();
    }

    @AfterEach
    void cleanAuditor() {
        TestAuditorConfig.clearAuditor();
    }

    @Test
    @DisplayName("创建油样")
    void shouldCreateOilSample() throws Exception {
        TestAuditorConfig.setAuditor(1L);

        CreateOilSampleRequest request = new CreateOilSampleRequest();
        request.setSampleNo("SAMPLE-001");
        request.setSampleName("Sample A");
        request.setUsage(OilSampleUsage.CLEANING);
        request.setCylinderNo(1001);
        request.setStatus(OilSampleStatus.ENABLED);

        String response = mockMvc
            .perform(
                post("/api/oil-samples")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        ApiResponse<OilSampleResponse> apiResponse = objectMapper.readValue(response, new TypeReference<>() {});
        OilSampleResponse data = apiResponse.getData();

        assertThat(data.getId()).isNotNull();
        assertThat(data.getSampleNo()).isEqualTo("SAMPLE-001");
        assertThat(data.getParameters()).isNotNull();
        assertThat(data.getParameters()).isEmpty();
    }

    @Test
    @DisplayName("更新油样")
    void shouldUpdateOilSample() throws Exception {
        TestAuditorConfig.setAuditor(1L);
        Long id = seedOilSample("SAMPLE-001");

        UpdateOilSampleRequest request = new UpdateOilSampleRequest();
        request.setSampleNo("SAMPLE-001-UPDATED");
        request.setSampleName("Sample A Updated");
        request.setUsage(OilSampleUsage.CALIBRATION);
        request.setCylinderNo(1002);
        request.setStatus(OilSampleStatus.DISABLED);

        mockMvc
            .perform(
                put("/api/oil-samples/" + id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isOk());

        String getResponse = mockMvc
            .perform(get("/api/oil-samples/" + id))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        ApiResponse<OilSampleResponse> apiResponse = objectMapper.readValue(getResponse, new TypeReference<>() {});
        assertThat(apiResponse.getData().getSampleNo()).isEqualTo("SAMPLE-001-UPDATED");
        assertThat(apiResponse.getData().getCylinderNo()).isEqualTo(1002);
        assertThat(apiResponse.getData().getStatus()).isEqualTo(OilSampleStatus.DISABLED);
        assertThat(apiResponse.getData().getUsage()).isEqualTo(OilSampleUsage.CALIBRATION);
    }

    @Test
    @DisplayName("删除油样")
    void shouldDeleteOilSample() throws Exception {
        TestAuditorConfig.setAuditor(1L);
        Long id = seedOilSample("SAMPLE-001");

        mockMvc.perform(delete("/api/oil-samples/" + id)).andExpect(status().isOk());

        mockMvc.perform(get("/api/oil-samples/" + id)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("分页查询油样")
    void shouldGetOilSamplesPage() throws Exception {
        TestAuditorConfig.setAuditor(1L);
        seedOilSample("SAMPLE-001");
        seedOilSample("SAMPLE-002");

        String response = mockMvc
            .perform(get("/api/oil-samples/page").param("page", "1").param("size", "10"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        ApiResponse<PageResult<OilSampleResponse>> apiResponse = objectMapper.readValue(response, new TypeReference<>() {});
        assertThat(apiResponse.getData().getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("校验油样编号唯一性")
    void shouldValidateSampleNoUnique() throws Exception {
        TestAuditorConfig.setAuditor(1L);
        seedOilSample("SAMPLE-001");

        mockMvc
            .perform(get("/api/oil-samples/validate-unique/SAMPLE-001"))
            .andExpect(status().isOk())
            .andExpect(result -> {
                String content = result.getResponse().getContentAsString();
                ApiResponse<Boolean> apiResponse = objectMapper.readValue(content, new TypeReference<>() {});
                assertThat(apiResponse.getData()).isFalse();
            });

        mockMvc
            .perform(get("/api/oil-samples/validate-unique/SAMPLE-NEW"))
            .andExpect(status().isOk())
            .andExpect(result -> {
                String content = result.getResponse().getContentAsString();
                ApiResponse<Boolean> apiResponse = objectMapper.readValue(content, new TypeReference<>() {});
                assertThat(apiResponse.getData()).isTrue();
            });
    }

    private Long seedOilSample(String sampleNo) throws Exception {
        CreateOilSampleRequest request = new CreateOilSampleRequest();
        request.setSampleNo(sampleNo);
        request.setSampleName("Sample");
        request.setUsage(OilSampleUsage.CLEANING);
        request.setCylinderNo(1001);
        request.setStatus(OilSampleStatus.ENABLED);

        String response = mockMvc
            .perform(
                post("/api/oil-samples")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        ApiResponse<OilSampleResponse> apiResponse = objectMapper.readValue(response, new TypeReference<>() {});
        return apiResponse.getData().getId();
    }
}

