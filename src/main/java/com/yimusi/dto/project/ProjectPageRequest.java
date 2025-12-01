package com.yimusi.dto.project;

import com.yimusi.dto.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 项目分页查询请求参数.
 * 继承自 PageRequest，支持分页、排序及项目特定的查询条件.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectPageRequest extends PageRequest {

    /**
     * 项目编号（模糊查询）.
     * 为空时不作为查询条件.
     */
    private String projectNo;

    /**
     * 项目名称（模糊查询）.
     * 为空时不作为查询条件.
     */
    private String projectName;

    /**
     * 项目负责人（模糊查询）.
     * 为空时不作为查询条件.
     */
    private String projectLeader;
}
