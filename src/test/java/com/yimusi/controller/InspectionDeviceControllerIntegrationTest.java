package com.yimusi.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yimusi.BaseIntegrationTest;
import com.yimusi.common.model.ApiResponse;
import com.yimusi.config.TestAuditorConfig;
import com.yimusi.dto.common.PageResult;
import com.yimusi.dto.inspection.CreateInspectionDeviceRequest;
import com.yimusi.dto.inspection.InspectionDeviceResponse;
import com.yimusi.dto.inspection.UpdateInspectionDeviceRequest;
import com.yimusi.entity.InspectionDevice;
import com.yimusi.entity.Project;
import com.yimusi.enums.InspectionDeviceStatus;
import com.yimusi.repository.InspectionDeviceRepository;
import com.yimusi.repository.ProjectRepository;
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
@DisplayName("检测设备控制器集成测试")
public class InspectionDeviceControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InspectionDeviceRepository deviceRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @BeforeEach
    void setup() {
        deviceRepository.deleteAll();
        projectRepository.deleteAll();
        TestAuditorConfig.clearAuditor();
    }

    @AfterEach
    void cleanAuditor() {
        TestAuditorConfig.clearAuditor();
    }

    @Test
    @DisplayName("创建检测设备")
    void shouldCreateDevice() throws Exception {
        TestAuditorConfig.setAuditor(1L);

        Project project = seedProject("PROJ-001");

        CreateInspectionDeviceRequest request = new CreateInspectionDeviceRequest();
        request.setSerialNumber("SN-001");
        request.setDeviceModel("MODEL-A");
        request.setIp("192.168.1.10");
        request.setPort(102);
        request.setProjectId(project.getId());
        request.setStatus(InspectionDeviceStatus.PENDING_INSPECTION);

        String response = mockMvc
            .perform(
                post("/api/inspection-devices")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        ApiResponse<InspectionDeviceResponse> apiResponse =
            objectMapper.readValue(response, new TypeReference<>() {});
        InspectionDeviceResponse data = apiResponse.getData();

        assertThat(data.getSerialNumber()).isEqualTo("SN-001");
        assertThat(data.getProjectInternalNo()).isEqualTo(1);
        assertThat(data.getDeviceNo()).isNotNull();
    }

    @Test
    @DisplayName("更新检测设备")
    void shouldUpdateDevice() throws Exception {
        TestAuditorConfig.setAuditor(1L);
        InspectionDevice device = seedDevice("SN-001", "192.168.1.10");

        UpdateInspectionDeviceRequest request = new UpdateInspectionDeviceRequest();
        request.setDeviceModel("MODEL-B");
        request.setPort(202);
        request.setStatus(InspectionDeviceStatus.CALIBRATED);

        String response = mockMvc
            .perform(
                put("/api/inspection-devices/" + device.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        ApiResponse<InspectionDeviceResponse> apiResponse =
            objectMapper.readValue(response, new TypeReference<>() {});
        InspectionDeviceResponse data = apiResponse.getData();

        assertThat(data.getDeviceModel()).isEqualTo("MODEL-B");
        assertThat(data.getPort()).isEqualTo(202);
        assertThat(data.getStatus()).isEqualTo(InspectionDeviceStatus.CALIBRATED);
    }

    @Test
    @DisplayName("删除检测设备")
    void shouldDeleteDevice() throws Exception {
        TestAuditorConfig.setAuditor(1L);
        InspectionDevice device = seedDevice("SN-001", "192.168.1.10");

        mockMvc
            .perform(delete("/api/inspection-devices/" + device.getId()))
            .andExpect(status().isOk());

        assertThat(deviceRepository.findById(device.getId())).isEmpty();
    }

    @Test
    @DisplayName("恢复检测设备")
    void shouldRestoreDevice() throws Exception {
        TestAuditorConfig.setAuditor(1L);
        InspectionDevice device = seedDevice("SN-001", "192.168.1.10");
        deviceRepository.deleteById(device.getId());

        mockMvc
            .perform(post("/api/inspection-devices/" + device.getId() + "/restore"))
            .andExpect(status().isOk());

        assertThat(deviceRepository.findById(device.getId())).isPresent();
    }

    @Test
    @DisplayName("分页查询检测设备")
    void shouldGetDevicesPage() throws Exception {
        seedDevice("SN-001", "192.168.1.10");
        seedDevice("SN-002", "192.168.1.11");

        String response = mockMvc
            .perform(get("/api/inspection-devices/page").param("page", "1").param("size", "10"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        ApiResponse<PageResult<InspectionDeviceResponse>> apiResponse =
            objectMapper.readValue(response, new TypeReference<>() {});

        assertThat(apiResponse.getData().getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("验证出厂编号唯一性 - 存在时抛出异常")
    void shouldThrowExceptionWhenSerialNumberExists() throws Exception {
        seedDevice("SN-001", "192.168.1.10");

        mockMvc
            .perform(get("/api/inspection-devices/validate-serial-number/SN-001"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("验证出厂编号唯一性 - 不存在时返回True")
    void shouldReturnTrueWhenSerialNumberNotExists() throws Exception {
        mockMvc
            .perform(get("/api/inspection-devices/validate-serial-number/SN-NEW"))
            .andExpect(status().isOk())
            .andExpect(result -> {
                String content = result.getResponse().getContentAsString();
                ApiResponse<Boolean> apiResponse = objectMapper.readValue(content, new TypeReference<>() {});
                assertThat(apiResponse.getData()).isTrue();
            });
    }

    private Project seedProject(String projectNo) {
        Project project = new Project();
        project.setProjectNo(projectNo);
        project.setProjectName("Test Project");
        return projectRepository.save(project);
    }

    private InspectionDevice seedDevice(String serialNumber, String ip) {
        InspectionDevice device = new InspectionDevice();
        device.setDeviceNo("IND-" + serialNumber);
        device.setSerialNumber(serialNumber);
        device.setIp(ip);
        device.setPort(102);
        device.setStatus(InspectionDeviceStatus.PENDING_INSPECTION);
        return deviceRepository.save(device);
    }
}
