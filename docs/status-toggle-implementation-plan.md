# 测试工位和油样管理状态切换接口实施方案

## 📋 文档概述

本文档描述了为测试工位（TestStation）和油样管理（OilSample）模块添加专用状态切换接口的完整实施方案。

---

## 🎯 问题背景

### 当前状态
- ✅ **功能可用**：通过 `PUT` 更新接口可修改状态字段
- ⚠️ **不够直观**：需要传递完整对象才能修改状态
- 📊 **对比分析**：两个模块都缺少专用的状态切换接口

### 改进目标
1. 提供直观、便捷的状态切换 API
2. 保持与现有代码风格的一致性
3. 支持三种操作模式：启用、禁用、切换
4. 统一两个模块的实现方式

---

## 📐 方案设计

### 设计原则

| 原则 | 说明 |
|------|------|
| **RESTful** | 使用 PATCH 方法，符合语义 |
| **一致性** | 两个模块使用相同的接口模式 |
| **简洁性** | 接口命名清晰，参数最少 |
| **可扩展** | 易于添加更多状态操作 |

### 接口模式对比

#### ❌ 当前方式（不推荐）
```http
PUT /api/test-stations/1
Content-Type: application/json

{
  "stationName": "工位A",
  "enabled": false,  // 需要传递其他字段
  ...其他字段
}
```

#### ✅ 推荐方式（新增）
```http
PATCH /api/test-stations/1/enable
// 或
PATCH /api/test-stations/1/disable
// 或
PATCH /api/test-stations/1/toggle
```

---

## 🔧 实现细节

### 模块一：测试工位管理（TestStation）

#### 1. Service 层新增方法

**文件**: `TestStationService.java`

```java
/**
 * 切换工位启用状态
 *
 * @param id 工位 ID
 * @param enabled true=启用, false=禁用, null=切换状态
 * @return 更新后的工位响应
 */
TestStationResponse toggleStationEnabled(Long id, Boolean enabled);
```

**文件**: `TestStationServiceImpl.java`

```java
@Override
public TestStationResponse toggleStationEnabled(Long id, Boolean enabled) {
    // 1. 验证 ID
    if (id == null) {
        throw new BadRequestException("工位 ID 不能为空");
    }

    // 2. 查询工位
    TestStation station = getStationById(id);

    // 3. 计算新状态
    Boolean newEnabled;
    if (enabled == null) {
        // 切换模式：反转当前状态
        newEnabled = !station.getEnabled();
    } else {
        // 指定模式：使用传入值
        newEnabled = enabled;
    }

    // 4. 更新状态
    station.setEnabled(newEnabled);
    TestStation saved = stationRepository.save(station);

    // 5. 记录日志
    log.info("工位状态变更: ID={}, 工位编号={}, 新状态={}",
        id, station.getStationNo(), newEnabled ? "启用" : "禁用");

    return stationMapper.toResponse(saved);
}
```

#### 2. Controller 层新增端点

**文件**: `TestStationController.java`

```java
/**
 * 启用测试工位
 *
 * @param id 工位 ID
 * @return 更新后的工位信息
 */
@PatchMapping("/{id}/enable")
public ApiResponse<TestStationResponse> enableStation(@PathVariable Long id) {
    TestStationResponse response = testStationService.toggleStationEnabled(id, true);
    return ApiResponse.success(response);
}

/**
 * 禁用测试工位
 *
 * @param id 工位 ID
 * @return 更新后的工位信息
 */
@PatchMapping("/{id}/disable")
public ApiResponse<TestStationResponse> disableStation(@PathVariable Long id) {
    TestStationResponse response = testStationService.toggleStationEnabled(id, false);
    return ApiResponse.success(response);
}

/**
 * 切换测试工位启用状态
 *
 * @param id 工位 ID
 * @return 更新后的工位信息
 */
@PatchMapping("/{id}/toggle")
public ApiResponse<TestStationResponse> toggleStation(@PathVariable Long id) {
    TestStationResponse response = testStationService.toggleStationEnabled(id, null);
    return ApiResponse.success(response);
}
```

---

### 模块二：油样管理（OilSample）

#### 1. Service 层新增方法

**文件**: `OilSampleService.java`

```java
/**
 * 切换油样状态
 *
 * @param id 油样 ID
 * @param status 目标状态, null 表示切换
 * @return 更新后的油样响应
 */
OilSampleResponse toggleOilSampleStatus(Long id, OilSampleStatus status);
```

**文件**: `OilSampleServiceImpl.java`

