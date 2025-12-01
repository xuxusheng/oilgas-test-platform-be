package com.yimusi.dto.project;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用于更新项目数据的数据传输对象 (DTO)。
 * 包含可以被更新的字段。
 * 注意：更新时字段为可选，因此不使用 @NotBlank 或 @NotNull，只限制格式。
 */
@Data
public class UpdateProjectRequest {

    /**
     * 项目编号（不可更改，但为完整性保留）。
     */
    @Size(max = 50, message = "项目编号长度不能超过 50 个字符")
    private String projectNo;

    /**
     * 项目名称。
     */
    @Size(max = 200, message = "项目名称长度不能超过 200 个字符")
    private String projectName;

    /**
     * 项目负责人。
     */
    @Size(max = 100, message = "项目负责人长度不能超过 100 个字符")
    private String projectLeader;

    /**
     * 备注信息。
     */
    @Size(max = 500, message = "备注长度不能超过 500 个字符")
    private String remark;
}
