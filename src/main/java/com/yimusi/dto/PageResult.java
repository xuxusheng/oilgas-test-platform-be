package com.yimusi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 通用分页查询结果封装.
 * @param <T> 数据记录的类型.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    /**
     * 当前页的数据列表.
     */
    private List<T> content;

    /**
     * 总记录数.
     */
    private long totalElements;

    /**
     * 总页数.
     */
    private int totalPages;

    /**
     * 当前页码 (从 0 开始).
     */
    private int page;

    /**
     * 每页大小.
     */
    private int size;

    /**
     * 是否是第一页.
     */
    private boolean first;

    /**
     * 是否是最后一页.
     */
    private boolean last;

    /**
     * 从 Spring Data JPA 的 Page 对象转换.
     * @param page Spring Page 对象.
     * @return 转换后的 PageResult 对象.
     */
    public static <T> PageResult<T> from(Page<T> page) {
        return new PageResult<>(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize(),
                page.isFirst(),
                page.isLast()
        );
    }
}
