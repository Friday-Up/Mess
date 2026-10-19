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
     * 【性能优化】自定义异常的性能注意（非可执行代码）
     * =========================================================================
     *
     * 一、异常构造的开销
     *   new ResourceNotFoundException() 会调用 super() -> Throwable()
     *   填充调用堆栈（fillInStackTrace），这是主要开销：
     *   - 浅堆栈（Controller->Service）：约 0.1ms
     *   - 深堆栈（Filter->...->Service）：约 1-5ms
     *   - 高频抛出（1000次/秒）：堆栈填充消耗 100-500ms CPU
     *
     * 二、优化方案
     *   1) 不填充堆栈：重写 fillInStackTrace() 返回 this
     *      适用：已知异常类型，日志不需要堆栈（如 404）
     *      风险：排障时丢失调用链
     *   2) 异常缓存：预构造单例异常，抛出时不重新构造
     *      适用：无状态异常（如固定的 404）
     *      风险：多个线程共享同一异常对象，堆栈是首次构造时的
     *   3) Optional 替代：返回 Optional.empty() 而非抛异常
     *      适用：查询可能不存在的场景，调用方显式处理
     *
     * 三、何时该抛异常 vs 返回空值
     *   抛异常：资源"应该存在"但没找到（数据不一致、权限问题）
     *   返回空：资源"可能不存在"是正常业务（如查询可选关联）
     *   原则：异常用于异常路径，不要用异常控制正常业务流程
     *
     * =========================================================================
     * 【监控可观测性】自定义异常指标
     * =========================================================================
     *   - 404 计数：Counter resource.not.found，按资源类型打 tag
     *   - 404 速率突增：可能是前端调了已下线接口或爬虫探测
     *   - 404 降级日志：高频时不记 WARN 日志，只记 Metrics
     *   - 业务关联：404 集中在某个资源类型时，检查该资源是否被误删
     * =========================================================================
     */
}