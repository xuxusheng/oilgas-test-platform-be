# 油样管理 Service 层设计方案

## 1. 概述

本文档描述了油样管理模块（OilSample）Service 层的设计方案。该方案基于现有的 `OilSample` 实体类和项目代码规范（DTO 模式、Service 接口模式）进行设计，旨在满足查询筛选、新建、编辑、删除（含批量）等业务需求。

## 2. 现有实体分析

实体类 `com.yimusi.entity.OilSample` 已存在，且满足大部分需求：
- **软删除支持**：已继承 `SoftDeletableEntity` 并配置 `@SQLDelete` 和 `@SQLRestriction`。
- **参数存储**：使用 `List<OilSampleParameter>` 配合 `@JdbcTypeCode(SqlTypes.JSON)` 存储动态键值对，满足 "支持增减参数项" 的需求。
- **字段完整性**：包含 `sampleNo` (唯一标识), `usage`, `status`, `cylinderNo` 等核心字段。

## 3. DTO 设计

遵循项目规范，在 `com.yimusi.dto.oilsample` 包下创建以下 DTO：

### 3.1. 请求对象 (Request)

#### `CreateOilSampleRequest`
用于新建油样。
```java
@Data
public class CreateOilSampleRequest {
    @NotBlank(message = "油样编号不能为空")
    private String sampleNo;

    @NotBlank(message = "油样名称不能为空")
    private String sampleName;

    @NotNull(message = "用途不能为空")
    private OilSampleUsage usage;

    // 接收前端传来的参数列表，需校验 key 是否在允许范围内
    @Valid // 启用级联校验
    private List<ParameterItem> parameters;

    @NotNull(message = "油缸编号不能为空")
    private Integer cylinderNo;

    private Instant offlineTestedAt;
    private String offlineTestNo;

    @NotNull(message = "状态不能为空")
    private OilSampleStatus status;

    private String remark;

    /**
     * 内部类用于接收参数，与 Entity 解耦并支持校验
     */
    @Data
    public static class ParameterItem {
        @NotBlank(message = "参数名不能为空")
        @Pattern(regexp = "^(CH4|C2H2|C2H4|C2H6|H2|CO|CO2|H2O)$", message = "参数名必须是 CH4, C2H2, C2H4, C2H6, H2, CO, CO2, H2O 之一")
        private String key;

        @NotNull(message = "参数值不能为空")
        private BigDecimal value;
    }
}
```

#### `UpdateOilSampleRequest`
用于编辑油样，字段与 Create 类似，但通常不包含不可变的业务主键（如果 sampleNo 允许修改则包含）。根据需求 "油样编号需保持全局唯一"，编辑时若修改编号需做唯一性校验。
```java
@Data
public class UpdateOilSampleRequest {
    // 允许修改编号，但需校验唯一性
    @NotBlank
    private String sampleNo;

    @NotBlank
    private String sampleName;

    @NotNull
    private OilSampleUsage usage;

    @Valid
    private List<CreateOilSampleRequest.ParameterItem> parameters;

    @NotNull
    private Integer cylinderNo;

    private Instant offlineTestedAt;
    private String offlineTestNo;

    @NotNull
    private OilSampleStatus status;

    private String remark;
}
```

#### `OilSamplePageRequest`
用于查询筛选。
```java
@Data
@EqualsAndHashCode(callSuper = true)
public class OilSamplePageRequest extends PageRequest {
    // 筛选条件
    private String sampleNo;       // 模糊或精确查询
    private String sampleName;     // 模糊查询
    private OilSampleUsage usage;  // 下拉筛选
    private OilSampleStatus status;// 状态筛选
    private Integer cylinderNo;    // 油缸编号筛选
    // 可根据需要添加时间范围筛选
}
```

### 3.2. 响应对象 (Response)

#### `OilSampleResponse`
用于列表展示和详情返回。
```java
@Data
public class OilSampleResponse {
    private Long id;
    private String sampleNo;
    private String sampleName;
    private OilSampleUsage usage;
    private List<OilSampleParameter> parameters;
    private Integer cylinderNo;
    private Instant offlineTestedAt;
    private String offlineTestNo;
    private OilSampleStatus status;
    private String remark;
    private Instant createdAt;
    private Instant updatedAt;
}
```

## 4. Service 接口设计

在 `com.yimusi.service` 包下创建 `OilSampleService` 接口。

```java
public interface OilSampleService {

    /**
     * 分页查询油样列表
     */
    PageResult<OilSampleResponse> getOilSamplesPage(OilSamplePageRequest request);

    /**
     * 获取单条油样详情
     */
    OilSampleResponse getOilSampleById(Long id);

    /**
     * 创建油样
     * 业务逻辑：
     * 1. 校验 sampleNo 全局唯一
     * 2. 校验 parameters 中的 key 是否合法 (CH4, C2H2, etc.)
     */
    OilSampleResponse createOilSample(CreateOilSampleRequest request);

    /**
     * 更新油样
     * 业务逻辑：
     * 1. 检查 ID 是否存在
     * 2. 若修改了 sampleNo，需校验唯一性（排除自身）
     * 3. 校验 parameters key 合法性
     */
    OilSampleResponse updateOilSample(Long id, UpdateOilSampleRequest request);

    /**
     * 删除单条油样（软删除）
     */
    void deleteOilSample(Long id);

    /**
     * 批量删除油样（软删除）
     */
    void batchDeleteOilSamples(List<Long> ids);

    /**
     * 校验编号唯一性接口（可供前端异步调用或内部使用）
     */
    boolean validateSampleNoUnique(String sampleNo);
}
```

## 5. 核心业务逻辑细节

### 5.1. 参数 Key 校验
需求限定参数 Key 必须为：`CH4`, `C2H2`, `C2H4`, `C2H6`, `H2`, `CO`, `CO2`, `H2O`。
- **实现方式**：定义一个常量 Set 或 Enum 包含这些 Key。在 `create` 和 `update` 方法中，遍历传入的 `parameters` 列表，检查每个 Item 的 `key` 是否在允许列表中。如果不合法，抛出 `IllegalArgumentException` 或自定义业务异常。

### 5.2. 唯一性校验
- **SampleNo**：使用 Repository 的 `existsBySampleNo(String sampleNo)` (新建时) 和 `existsBySampleNoAndIdNot(String sampleNo, Long id)` (更新时) 进行校验。

### 5.3. 批量删除
- **实现方式**：
  - 接口接收 `List<Long> ids`。
  - 调用 Repository 的 `deleteAllById(ids)`。
  - 由于实体配置了 `@SQLDelete`，Hibernate 会自动为每个 ID 生成 `UPDATE ... SET deleted=true` 语句（或者批量 Update，取决于 JPA/Hibernate 版本和配置）。
  - **注意**：需确保传入的 ID 列表不为空。

### 5.4. 实体映射
- 使用 MapStruct 或手动转换将 DTO 转换为 Entity。
- **参数转换**：需将 Request 中的 `ParameterItem` 列表转换为 Entity 中的 `OilSampleParameter` 列表。

## 6. Repository 扩展

在 `OilSampleRepository` 中添加必要的方法：

```java
boolean existsBySampleNo(String sampleNo);
boolean existsBySampleNoAndIdNot(String sampleNo, Long id);
```
