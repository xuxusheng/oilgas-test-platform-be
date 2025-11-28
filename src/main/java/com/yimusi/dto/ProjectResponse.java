package com.yimusi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.Instant;

/**
 * 用于返回项目公开信息的数据传输对象 (DTO)。
 * 包含项目的完整信息。
 */
@Data
public class ProjectResponse {

    /**
     * 项目的唯一标识符。
     */
    private Long id;

    /**
     * 项目编号。
     */
    private String projectNo;

    /**
     * 项目名称。
     */
    private String projectName;

    /**
     * 项目负责人。
     */
    private String projectLeader;

    /**
     * 备注信息。
     */
    private String remark;

    /**
     * 创建者。
     */
    private String createdBy;

    /**
     * 创建时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Instant createdAt;

    /**
     * 更新者。
     */
    private String updatedBy;

    /**
     * 更新时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Instant updatedAt;
}