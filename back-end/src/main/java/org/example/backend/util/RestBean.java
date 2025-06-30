package org.example.backend.util;

import lombok.Data;

/**
 * @author Flyinsky
 * @email w2084151024@gmail.com
 * @date 2024/9/1 11:30
 */
@Data
public class RestBean<T> {
    private int status;
    private boolean success;
    private String message;
    private T data;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public RestBean(int status, boolean success, String message , T data) {
        this.status = status;
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static <T> RestBean<T> success(T data) {
        return new RestBean<>(200, true, "请求成功", data);
    }

    public static <T> RestBean<T> success(String message) {
        return new RestBean<>(200, true, message ,null);
    }

    public static <T> RestBean<T> success(String message , T data) {
        return new RestBean<>(200, true, message, data);
    }

    public static <T> RestBean<T> failure(int status, String message) {
        return new RestBean<>(status, false, message, null);
    }
}