```java
@Override
public OilSampleResponse toggleOilSampleStatus(Long id, OilSampleStatus status) {
    // 1. 验证 ID
    if (id == null) {
        throw new BusinessException(ErrorCode.BAD_REQUEST, "油样 ID 不能为空");
    }

    // 2. 查询油样
    OilSample oilSample = oilSampleRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("OilSample not found with id: " + id));

    // 3. 计算新状态
    OilSampleStatus newStatus;
    if (status == null) {
        // 切换模式：反转当前状态
        newStatus = oilSample.getStatus() == OilSampleStatus.ENABLED
            ? OilSampleStatus.DISABLED
            : OilSampleStatus.ENABLED;
    } else {
        // 指定模式：使用传入值
        newStatus = status;
    }

    // 4. 更新状态
    oilSample.setStatus(newStatus);
    oilSample = oilSampleRepository.save(oilSample);

    // 5. 记录日志
    log.info("油样状态变更: ID={}, 油样编号={}, 新状态={}",
        id, oilSample.getSampleNo(), newStatus);

    return oilSampleMapper.toResponse(oilSample);
}
```

#### 2. Controller 层新增端点

**文件**: `OilSampleController.java`

```java
/**
 * 启用油样
 *
 * @param id 油样 ID
 * @return 更新后的油样信息
 */
@PatchMapping("/{id}/enable")
public ApiResponse<OilSampleResponse> enableOilSample(@PathVariable Long id) {
    OilSampleResponse response = oilSampleService.toggleOilSampleStatus(id, OilSampleStatus.ENABLED);
    return ApiResponse.success(response);
}

/**
 * 禁用油样
 *
 * @param id 油样 ID
 * @return 更新后的油样信息
 */
@PatchMapping("/{id}/disable")
public ApiResponse<OilSampleResponse> disableOilSample(@PathVariable Long id) {
    OilSampleResponse response = oilSampleService.toggleOilSampleStatus(id, OilSampleStatus.DISABLED);
    return ApiResponse.success(response);
}

/**
 * 切换油样状态
 *
 * @param id 油样 ID
 * @return 更新后的油样信息
 */
@PatchMapping("/{id}/toggle")
public ApiResponse<OilSampleResponse> toggleOilSample(@PathVariable Long id) {
    OilSampleResponse response = oilSampleService.toggleOilSampleStatus(id, null);
    return ApiResponse.success(response);
}
```

---

## 📊 API 接口清单

### 测试工位模块

| 接口路径 | 方法 | 功能 | 请求参数 | 返回类型 |
|---------|------|------|---------|---------|
| `/api/test-stations/{id}/enable` | PATCH | 启用工位 | `id` (路径) | `TestStationResponse` |
| `/api/test-stations/{id}/disable` | PATCH | 禁用工位 | `id` (路径) | `TestStationResponse` |
| `/api/test-stations/{id}/toggle` | PATCH | 切换工位状态 | `id` (路径) | `TestStationResponse` |

### 油样管理模块

| 接口路径 | 方法 | 功能 | 请求参数 | 返回类型 |
|---------|------|------|---------|---------|
| `/api/oil-samples/{id}/enable` | PATCH | 启用油样 | `id` (路径) | `OilSampleResponse` |
| `/api/oil-samples/{id}/disable` | PATCH | 禁用油样 | `id` (路径) | `OilSampleResponse` |
| `/api/oil-samples/{id}/toggle` | PATCH | 切换油样状态 | `id` (路径) | `OilSampleResponse` |

---

## 🔄 使用示例

### 场景 1：启用工位
```http
PATCH /api/test-stations/1/enable

# 响应
{
  "success": true,
  "data": {
    "id": 1,
    "stationNo": 101,
    "stationName": "测试工位A",
    "enabled": true,
    ...
  }
}
```

### 场景 2：禁用油样
```http
PATCH /api/oil-samples/5/disable

# 响应
{
  "success": true,
  "data": {
    "id": 5,
    "sampleNo": "OIL20250119001",
    "sampleName": "油样A",
    "status": "DISABLED",
    ...
  }
}
```

### 场景 3：切换状态
```http
PATCH /api/test-stations/2/toggle

# 响应（假设原为启用）
{
  "success": true,
  "data": {
    "id": 2,
    "enabled": false,  // 已切换为禁用
    ...
  }
}
```

---

## ⚙️ 技术实现要点

### 1. 事务管理
```java
@Transactional
public TestStationResponse toggleStationEnabled(Long id, Boolean enabled) {
    // 方法内部会自动提交事务
}
```

### 2. 异常处理
- ID 为空 → `BadRequestException`
- 记录不存在 → `ResourceNotFoundException`
- 统一由全局异常处理器处理

### 3. 日志记录
```java
log.info("工位状态变更: ID={}, 工位编号={}, 新状态={}",
    id, station.getStationNo(), newEnabled ? "启用" : "禁用");
```

### 4. 返回值
- 统一返回 `Response` DTO
- 包含完整的对象信息
- 便于前端更新状态显示

---

## 📝 代码变更清单

### 需要修改的文件

#### TestStation 模块
1. ✅ `TestStationService.java` - 新增接口方法
2. ✅ `TestStationServiceImpl.java` - 实现业务逻辑
3. ✅ `TestStationController.java` - 新增 3 个端点

#### OilSample 模块
1. ✅ `OilSampleService.java` - 新增接口方法
2. ✅ `OilSampleServiceImpl.java` - 实现业务逻辑
3. ✅ `OilSampleController.java` - 新增 3 个端点

### 需要新增的测试

