package com.yimusi.common.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApiResponse<T> {

    private static final int SUCCESS_CODE = 200;
    private static final String SUCCESS_MESSAGE = "Success";

    private int code;
    private String message;
    private T data;
    private Object errors;

    private ApiResponse(int code, String message, T data, Object errors) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.errors = errors;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(SUCCESS_CODE, SUCCESS_MESSAGE, data, null);
    }

    public static <T> ApiResponse<T> success() {
        return success(null);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null, null);
    }

    /**
     * 返回带有错误详情的响应
     * @param code 错误码
     * @param message 错误信息
     * @param errors 错误详情 (通常是 Map 或 List)
     */
    public static <T> ApiResponse<T> error(int code, String message, Object errors) {
        return new ApiResponse<>(code, message, null, errors);
    }
}
