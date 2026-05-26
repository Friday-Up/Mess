package com.example.mess.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * 统一处理应用中抛出的所有异常
 * 
 * @RestControllerAdvice 组合注解，包含@ControllerAdvice和@ResponseBody
 * 作用范围：所有Controller层抛出的异常
 * 
 * 设计目的:
 * - 统一异常处理，避免在每个Controller中重复处理
 * - 提供一致的API错误响应格式
 * - 将技术异常转换为友好的用户错误信息
 * - 集中日志记录和监控
 * 
 * 处理策略:
 * - 特定异常：特定异常类型使用特定处理器
 * - 通用异常：未捕获的异常使用通用处理器
 * - 验证异常：处理参数验证失败
 * - 业务异常：处理业务逻辑错误
 * 
 * 作者: zhangyaolong.5
 * 创建时间: 2026-05-26
 * 
 * 最佳实践:
 * - 为每种异常类型提供专门的处理器
 * - 返回统一的错误响应格式
 * - 记录详细的错误日志
 * - 避免暴露敏感信息
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理资源未找到异常
     * 
     * @param ex ResourceNotFoundException异常实例
     * @return ResponseEntity<ErrorResponse> 包含错误信息的HTTP响应
     * 
     * HTTP状态码: 404 Not Found
     * 
     * 使用场景:
     * - 用户查询不存在的资源
     * - 访问已被删除的资源
     * - 资源ID错误
     * 
     * 响应格式:
     * {
     *   "status": 404,
     *   "message": "用户不存在，ID: 123",
     *   "timestamp": "2026-05-26T10:30:00"
     * }
     * 
     * 日志记录:
     * - 记录异常堆栈信息用于调试
     * - 记录请求信息用于追踪
     * 
     * 示例:
     * throw new ResourceNotFoundException("用户不存在，ID: " + userId);
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        // 创建错误响应对象
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),  // HTTP状态码
            ex.getMessage(),                 // 异常消息
            LocalDateTime.now()              // 当前时间
        );
        
        // 记录错误日志
        // 在实际项目中，应该使用专业的日志框架
        System.err.println("资源未找到: " + ex.getMessage());
        
        // 返回404状态码和错误响应
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * 处理方法参数验证异常
     * 当@RequestBody或@RequestParam验证失败时抛出
     * 
     * @param ex MethodArgumentNotValidException异常实例
     * @return ResponseEntity<Map<String, String>> 包含字段错误信息的HTTP响应
     * 
     * HTTP状态码: 400 Bad Request
     * 
     * 使用场景:
     * - 创建用户时用户名验证失败
     * - 邮箱格式不正确
     * - 必填字段为空
     * 
     * 响应格式:
     * {
     *   "username": "用户名不能为空",
     *   "email": "邮箱格式不正确"
     * }
     * 
     * 处理逻辑:
     * - 遍历所有验证错误
     * - 提取字段名和错误消息
     * - 构建字段-错误消息的映射
     * 
     * 注意:
     * - 返回格式与标准ErrorResponse不同，便于前端处理
     * - 包含所有验证错误，不只是第一个
     * 
     * 示例:
     * @Valid @RequestBody UserDto userDto
     * 如果userDto验证失败，会触发此处理器
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // 创建错误映射，用于存储字段名和错误消息
        Map<String, String> errors = new HashMap<>();
        
        // 遍历所有验证错误
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            // 获取字段名
            String fieldName = ((FieldError) error).getField();
            // 获取错误消息
            String errorMessage = error.getDefaultMessage();
            // 将字段名和错误消息添加到映射中
            errors.put(fieldName, errorMessage);
        });
        
        // 记录验证错误日志
        System.err.println("参数验证失败: " + errors);
        
        // 返回400状态码和验证错误信息
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    /**
     * 处理通用异常
     * 捕获所有未处理的异常
     * 
     * @param ex Exception异常实例
     * @return ResponseEntity<ErrorResponse> 包含错误信息的HTTP响应
     * 
     * HTTP状态码: 500 Internal Server Error
     * 
     * 使用场景:
     * - 数据库连接失败
     * - 空指针异常
     * - 数组越界异常
     * - 其他未预期的异常
     * 
     * 响应格式:
     * {
     *   "status": 500,
     *   "message": "An unexpected error occurred: NullPointerException",
     *   "timestamp": "2026-05-26T10:30:00"
     * }
     * 
     * 安全措施:
     * - 不暴露详细的异常堆栈给客户端
     * - 只返回通用的错误消息
     * - 详细的错误信息记录在服务器日志中
     * 
     * 日志记录:
     * - 记录完整的异常堆栈
     * - 记录请求上下文信息
     * - 便于问题排查和修复
     * 
     * 注意:
     * - 这是最后的异常处理器，应该捕获所有未处理的异常
     * - 在生产环境中，应该使用专业的日志框架
     * - 考虑添加告警机制
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
        // 创建错误响应对象
        // 注意：不暴露详细的异常信息给客户端
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),           // HTTP状态码
            "An unexpected error occurred: " + ex.getMessage(),   // 通用错误消息
            LocalDateTime.now()                                 // 当前时间
        );
        
        // 记录详细的错误日志
        // 在实际项目中，应该使用专业的日志框架如Logback或Log4j2
        System.err.println("未预期的异常: " + ex.getMessage());
        ex.printStackTrace(); // 开发环境使用，生产环境应该使用日志框架
        
        // 返回500状态码和错误响应
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 处理运行时异常
     * 捕获RuntimeException及其子类
     * 
     * @param ex RuntimeException异常实例
     * @return ResponseEntity<ErrorResponse> 包含错误信息的HTTP响应
     * 
     * HTTP状态码: 500 Internal Server Error
     * 
     * 使用场景:
     * - 业务逻辑错误
     * - 自定义运行时异常
     * - 其他运行时异常
     * 
     * 注意:
     * - 这个处理器在Exception处理器之前执行
     * - 更具体的异常应该有专门的处理器
     * - 如果没有更具体的处理器，会由这个处理器处理
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "运行时异常: " + ex.getMessage(),
            LocalDateTime.now()
        );
        
        System.err.println("运行时异常: " + ex.getMessage());
        ex.printStackTrace();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

/**
 * 错误响应对象
 * 用于封装错误信息的标准格式
 * 
 * 设计目的:
 * - 提供一致的错误响应格式
 * - 包含必要的错误信息
 * - 便于前端统一处理
 * 
 * 字段说明:
 * - status: HTTP状态码
 * - message: 错误消息
 * - timestamp: 错误发生时间
 * 
 * 使用场景:
 * - 全局异常处理器的返回值
 * - 统一的错误响应格式
 * - API文档中的错误示例
 * 
 * 注意:
 * - 不包含敏感信息
 * - 不包含详细的异常堆栈
 * - 时间格式为ISO-8601
 */
