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
     * 【技术债务】TD-010 全局异常处理
     * =========================================================================
     *
     * TD-010-1: 缺少参数校验异常处理
     *   现状：无 @ExceptionHandler(MethodArgumentNotValidException.class)
     *   影响：@Valid 校验失败抛异常被兜底处理器当成 500
     *   优先级：P1
     *   修复方案：加 MethodArgumentNotValidException 处理器，返回 400 + 字段错误
     *   预估工时：0.5d
     *
     * TD-010-2: 缺少唯一约束冲突处理
     *   现状：DataIntegrityViolationException 被兜底当成 500
     *   影响：用户名/邮箱重复时前端收到 500 而非 409
     *   优先级：P2
     *   修复方案：加 DataIntegrityViolationException 处理器，返回 409
     *   预估工时：0.5d
     *
     * TD-010-3: Security 异常未统一格式
     *   现状：401/403 由 Security 过滤器处理，不经过 @RestControllerAdvice
     *   影响：认证/授权错误返回格式与业务错误不一致
     *   优先级：P2
     *   修复方案：配置 AuthenticationEntryPoint + AccessDeniedHandler 返回 ApiResponse
     *   预估工时：1d
     *
     * TD-010-4: 5xx 错误无 traceId
     *   现状：错误响应无 traceId，无法关联前端报错与后端日志
     *   影响：用户反馈"出错了"但无法定位日志行
     *   优先级：P2
     *   修复方案：加 MDC traceId，异常处理器写入响应
     *   预估工时：1d
     *
     * =========================================================================
     * 【重构路线图】异常处理演进方向
     * =========================================================================
     * Phase 1（当前）：ResourceNotFoundException + 兜底 500
     * Phase 2：加参数校验 400 + 唯一约束 409 + Security 401/403 统一格式
     * Phase 3：traceId + RFC7807 Problem Details + 异常告警集成
     * =========================================================================
     */
}