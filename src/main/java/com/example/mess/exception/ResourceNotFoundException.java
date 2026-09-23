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
     * 【ADR-011】自定义异常继承策略
     * =========================================================================
     * 上下文：需要为"资源不存在"场景定义业务异常，选择继承 Exception 还是
     *         RuntimeException 影响调用方是否必须处理。
     * 决策：继承 RuntimeException（unchecked），让 Service 方法签名保持干净，
     *       由全局异常处理器统一收敛并映射 404。
     * 替代方案：
     *   A) 继承 Exception（checked）—— 每个调用方都要 try-catch 或 throws，
     *      代码膨胀，且与全局处理器的设计理念冲突。
     *   B) 不自定义，直接抛 IllegalArgumentException —— 语义不精确，
     *      全局处理器无法区分"参数错误"和"资源不存在"。
     *   C) 用一个通用 BusinessException + code 字段 —— 可行但异常类不直观，
     *      @ExceptionHandler 无法按类型精确映射。
     * 后果：Service 抛 ResourceNotFoundException，全局处理器自动映射 404；
     *       新增异常类型（如 DuplicateResourceException -> 409）只需加类加 Handler。
     *
     * =========================================================================
     * 【代码审查要点】自定义异常
     * =========================================================================
     * [ ] 继承 RuntimeException（unchecked），不继承 Exception
     * [ ] 提供无参构造（默认文案）和带 message 构造（动态信息）
     * [ ] 异常名见名知义（ResourceNotFoundException > BizException）
     * [ ] message 带上下文（"用户ID=123不存在" > "资源不存在"）
     * [ ] 有对应 @ExceptionHandler，不会漏到 500 兜底
     * [ ] 不在异常 message 中带 SQL/表名/堆栈（安全风险）
     * [ ] 不每个字段一个异常类（维护成本远超收益）
     * =========================================================================
     */

    /*
     * =========================================================================
     * 【ADR-011-S】异常层次设计策略（补充）
     * =========================================================================
     * 上下文：随着业务增长，异常类型会增多，需要合理的层次结构。
     * 决策：按 HTTP 语义维度建异常类，不按业务字段维度。
     *       可选中间层 BusinessException 统一标记业务异常。
     * 推荐层次：
     *   RuntimeException
     *     └─ BusinessException（可选中间层，统一标记业务异常）
     *          ├─ ResourceNotFoundException   -> 404
     *          ├─ DuplicateResourceException   -> 409
     *          ├─ ValidationException          -> 400
     *          └─ OperationNotAllowedException -> 403
     * 后果：新增异常类型只需加类加 Handler，不影响已有映射；
     *       全局处理器可按 BusinessException 统一处理共性逻辑（如日志格式）。
     * =========================================================================
     */
}