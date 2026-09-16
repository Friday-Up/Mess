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
     * 【检查清单】异常处理自检表（非可执行代码）
     * =========================================================================
     *
     * [ ] 业务异常有对应 @ExceptionHandler，映射到正确 HTTP 状态码
     * [ ] 参数校验异常返回 400 且附带字段级错误信息
     * [ ] 未知异常兜底返回 500，不把堆栈/e.getMessage() 给前端
     * [ ] 5xx 记 ERROR 日志（带堆栈），4xx 记 WARN（不记堆栈）
     * [ ] 响应体含 path 字段，便于日志关联
     *
     * 异常排障速查
     *   现象：自定义异常被当成 500 而非 404
     *     -> 检查 @ExceptionHandler 的类型是否精确匹配
     *     -> 检查是否有更宽泛的 Exception handler 抢先匹配
     *
     *   现象：@Valid 校验失败返回 500 而非 400
     *     -> 没有 @ExceptionHandler(MethodArgumentNotValidException.class)
     *     -> 该异常冒泡到兜底 handler 被当成未知异常
     *
     *   现象：全局异常处理器本身抛异常
     *     -> handler 内部逻辑有 bug（如 NPE），导致二次异常
     *     -> 解决：handler 内保持极简，只做映射和日志
     *
     *   现象：前端拿不到统一格式
     *     -> Security 过滤器链中的异常（401/403）不经过 @RestControllerAdvice
     *     -> 需要单独配置 AuthenticationEntryPoint / AccessDeniedHandler
     * =========================================================================
     */

    /*
     * =========================================================================
     * 【补充手册】异常分类与日志级别速查（非可执行代码）
     * =========================================================================
     *
     * 一、异常分类决策树
     *   客户端传入参数有问题？
     *     -> 4xx（不重试，改了请求再试）
     *   服务端代码有 bug 或依赖故障？
     *     -> 5xx（可告警，可有限重试）
     *   资源不存在？
     *     -> 404（不是错误，是"没有"）
     *   权限不够？
     *     -> 403（不泄露"存在但无权"，与 404 区分要谨慎）
     *
     * 二、日志级别与异常的对应关系
     *   4xx 客户端错误 -> WARN（不是 bug，是用户用错了）
     *   5xx 服务端错误 -> ERROR（带完整堆栈，需要人介入）
     *   预期内的业务异常（如 ResourceNotFoundException）-> WARN 或 INFO
     *   限流/熔断 -> WARN（系统在自我保护，不是故障）
     *
     *   注意：不要把所有异常都 log.error(e.getMessage())，
     *   丢失堆栈的 ERROR 日志等于没有信息。
     *
     * 三、不吞异常原则
     *   反模式：catch (Exception e) { log.error("出错了"); return null; }
     *   问题：异常被吞，上层以为成功，数据不一致，排查到崩溃。
     *   正确做法：要么抛出（让全局处理器兜底），要么记录后做补偿。
     *
     * 四、告警阈值参考
     *   5xx 速率 > 1%/分钟 -> 触发告警
     *   4xx 速率 > 10%/分钟 -> 可能是接口变更或攻击，关注但不告警
     *   单接口 5xx 突增 -> 先看发布记录，再看依赖状态
     * =========================================================================
     */
}