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
         * 【可观测性与生产运维】全局异常处理与错误可观测闭环（非可执行代码）
         * =========================================================================
         *
         * 一、异常处理的观测职责：记录、映射、计数
         *   1) 三件事必须同时做：ERROR 日志（含 stackTrace + traceId）->
         *      统一错误响应（前端可读）-> 指标计数（按异常类型标签）——
         *      只做响应不做日志/指标，等于"用户看到了错误，系统却不知道"
         *   2) 分级记录纪律：
         *      业务异常（NotFound/参数错）-> WARN 级 + 无栈
         *      系统异常（NPE/DB 超时/OOM）-> ERROR 级 + 全栈
         *      全部按 ERROR 记录是告警疲劳的根本来源，分级是降噪第一刀
         *   3) 异常类型做指标标签：exception=ResourceNotFound / DataAccessException /
         *      Unknown——错误率按类型拆看，比单一"错误率上涨"可定位一个数量级
         *
         * 二、错误信息的安全与隐私
         *   1) message 对外要"友好且不泄漏"：不透出 SQL/约束名/内部 IP/表名，
         *      stackTrace 永不返回前端——错误详情落日志，响应只给业务语义
         *   2) traceId 一定要回给前端：这是"用户报障 -> 秒级定位"的最短路径，
         *      缺 traceId 的错误响应把 5 分钟的排查变成 50 分钟
         *
         * 三、错误处理的监控与自愈联动
         *   1) 错误率告警要按 业务域+异常类型 双维度设阈值：
         *      "整体 5xx>1%" 太粗，"支付域 DB 异常>0.5%" 才可执行
         *   2) 面板设计：异常计数热力图（时间 × 异常类型），
         *      一眼看出"某类异常在某时段集中爆发"——表格做不到的洞察图形可以
         *   3) 自愈边界：处理器可做有限重试（幂等操作），但不要在处理器里
         *      做复杂补偿——补偿逻辑可观测性差，放业务层 + 事件驱动更可靠
         =========================================================================
         */
}