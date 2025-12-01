package com.yimusi.dto.user;

import com.yimusi.common.enums.UserRole;
import com.yimusi.dto.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户分页查询请求参数.
 * 继承自 PageRequest，支持分页、排序及用户特定的查询条件.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserPageRequest extends PageRequest {

    /**
     * 用户名（模糊查询）.
     * 为空时不作为查询条件.
     */
    private String username;

    /**
     * 用户角色（精确匹配）.
     * 为空时不作为查询条件.
     */
    private UserRole role;
}
