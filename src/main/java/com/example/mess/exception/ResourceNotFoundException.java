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
     * 【检查清单】自定义异常自检表（非可执行代码）
     * =========================================================================
     *
     * [ ] 继承 RuntimeException（unchecked，不污染 Service 方法签名）
     * [ ] 提供无参构造（默认文案）和带 message 构造（动态信息）
     * [ ] 异常名见名知义（ResourceNotFoundException > BizException）
     * [ ] message 带上下文（"用户ID=123不存在" > "资源不存在"）
     * [ ] 有对应 @ExceptionHandler，不会漏到 500 兜底
     *
     * 常见误区
     *   - 继承 Exception（checked）-> 每个调用方都要 try-catch 或 throws，代码膨胀
     *   - 一个异常类打天下 -> 无法在全局处理器里精确映射状态码
     *   - 异常 message 带 SQL/堆栈 -> 泄露技术细节给前端
     *   - 每个字段一个异常类 -> 类爆炸，维护成本远超收益
     *
     * 推荐的异常层次设计
     *   RuntimeException
     *     └─ BusinessException（可选中间层，统一标记业务异常）
     *          ├─ ResourceNotFoundException   -> 404
     *          ├─ DuplicateResourceException   -> 409
     *          ├─ ValidationException          -> 400
     *          └─ OperationNotAllowedException -> 403
     * =========================================================================
     */
}