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
         * 【JVM内存管理与GC调优】异常对象分配成本与栈轨迹（非可执行代码）
         * =========================================================================
         *
         * 一、throw 一个异常 JVM 要付多少钱
         *   1) 异常创建时立即执行 fillInStackTrace()：遍历整个调用栈，
         *      每帧创建 StackTraceElement 对象——栈深 10000 帧时分配数万个对象
         *   2) 异常对象本身是引用类型（Throwable/Exception 子类）：
         *      本类 ResourceNotFoundException extends RuntimeException，
         *      加上 message/dialogSuppressed 等字段，浅占用几十字节——栈轨迹才是大头
         *
         * 二、异常的性能反模式与修复
         *   1) 慢调用栈是性能杀手：try { 期待失败 } catch 这种"控制流异常"，
         *      每次都建几 KB 栈轨迹——异常只留给"真正异常"路径
         *   2) 关闭昂贵栈轨迹：本类异常继承 RuntimeException 后重写
         *      fillInStackTrace() { return this; }——404 类"业务分支"不该付栈轨迹成本
         *   3) 分级处理：业务型异常（NotFound/参数校验）关闭栈轨迹；
         *      系统错误（OOM/NPE）保留完整轨迹用于排障
         *   4) jstack/jfr 抓"异常事件"频率：异常 cap 暴涨常是性能问题信号，
         *      -XX:+StackTraceInThrowable 关闭后丢失排障能力，慎用全局开关
         * 三、栈轨迹填充的成本控制细节
         *   1) -XX:-StackTraceInThrowable 全局关闭：所有异常不再有轨迹，
         *      排障能力归零——只在硬性能 SLA 缺口时作为兜底
         *   2) 重写 fillInStackTrace 只针对业务异常类——粒度可控，
         *      推荐做法：业务异常关、系统异常保留
         *
         =========================================================================
         */
}