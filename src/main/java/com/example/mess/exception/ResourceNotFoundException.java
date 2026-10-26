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
         * 【JVM内存管理与GC调优】业务异常的栈轨迹优化与对象设计（非可执行代码）
         * =========================================================================
         *
         * 一、本类作为 404 业务异常的 JVM 成本
         *   1) 每次查询不到就 throw new ResourceNotFoundException(msg)：
         *      默认填充满栈轨迹——但 404 是业务分支，不是真正的错误
         *   2) 高频 404（每秒几百次）时：异常对象 + 栈轨迹元素成批进入新生代，
         *      Young GC 停顿时间小幅后移——量化后才判断是否需要优化
         *   3) 优化建议：本类重写 fillInStackTrace() 返回 this，
         *      或提供"无轨迹构造器"——业务异常不需要栈（业务日志已含上下文）
         *
         * 二、异常继承与对象设计的取舍
         *   1) 继承 RuntimeException 而非 Throwable：语义更窄、与事务回滚
         *      行为自动配合——设计在语义层，性能在 JVM 层，两者要一致
         *   2) 字段设计：本项目仅 message + errorMessage 两个字段（浅占用小），
         *      年吞吐亿级请求时累计浪费仍可观——优化点永远在小对象的大数量上
         *   3) 自动化的隐患：全局日志切面捕获后又再 throw 一次：
         *      一次业务 404 产生两次栈轨迹填充——切面逻辑要避免链式 rethrow
         * 三、message 字符串的驻留与去重
         *   1) 异常 message 每次是新建 String：同一信息高频 throw 仍重复分配，
         *      热点 404 用静态常量 String 可省分配
         *   2) -XX:+UseStringDeduplication 让相同内容的 value 数组去重，
         *      G1 下对高频重复 message 场景有额外收益
         *
         =========================================================================
         */
}