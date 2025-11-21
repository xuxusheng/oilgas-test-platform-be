package com.yimusi.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 通用分页请求参数封装.
 * 可作为基类被具体的业务查询请求继承.
 */
@Data
public class PageRequest {

    /**
     * 当前页码 (Spring Data JPA 中页码从 0 开始).
     * 默认为 0.
     */
    @Min(value = 0, message = "页码不能小于 0")
    private int page = 0;

    /**
     * 每页记录数.
     * 默认为 10.
     */
    @Min(value = 1, message = "每页数量不能小于 1")
    @Max(value = 100, message = "每页数量不能超过 100")
    private int size = 10;

    /**
     * 排序字段 (对应实体类的属性名).
     * 例如: "createTime".
     * 只允许字母、数字和下划线，防止SQL注入风险.
     */
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "排序字段格式不正确")
    private String sortField;

    /**
     * 排序顺序 ("asc" 或 "desc").
     */
    @Pattern(regexp = "^(asc|desc)$", flags = Pattern.Flag.CASE_INSENSITIVE, message = "排序顺序只能是 'asc' 或 'desc'")
    private String sortOrder;
}
