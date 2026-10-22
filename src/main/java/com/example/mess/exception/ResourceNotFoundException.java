package com.example.mess.exception;

/**
 * 资源未找到异常 - 当请求的资源（如用户、订单等）不存在时抛出。
 * 
 * <p>设计说明:
 * <ul>
 *   <li>继承RuntimeException（非受检异常），不需要在方法签名中声明throws</li>
 *   <li>由GlobalExceptionHandler统一处理，自动转换为404 Not Found响应</li>
 *   <li>提供多个构造函数，支持不同的使用场景</li>
 * </ul>
 * 
 * <p>使用场景:
 * <ul>
 *   <li>用户查询: findById返回空时抛出 new ResourceNotFoundException("用户不存在")</li>
 *   <li>删除操作: 用户不存在时抛出 new ResourceNotFoundException("用户不存在")</li>
 *   <li>更新操作: 用户不存在时抛出 new ResourceNotFoundException("用户不存在")</li>
 * </ul>
 * 
 * <p>与Spring标准异常的关系:
 * <ul>
 *   <li>不同于NoSuchElementException（集合操作）</li>
 *   <li>不同于EntityNotFoundException（JPA标准异常）</li>
 *   <li>自定义异常更语义化，便于统一处理</li>
 * </ul>
 * 
 * @see com.example.mess.exception.GlobalExceptionHandler 全局异常处理器
 * @since 1.0
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * 使用自定义消息创建异常。
     * <p>最常用的构造函数，直接传入错误描述信息。
     * 
     * @param message 错误描述信息，如"用户不存在"、"订单未找到"
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * 使用自定义消息和原始异常创建异常。
     * <p>用于包装底层异常，保留异常链以便调试。
     * 
     * @param message 错误描述信息
     * @param cause 原始异常（如SQLException、NoResultException等）
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 使用默认消息创建异常。
     * <p>默认消息为"请求的资源不存在"，适用于不需要具体描述的场景。
     */
    public ResourceNotFoundException() {
        super("请求的资源不存在");
    }

    /*
     * =========================================================================
     * 【Spring 内幕】异常在代理层穿透的机制与调用链还原（非可执行代码）
     * =========================================================================
     *
     * 一、为什么 Service 直接 throw，Controller 不需要 try-catch
     *   本异常是 unchecked（继承 RuntimeException）：
     *   1) Java 方法签名无需 throws 声明（unchecked 不强制）
     *   2) Spring 代理层不包装：默认情况下 JdkDynamicAopProxy 直接
     *      rethrow 原异常（只对 checked 用 UndeclaredThrowableException 包装）
     *   3) 异常沿栈向上穿过 UserService 代理 -> UserController -> DispatcherServlet
     *      -> HandlerExceptionResolver -> GlobalExceptionHandler 命中 @ExceptionHandler
     *   -> 全程无需手写 try-catch，代码保持平铺直线
     *
     * 二、异常链与堆栈：谁抛的如何保留
     *   - 发生点：orElseThrow(() -> new ResourceNotFoundException(用户不存在))
     *     堆栈从 lambda 调用处开始，清晰指向 Service 代码行
     *   - 异常被 GlobalExceptionHandler 接住时仍保留完整栈
     *   - 生产建议：本异常不应在日志中打印 ERROR 级（业务可控），DEBUG 即可，
     *     防止日志洪泛（恶意高频访问不存在的 ID 会与 NPE 一样刷屏）
     *
     * 三、JDK 反射方法调用对异常的处理
     *   1) Method.invoke 会把目标方法里的 checked 异常包成
     *      InvocationTargetException（它保留 target 指向原始 checked 异常）
     *   2) unchecked（如本 RuntimeException）则直接抛出，不会包装
     *   3) AOP 拦截链调用 proceed() -> ReflectiveMethodInvocation 接住
     *      InvocationTargetException -> 取 getCause 再向上抛 -> 去掉包装
     *   所以本异常能光溜溜冒到 Controller 是 AOP 链特意做的扁平化处理
     *
     * 四、自定义异常的设计原则小结
     *   - 语义化：资源不存在 -> 404（前端可据此提示友好信息）
     *   - 可扩展：继承 RuntimeException，支持 message + cause（本类两个构造器）
     *   - 不滥用：只在业务可预期时使用；编程错误（NPE/数组越界）
     *     不应被捕获成业务异常，否则掩盖真实 Bug
     *   - 可测试：测试中可以 assertThrows(ResourceNotFoundException.class, ...)
     *     （参照 UserServiceTest 中的测试用例）
     *
     * 五、异常与日志框架的联动实践
     *   1) 本异常的日志策略：业务可控的找不到应记 WARN/INFO 而非 ERROR，
     *      否则会触发告警风暴（PagerDuty/钉钉）
     *   2) 堆栈打印的代价：fillInStackTrace 约几百微秒，高频构造同异常时可
     *      覆盖 fillInStackTrace 返回 this（慎用，会丢失排错信息）
     *   3) 脱敏：message 不要拼接手机号/邮箱等敏感信息
     *   4) 与监控联动：GlobalExceptionHandler 里用 Micrometer Counter 打点，
     *      tag 含 exception_type 与 status，看板可见 404 趋势
     *   5) 404 突然激增常是前端传参变更或数据迁移导致——异常也是
     *      Observability 的重要信号，不只是错误
     * =========================================================================
     */
}