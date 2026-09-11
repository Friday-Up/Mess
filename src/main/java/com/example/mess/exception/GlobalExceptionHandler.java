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
     * 【设计文档】GlobalExceptionHandler 全局异常处理设计说明（补充文档，非可执行代码）
     * ============================================================================
     *
     * 一、职责定位
     * ----------------------------------------------------------------------------
     * 本类以 @RestControllerAdvice 织入所有 Controller，充当"异常兜底中枢"，
     * 将业务/框架抛出的各类异常统一转换为结构一致的 ApiResponse 错误响应，
     * 使 Controller 内部无需散落 try-catch，保持业务代码整洁。
     *
     * 二、异常到 HTTP 状态的映射约定
     * ----------------------------------------------------------------------------
     *   ResourceNotFoundException        → 404 Not Found（资源不存在）;
     *   MethodArgumentNotValidException  → 400 Bad Request（参数校验失败）;
     *   IllegalArgumentException/业务异常 → 400 Bad Request（非法输入）;
     *   其它未预期 Exception             → 500 Internal Server Error（兜底）。
     *
     * 三、关键设计决策
     * ----------------------------------------------------------------------------
     * 决策 1：500 响应对外只返回"系统内部错误"，不透出堆栈
     *   理由：堆栈可能含表名、SQL、路径等敏感信息，暴露给客户端存在安全风险;
     *        真实堆栈应记入服务端日志供排障，而非返回给调用方。
     * 决策 2：使用 @RestControllerAdvice 而非每个 Controller 各自 try-catch
     *   理由：集中治理异常，避免重复代码，保证响应结构全局一致。
     * 决策 3：区分"预期异常"与"未预期异常"
     *   理由：预期异常（如资源不存在）返回明确状态与信息；未预期异常统一 500 兜底，
     *        既保护系统细节又避免遗漏。
     *
     * 四、与其它组件的边界
     * ----------------------------------------------------------------------------
     *   - 与 SecurityConfig 的分工：认证/授权失败由 Security 过滤链处理（401/403），
     *     业务与参数异常由本类处理，二者互不越界;
     *   - 与 ApiResponse 的协作：所有错误响应统一用 ApiResponse.error 构造;
     *   - 与 ResourceNotFoundException 的协作：后者是本类识别 404 的关键信号。
     *
     * 五、日志与可观测性建议
     * ----------------------------------------------------------------------------
     *   - 5xx 异常应以 error 级别记录完整堆栈，4xx 可按需 warn/info;
     *   - 建议在响应或日志中带上 traceId，便于将一次请求的前后端日志串联;
     *   - 可结合 Micrometer 对不同异常类型计数，观测系统健康度。
     *
     * 六、扩展指南
     * ----------------------------------------------------------------------------
     *   - 新增业务异常类型：定义新异常并在此新增对应 @ExceptionHandler 方法;
     *   - 国际化错误信息：接入 MessageSource，按 Locale 返回本地化 message;
     *   - 更细粒度的校验错误：从 BindingResult 提取字段级错误明细返回给前端。
     * ============================================================================
     */
}