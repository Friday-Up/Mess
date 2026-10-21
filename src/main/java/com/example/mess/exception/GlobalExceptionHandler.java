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
     * 【分布式系统】跨服务异常传播与可观测性（非可执行代码）
     * =========================================================================
     *
     * 一、微服务中的异常传播
     *   单体：异常在同一进程内传播，try-catch 直接捕获
     *   微服务：服务 A 调服务 B，B 的异常通过网络返回 A
     *   问题：B 的异常类型/堆栈在 A 侧不一定能反序列化
     *   方案：
     *   1) HTTP 状态码 + 错误体：B 返回 4xx/5xx + ApiResponse，
     *      A 侧按状态码处理，解析错误体获取 message
     *   2) Feign ErrorDecoder：Feign 调用时自定义 ErrorDecoder，
     *      将 B 的错误体转换为 A 侧的业务异常
     *   3) 异常语义化：B 返回的错误码是业务语义（409/404），
     *      A 侧按业务语义处理，不依赖 B 的异常类
     *   4) 不透传堆栈：B 的堆栈不返回给 A（安全+带宽），
     *      A 只拿到 code/message/traceId
     *
     * 二、分布式链路追踪（Distributed Tracing）
     *   跨服务调用的问题：请求经过 网关 -> 服务 A -> 服务 B -> DB，
     *   任一环节慢/错，如何定位？
     *   方案：traceId 贯穿整条链路
     *   1) 生成：入口（网关）生成 traceId（如 64-bit hex）
     *   2) 传播：HTTP Header（W3C traceparent/b3）/ MQ Header 传递
     *   3) 记录：每个服务将 traceId 记入日志（MDC + %X{traceId}）
     *   4) 收集：SkyWalking/Zipkin/Jaeger 收集 Span，绘制调用链路图
     *   5) 关联：前端报错返回 traceId -> 查日志 -> 查链路图 -> 定位慢/错环节
     *   Spring Boot 3 + Micrometer Tracing：
     *   - 自动埋点：HTTP 客户端/服务端、JDBC、Redis、Kafka
     *   - MDC 自动注入 traceId/spanId
     *   - 配置：management.tracing.sampling.probability=1.0（全采样）
     *
     * 三、分布式日志聚合
     *   单体：日志在本地文件，grep 即可
     *   微服务：日志分散在 N 个 Pod，需聚合查询
     *   方案：
     *   1) ELK：Filebeat 采集 -> Logstash 解析 -> Elasticsearch 存储 -> Kibana 查询
     *   2) Loki：轻量级，Promtail 采集 -> Loki 存储 -> Grafana 查询
     *   3) 结构化日志：JSON 格式，含 traceId/level/logger/message
     *      {"traceId":"a1b2c3","level":"ERROR","logger":"c.e.m.e.GlobalExceptionHandler",
     *       "message":"...","stackTrace":"..."}
     *   4) 查询：按 traceId 聚合一次请求在所有服务的日志
     *
     * 四、异常告警与 On-Call
     *   1) 告警分级：
     *      P0：全站不可用，5 分钟内响应，电话叫醒
     *      P1：核心功能不可用，15 分钟内响应，企业微信/钉钉
     *      P2：非核心功能异常，工作时间处理
     *      P3：潜在问题，排期处理
     *   2) 告警规则：
     *      5xx 速率 > 1%/min -> P1
     *      单接口 P99 > 2s 持续 5min -> P2
     *      ERROR 日志 > 100/min -> P2
     *   3) 告警收敛：同一根因的告警合并，避免告警风暴
     *   4) On-Call 轮值：周轮值，交接文档，告警升级机制
     * =========================================================================
     */
}