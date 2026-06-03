package com.example.mess.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * 统一API响应对象
 * 用于标准化所有API接口的响应格式
 * 
 * @param <T> 响应数据的类型
 * 
 * 作者: zhangyaolong.5
 * 创建时间: 2026-05-26
 */
public class ApiResponse<T> {
    
    private boolean success;
    private String message;
    private T data;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime timestamp;
    
    private String path;

    /**
     * 私有构造函数，使用静态工厂方法创建实例
     */
    private ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * 创建成功响应
     * 
     * @param data 响应数据
     * @param <T> 数据类型
     * @return ApiResponse<T> 成功响应对象
     */
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.message = "Success";
        response.data = data;
        return response;
    }

    /**
     * 创建成功响应（无数据）
     * 
     * @param <T> 数据类型
     * @return ApiResponse<T> 成功响应对象
     */
    public static <T> ApiResponse<T> success() {
        return success(null);
    }

    /**
     * 创建错误响应
     * 
     * @param message 错误消息
     * @param <T> 数据类型
     * @return ApiResponse<T> 错误响应对象
     */
    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.message = message;
        return response;
    }

    // Getter和Setter方法
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