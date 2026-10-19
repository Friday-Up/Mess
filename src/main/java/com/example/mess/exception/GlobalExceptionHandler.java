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
     * 【性能优化】异常处理性能与可观测性（非可执行代码）
     * =========================================================================
     *
     * 一、异常处理开销分析
     *   1) 异常构造：new Exception() 填充堆栈，约 1-10ms（堆栈深时更慢）
     *   2) 堆栈获取：getStackTrace() 反射开销，避免在正常路径主动调用
     *   3) 异常日志：ERROR 级日志带堆栈，IO 开销约 1-5ms（异步日志可消除）
     *   4) 结论：异常用于异常路径，不要用异常控制正常流程
     *
     * 二、高频异常的性能风险
     *   1) 5xx 风暴：下游故障导致大量 5xx，每条都记 ERROR 日志 + 堆栈
     *      日志 IO 成为瓶颈，磁盘写满
     *      解决：熔断降级，5xx 响应不走兜底处理器，直接返回降级响应
     *   2) 4xx 风暴：恶意请求触发大量 400/404
     *      解决：限流 + WAF 拦截，4xx 日志降级为 WARN 不记堆栈
     *   3) 日志采样：高频异常只记 10% 日志，全量记 Metrics
     *
     * 三、异常处理最佳实践
     *   1) 快速失败：参数校验在入口拦截，不进 Service
     *   2) 异常分类：业务异常（404/409）不记堆栈，系统异常（500）记堆栈
     *   3) 日志异步：AsyncAppender，业务线程不等日志写入
     *   4) 响应缓存：静态错误页缓存，避免重复渲染
     *
     * =========================================================================
     * 【监控可观测性】异常层关键指标
     * =========================================================================
     *   - 异常计数：http.server.requests{status=4xx/5xx} 的 count
     *   - 异常类型分布：自定义 Counter（exception.type=ResourceNotFound/Validation）
     *   - ERROR 日志速率：logback.events{level=ERROR}，>100/min 告警
     *   - 5xx P99 延迟：异常处理 + 日志写入的耗时
     *   - 告警规则：5xx rate > 1%/min 触发 P1 告警，自动创建工单
     *   接入：Micrometer Counter + Prometheus + Alertmanager + 企业微信/钉钉通知
     * =========================================================================
     */

    /*
     * =========================================================================
     * 【性能优化补充】可观测性三大支柱（非可执行代码）
     * =========================================================================
     *
     * 可观测性 = Metrics + Logging + Tracing
     *
     * 一、Metrics（指标）
     *   定义：数值型时间序列数据，回答"发生了什么"
     *   工具：Micrometer（门面） + Prometheus（存储） + Grafana（展示）
     *   四种类型：
     *     Counter：只增不减（请求数/错误数）
     *     Gauge：可增可减（当前连接数/队列长度）
     *     Timer：耗时分布（P50/P95/P99 延迟）
     *     Distribution Summary：自定义分布（响应大小）
     *   Spring Boot Actuator 自动暴露 /actuator/prometheus 端点
     *
     * 二、Logging（日志）
     *   定义：离散事件记录，回答"具体发生了什么"
     *   工具：SLF4J + Logback（本地） + ELK/Loki（集中）
     *   结构化日志：JSON 格式，便于检索和分析
     *     {"timestamp":"2024-01-15T10:30:00","level":"ERROR",
     *      "logger":"c.e.m.e.GlobalExceptionHandler","message":"...",
     *      "traceId":"abc123","spanId":"def456"}
     *   日志关联：traceId 贯穿 Metrics/Logging/Tracing
     *
     * 三、Tracing（链路追踪）
     *   定义：请求在分布式系统中的调用链路，回答"问题在哪"
     *   工具：Micrometer Tracing + Zipkin/Jaeger/SkyWalking
     *   核心概念：
     *     Trace：一次请求的完整链路
     *     Span：链路中的一个操作（如 HTTP 调用/DB 查询）
     *     traceId/spanId：贯穿传递，日志中输出以关联
     *   Spring Boot 3 + Micrometer Tracing 自动埋点 HTTP/JDBC/Redis
     * =========================================================================
     */
}