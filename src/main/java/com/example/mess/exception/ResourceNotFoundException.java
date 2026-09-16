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
     * 【面试问答】关于自定义异常的常见面试题（非可执行代码）
     * =========================================================================
     *
     * Q1: 为什么继承 RuntimeException 而不是 Exception？
     * A1: RuntimeException 是 unchecked，调用方不必 try-catch 或 throws，
     *     Service 方法签名保持干净。配合全局异常处理器统一收敛。
     *     若继承 Exception（checked），每个调用层都要声明，代码膨胀。
     *
     * Q2: 一个项目应该设计多少个异常类？
     * A2: 按 HTTP 语义维度建，不是按字段维度：
     *     ResourceNotFoundException -> 404
     *     DuplicateResourceException -> 409
     *     ValidationException -> 400
     *     OperationNotAllowedException -> 403
     *     不必每个字段一个异常类，维护成本远超收益。
     *
     * Q3: 异常 message 应该写什么？
     * A3: 面向用户的可读文案 + 关键上下文。
     *     "用户ID=123不存在" 比 "资源不存在" 更有利于排障。
     *     不要带 SQL/表名/堆栈（安全风险）。
     *
     * Q4: 自定义异常需要序列化吗？
     * A4: 如果可能跨网络传输（如 RPC），需实现 Serializable。
     *     本项目内 Web API 不需要，全局处理器把异常转为 JSON 响应即可。
     *
     * Q5: 为什么不在 Controller 里 try-catch？
     * A5: Controller 应保持轻薄。异常处理集中在全局处理器，
     *     Controller 只管"正常路径"，异常路径由框架兜底。
     *     到处 try-catch 返回错误 JSON 会导致重复代码和遗漏。
     * =========================================================================
     */
}