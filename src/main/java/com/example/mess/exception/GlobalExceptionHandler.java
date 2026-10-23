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
     * 【Java 并发】全局异常处理的线程视角：栈、未捕获处理器与异步异常（非可执行代码）
     * =========================================================================
     *
     * 一、异常栈为什么带"线程字段"
     *   1) 每个 Throwable 构造时通过 fillInStackTrace 抓取当前线程栈快照，
     *      栈帧依附在抛出它的那个线程上
     *   2) 打印方式：异常跨线程传播后，日志里的栈仍是"构栈栈"，
     *      不会变成"打印线程"的栈；这也是异步任务异常排错难的根源之一
     *   3) 线程名出现在报错头的概率：logback 图案含 %thread，能区分
     *      pool-2-thread-1 是哪个池（命名规范在此体现价值，见启动注释块）
     *
     * 二、全局异常处理器的调用线程
     *   1) 本类 @ExceptionHandler 由 DispatcherServlet 调用，
     *      执行线程 = 抛异常的 Tomcat 工作线程（同一线程，无切换）
     *   2) 推论：异常处理中可继续使用当前线程的 ThreadLocal 资源
     *      （安全上下文仍在、事务绑定仍在，但事务已被标记回滚）
     *   3) 注意：处理逻辑中不可再抛需要回滚的异常否则吞掉，本类用
     *      ResponseEntity 返回是稳妥做法（HTTP 响应代替异常继续传播）
     *
     * 三、异步链路中的异常传播
     *   1) @Async void 返回值：异常走 AsyncUncaughtExceptionHandler，
     *      默认仅打 ERROR 日志 -> 必须注册自定义处理器上报监控/告警
     *   2) @Async CompletableFuture：异常被封装到 future，
     *      调用方 .join()/.get() 才抛出 CompletionException（包装原异常）
     *      忘记 .exceptionally 会默默吞掉错误
     *   3) CompletableFuture.supplyAsync 不带 Executor 时用 ForkJoinPool，
     *      异常栈出现在 ForkJoinPool worker 线程 -> 加 taskExecutor 便于区分
     *   4) 本类可拓展：@ExceptionHandler(CompletionException.class) 解包后
     *      转给具体异常处理器，避免前端拿到原始并发异常
     *
     * 四、UncaughtExceptionHandler 与线程的优雅退出
     *   1) 线程 run() 抛未捕获异常 -> 线程终止 -> 按 Thread.getUncaughtExceptionHandler
     *      处理（默认打印 stderr）
     *   2) Tomcat/Jetty 都为工作线程设置了处理器：把异常交给容器日志，
     *      并决定是否替换/保留该线程
     *   3) 自建线程池建议保留 ThreadFactory 的命名与 UncaughtExceptionHandler：
     *      pool 是你的，栈里没名字没告警，排查就是盲人摸象
     *   4) 线程终止的内存副作用：ThreadLocal Map 随线程对象销毁而回收，
     *      但线程池复用线程意味着不销毁 -> 持有的 ThreadLocal 值若指向
     *      类加载器还会阻止类卸载 -> 严重的内存泄漏点（热部署环境尤甚）
     *
     * 五、错误结构化与多线程日志关联
     *   1) 本类返回 ApiResponse 结构化的统一错误响应，对前端友好
     *   2) 服务端日志应附 traceId：用 MDC 注入（Filter/Interceptor），
     *      多线程编排时通过 TaskDecorator 复制 MDC，保证:async-3 与
     *      pool-1 的日志在同一 traceId 下可关联
     *   3) 排错口诀：先按 traceId 聚合 -> 再按线程名分组 -> 最后读栈
     *   4) 敏感信息在异常 message 的脱敏规则同样适用于多线程日志
     *      采集（ELK 侧做 mask，而非仅在此处理器里做）
     * =========================================================================
     */
}