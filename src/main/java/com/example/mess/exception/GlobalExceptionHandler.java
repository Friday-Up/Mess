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
     * 【设计模式】责任链与观察者：异常解析链的分派与事件广播（非可执行代码）
     * =========================================================================
     *
     * 一、分派式责任链：HandlerExceptionResolver 的选路逻辑
     *   1) DispatcherServlet 持有 HandlerExceptionResolverComposite，
     *      依次询问已注册解析器：
     *      ExceptionHandlerExceptionResolver -> ResponseStatusExceptionResolver
     *      -> DefaultHandlerExceptionResolver
     *   2) 每个解析器尝试 resolveException 返回 ModelAndView：
     *      返回非空即命中（本类 @ExceptionHandler 在第一个解析器命中）；
     *      全部为空则异常被抛给容器，表现为 500 页面
     *   3) 与纯责任链（Servlet Filter）的差异：
     *      Filter 链每环节主动链式调用下一个（doFilter 转交）；
     *      异常链由调度者统一遍历，命中即停——分派模式
     *
     * 二、@ControllerAdvice：注册表 + 选择器，不是观察者
     *   1) 表面：多个 Advice Bean 关注 Controller 异常，状似广播
     *   2) 实质：RequestMappingHandlerAdapter 维护 "
     *      异常类型 -> @ExceptionHandler 方法"注册表，
     *      抛异常时按类型查找第一个匹配方法执行，不是多播
     *   3) 推论：同一异常多个 Advice 都有 @ExceptionHandler 时
     *      只执行 @Order 最优先者，而非全部回调
     *
     * 三、观察者模式（Observer）的真正落点：Spring 应用事件
     *   1) 事件总线：ApplicationEventPublisher.publishEvent(event)
     *      订阅端：@EventListener 方法/ @TransactionalEventListener
     *   2) 默认同步执行（同线程、同事务），加 @Async 转异步
     *   3) 与异常处理互补的架构定位：
     *      异常处理：拦截控制流（返回 404 给客户端）
     *      事件广播：旁路通知（审计记录、缓存失效、监控打点）
     *      互不替代，常相伴：抛 ResourceNotFoundException 时
     *      同时可发布 ResourceNotFoundEvent 供审计订阅
     *   4) 弱观察 vs 强观察：Spring 事件是"中介者"模式实现的
     *      广播（Publisher 不知道谁订阅），真正解耦
     *
     * 四、兜底处理器的"终点保证"原则
     *   1) 本类 handleGenericException(Throwable e) 是链终点：
     *      无论发生什么未知异常，都转 500 + 脱敏 message
     *   2) 责任链设计纪律：终点必须无条件捕获、保证有出口，
     *      否则异常会以未处理状态冲击容器默认页
     *   3) 终点记日志 ERROR 级（全链唯一该 ERROR 的地方），
     *      业务可控异常（404/400）记 WARN/INFO——告警噪声控制命脉
     * =========================================================================
     */
}