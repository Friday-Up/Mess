package com.example.mess.exception;

import com.example.mess.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 
 * 使用Spring的@RestControllerAdvice注解实现全局异常处理，
 * 统一捕获和处理Controller层抛出的异常，返回标准化的错误响应。
 * 
 * 设计原则:
 * - 集中处理：所有异常在一个类中统一处理，避免重复代码
 * - 标准响应：所有错误返回统一的ApiResponse格式
 * - 分级处理：不同类型的异常返回不同的HTTP状态码
 * - 日志记录：记录异常详情，便于问题排查
 * 
 * 异常处理策略:
 * - ResourceNotFoundException: 返回404 Not Found
 * - Exception: 返回500 Internal Server Error
 * 
 * @RestControllerAdvice 说明:
 * - 组合了@ControllerAdvice和@ResponseBody
 * - 自动扫描所有@Controller和@RestController
 * - 捕获Controller层抛出的异常
 * - 返回值自动序列化为JSON
 * 
 * 扩展建议:
 * - 添加参数验证异常处理（MethodArgumentNotValidException）
 * - 添加权限异常处理（AccessDeniedException）
 * - 添加认证异常处理（AuthenticationException）
 * - 添加自定义业务异常处理
 * - 添加请求方法不支持异常处理（HttpRequestMethodNotSupportedException）
 * 
 * 日志策略:
 * - 业务异常（如ResourceNotFoundException）: 记录ERROR级别
 * - 系统异常（如Exception）: 记录ERROR级别，包含完整堆栈
 * 
 * 线程安全:
 * - 无状态类，线程安全
 * - 每次请求创建新的ApiResponse实例
 * 
 * 作者: zhangyaolong.5
 * 创建时间: 2026-05-26
 * 
 * @see com.example.mess.exception.ResourceNotFoundException 资源未找到异常
 * @see com.example.mess.dto.ApiResponse 统一API响应对象
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 日志记录器
     * 用于记录异常信息，支持不同级别的日志输出
     * 
     * 日志级别:
     * - ERROR: 系统错误和异常
     * - WARN: 警告信息
     * - INFO: 一般信息
     * - DEBUG: 调试信息
     */
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理资源未找到异常
     * 
     * 当Controller层抛出ResourceNotFoundException时，此方法自动捕获并处理。
     * 返回HTTP 404状态码和标准化的错误响应。
     * 
     * 触发场景:
     * - 根据ID查询用户，但用户不存在
     * - 根据用户名查询用户，但用户不存在
     * - 访问不存在的资源
     * 
     * 响应格式:
     * HTTP/1.1 404 Not Found
     * {
     *   "success": false,
     *   "message": "用户不存在，ID: 123",
     *   "data": null,
     *   "timestamp": "2026-05-26T10:30:00.000000"
     * }
     * 
     * 日志记录:
     * - 级别: ERROR
     * - 内容: 异常消息
     * - 目的: 监控资源访问异常，发现潜在问题
     * 
     * @param ex ResourceNotFoundException异常对象，包含错误详情
     * @return ResponseEntity<ApiResponse<Void>> 包含错误信息的HTTP响应
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        // 记录资源未找到的异常信息
        log.error("资源未找到: {}", ex.getMessage());
        // 创建标准化的错误响应
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage());
        // 返回404状态码和错误响应
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * 处理通用异常
     * 
     * 当Controller层抛出未被其他处理器捕获的异常时，此方法作为兜底处理。
     * 返回HTTP 500状态码和通用的错误响应。
     * 
     * 触发场景:
     * - 数据库连接异常
     * - 空指针异常
     * - 数组越界异常
     * - 其他未预期的运行时异常
     * 
     * 响应格式:
     * HTTP/1.1 500 Internal Server Error
     * {
     *   "success": false,
     *   "message": "系统内部错误",
     *   "data": null,
     *   "timestamp": "2026-05-26T10:30:00.000000"
     * }
     * 
     * 安全考虑:
     * - 不向客户端暴露具体的异常信息（防止信息泄露）
     * - 只返回通用的"系统内部错误"消息
     * - 详细的异常信息只记录在服务器日志中
     * 
     * 日志记录:
     * - 级别: ERROR
     * - 内容: 异常消息 + 完整堆栈
     * - 目的: 快速定位和修复系统问题
     * 
     * @param ex Exception异常对象，包含错误详情和堆栈信息
     * @return ResponseEntity<ApiResponse<Void>> 包含通用错误信息的HTTP响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        // 记录系统异常的详细信息，包含完整堆栈
        log.error("系统异常: {}", ex.getMessage(), ex);
        // 创建标准化的错误响应，不暴露具体异常信息
        ApiResponse<Void> response = ApiResponse.error("系统内部错误");
        // 返回500状态码和错误响应
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}