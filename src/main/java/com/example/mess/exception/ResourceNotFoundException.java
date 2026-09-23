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
     * 【技术债务】TD-011 自定义异常
     * =========================================================================
     *
     * TD-011-1: 缺少更多业务异常类型
     *   现状：只有 ResourceNotFoundException，无法区分 400/409 等语义
     *   影响：参数校验失败、唯一约束冲突等无法精确映射状态码
     *   优先级：P2
     *   修复方案：加 DuplicateResourceException(409)、ValidationException(400)
     *   预估工时：1d
     *
     * TD-011-2: 异常 message 缺少上下文
     *   现状：默认 message 是"请求的资源不存在"，无具体 ID/类型信息
     *   影响：排障时无法快速定位是哪个资源
     *   优先级：P2
     *   修复方案：加带 message 的构造器：new ResourceNotFoundException("用户ID=" + id + "不存在")
     *   预估工时：0.5d
     *
     * TD-011-3: 无业务码字段
     *   现状：异常只有 message，无结构化业务码
     *   影响：前端无法根据码做差异化处理
     *   优先级：P3
     *   修复方案：加 code 字段，全局处理器将 code 写入 ApiResponse
     *   预估工时：0.5d
     *
     * =========================================================================
     * 【重构路线图】异常体系演进方向
     * =========================================================================
     * Phase 1（当前）：单一 ResourceNotFoundException + 默认 message
     *   Phase 2：加 DuplicateResourceException/ValidationException + 带 message 构造器
     * Phase 3：业务码字段 + 异常层次（BusinessException 中间层）+ 国际化 message
     * =========================================================================
     */
}