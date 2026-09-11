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
     * ============================================================================
     * 【阅读笔记】全局异常处理的套路与反模式（非可执行代码）
     * ============================================================================
     *
     * 一、@ExceptionHandler 的匹配优先级
     * ----------------------------------------------------------------------------
     *   - 优先精确类型：ResourceNotFoundException > RuntimeException > Exception;
     *   - 多个 Handler 时 Spring 按"继承距离"选最近的一个;
     *   - 同一个类内类型不冲突，跨类时考虑用 @Order 控制顺序。
     *
     * 二、推荐的分层异常设计
     * ----------------------------------------------------------------------------
     *   Controller/Service      抛业务异常（ResourceNotFoundException 等）;
     *   Repository              让 JPA/DataAccess 异常自然上抛;
     *   GlobalExceptionHandler  统一收敛并映射 HTTP 语义 + 业务码。
     *   好处：业务层干净、API 稳定、新人易推断异常何时被谁处理。
     *
     * 三、错误响应体的推荐字段
     * ----------------------------------------------------------------------------
     *   timestamp、code、message、path、traceId（可选）、fieldErrors（可选）。
     *   RFC7807（Problem Details）也是一种标准化选择，从 6.0 开始内建支持。
     *
     * 四、反模式避坑
     * ----------------------------------------------------------------------------
     *   1) 在业务代码里到处 try-catch 然后返回 null——异常被吞，排查到崩溃;
     *   2) 全局 Handler 里返回固定"操作失败"——前端无法区分 4xx/5xx;
     *   3) 把堆栈直接给前端——暴露内部实现与依赖版本，安全风险;
     *   4) 所有异常都返回 200 + 业务码——监控/网关无法识别真实故障，不健壮。
     *
     * 五、可观测性增强
     * ----------------------------------------------------------------------------
     *   - 记录 5xx 到 ERROR 级，4xx 到 WARN 级，避免噪音;
     *   - 把 traceId 写进响应,用户反馈时能定位到具体日志行;
     *   - 对未知异常可以接入告警，宁可多报也不要漏报。
     * ============================================================================
     */

    /*
     * ============================================================================
     * 【补充阅读】常见异常 -> HTTP 状态码映射参考表（非可执行代码）
     * ============================================================================
     *
     * 客户端错误（4xx，调用方问题，重试前先修正请求）
     * ----------------------------------------------------------------------------
     *   400  参数校验失败        MethodArgumentNotValidException / 自定义校验异常
     *   401  未认证              AuthenticationException（由 Security 过滤器处理）
     *   403  无权限              AccessDeniedException
     *   404  资源不存在          ResourceNotFoundException / NoHandlerFoundException
     *   405  方法不允许          HttpRequestMethodNotSupportedException
     *   409  资源冲突            DataIntegrityViolationException（唯一约束等）
     *   415  不支持的媒体类型    HttpMediaTypeNotSupportedException
     *   429  请求过于频繁        限流组件抛出（如自定义 RateLimitException）
     *
     * 服务端错误（5xx，服务方问题，可告警，可有限重试）
     * ----------------------------------------------------------------------------
     *   500  未捕获的兜底异常     Exception（透传通用提示，不暴露堆栈）
     *   502  下游服务异常         网关层常见；本服务内调用下游失败建议自定义 5xx 语义
     *   503  服务不可用/熔断      配合 Resilience4j 熔断器返回
     *
     * 设计要点
     * ----------------------------------------------------------------------------
     *   - 4xx 记 WARN，5xx 记 ERROR，监控告警只看 5xx 速率上升;
     *   - message 面向用户（可展示），日志面向开发者（带堆栈与上下文）;
     *   - 映射表集中在本类，新增异常先想状态码再想 message，保持语义正交。
     * ============================================================================
     */
}