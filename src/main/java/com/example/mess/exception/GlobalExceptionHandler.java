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
     * 【HTTP/REST】全局异常处理器的状态码映射规则（非可执行代码）
     * =========================================================================
     *
     * 一、状态码映射的 REST 语义完整对照表
     *   1) 本类三个 @ExceptionHandler 的映射决策：
     *      ResourceNotFoundException -> 404 Not Found（资源真不存在）
     *      IllegalArgumentException -> 400 Bad Request（请求参数业务不合法）
     *      Exception（兜底） -> 500 Internal Server Error（服务端未知故障）
     *   2) REST 派主张的完整状态码分配（项目演进参考）：
     *      200 OK：GET/PUT/DELETE 成功（返回资源或空体）
     *      201 Created：POST 创建成功（带 Location 头指向新资源）
     *      204 No Content：DELETE 成功无返回体 / PUT 成功无返回体
     *      400 Bad Request：语法错误/校验失败（Body 格式坏、必填空）
     *      401 Unauthorized：未认证（注意是"没登录"不是"没权限"，术语坑）
     *      403 Forbidden：已认证但无权限（业务越权）
     *      404 Not Found：资源不存在 / 路由不存在（两者可细分：用包体 errorCode）
     *      405 Method Not Allowed：路由存在但不支持该 HTTP 方法
     *         必须带 Allow: GET, POST 响应头
     *      409 Conflict：业务冲突（重复创建、版本冲突、状态机不允许）
     *      410 Gone：资源曾存在已被永久删除（区别于 404"从未存在"）
     *      412 Precondition Failed：If-Match/If-Unmodified-Since 不满足
     *      415 Unsupported Media Type：Content-Type 不支持（如发 XML 给只收 JSON 的端点）
     *      422 Unprocessable Entity：语法对但语义错（如邮箱格式合法但域名不存在）
     *      429 Too Many Requests：限流触发，必带 Retry-After 头
     *      500：未预期异常，必须记 ERROR 日志并告警
     *      503 Service Unavailable：依赖不可用（DB/下游 RPC 挂），
     *         必带 Retry-After 头，配合断路器半开状态

     * 二、4xx 客户端侧 vs 5xx 服务端侧的归责原则
     *   1) 4xx = 客户端责任：请求改一改可以成功（400 参数错、404 路径错、
     *      401 加 token、429 等一会再试）——不要把服务端故障标 4xx，会误导
     *   2) 5xx = 服务端责任：客户端请求没问题，是服务自己出毛病——
     *      不要把客户端参数错标 500（会触发无意义的告警风暴）
     *   3) 边界争议案例：
     *      数据库唯一约束冲突（客户端重发同样的 username）：
     *      是 4xx（重复数据是客户端问题），按 409 Conflict——
     *      本项目现状会抛 DataIntegrityViolationException，
     *      演进方向是 Service 层提前 exists 检查 + 映射 409
     *      三方依赖超时：是 5xx（服务不该让客户端等），504 Gateway Timeout

     * 三、本类的 REST 合规体检与补强清单
     *   1) 404 处理合规（ResourceNotFoundException -> 404 + 包体）✓
     *   2) 400 处理基本合规——但 Bean Validation 的
     *      MethodArgumentNotValidException 未专门映射；
     *      补强：@ExceptionHandler(MethodArgumentNotValidException.class)
     *      -> 400 + 字段级错误列表（fieldName + message）
     *   3) 500 兜底合规，但缺少与监控联动：
     *      补强点 A：拉 Micrometer Counter（异常计数 + 状态码 tag）
     *      补强点 B：logger.error 必须带 traceId（MDC 自动注入）
     *      补强点 C：响应里不返堆栈（脱敏），仅 message
     *   4) 缺失的场景映射：
     *      NoResourceFoundException（404 静态资源）—— 可转 404 包体
     *      HttpRequestMethodNotSupportedException -> 405 + Allow 头
     *      HttpMediaTypeNotSupportedException -> 415
     *      DataIntegrityViolationException -> 409 Conflict（唯一约束）
     *      OptimisticLockingFailureException -> 409（版本冲突）

     * 四、错误响应的可观测性约定
     *   1) 每个错误响应必须带 X-Request-Id（与日志同 ID）
     *   2) 错误日志关键字段：traceId / userId / path / method / 状态码 /
     *      异常类型与 message（不含敏感数据）
     *   3) 告警分级：
     *      5xx 突增 -> P1 告警（线上事故级）
     *      4xx 突增 -> P2 告警（可能前端 Bug 或攻击者扫描）
     *      单次 5xx -> 不告警（噪声控制）
     *   4) 错误率与 RT 联动看：错误率高 + RT 高 = 容量崩塌
     *      错误率高 + RT 正常 = 业务逻辑/鉴权问题
     *   5) 排障口诀：按 traceId 查日志 -> 按状态码分类聚合 ->
     *      按异常类型看堆栈根因 -> 再决定修复层级（代码/配置/容量）
     * =========================================================================
     */
}