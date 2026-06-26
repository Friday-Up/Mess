package com.example.mess.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * 统一API响应对象
 * 
 * 标准化所有API接口的响应格式，采用泛型设计支持任意类型的数据负载。
 * 通过静态工厂方法（success/error）创建实例。
 * 
 * 响应结构: { success, message, data, timestamp, path }
 * 
 * @param <T> 响应数据的类型
 * @see com.example.mess.exception.GlobalExceptionHandler 全局异常处理器中使用此类
 */
public class ApiResponse<T> {
    
    /** 请求是否成功，前端应根据此字段决定如何处理响应数据 */
    private boolean success;
    
    /** 响应消息，成功时为"Success"，失败时包含错误描述 */
    private String message;
    
    /** 响应数据，泛型类型，成功时包含业务数据，失败时为null */
    private T data;
    
    /** 响应时间戳，格式：yyyy-MM-dd'T'HH:mm:ss.SSSSSS */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime timestamp;
    
    /** 请求路径，可选字段，用于日志关联和问题排查 */
    private String path;

    /** 私有构造函数，使用静态工厂方法创建实例，自动设置时间戳 */
    private ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    /** 创建成功响应（带数据） */
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.message = "Success";
        response.data = data;
        return response;
    }

    /** 创建成功响应（无数据），适用于删除、更新等操作 */
    public static <T> ApiResponse<T> success() {
        return success(null);
    }

    /** 创建错误响应，自动设置success=false，data=null */
    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.message = message;
        return response;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}