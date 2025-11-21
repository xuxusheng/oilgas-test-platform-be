package com.yimusi.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;

/**
 * 通用分页请求参数封装.
 * 可作为基类被具体的业务查询请求继承.
 */
@Data
public class PageRequest {

    /**
     * 当前页码 (前端传入从 1 开始，后端会自动转换为从 0 开始).
     * 默认为 1.
     */
    @Min(value = 1, message = "页码不能小于 1")
    private int page = 1;

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

    /**
     * 获取 JPA 的页码 (从 0 开始).
     * 用于内部转换，前端传入的页码从 1 开始.
     */
    public int getJpaPage() {
        return page - 1;
    }

    /**
     * 构建排序对象.
     * 如果未指定排序字段，则使用默认排序字段.
     *
     * @param defaultSortField 默认排序字段
     * @return Sort 排序对象
     */
    @NonNull
    public Sort toSort(String defaultSortField) {
        if (sortField != null && !sortField.trim().isEmpty()) {
            Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
            return Sort.by(direction, sortField);
        }
        // 使用默认排序字段（倒序）
        return Sort.by(Sort.Direction.DESC, defaultSortField);
    }

    /**
     * 构建排序对象.
     * 如果未指定排序字段，则使用默认排序（按 ID 倒序）.
     *
     * @return Sort 排序对象
     */
    @NonNull
    public Sort toSort() {
        return toSort("id");
    }

    /**
     * 转换为 Spring Data JPA 的 PageRequest 对象.
     * 自动处理页码转换（1-based 转 0-based）和排序构建.
     *
     * @param defaultSortField 默认排序字段
     * @return Spring Data JPA 的 PageRequest 对象
     */
    @NonNull
    public org.springframework.data.domain.PageRequest toJpaPageRequest(String defaultSortField) {
        return org.springframework.data.domain.PageRequest.of(
            getJpaPage(),
            size,
            toSort(defaultSortField)
        );
    }

    /**
     * 转换为 Spring Data JPA 的 PageRequest 对象.
     * 使用默认排序字段（ID 倒序）.
     *
     * @return Spring Data JPA 的 PageRequest 对象
     */
    @NonNull
    public org.springframework.data.domain.PageRequest toJpaPageRequest() {
        return toJpaPageRequest("id");
    }
}
