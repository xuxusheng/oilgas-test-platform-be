package com.yimusi.common.util;

import cn.dev33.satoken.stp.StpUtil;

/**
 * 操作者工具类
 * 提供获取当前操作者信息的通用方法
 */
public class OperatorUtil {

    private OperatorUtil() {
        // 工具类，私有构造函数
    }

    /**
     * 获取当前操作者ID
     * 优先从 SaToken 获取登录用户ID，失败时返回 "system"
     *
     * @return 操作者ID
     */
    public static String getOperator() {
        try {
            return StpUtil.isLogin() ? StpUtil.getLoginIdAsString() : "system";
        } catch (Exception e) {
            // 处理测试场景中 SaToken 上下文不可用的情况
            return "system";
        }
    }
}