class ErrorResponse {
    
    /**
     * HTTP状态码
     * 标准的HTTP状态码
     * 
     * 常见值:
     * - 400: Bad Request（参数错误）
     * - 401: Unauthorized（未认证）
     * - 403: Forbidden（权限不足）
     * - 404: Not Found（资源不存在）
     * - 500: Internal Server Error（服务器错误）
     */
    private int status;
    
    /**
     * 错误消息
     * 描述错误原因的文本信息
     * 
     * 格式要求:
     * - 简洁明了
     * - 用户友好
     * - 不包含技术细节
     * 
     * 示例:
     * - "用户不存在"
     * - "参数验证失败"
     * - "权限不足"
     */
    private String message;
    
    /**
     * 错误发生时间
     * 记录错误发生的时间戳
     * 
     * 格式:
     * ISO-8601格式，如"2026-05-26T10:30:00"
     * 
     * 用途:
     * - 调试和日志分析
     * - 错误追踪
     * - 性能监控
     */
    private LocalDateTime timestamp;

    /**
     * 构造函数
     * 
     * @param status HTTP状态码
     * @param message 错误消息
     * @param timestamp 错误发生时间
     * 
     * 使用场景:
     * - 全局异常处理器创建错误响应
     * - 手动创建错误响应
     */
    public ErrorResponse(int status, String message, LocalDateTime timestamp) {
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
    }

    /**
     * 获取HTTP状态码
     * @return HTTP状态码
     */
    public int getStatus() {
        return status;
    }

    /**
     * 获取错误消息
     * @return 错误消息
     */
    public String getMessage() {
        return message;
    }

    /**
     * 获取错误发生时间
     * @return 错误发生时间
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * 转换为字符串表示
     * 用于日志记录和调试
     * 
     * @return 错误响应的字符串表示
     */
    @Override
    public String toString() {
        return "ErrorResponse{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}