#### 单元测试
1. `TestStationServiceImplTest.java`
   - `toggleStationEnabled_WithNull_ShouldToggle()`
   - `toggleStationEnabled_WithTrue_ShouldEnable()`
   - `toggleStationEnabled_WithFalse_ShouldDisable()`
   - `toggleStationEnabled_WithInvalidId_ShouldThrowException()`

2. `OilSampleServiceImplTest.java`
   - `toggleOilSampleStatus_WithNull_ShouldToggle()`
   - `toggleOilSampleStatus_WithEnabled_ShouldEnable()`
   - `toggleOilSampleStatus_WithDisabled_ShouldDisable()`
   - `toggleOilSampleStatus_WithInvalidId_ShouldThrowException()`

#### 集成测试
1. `TestStationControllerIntegrationTest.java`
   - 测试所有 3 个端点的 HTTP 调用

2. `OilSampleControllerIntegrationTest.java`
   - 测试所有 3 个端点的 HTTP 调用

---

## 🎨 设计优势

### 1. 用户体验
- ✅ **直观**：接口名称直接表达意图
- ✅ **便捷**：无需传递复杂请求体
- ✅ **灵活**：支持三种操作模式

### 2. 代码质量
- ✅ **一致性**：两个模块使用相同模式
- ✅ **可维护**：逻辑集中，易于修改
- ✅ **可测试**：方法职责单一

### 3. RESTful 设计
- ✅ **语义化**：使用 PATCH 表示部分更新
- ✅ **幂等性**：多次调用结果一致
- ✅ **可发现**：URL 结构清晰

---

## ⚠️ 注意事项

### 1. 向后兼容
- ✅ 现有 `PUT` 接口仍然可用
- ✅ 不会破坏现有功能
- ✅ 新接口是补充而非替代

### 2. 权限控制
```java
// 建议添加权限注解
@PreAuthorize("hasRole('ADMIN') or hasPermission('test_station', 'write')")
@PatchMapping("/{id}/enable")
public ApiResponse<TestStationResponse> enableStation(@PathVariable Long id) {
    ...
}
```

### 3. 幂等性保证
- 启用已启用的工位 → 返回成功，状态不变
- 禁用已禁用的工位 → 返回成功，状态不变
- 切换操作 → 总是反转状态

---

## 📚 扩展建议

### 未来可添加的功能

1. **批量状态操作**
   ```http
   PATCH /api/test-stations/batch/enable
   Body: { "ids": [1, 2, 3] }
   ```

2. **状态变更历史**
   ```http
   GET /api/test-stations/{id}/status-history
   ```

3. **条件状态切换**
   ```http
   PATCH /api/test-stations/{id}/enable-if/{condition}
   ```

4. **状态变更回调**
   - 支持 Webhook 通知
   - 记录操作审计日志

---

## ✅ 验收标准

### 功能验证
- [ ] 启用接口正常工作
- [ ] 禁用接口正常工作
- [ ] 切换接口正常工作
- [ ] 异常情况正确处理
- [ ] 日志记录完整

### 代码质量
- [ ] 单元测试覆盖率 > 80%
- [ ] 集成测试通过
- [ ] 代码符合规范
- [ ] 文档完整

### 性能要求
- [ ] 响应时间 < 100ms
- [ ] 支持并发调用
- [ ] 无内存泄漏

---

## 📅 实施计划

| 阶段 | 任务 | 预计时间 | 状态 |
|------|------|---------|------|
| 1 | 编写 Service 层代码 | 30 分钟 | ⏳ |
| 2 | 编写 Controller 层代码 | 20 分钟 | ⏳ |
| 3 | 编写单元测试 | 40 分钟 | ⏳ |
| 4 | 编写集成测试 | 30 分钟 | ⏳ |
| 5 | 运行测试验证 | 10 分钟 | ⏳ |
| 6 | 更新文档 | 10 分钟 | ⏳ |
| **总计** | **完整实现** | **约 2.5 小时** | ⏳ |

---

## 📖 参考资料

### 相关文件
- `src/main/java/com/yimusi/entity/TestStation.java`
- `src/main/java/com/yimusi/entity/OilSample.java`
- `src/main/java/com/yimusi/enums/TestStationUsage.java`
- `src/main/java/com/yimusi/enums/OilSampleStatus.java`

### 设计模式
- RESTful API 设计
- 服务层抽象
- 异常处理策略
- 日志记录规范

---

## 🎉 总结

本方案通过添加 6 个新的 API 端点（每个模块 3 个），为测试工位和油样管理模块提供了专业、便捷的状态切换能力。方案遵循 RESTful 设计原则，保持代码风格一致性，并提供了完整的测试覆盖。

**核心价值**：
- 🚀 **提升开发效率**：简化状态变更操作
- 🛡️ **增强代码质量**：统一的实现模式
- 📖 **改善 API 体验**：直观的接口设计
- 🔧 **易于维护**：清晰的代码结构

---

**文档版本**: v1.0
**创建日期**: 2025-01-19
**作者**: Claude Code
**审核状态**: 待审核
