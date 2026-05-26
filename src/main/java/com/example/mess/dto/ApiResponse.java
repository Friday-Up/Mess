package com.example.mess.dto;

import java.time.LocalDateTime;

/**
 * 统一API响应对象
 * 用于标准化所有API接口的响应格式
 * 
 * 设计目的:
 * - 统一API响应格式，提高前端开发体验
 * - 提供一致的响应结构，便于错误处理
 * - 包含成功/失败状态、消息、数据和时间戳
 * 
 * 响应结构:
 * {
 *   "success": true,           // 请求是否成功
 *   "message": "Success",      // 响应消息
 *   "data": {...},             // 响应数据
 *   "timestamp": "2026-05-26T10:30:00", // 响应时间
 *   "path": "/api/users"       // 请求路径（可选）
 * }
 * 
 * 使用场景:
 * - 所有Controller方法的返回值
 * - 统一的错误响应格式
 * - 分页查询的响应包装
 * 
 * 作者: zhangyaolong.5
 * 创建时间: 2026-05-26
 */
public class ApiResponse<T> {
    
    /**
     * 请求是否成功
     * true表示请求处理成功
     * false表示请求处理失败
     * 
     * 使用场景:
     * - 前端判断请求是否成功
     * - 统一的错误处理逻辑
     * 
     * 示例值:
     * - true: 登录成功、数据查询成功
     * - false: 参数验证失败、权限不足、系统错误
     */
    private boolean success;
    
    /**
     * 响应消息
     * 描述请求处理结果的文本信息
     * 
     * 使用场景:
     * - 向用户展示操作结果
     * - 调试和错误排查
     * - 国际化支持的基础
     * 
     * 示例值:
     * - "操作成功"
     * - "用户名已存在"
     * - "服务器内部错误"
     * 
     * 建议:
     * - 成功时使用简洁的确认消息
     * - 失败时提供具体的错误描述
     * - 避免暴露敏感信息
     */
    private String message;
    
    /**
     * 响应数据
     * 泛型类型，可以包装任意类型的数据
     * 
     * 使用场景:
     * - 查询结果数据
     * - 创建/更新后的对象
     * - 分页查询结果
     * - 错误详情信息
     * 
     * 示例值:
     * - User对象
     * - List<User>列表
     * - Page<User>分页结果
     * - Map<String, Object>复杂数据结构
     * 
     * 注意:
     * - 失败时可以为null
     * - 成功时不应该为null（除非是删除操作）
     */
    private T data;
    
    /**
     * 响应时间戳
     * 记录响应生成的时间
     * 使用LocalDateTime类型，精确到毫秒
     * 
     * 使用场景:
     * - 调试和性能分析
     * - 日志记录
     * - 缓存失效判断
     * 
     * 格式:
     * ISO-8601格式，如"2026-05-26T10:30:00.123"
     * 
     * 注意:
     * - 自动设置为当前时间
     * - 可用于计算响应延迟
     */
    private LocalDateTime timestamp;
    
    /**
     * 请求路径
     * 记录发起请求的URL路径
     * 可选字段，主要用于调试和日志
     * 
     * 使用场景:
     * - 调试多个API调用
     * - 日志分析
     * - 错误追踪
     * 
     * 示例值:
     * - "/api/users"
     * - "/api/users/1"
     * - "/hello"
     * 
     * 注意:
     * - 不是必须字段
     * - 主要用于调试目的
     */
    private String path;

    /**
     * 默认构造函数
     * 由Spring框架在反序列化JSON时使用
     * 自动设置当前时间为时间戳
     * 
     * 使用场景:
     * - Jackson JSON反序列化
     * - Spring MVC自动绑定
     * - 手动创建响应对象
     * 
     * 注意:
     * - 必须提供默认构造函数
     * - 自动设置timestamp为当前时间
     */
    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * 创建成功响应的静态工厂方法
     * 
     * @param data 响应数据
     * @param <T> 数据类型
     * @return ApiResponse<T> 成功响应对象
     * 
     * 使用场景:
     * - 查询操作成功
     * - 创建操作成功
     * - 更新操作成功
     * 
     * 示例:
     * return ApiResponse.success(user);
     * return ApiResponse.success(userList);
     * return ApiResponse.success(pageResult);
     */
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setMessage("Success");
        response.setData(data);
        return response;
    }

    /**
     * 创建带自定义消息的成功响应的静态工厂方法
     * 
     * @param message 自定义成功消息
     * @param data 响应数据
     * @param <T> 数据类型
     * @return ApiResponse<T> 成功响应对象
     * 
     * 使用场景:
     * - 需要自定义成功消息的操作
     * - 批量操作成功
     * - 复杂业务操作成功
     * 
     * 示例:
     * return ApiResponse.success("用户创建成功", user);
     * return ApiResponse.success("数据更新完成", updatedData);
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    /**
     * 创建错误响应的静态工厂方法
     * 
     * @param message 错误消息
     * @param <T> 数据类型
     * @return ApiResponse<T> 错误响应对象
     * 
     * 使用场景:
     * - 参数验证失败
     * - 业务逻辑错误
     * - 权限不足
     * 
     * 示例:
     * return ApiResponse.error("用户名已存在");
     * return ApiResponse.error("权限不足");
     */
    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        // 错误响应通常不需要数据，设置为null
        response.setData(null);
        return response;
    }

    /**
     * 创建带数据的错误响应的静态工厂方法
     * 
     * @param message 错误消息
     * @param data 错误相关数据（如验证错误详情）
     * @param <T> 数据类型
     * @return ApiResponse<T> 错误响应对象
     * 
     * 使用场景:
     * - 参数验证错误，返回具体错误字段
     * - 批量操作部分失败
     * - 复杂错误需要返回详细信息
     * 
     * 示例:
     * return ApiResponse.error("参数验证失败", validationErrors);
     * return ApiResponse.error("部分操作失败", failedItems);
     */
    public static <T> ApiResponse<T> error(String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    /**
     * 创建带路径的成功响应
     * 
     * @param data 响应数据
     * @param path 请求路径
     * @param <T> 数据类型
     * @return ApiResponse<T> 成功响应对象
     * 
     * 使用场景:
     * - 需要记录请求路径的调试场景
     * - 日志分析
     * - 复杂API调用追踪
     * 
     * 示例:
     * return ApiResponse.successWithPath(user, request.getRequestURI());
     */
    public static <T> ApiResponse<T> successWithPath(T data, String path) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setMessage("Success");
        response.setData(data);
        response.setPath(path);
        return response;
    }

    /**
     * 创建带路径的错误响应
     * 
     * @param message 错误消息
     * @param path 请求路径
     * @param <T> 数据类型
     * @return ApiResponse<T> 错误响应对象
     * 
     * 使用场景:
     * - 需要记录请求路径的错误场景
     * - 错误日志记录
     * - 调试复杂API调用
     * 
     * 示例:
     * return ApiResponse.errorWithPath("用户不存在", request.getRequestURI());
     */
    public static <T> ApiResponse<T> errorWithPath(String message, String path) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        response.setData(null);
        response.setPath(path);
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

    /**
     * 转换为字符串表示
     * 用于日志记录和调试
     * 
     * @return API响应的字符串表示
     * 
     * 注意:
     * - 提供的信息用于调试和日志记录
     * - 避免在toString中输出敏感信息
     */
    @Override
    public String toString() {
        return "ApiResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", timestamp=" + timestamp +
                ", path='" + path + '\'' +
                '}';
    }
}