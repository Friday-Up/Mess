package com.example.mess.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * 统一API响应对象
 * 
 * 用于标准化所有API接口的响应格式，提供一致的响应结构。
 * 采用泛型设计，支持任意类型的数据负载。
 * 
 * 设计原则:
 * - 统一格式：所有API返回相同结构的JSON，便于前端解析
 * - 状态标识：通过success字段明确标识请求是否成功
 * - 消息传递：通过message字段传递操作结果或错误信息
 * - 时间戳：每次响应都包含时间戳，便于调试和审计
 * - 路径追踪：记录请求路径，便于日志关联
 * 
 * 响应结构:
 * {
 *   "success": true,           // 请求是否成功
 *   "message": "Success",      // 响应消息
 *   "data": {...},             // 响应数据（泛型）
 *   "timestamp": "2026-05-26T10:30:00.000000",  // 响应时间戳
 *   "path": "/api/users/1"     // 请求路径（可选）
 * }
 * 
 * 使用示例:
 * // 成功响应
 * ApiResponse<UserDto> response = ApiResponse.success(userDto);
 * 
 * // 成功响应（无数据）
 * ApiResponse<Void> response = ApiResponse.success();
 * 
 * // 错误响应
 * ApiResponse<Void> response = ApiResponse.error("用户不存在");
 * 
 * Jackson注解说明:
 * - @JsonFormat: 指定时间戳的序列化格式
 * - @JsonInclude: 控制JSON序列化时是否包含null值（可选）
 * 
 * 线程安全:
 * - 每次请求创建新的ApiResponse实例
 * - 不存在线程安全问题
 * 
 * 扩展建议:
 * - 添加errorCode字段，支持错误码体系
 * - 添加traceId字段，支持分布式追踪
 * - 添加pagination字段，支持分页信息
 * - 实现统一的错误码枚举
 * 
 * 作者: zhangyaolong.5
 * 创建时间: 2026-05-26
 * 
 * @param <T> 响应数据的类型，可以是任意Java类型
 * @see com.example.mess.exception.GlobalExceptionHandler 全局异常处理器中使用此类
 */
public class ApiResponse<T> {
    
    /**
     * 请求是否成功
     * 
     * true表示请求成功处理，false表示请求处理失败
     * 前端应根据此字段决定如何处理响应数据
     * 
     * 使用场景:
     * - true: 正常返回数据
     * - false: 业务异常或系统错误
     */
    private boolean success;
    
    /**
     * 响应消息
     * 
     * 成功时通常为"Success"
     * 失败时包含具体的错误描述信息
     * 
     * 使用场景:
     * - 成功消息：如"Success"、"操作成功"
     * - 错误消息：如"用户不存在"、"参数验证失败"
     * - 提示消息：如"数据已更新"、"删除成功"
     */
    private String message;
    
    /**
     * 响应数据
     * 
     * 泛型类型，可以是任意Java对象
     * 成功时包含业务数据，失败时通常为null
     * 
     * 支持的数据类型:
     * - 单个对象：如UserDto
     * - 集合对象：如List<UserDto>
     * - 分页对象：如Page<UserDto>
     * - 简单类型：如String、Integer
     * - 空值：如Void（无返回数据时）
     */
    private T data;
    
    /**
     * 响应时间戳
     * 
     * 记录响应生成的时间，精确到微秒
     * 格式：yyyy-MM-dd'T'HH:mm:ss.SSSSSS
     * 
     * 使用场景:
     * - 请求追踪和调试
     * - 性能分析（计算响应时间）
     * - 数据版本控制
     * - 审计日志
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime timestamp;
    
    /**
     * 请求路径
     * 
     * 记录触发此响应的API请求路径
     * 可选字段，用于日志关联和问题排查
     * 
     * 使用场景:
     * - 日志追踪：将响应与请求关联
     * - 问题排查：定位具体的API端点
     * - 审计记录：记录操作来源
     */
    private String path;

    /**
     * 私有构造函数
     * 
     * 使用静态工厂方法创建实例，禁止外部直接实例化
     * 自动设置时间戳为当前时间
     * 
     * 设计模式: 工厂方法模式
     * 优点:
     * - 控制实例创建过程
     * - 统一初始化逻辑
     * - 支持未来扩展（如缓存、池化）
     * - 方法名具有语义（success/error）
     */
    private ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * 创建成功响应（带数据）
     * 
     * 工厂方法，创建包含业务数据的成功响应
     * 自动设置success=true，message="Success"
     * 
     * 使用示例:
     * UserDto user = userService.getUserById(1L);
     * ApiResponse<UserDto> response = ApiResponse.success(user);
     * 
     * @param data 响应数据，可以是任意类型
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
     * 工厂方法，创建不含业务数据的成功响应
     * 适用于删除、更新等不需要返回数据的操作
     * 
     * 使用示例:
     * userService.deleteUser(1L);
     * ApiResponse<Void> response = ApiResponse.success();
     * 
     * @param <T> 数据类型
     * @return ApiResponse<T> 成功响应对象，data字段为null
     */
    public static <T> ApiResponse<T> success() {
        return success(null);
    }

    /**
     * 创建错误响应
     * 
     * 工厂方法，创建包含错误信息的失败响应
     * 自动设置success=false，data=null
     * 
     * 使用示例:
     * ApiResponse<Void> response = ApiResponse.error("用户不存在");
     * ApiResponse<Void> response = ApiResponse.error("参数验证失败: 邮箱格式不正确");
     * 
     * @param message 错误消息，应包含具体的错误描述
     * @param <T> 数据类型
     * @return ApiResponse<T> 错误响应对象
     */
    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.message = message;
        return response;
    }

    /**
     * 获取请求是否成功
     * 
     * @return boolean true表示成功，false表示失败
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * 设置请求是否成功
     * 
     * 通常由工厂方法自动设置，不建议外部修改
     * 
     * @param success 请求是否成功
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * 获取响应消息
     * 
     * @return String 响应消息
     */
    public String getMessage() {
        return message;
    }

    /**
     * 设置响应消息
     * 
     * @param message 响应消息
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 获取响应数据
     * 
     * @return T 响应数据，失败时可能为null
     */
    public T getData() {
        return data;
    }

    /**
     * 设置响应数据
     * 
     * @param data 响应数据
     */
    public void setData(T data) {
        this.data = data;
    }

    /**
     * 获取响应时间戳
     * 
     * @return LocalDateTime 响应时间戳
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * 设置响应时间戳
     * 
     * 通常由构造函数自动设置，不建议外部修改
     * 
     * @param timestamp 响应时间戳
     */
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * 获取请求路径
     * 
     * @return String 请求路径，可能为null
     */
    public String getPath() {
        return path;
    }

    /**
     * 设置请求路径
     * 
     * @param path 请求路径
     */
    public void setPath(String path) {
        this.path = path;
    }
}