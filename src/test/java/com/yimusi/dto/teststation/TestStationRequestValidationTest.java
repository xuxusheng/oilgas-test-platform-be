package com.yimusi.dto.teststation;

import com.yimusi.dto.teststation.parameter.TestStationParameterRequest;
import com.yimusi.enums.TestStationUsage;
import com.yimusi.enums.ValveCommType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试工位请求对象验证测试
 *
 * 测试 Bean Validation 注解的正确性
 */
@DisplayName("测试工位请求对象验证测试")
class TestStationRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ==================== CreateTestStationRequest 验证 ====================

    @Test
    @DisplayName("CreateTestStationRequest - 完整有效数据")
    void createRequest_Valid() {
        CreateTestStationRequest request = new CreateTestStationRequest();
        request.setStationNo(1001);
        request.setStationName("有效工位");
        request.setUsage(TestStationUsage.INHOUSE_TEST);
        request.setValveCommType(ValveCommType.SERIAL_MODBUS);
        request.setResponsiblePerson("张三");
        request.setEnabled(true);
        request.setValveControlParams(List.of(new TestStationParameterRequest("key", "value")));
        request.setOilValveMapping(List.of(new TestStationParameterRequest("oil", "valve")));

        Set<ConstraintViolation<CreateTestStationRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "不应有验证错误");
    }

    @Test
    @DisplayName("CreateTestStationRequest - 工位编号为空")
    void createRequest_NullStationNo() {
        CreateTestStationRequest request = new CreateTestStationRequest();
        request.setStationNo(null);
        request.setStationName("有效工位");
        request.setUsage(TestStationUsage.INHOUSE_TEST);
        request.setValveCommType(ValveCommType.SERIAL_MODBUS);
        request.setResponsiblePerson("张三");
        request.setEnabled(true);
        request.setValveControlParams(List.of(new TestStationParameterRequest("key", "value")));
        request.setOilValveMapping(List.of(new TestStationParameterRequest("oil", "valve")));

        Set<ConstraintViolation<CreateTestStationRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("工位编号不能为空")));
    }

    @Test
    @DisplayName("CreateTestStationRequest - 工位名称为空")
    void createRequest_BlankStationName() {
        CreateTestStationRequest request = new CreateTestStationRequest();
        request.setStationNo(1001);
        request.setStationName("");
        request.setUsage(TestStationUsage.INHOUSE_TEST);
        request.setValveCommType(ValveCommType.SERIAL_MODBUS);
        request.setResponsiblePerson("张三");
        request.setEnabled(true);
        request.setValveControlParams(List.of(new TestStationParameterRequest("key", "value")));
        request.setOilValveMapping(List.of(new TestStationParameterRequest("oil", "valve")));

        Set<ConstraintViolation<CreateTestStationRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("工位名称不能为空")));
    }

    @Test
    @DisplayName("CreateTestStationRequest - 工位名称过长")
    void createRequest_StationNameTooLong() {
        CreateTestStationRequest request = new CreateTestStationRequest();
        request.setStationNo(1001);
        request.setStationName("a".repeat(101)); // 101个字符，超过100限制
        request.setUsage(TestStationUsage.INHOUSE_TEST);
        request.setValveCommType(ValveCommType.SERIAL_MODBUS);
        request.setResponsiblePerson("张三");
        request.setEnabled(true);
        request.setValveControlParams(List.of(new TestStationParameterRequest("key", "value")));
        request.setOilValveMapping(List.of(new TestStationParameterRequest("oil", "valve")));

        Set<ConstraintViolation<CreateTestStationRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("长度不能超过 100")));
    }

    @Test
    @DisplayName("CreateTestStationRequest - 用途为空")
    void createRequest_NullUsage() {
        CreateTestStationRequest request = new CreateTestStationRequest();
        request.setStationNo(1001);
        request.setStationName("有效工位");
        request.setUsage(null);
        request.setValveCommType(ValveCommType.SERIAL_MODBUS);
        request.setResponsiblePerson("张三");
        request.setEnabled(true);
        request.setValveControlParams(List.of(new TestStationParameterRequest("key", "value")));
        request.setOilValveMapping(List.of(new TestStationParameterRequest("oil", "valve")));

        Set<ConstraintViolation<CreateTestStationRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("工位用途不能为空")));
    }

    @Test
    @DisplayName("CreateTestStationRequest - 电磁阀通信类型为空")
    void createRequest_NullValveCommType() {
        CreateTestStationRequest request = new CreateTestStationRequest();
        request.setStationNo(1001);
        request.setStationName("有效工位");
        request.setUsage(TestStationUsage.INHOUSE_TEST);
        request.setValveCommType(null);
        request.setResponsiblePerson("张三");
        request.setEnabled(true);
        request.setValveControlParams(List.of(new TestStationParameterRequest("key", "value")));
        request.setOilValveMapping(List.of(new TestStationParameterRequest("oil", "valve")));

        Set<ConstraintViolation<CreateTestStationRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("电磁阀通信类型不能为空")));
    }

    @Test
    @DisplayName("CreateTestStationRequest - 电磁阀控制参数为空")
    void createRequest_NullValveControlParams() {
        CreateTestStationRequest request = new CreateTestStationRequest();
        request.setStationNo(1001);
        request.setStationName("有效工位");
        request.setUsage(TestStationUsage.INHOUSE_TEST);
        request.setValveCommType(ValveCommType.SERIAL_MODBUS);
        request.setResponsiblePerson("张三");
        request.setEnabled(true);
        request.setValveControlParams(null);
        request.setOilValveMapping(List.of(new TestStationParameterRequest("oil", "valve")));

        Set<ConstraintViolation<CreateTestStationRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("电磁阀控制参数不能为空")));
    }

    @Test
    @DisplayName("CreateTestStationRequest - 电磁阀控制参数为空列表")
    void createRequest_EmptyValveControlParams() {
        CreateTestStationRequest request = new CreateTestStationRequest();
        request.setStationNo(1001);
        request.setStationName("有效工位");
        request.setUsage(TestStationUsage.INHOUSE_TEST);
        request.setValveCommType(ValveCommType.SERIAL_MODBUS);
        request.setResponsiblePerson("张三");
        request.setEnabled(true);
        request.setValveControlParams(new ArrayList<>());
        request.setOilValveMapping(List.of(new TestStationParameterRequest("oil", "valve")));

        Set<ConstraintViolation<CreateTestStationRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("电磁阀控制参数不能为空")));
    }

    @Test
    @DisplayName("CreateTestStationRequest - 油阀映射为空")
    void createRequest_NullOilValveMapping() {
        CreateTestStationRequest request = new CreateTestStationRequest();
        request.setStationNo(1001);
        request.setStationName("有效工位");
        request.setUsage(TestStationUsage.INHOUSE_TEST);
        request.setValveCommType(ValveCommType.SERIAL_MODBUS);
        request.setResponsiblePerson("张三");
        request.setEnabled(true);
        request.setValveControlParams(List.of(new TestStationParameterRequest("key", "value")));
        request.setOilValveMapping(null);

        Set<ConstraintViolation<CreateTestStationRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("油-阀对应关系不能为空")));
    }

    @Test
    @DisplayName("CreateTestStationRequest - 油阀映射为空列表")
    void createRequest_EmptyOilValveMapping() {
        CreateTestStationRequest request = new CreateTestStationRequest();
        request.setStationNo(1001);
        request.setStationName("有效工位");
        request.setUsage(TestStationUsage.INHOUSE_TEST);
        request.setValveCommType(ValveCommType.SERIAL_MODBUS);
        request.setResponsiblePerson("张三");
        request.setEnabled(true);
        request.setValveControlParams(List.of(new TestStationParameterRequest("key", "value")));
        request.setOilValveMapping(new ArrayList<>());

        Set<ConstraintViolation<CreateTestStationRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("油-阀对应关系不能为空")));
    }

    @Test
    @DisplayName("CreateTestStationRequest - 责任人为空")
    void createRequest_NullResponsiblePerson() {
        CreateTestStationRequest request = new CreateTestStationRequest();
        request.setStationNo(1001);
        request.setStationName("有效工位");
        request.setUsage(TestStationUsage.INHOUSE_TEST);
        request.setValveCommType(ValveCommType.SERIAL_MODBUS);
        request.setResponsiblePerson(null);
        request.setEnabled(true);
        request.setValveControlParams(List.of(new TestStationParameterRequest("key", "value")));
        request.setOilValveMapping(List.of(new TestStationParameterRequest("oil", "valve")));

        Set<ConstraintViolation<CreateTestStationRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("责任人不能为空")));
    }

    @Test
    @DisplayName("CreateTestStationRequest - 责任人过长")
    void createRequest_ResponsiblePersonTooLong() {
        CreateTestStationRequest request = new CreateTestStationRequest();
        request.setStationNo(1001);
        request.setStationName("有效工位");
        request.setUsage(TestStationUsage.INHOUSE_TEST);
        request.setValveCommType(ValveCommType.SERIAL_MODBUS);
        request.setResponsiblePerson("a".repeat(51)); // 51个字符，超过50限制
        request.setEnabled(true);
        request.setValveControlParams(List.of(new TestStationParameterRequest("key", "value")));
        request.setOilValveMapping(List.of(new TestStationParameterRequest("oil", "valve")));

        Set<ConstraintViolation<CreateTestStationRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("长度不能超过 50")));
    }

    @Test
    @DisplayName("CreateTestStationRequest - 启用状态为空")
    void createRequest_NullEnabled() {
        CreateTestStationRequest request = new CreateTestStationRequest();
        request.setStationNo(1001);
        request.setStationName("有效工位");
        request.setUsage(TestStationUsage.INHOUSE_TEST);
        request.setValveCommType(ValveCommType.SERIAL_MODBUS);
        request.setResponsiblePerson("张三");
        request.setEnabled(null);
        request.setValveControlParams(List.of(new TestStationParameterRequest("key", "value")));
        request.setOilValveMapping(List.of(new TestStationParameterRequest("oil", "valve")));

        Set<ConstraintViolation<CreateTestStationRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("启用状态不能为空")));
    }

    @Test
    @DisplayName("CreateTestStationRequest - 工位名称空白字符")
    void createRequest_StationNameWhitespaceOnly() {
        CreateTestStationRequest request = new CreateTestStationRequest();
        request.setStationNo(1001);
        request.setStationName("   "); // 只有空白字符
        request.setUsage(TestStationUsage.INHOUSE_TEST);
        request.setValveCommType(ValveCommType.SERIAL_MODBUS);
        request.setResponsiblePerson("张三");
        request.setEnabled(true);
        request.setValveControlParams(List.of(new TestStationParameterRequest("key", "value")));
        request.setOilValveMapping(List.of(new TestStationParameterRequest("oil", "valve")));

        Set<ConstraintViolation<CreateTestStationRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty()); // @NotBlank 会拒绝纯空白字符
    }

    // ==================== UpdateTestStationRequest 验证 ====================

    @Test
    @DisplayName("UpdateTestStationRequest - 完整有效数据")
    void updateRequest_Valid() {
        UpdateTestStationRequest request = new UpdateTestStationRequest();
        request.setStationNo(1002);
        request.setStationName("更新工位");
        request.setUsage(TestStationUsage.RND_TEST);
        request.setValveCommType(ValveCommType.TCP_MODBUS);
        request.setResponsiblePerson("李四");
        request.setEnabled(false);
        request.setValveControlParams(List.of(new TestStationParameterRequest("newKey", "newValue")));
        request.setOilValveMapping(List.of(new TestStationParameterRequest("newOil", "newValve")));

        Set<ConstraintViolation<UpdateTestStationRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "不应有验证错误");
    }

    @Test
    @DisplayName("UpdateTestStationRequest - 所有字段可选")
    void updateRequest_AllFieldsOptional() {
        UpdateTestStationRequest request = new UpdateTestStationRequest();
        // 不设置任何字段，应该也有效

        Set<ConstraintViolation<UpdateTestStationRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "更新请求的所有字段都应该是可选的");
    }

    @Test
    @DisplayName("UpdateTestStationRequest - 工位名称过长")
    void updateRequest_StationNameTooLong() {
        UpdateTestStationRequest request = new UpdateTestStationRequest();
        request.setStationName("a".repeat(101));

        Set<ConstraintViolation<UpdateTestStationRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("长度不能超过 100")));
    }

    @Test
    @DisplayName("UpdateTestStationRequest - 责任人过长")
    void updateRequest_ResponsiblePersonTooLong() {
        UpdateTestStationRequest request = new UpdateTestStationRequest();
        request.setResponsiblePerson("a".repeat(51));

        Set<ConstraintViolation<UpdateTestStationRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("长度不能超过 50")));
    }

    @Test
    @DisplayName("UpdateTestStationRequest - 空字符串工位名称")
    void updateRequest_EmptyStationName() {
        UpdateTestStationRequest request = new UpdateTestStationRequest();
        request.setStationName("");

        // 空字符串对于 @Size 来说是有效的，但如果业务需要，应该在 Service 层验证
        Set<ConstraintViolation<UpdateTestStationRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    // ==================== TestStationPageRequest 验证 ====================

    @Test
    @DisplayName("TestStationPageRequest - 完整有效数据")
    void pageRequest_Valid() {
        TestStationPageRequest request = new TestStationPageRequest();
        request.setPage(1);  // 注意：继承的是page，不是pageNum
        request.setSize(10); // 注意：继承的是size，不是pageSize
        request.setStationNo(1001);
        request.setStationName("测试");
        request.setUsage(TestStationUsage.INHOUSE_TEST);
        request.setValveCommType(ValveCommType.SERIAL_MODBUS);
        request.setResponsiblePerson("张三");
        request.setEnabled(true);

        Set<ConstraintViolation<TestStationPageRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "不应有验证错误");
    }

    @Test
    @DisplayName("TestStationPageRequest - 最小值页码和大小")
    void pageRequest_MinimumValues() {
        TestStationPageRequest request = new TestStationPageRequest();
        request.setPage(1);
        request.setSize(1);
        // 其他字段可以为空

        Set<ConstraintViolation<TestStationPageRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("TestStationPageRequest - 页码小于最小值")
    void pageRequest_PageNumTooSmall() {
        TestStationPageRequest request = new TestStationPageRequest();
        request.setPage(0);
        request.setSize(10);

        Set<ConstraintViolation<TestStationPageRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("TestStationPageRequest - 每页大小小于最小值")
    void pageRequestPageSizeTooSmall() {
        TestStationPageRequest request = new TestStationPageRequest();
        request.setPage(1);
        request.setSize(0);

        Set<ConstraintViolation<TestStationPageRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("TestStationPageRequest - 每页大小过大")
    void pageRequestPageSizeTooLarge() {
        TestStationPageRequest request = new TestStationPageRequest();
        request.setPage(1);
        request.setSize(10000);

        Set<ConstraintViolation<TestStationPageRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("TestStationPageRequest - 仅页码和大小")
    void pageRequest_MinimalPageParams() {
        TestStationPageRequest request = new TestStationPageRequest();
        request.setPage(1);
        request.setSize(20);
        // 其他筛选条件可以为空

        Set<ConstraintViolation<TestStationPageRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("TestStationPageRequest - 所有字段为空")
    void pageRequest_AllFieldsNull() {
        TestStationPageRequest request = new TestStationPageRequest();
        // 所有字段都不设置，使用默认值

        Set<ConstraintViolation<TestStationPageRequest>> violations = validator.validate(request);
        // 使用默认值应该有效
        assertTrue(violations.isEmpty());
    }

    // ==================== 边界情况测试 ====================

    @Test
    @DisplayName("CreateTestStationRequest - 边界值测试")
    void createRequest_BoundaryValues() {
        CreateTestStationRequest request = new CreateTestStationRequest();
        request.setStationNo(1);
        request.setStationName("a".repeat(100)); // 最大长度名称
        request.setUsage(TestStationUsage.INHOUSE_TEST);
        request.setValveCommType(ValveCommType.SERIAL_MODBUS);
        request.setResponsiblePerson("a".repeat(50)); // 最大长度责任人
        request.setEnabled(true);
        request.setValveControlParams(List.of(new TestStationParameterRequest("k", "v")));
        request.setOilValveMapping(List.of(new TestStationParameterRequest("o", "v")));

        Set<ConstraintViolation<CreateTestStationRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "边界值应该通过验证");
    }

    @Test
    @DisplayName("CreateTestStationRequest - 参数对象完整性验证")
    void createRequest_ParameterObjectsComplete() {
        CreateTestStationRequest request = new CreateTestStationRequest();
        request.setStationNo(1001);
        request.setStationName("工位");
        request.setUsage(TestStationUsage.INHOUSE_TEST);
        request.setValveCommType(ValveCommType.SERIAL_MODBUS);
        request.setResponsiblePerson("张三");
        request.setEnabled(true);

        // 参数对象本身没有约束，可以任意值
        request.setValveControlParams(List.of(new TestStationParameterRequest("", "")));
        request.setOilValveMapping(List.of(new TestStationParameterRequest("", "")));

        Set<ConstraintViolation<CreateTestStationRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "参数对象的字段没有约束");
    }
}
