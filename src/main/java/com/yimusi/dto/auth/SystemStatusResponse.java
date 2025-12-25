package com.yimusi.dto.auth;

import lombok.Builder;
import lombok.Data;

/**
 * 系统状态响应DTO
 * 用于前端判断系统是否首次部署
 */
@Data
@Builder
public class SystemStatusResponse {

    /**
     * 是否首次部署（无任何用户）
     */
    private boolean firstDeployment;

    /**
     * 当前用户数量
     */
    private long userCount;

    /**
     * 提示信息
     */
    private String message;
}
