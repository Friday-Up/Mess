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
     * 【Spring 内幕】HandlerExceptionResolver 链与 @ControllerAdvice 的注册原理（非可执行代码）
     * =========================================================================
     *
     * 一、异常在 DispatcherServlet 内部的传播与拦截
     *   1) Handler 步骤（Controller/Service/Repository）抛出
     *      ResourceNotFoundException（本类捕获的异常）
     *   2) DispatcherServlet.processDispatchResult 调用
     *      doResolveException -> HandlerExceptionResolverComposite
     *   3) 遍历三条 Resolver（顺序敏感，先匹配先返回）：
     *      a. ExceptionHandlerExceptionResolver：搜索 @ControllerAdvice 的
     *         @ExceptionHandler 匹配
     *      b. ResponseStatusExceptionResolver：处理 @ResponseStatus
     *         与 ResponseStatusException
     *      c. DefaultHandlerExceptionResolver：处理 Spring 框架标准异常（如 405）
     *   4) 本类标注 @ExceptionHandler(ResourceNotFoundException.class) 在 a 步撞中
     *      -> 返回 ModelAndView（空）+ 状态码 404 -> 交给视图或响应处理
     *   5) 若没有任何 Resolver 命中 -> 异常继续向上抛给容器 -> 500 页面
     *
     * 二、@ControllerAdvice 的扫描与缓存机制
     *   1) 启动时 RequestMappingHandlerAdapter.afterPropertiesSet
     *      -> ControllerAdviceBean.findAnnotatedBeans(ApplicationContext)
     *      找到所有 @ControllerAdvice 且非 scopedProxy 的 Bean
     *   2) ExceptionHandlerMethodCache：每个 Advice 的 @ExceptionHandler 方法
     *      按异常类型注册进 mappedMethods（按异常深度优先排序）
     *   3) 运行时按异常类 -> 类及父类超类 -> 最精确匹配找到 handler method
     *   - @RestControllerAdvice = @ControllerAdvice + @ResponseBody
     *   - 可以限定 basePackages/classes 扫描范围（精细控制 Advice 的作用域）
     *   - 全局 Advice 应保持无状态（多线程并发访问）
     *
     * 三、本类三个 ExceptionHandler 分别映射的语义
     *   1) handleResourceNotFound：找不到资源 -> 404 + ApiResponse(false)
     *   2) handleIllegalArgumentException：参数绑定/手动校验失败 -> 400
     *   3) handleGenericException：兜底场景 -> 500 + 避免堆栈外泄
     *   所有处理器都返回 ResponseEntity<ApiResponse>：
     *   - ResponseEntity 同时控制状态码与体
     *   - ApiResponse 保证前端有统一解析结构（前端只看 success/message/data）
     *
     * 四、全局异常处理与安全过滤器的边界
     *   - Spring Security 的 Filter 链异常不会进入 HandlerExceptionResolver
     *     （在 DispatcherServlet 之前，由 ExceptionTranslationFilter 处理）
     *   - 鉴权失败 -> AuthenticationEntryPoint（401 重定向到登录页）
     *   - 授权失败 -> AccessDeniedHandler（403）
     *   因此业务层异常走 GlobalExceptionHandler，安全层异常走安全过滤器，
     *   两者分工清晰，不互相覆盖
     *
     * 五、Advice 细节与异常处理优先级陷阱
     *   1) 同一异常在多个 Advice 都有匹配方法时默认取第一个匹配，
     *      Order 未配置可能落到非预期的 Advice：用 @Order 显式声明优先级
     *   2) 匹配规则不只精确类型，还查父类/父接口（Throwable 顶层兜底）
     *   3) @RestControllerAdvice = @ControllerAdvice + @ResponseBody，
     *      本项目用它统一返回 ApiResponse，避免每个方法重复注解
     *   4) Advice 应避免吞掉异常链：日志记录 stackTrace 仍然必要，
     *      只把用户可见的信息脱敏后放入 message
     *   5) 测试策略：@WebMvcTest 只引入 Controller + Advice，
     *      每个异常写最少 1 个用例（404/400/500 各一），守住 API 契约
     *
     * 六、Spring 框架标准异常到 HTTP 状态码的映射表
     *   HttpRequestMethodNotSupportedException -> 405（方法不受支持）
     *   HttpMediaTypeNotSupportedException -> 415（Content-Type 不匹配）
     *   MissingServletRequestParameterException -> 400（缺必填参数）
     *   MethodArgumentTypeMismatchException -> 400（类型转换失败）
     *   ConstraintViolationException -> 400（Bean Validation 校验失败）
     *   NoHandlerFoundException -> 404（无匹配路由，需配置 add-mappings=false）
     *   这些默认由 DefaultHandlerExceptionResolver 映射，
     *   本类不需要重复处理，只在需要统一 ApiResponse 包装时覆盖对应映射
     * =========================================================================
     */
}