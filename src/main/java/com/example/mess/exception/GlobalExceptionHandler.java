package com.example.mess.exception;

import com.example.mess.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器 - 使用@RestControllerAdvice统一捕获Controller层异常。
 * 
 * <p>设计目标:
 * <ul>
 *   <li>统一异常处理: 避免在每个Controller中重复编写try-catch</li>
 *   <li>标准化响应格式: 所有异常返回ApiResponse格式，确保前端解析一致</li>
 *   <li>安全防护: 不向客户端暴露具体异常堆栈信息，防止信息泄露</li>
 *   <li>日志记录: 记录异常详情，便于问题排查和监控</li>
 * </ul>
 * 
 * <p>异常处理策略:
 * <table border="1">
 *   <tr><th>异常类型</th><th>HTTP状态码</th><th>响应消息</th><th>日志级别</th></tr>
 *   <tr><td>ResourceNotFoundException</td><td>404 Not Found</td><td>异常消息原文</td><td>ERROR</td></tr>
 *   <tr><td>其他Exception</td><td>500 Internal Server Error</td><td>"系统内部错误"</td><td>ERROR（含堆栈）</td></tr>
 * </table>
 * 
 * <p>扩展指南:
 * <ul>
 *   <li>添加新的@ExceptionHandler处理特定业务异常（如ValidationException → 400）</li>
 *   <li>添加@ExceptionHandler(MethodArgumentNotValidException.class)处理参数校验失败</li>
 *   <li>添加@ExceptionHandler(AccessDeniedException.class)处理权限不足 → 403</li>
 *   <li>添加@ExceptionHandler(HttpMessageNotReadableException.class)处理请求体格式错误</li>
 * </ul>
 * 
 * @see com.example.mess.exception.ResourceNotFoundException 资源未找到异常
 * @see com.example.mess.dto.ApiResponse 统一响应对象
 * @since 1.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理资源未找到异常。
     * <p>当请求的资源（如用户、订单等）不存在时，返回404 Not Found状态码。
     * 直接使用异常消息作为响应内容，便于前端展示具体错误信息。
     * 
     * @param ex 资源未找到异常，包含具体的错误消息
     * @return 包含错误信息的404响应
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        // 记录ERROR级日志（资源未找到属于可预期的业务异常，无需打印堆栈）
        log.error("资源未找到: {}", ex.getMessage());
        // 用异常消息构建错误响应，便于前端展示具体的未找到原因
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage());
        // 返回HTTP 404状态码及错误响应体
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * 处理通用异常（兜底处理）。
     * <p>捕获所有未被特定处理器处理的异常，返回500 Internal Server Error。
     * 不向客户端暴露具体异常信息，只返回"系统内部错误"，
     * 但会在日志中记录完整的异常堆栈，便于开发人员排查。
     * 
     * @param ex 未处理的异常
     * @return 包含通用错误消息的500响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        // 记录完整异常堆栈（第三个参数ex），便于开发人员定位未预期的系统错误
        log.error("系统异常: {}", ex.getMessage(), ex);
        // 对外只返回笼统的"系统内部错误"，不暴露堆栈细节，防止敏感信息泄露
        ApiResponse<Void> response = ApiResponse.error("系统内部错误");
        // 返回HTTP 500状态码及错误响应体
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /*
     * =========================================================================
     * 【面试问答】关于全局异常处理的常见面试题（非可执行代码）
     * =========================================================================
     *
     * Q1: @RestControllerAdvice 和 @ControllerAdvice 的区别？
     * A1: @RestControllerAdvice = @ControllerAdvice + @ResponseBody。
     *     前者返回值自动序列化为 JSON，后者可能返回视图。
     *     REST API 项目统一用 @RestControllerAdvice。
     *
     * Q2: @ExceptionHandler 的匹配优先级？
     * A2: 优先匹配精确类型。ResourceNotFoundException 比 RuntimeException 优先，
     *     RuntimeException 比 Exception 优先。继承距离最近的优先。
     *
     * Q3: 校验失败为什么返回 500 而不是 400？
     * A3: 没有 @ExceptionHandler(MethodArgumentNotValidException.class)，
     *     异常冒泡到兜底的 Exception handler 被当成 500。
     *     需单独捕获并返回 400 + 字段级错误信息。
     *
     * Q4: 异常被 catch 吞了怎么办？
     * A4: catch 后不抛出 -> @Transactional 不回滚 -> 全局处理器收不到。
     *     这是"事务没回滚"的最常见原因之一。原则：要么抛，要么记录后补偿。
     *
     * Q5: 5xx 异常应该记什么日志级别？
     * A5: ERROR 级，带完整堆栈（log.error(msg, e) 不是 log.error(e.getMessage())）。
     *     4xx 记 WARN，不需要堆栈。预期内业务异常（如 404）记 WARN 或 INFO。
     *
     * Q6: 如何让异常响应也携带 traceId？
     * A6: 在过滤器中用 MDC 注入 traceId，全局处理器从 MDC 取并放入
     *     ApiResponse 的字段，同时 logback pattern 输出 %X{traceId}。
     *     用户反馈问题时提供 traceId，直接定位日志行。
     * =========================================================================
     */
}