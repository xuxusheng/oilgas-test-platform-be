package com.yimusi.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用于创建新项目的数据传输对象 (DTO)。
 * 包含创建项目所必需的字段。
 */
@Data
public class CreateProjectRequest {

    /**
     * 项目编号，必须唯一。
     */
    @NotBlank(message = "项目编号不能为空")
    @Size(max = 50, message = "项目编号长度不能超过 50 个字符")
    private String projectNo;

    /**
     * 项目名称，可以重复。
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
