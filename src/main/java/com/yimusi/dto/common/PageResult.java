package com.yimusi.dto.common;

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
    private long total;

    /**
     * 当前页码 (从 1 开始，前端友好).
     */
    private int page;

    /**
     * 每页大小.
     */
    private int size;

    /**
     * 从 Spring Data JPA 的 Page 对象转换.
     * 自动将 JPA 的 0-based 页码转换为前端友好的 1-based 页码.
     * @param page Spring Page 对象.
     * @return 转换后的 PageResult 对象.
     */
    public static <T> PageResult<T> from(Page<T> page) {
        return new PageResult<>(
                page.getContent(),
                page.getTotalElements(),
                page.getNumber() + 1,  // JPA 页码 + 1
                page.getSize()
        );
    }
}
